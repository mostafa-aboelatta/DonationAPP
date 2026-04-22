package com.example.donationapp.dao;

import com.example.donationapp.database.DatabaseManager;
import com.example.donationapp.email.EmailService;
import com.example.donationapp.exceptions.DatabaseConnectionException;
import com.example.donationapp.models.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Data Access Object for Donation operations.
 * Handles all database operations related to donations.
 */
public class DonationDAO {
    
    private static final Logger LOGGER = Logger.getLogger(DonationDAO.class.getName());
    private final DatabaseManager dbManager;
    
    public DonationDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Inserts a new donation into the database.
     * 
     * @param donation The donation to insert
     * @param donorId The ID of the donor
     * @param caseId The ID of the associated case (can be null)
     * @return The generated donation ID
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public int insertDonation(Donation donation, int donorId, Integer caseId) 
            throws SQLException, DatabaseConnectionException {
        
        String sql = "INSERT INTO donations (donor_id, case_id, donation_type, amount, quantity, " +
                     "medical_equipment, category, date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, donorId);
            
            if (caseId != null) {
                pstmt.setInt(2, caseId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            // Determine donation type and set appropriate fields
            if (donation instanceof FinancialDonation) {
                pstmt.setString(3, "Financial");
                pstmt.setDouble(4, donation.getAmount());
                pstmt.setInt(5, 0);
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setNull(7, Types.VARCHAR);
            } else if (donation instanceof MedicalDonation) {
                MedicalDonation med = (MedicalDonation) donation;
                pstmt.setString(3, "Medical");
                pstmt.setDouble(4, 0.0);
                pstmt.setInt(5, med.getQuantity());
                pstmt.setString(6, med.getMedicalEquipment());
                pstmt.setNull(7, Types.VARCHAR);
            } else if (donation instanceof OtherDonation) {
                OtherDonation other = (OtherDonation) donation;
                pstmt.setString(3, "Other");
                pstmt.setDouble(4, other.getAmount());
                pstmt.setInt(5, other.getQuantity());
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setString(7, other.getCategory());
            } else {
                throw new SQLException("Unknown donation type");
            }
            
            pstmt.setDate(8, Date.valueOf(LocalDate.parse(donation.getDate())));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating donation failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int donationId = generatedKeys.getInt(1);
                    LOGGER.info("Donation created with ID: " + donationId);
                    
                    // Update case amount if applicable
                    if (caseId != null && donation.getAmount() > 0) {
                        updateCaseAmount(caseId, donation.getAmount());
                    }
                    
                    // ===== EMAIL INTEGRATION =====
                    // Send donation confirmation email asynchronously
                    try {
                        String donorEmail = getDonorEmail(donorId);
                        String donorName = getDonorName(donorId);
                        String caseName = caseId != null ? getCaseName(caseId) : "General Fund";
                        
                        String transactionId = "TXN" + String.format("%06d", donationId);
                        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
                        
                        // Determine donation type and amount string
                        String donationType = donation.getClass().getSimpleName().replace("Donation", "");
                        String amountStr;
                        if (donation instanceof FinancialDonation) {
                            amountStr = "$" + String.format("%.2f", donation.getAmount());
                        } else if (donation instanceof MedicalDonation) {
                            MedicalDonation med = (MedicalDonation) donation;
                            amountStr = med.getQuantity() + " x " + med.getMedicalEquipment();
                        } else {
                            amountStr = donation.toString();
                        }
                        
                        final String finalDonorEmail = donorEmail;
                        EmailService.getInstance().sendDonationConfirmation(
                            donorEmail,
                            donorName,
                            donationType,
                            amountStr,
                            caseName,
                            transactionId,
                            formattedDate
                        ).thenAccept(result -> {
                            if (result.isSuccess()) {
                                LOGGER.info("Donation confirmation email sent to: " + finalDonorEmail);
                            } else {
                                LOGGER.warning("Failed to send confirmation email: " + result.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        LOGGER.warning("Could not send donation confirmation email: " + e.getMessage());
                    }
                    // =============================
                    
                    return donationId;
                } else {
                    throw new SQLException("Creating donation failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Updates the case amount after a donation.
     */
    private void updateCaseAmount(int caseId, double amount) throws SQLException, DatabaseConnectionException {
        String sql = "UPDATE charity_cases SET current_amount = current_amount + ? WHERE case_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, caseId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Retrieves all donations made by a specific donor.
     * 
     * @param donorId The donor's ID
     * @return ArrayList of donations
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<Donation> getDonationsByDonorId(int donorId) throws SQLException, DatabaseConnectionException {
        ArrayList<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE donor_id = ? ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, donorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    donations.add(createDonationFromResultSet(rs));
                }
            }
        }
        
        return donations;
    }
    
    /**
     * Retrieves all donations for a specific charity case.
     * 
     * @param caseId The case ID
     * @return ArrayList of donations
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<Donation> getDonationsByCaseId(int caseId) throws SQLException, DatabaseConnectionException {
        ArrayList<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE case_id = ? ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, caseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    donations.add(createDonationFromResultSet(rs));
                }
            }
        }
        
        return donations;
    }
    
    /**
     * Retrieves all donations from the database.
     * 
     * @return ArrayList of all donations
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<Donation> getAllDonations() throws SQLException, DatabaseConnectionException {
        ArrayList<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM donations ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                donations.add(createDonationFromResultSet(rs));
            }
        }
        
        return donations;
    }
    
    /**
     * Overloaded method - retrieves donations by type.
     * Demonstrates method overloading.
     * 
     * @param donationType The type of donations to retrieve
     * @return ArrayList of donations
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public ArrayList<Donation> getDonations(String donationType) throws SQLException, DatabaseConnectionException {
        ArrayList<Donation> donations = new ArrayList<>();
        String sql = "SELECT * FROM donations WHERE donation_type = ? ORDER BY date DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, donationType);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    donations.add(createDonationFromResultSet(rs));
                }
            }
        }
        
        return donations;
    }
    
    /**
     * Gets the total monetary amount of all donations.
     * 
     * @return The total donation amount
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public double getTotalDonationAmount() throws SQLException, DatabaseConnectionException {
        String sql = "SELECT SUM(amount) as total FROM donations";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        
        return 0.0;
    }
    
    /**
     * Gets the total count of donations.
     * 
     * @return The total number of donations
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public int getDonationCount() throws SQLException, DatabaseConnectionException {
        String sql = "SELECT COUNT(*) FROM donations";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Gets donation count by type.
     * 
     * @param donationType The type of donations to count
     * @return The count of donations of that type
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    public int getDonationCountByType(String donationType) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT COUNT(*) FROM donations WHERE donation_type = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, donationType);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Creates a Donation object from a ResultSet.
     * 
     * @param rs The ResultSet containing donation data
     * @return The appropriate Donation subclass
     * @throws SQLException if data retrieval fails
     */
    private Donation createDonationFromResultSet(ResultSet rs) throws SQLException {
        int donationId = rs.getInt("donation_id");
        String donationType = rs.getString("donation_type");
        String date = rs.getDate("date").toString();
        double amount = rs.getDouble("amount");
        int quantity = rs.getInt("quantity");
        String medicalEquipment = rs.getString("medical_equipment");
        String category = rs.getString("category");
        int donorId = rs.getInt("donor_id");
        Integer caseId = rs.getObject("case_id", Integer.class);
        
        Donation donation;
        
        switch (donationType) {
            case "Financial":
                donation = new FinancialDonation(donationId, donationType, date, amount);
                break;
            case "Medical":
                donation = new MedicalDonation(donationId, donationType, date, quantity, 
                    medicalEquipment != null ? medicalEquipment : "Medical supplies");
                break;
            case "Other":
                donation = new OtherDonation(donationId, donationType, date, quantity, amount, 
                    category != null ? category : "Other");
                break;
            default:
                throw new SQLException("Unknown donation type: " + donationType);
        }
        
        donation.setDonorId(donorId);
        donation.setCaseId(caseId);
        
        return donation;
    }
    
    // ==================== EMAIL HELPER METHODS ====================
    
    /**
     * Gets donor email by ID for email notifications.
     * 
     * @param donorId The donor's ID
     * @return The donor's email address, or null if not found
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    private String getDonorEmail(int donorId) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT email FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, donorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        }
        return null;
    }
    
    /**
     * Gets donor name by ID for email notifications.
     * 
     * @param donorId The donor's ID
     * @return The donor's name, or "Valued Donor" if not found
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    private String getDonorName(int donorId) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT name FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, donorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return "Valued Donor";
    }
    
    /**
     * Gets case title/type by ID for email notifications.
     * 
     * @param caseId The case ID
     * @return The case type/title, or "Charity Case" if not found
     * @throws SQLException if database operation fails
     * @throws DatabaseConnectionException if connection fails
     */
    private String getCaseName(int caseId) throws SQLException, DatabaseConnectionException {
        String sql = "SELECT type FROM charity_cases WHERE case_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, caseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("type");
                }
            }
        }
        return "Charity Case";
    }
}
