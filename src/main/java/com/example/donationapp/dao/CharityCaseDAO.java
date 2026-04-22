package com.example.donationapp.dao;

import com.example.donationapp.database.DatabaseManager;
import com.example.donationapp.email.EmailService;
import com.example.donationapp.exceptions.CaseNotFoundException;
import com.example.donationapp.exceptions.DatabaseConnectionException;
import com.example.donationapp.models.CharityCase;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Data Access Object for CharityCase operations.
 * Handles all database operations related to charity cases.
 */
public class CharityCaseDAO {
    
    private static final Logger LOGGER = Logger.getLogger(CharityCaseDAO.class.getName());
    private final DatabaseManager dbManager;
    
    public CharityCaseDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Inserts a new charity case into the database.
     * 
     * @param charityCase The case to insert
     * @return The generated case ID
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public int insertCase(CharityCase charityCase) throws SQLException, DatabaseConnectionException {
        String sql = "INSERT INTO charity_cases (user_id, type, description, status, priority_level, goal_amount, category) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, charityCase.getUserId());
            pstmt.setString(2, charityCase.getType());
            pstmt.setString(3, charityCase.getDescription());
            pstmt.setString(4, charityCase.getStatus());
            pstmt.setString(5, charityCase.getPriorityLevel());
            pstmt.setDouble(6, charityCase.getGoalAmount());
            pstmt.setString(7, charityCase.getCategory());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating charity case failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int caseId = generatedKeys.getInt(1);
                    LOGGER.info("Charity case created with ID: " + caseId);
                    
                    // ===== EMAIL INTEGRATION =====
                    // Send case submission confirmation email asynchronously
                    try {
                        String receiverEmail = getUserEmail(charityCase.getUserId());
                        String receiverName = getUserName(charityCase.getUserId());
                        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                        
                        final String finalEmail = receiverEmail;
                        EmailService.getInstance().sendCaseSubmissionConfirmation(
                            receiverEmail,
                            receiverName,
                            charityCase.getType(),
                            charityCase.getDescription(),
                            "$" + String.format("%.2f", charityCase.getGoalAmount()),
                            "CASE-" + String.format("%05d", caseId),
                            formattedDate
                        ).thenAccept(result -> {
                            if (result.isSuccess()) {
                                LOGGER.info("Case submission email sent to: " + finalEmail);
                            } else {
                                LOGGER.warning("Failed to send case submission email: " + result.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        LOGGER.warning("Could not send case submission email: " + e.getMessage());
                    }
                    // =============================
                    
                    return caseId;
                } else {
                    throw new SQLException("Creating charity case failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Retrieves a charity case by its ID.
     * 
     * @param caseId The ID of the case to retrieve
     * @return The CharityCase object
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     * @throws CaseNotFoundException if case not found
     */
    public CharityCase getCaseById(int caseId) throws SQLException, DatabaseConnectionException, CaseNotFoundException {
        String sql = "SELECT * FROM charity_cases WHERE case_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, caseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createCaseFromResultSet(rs);
                } else {
                    throw new CaseNotFoundException("Charity case not found with ID: " + caseId);
                }
            }
        }
    }
    
    /**
     * Retrieves all charity cases for a specific user.
     * 
     * @param userId The user ID
     * @return ArrayList of charity cases
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<CharityCase> getCasesByUserId(int userId) throws SQLException, DatabaseConnectionException {
        ArrayList<CharityCase> cases = new ArrayList<>();
        String sql = "SELECT * FROM charity_cases WHERE user_id = ? ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cases.add(createCaseFromResultSet(rs));
                }
            }
        }
        
        return cases;
    }
    
    /**
     * Retrieves all pending cases.
     * 
     * @return ArrayList of pending charity cases
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<CharityCase> getPendingCases() throws SQLException, DatabaseConnectionException {
        ArrayList<CharityCase> cases = new ArrayList<>();
        String sql = "SELECT * FROM charity_cases WHERE status = 'Pending' ORDER BY created_at ASC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                cases.add(createCaseFromResultSet(rs));
            }
        }
        
        return cases;
    }
    
    /**
     * Retrieves accepted cases by priority level.
     * 
     * @param priority The priority level ("High" or "Low")
     * @return ArrayList of accepted charity cases
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<CharityCase> getAcceptedCases(String priority) throws SQLException, DatabaseConnectionException {
        ArrayList<CharityCase> cases = new ArrayList<>();
        String status = "Accepted - " + priority + " Priority";
        String sql = "SELECT * FROM charity_cases WHERE status = ? ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cases.add(createCaseFromResultSet(rs));
                }
            }
        }
        
        return cases;
    }
    
    /**
     * Retrieves all accepted cases (both high and low priority).
     * 
     * @return ArrayList of all accepted charity cases
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<CharityCase> getAllAcceptedCases() throws SQLException, DatabaseConnectionException {
        ArrayList<CharityCase> cases = new ArrayList<>();
        String sql = "SELECT * FROM charity_cases WHERE status LIKE 'Accepted%' ORDER BY " +
                     "CASE WHEN priority_level = 'High' THEN 1 ELSE 2 END, created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                cases.add(createCaseFromResultSet(rs));
            }
        }
        
        return cases;
    }
    
    /**
     * Retrieves all charity cases from the database.
     * 
     * @return ArrayList of all charity cases
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<CharityCase> getAllCases() throws SQLException, DatabaseConnectionException {
        ArrayList<CharityCase> cases = new ArrayList<>();
        String sql = "SELECT * FROM charity_cases ORDER BY created_at DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                cases.add(createCaseFromResultSet(rs));
            }
        }
        
        return cases;
    }
    
    /**
     * Updates the status and priority of a charity case.
     * Sends email notification based on the new status.
     * 
     * @param caseId The case ID
     * @param status The new status
     * @param priority The new priority level
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public void updateCaseStatus(int caseId, String status, String priority) 
            throws SQLException, DatabaseConnectionException {
        updateCaseStatus(caseId, status, priority, null);
    }
    
    /**
     * Updates the status and priority of a charity case with optional decline reason.
     * Sends email notification based on the new status.
     * 
     * @param caseId The case ID
     * @param status The new status
     * @param priority The new priority level
     * @param reason The reason for decline (optional, used only for declined cases)
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public void updateCaseStatus(int caseId, String status, String priority, String reason) 
            throws SQLException, DatabaseConnectionException {
        
        String sql = "UPDATE charity_cases SET status = ?, priority_level = ?, reviewed_at = CURRENT_TIMESTAMP " +
                     "WHERE case_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, priority);
            pstmt.setInt(3, caseId);
            
            int affectedRows = pstmt.executeUpdate();
            LOGGER.info("Case status updated: " + caseId + " to " + status);
            
            // ===== EMAIL INTEGRATION =====
            // Send approval/decline notification email asynchronously
            if (affectedRows > 0) {
                try {
                    CharityCase charityCase = getCaseById(caseId);
                    String receiverEmail = getUserEmail(charityCase.getUserId());
                    String receiverName = getUserName(charityCase.getUserId());
                    String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                    final String finalEmail = receiverEmail;
                    
                    if (status.toLowerCase().contains("accepted")) {
                        // Case APPROVED
                        EmailService.getInstance().sendCaseApprovalNotification(
                            receiverEmail,
                            receiverName,
                            charityCase.getType(),
                            "CASE-" + String.format("%05d", caseId),
                            "$" + String.format("%.2f", charityCase.getGoalAmount()),
                            formattedDate
                        ).thenAccept(result -> {
                            if (result.isSuccess()) {
                                LOGGER.info("Approval email sent to: " + finalEmail);
                            } else {
                                LOGGER.warning("Failed to send approval email: " + result.getMessage());
                            }
                        });
                        
                    } else if (status.toLowerCase().contains("declined") || 
                               status.toLowerCase().contains("rejected")) {
                        // Case DECLINED
                        String declineReason = reason != null ? reason : "Does not meet our current guidelines";
                        EmailService.getInstance().sendCaseDeclineNotification(
                            receiverEmail,
                            receiverName,
                            charityCase.getType(),
                            "CASE-" + String.format("%05d", caseId),
                            declineReason,
                            formattedDate
                        ).thenAccept(result -> {
                            if (result.isSuccess()) {
                                LOGGER.info("Decline notification sent to: " + finalEmail);
                            } else {
                                LOGGER.warning("Failed to send decline email: " + result.getMessage());
                            }
                        });
                    }
                } catch (CaseNotFoundException e) {
                    LOGGER.warning("Could not send status email - case not found: " + e.getMessage());
                } catch (Exception e) {
                    LOGGER.warning("Could not send status notification email: " + e.getMessage());
                }
            }
            // =============================
        }
    }
    
    /**
     * Deletes a charity case from the database.
     * 
     * @param caseId The ID of the case to delete
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public void deleteCase(int caseId) throws SQLException, DatabaseConnectionException {
        String sql = "DELETE FROM charity_cases WHERE case_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, caseId);
            pstmt.executeUpdate();
            LOGGER.info("Charity case deleted: " + caseId);
        }
    }
    
    /**
     * Gets the count of cases by status.
     * 
     * @param status The status to count
     * @return The number of cases with that status
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public int getCaseCountByStatus(String status) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT COUNT(*) FROM charity_cases WHERE status = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Creates a CharityCase object from a ResultSet.
     * 
     * @param rs The ResultSet containing case data
     * @return The CharityCase object
     * @throws SQLException if data retrieval fails
     */
    private CharityCase createCaseFromResultSet(ResultSet rs) throws SQLException {
        int caseId = rs.getInt("case_id");
        int userId = rs.getInt("user_id");
        String type = rs.getString("type");
        String description = rs.getString("description");
        String status = rs.getString("status");
        String priorityLevel = rs.getString("priority_level");
        double goalAmount = rs.getDouble("goal_amount");
        double currentAmount = rs.getDouble("current_amount");
        String category = rs.getString("category");
        
        CharityCase charityCase = new CharityCase(caseId, userId, type, description, 
                                                   goalAmount, currentAmount, 
                                                   category != null ? category : "Other",
                                                   status, priorityLevel);
        return charityCase;
    }
    
    // ==================== EMAIL HELPER METHODS ====================
    
    /**
     * Gets user email by ID for email notifications.
     * 
     * @param userId The user's ID
     * @return The user's email address, or null if not found
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    private String getUserEmail(int userId) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }
    
    /**
     * Gets user name by ID for email notifications.
     * 
     * @param userId The user's ID
     * @return The user's name, or "Valued User" if not found
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    private String getUserName(int userId) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT name FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return "Valued User";
    }
}
