package com.example.donationapp.dao;

import com.example.donationapp.database.DatabaseManager;
import com.example.donationapp.email.EmailService;
import com.example.donationapp.exceptions.*;
import com.example.donationapp.models.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User operations.
 * Handles all database operations related to users.
 */
public class UserDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private final DatabaseManager dbManager;
    
    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Inserts a new user into the database.
     * 
     * @param user The user to insert
     * @param password The user's password (will be hashed)
     * @param userType The type of user (DONOR, RECEIVER, VOLUNTEER, ADMIN)
     * @return The generated user ID
     * @throws SQLException if database operation fails
     * @throws DuplicateUserException if email already exists
     * @throws DatabaseConnectionException if connection fails
     */
    public int insertUser(User user, String password, String userType) 
            throws SQLException, DuplicateUserException, DatabaseConnectionException {
        
        // Check for duplicate email
        if (emailExists(user.getEmail())) {
            throw new DuplicateUserException("User with email " + user.getEmail() + " already exists");
        }
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        String sql = "INSERT INTO users (name, email, phone, password, user_type, location, is_verified, role, working_hours) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, userType);
            
            // Set location for Admin and Volunteer
            if (user instanceof Admin) {
                pstmt.setString(6, ((Admin) user).getLocation());
            } else if (user instanceof Volunteer) {
                pstmt.setString(6, ((Volunteer) user).getLocation());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }
            
            // Set is_verified for Donor
            if (user instanceof Donor) {
                pstmt.setBoolean(7, ((Donor) user).isVerified());
            } else {
                pstmt.setBoolean(7, false);
            }
            
            // Set role and working_hours for Volunteer
            if (user instanceof Volunteer) {
                pstmt.setString(8, ((Volunteer) user).getRole());
                pstmt.setInt(9, ((Volunteer) user).getWorkingHours());
            } else {
                pstmt.setNull(8, Types.VARCHAR);
                pstmt.setInt(9, 0);
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    LOGGER.info("User created with ID: " + userId);
                    
                    // ===== EMAIL INTEGRATION =====
                    // Send welcome email asynchronously (non-blocking)
                    final String userEmail = user.getEmail();
                    final String userName = user.getName();
                    EmailService.getInstance().sendWelcomeEmail(
                        userEmail,
                        userName,
                        userType
                    ).thenAccept(result -> {
                        if (result.isSuccess()) {
                            LOGGER.info("Welcome email sent to: " + userEmail);
                        } else {
                            LOGGER.warning("Failed to send welcome email: " + result.getMessage());
                        }
                    });
                    // =============================
                    
                    return userId;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Retrieves a user by their ID.
     * 
     * @param id The user ID
     * @return The user object
     * @throws SQLException if database operation fails
     * @throws UserNotFoundException if user is not found
     * @throws DatabaseConnectionException if connection fails
     */
    public User getUserById(int id) throws SQLException, UserNotFoundException, DatabaseConnectionException {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createUserFromResultSet(rs);
                } else {
                    throw new UserNotFoundException("User with ID " + id + " not found");
                }
            }
        }
    }
    
    /**
     * Retrieves a user by their email.
     * 
     * @param email The user's email
     * @return The user object
     * @throws SQLException if database operation fails
     * @throws UserNotFoundException if user is not found
     * @throws DatabaseConnectionException if connection fails
     */
    public User getUserByEmail(String email) throws SQLException, UserNotFoundException, DatabaseConnectionException {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createUserFromResultSet(rs);
                } else {
                    throw new UserNotFoundException("User with email " + email + " not found");
                }
            }
        }
    }
    
    /**
     * Validates user login credentials.
     * 
     * @param email The user's email
     * @param password The user's password
     * @return The authenticated user
     * @throws SQLException if database operation fails
     * @throws InvalidCredentialsException if credentials are invalid
     * @throws DatabaseConnectionException if connection fails
     */
    public User validateLogin(String email, String password) 
            throws SQLException, InvalidCredentialsException, DatabaseConnectionException {
        
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    
                    if (BCrypt.checkpw(password, storedHash)) {
                        return createUserFromResultSet(rs);
                    } else {
                        throw new InvalidCredentialsException("Invalid password");
                    }
                } else {
                    throw new InvalidCredentialsException("User with email " + email + " not found");
                }
            }
        }
    }
    
    /**
     * Updates an existing user in the database.
     * 
     * @param user The user to update
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public void updateUser(User user) throws SQLException, DatabaseConnectionException {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, location = ?, " +
                     "is_verified = ?, role = ?, working_hours = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            
            if (user instanceof Admin) {
                pstmt.setString(4, ((Admin) user).getLocation());
            } else if (user instanceof Volunteer) {
                pstmt.setString(4, ((Volunteer) user).getLocation());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            
            if (user instanceof Donor) {
                pstmt.setBoolean(5, ((Donor) user).isVerified());
            } else {
                pstmt.setBoolean(5, false);
            }
            
            if (user instanceof Volunteer) {
                pstmt.setString(6, ((Volunteer) user).getRole());
                pstmt.setInt(7, ((Volunteer) user).getWorkingHours());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setInt(7, 0);
            }
            
            pstmt.setInt(8, user.getId());
            
            pstmt.executeUpdate();
            LOGGER.info("User updated: " + user.getId());
        }
    }
    
    /**
     * Deletes a user from the database.
     * 
     * @param id The ID of the user to delete
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public void deleteUser(int id) throws SQLException, DatabaseConnectionException {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            LOGGER.info("User deleted: " + id);
        }
    }
    
    /**
     * Retrieves all users of a specific type.
     * 
     * @param userType The type of users to retrieve
     * @return ArrayList of users
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<User> getAllUsers(String userType) throws SQLException, DatabaseConnectionException {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE user_type = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userType);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(createUserFromResultSet(rs));
                }
            }
        }
        
        return users;
    }
    
    /**
     * Retrieves all users from the database.
     * 
     * @return ArrayList of all users
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<User> getAllUsers() throws SQLException, DatabaseConnectionException {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        }
        
        return users;
    }
    
    /**
     * Updates the verification status of a donor.
     * 
     * @param userId The user ID
     * @param isVerified The new verification status
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public void updateVerificationStatus(int userId, boolean isVerified) 
            throws SQLException, DatabaseConnectionException {
        
        String sql = "UPDATE users SET is_verified = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isVerified);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
            LOGGER.info("Verification status updated for user: " + userId);
        }
    }
    
    /**
     * Checks if an email already exists in the database.
     * 
     * @param email The email to check
     * @return true if email exists, false otherwise
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public boolean emailExists(String email) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Creates a User object from a ResultSet.
     * 
     * @param rs The ResultSet containing user data
     * @return The appropriate User subclass
     * @throws SQLException if data retrieval fails
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String userType = rs.getString("user_type");
        String location = rs.getString("location");
        boolean isVerified = rs.getBoolean("is_verified");
        String role = rs.getString("role");
        int workingHours = rs.getInt("working_hours");
        
        User user;
        
        switch (userType) {
            case "DONOR":
                user = new Donor(id, name, email, phone, isVerified);
                break;
            case "RECEIVER":
                user = new Receiver(id, name, email, phone);
                break;
            case "ADMIN":
                user = new Admin(id, name, email, phone, location);
                break;
            case "VOLUNTEER":
                user = new Volunteer(id, name, email, phone, location, role, workingHours);
                break;
            default:
                throw new SQLException("Unknown user type: " + userType);
        }
        
        return user;
    }
    
    /**
     * Gets the user type for a given email.
     * 
     * @param email The user's email
     * @return The user type as a string
     * @throws SQLException if database operation fails
     * @throws UserNotFoundException if user is not found
     * @throws DatabaseConnectionException if connection fails
     */
    public String getUserType(String email) throws SQLException, UserNotFoundException, DatabaseConnectionException {
        String sql = "SELECT user_type FROM users WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_type");
                } else {
                    throw new UserNotFoundException("User with email " + email + " not found");
                }
            }
        }
    }
}
