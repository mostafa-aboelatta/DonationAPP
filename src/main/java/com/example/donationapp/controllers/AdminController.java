package com.example.donationapp.controllers;

import com.example.donationapp.HelloApplication;
import com.example.donationapp.dao.CharityCaseDAO;
import com.example.donationapp.dao.DonationDAO;
import com.example.donationapp.dao.UserDAO;
import com.example.donationapp.exceptions.*;
import com.example.donationapp.interfaces.Observer;
import com.example.donationapp.models.*;
import com.example.donationapp.util.NotificationManager;
import com.example.donationapp.util.UtilitySupport;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

//Handles case review, approval, user management, and system administration.
public class AdminController implements Initializable, Observer
{
    
    private static final Logger LOGGER = Logger.getLogger(AdminController.class.getName());
    
    // Top bar elements
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    
    // Sidebar buttons
    @FXML private Button manageUsersBtn;
    @FXML private Button manageCasesBtn;
    @FXML private Button viewDonationsBtn;
    @FXML private Button verifyDonorsBtn;
    @FXML private Button reportsBtn;
    
    // Sidebar notification
    @FXML private Label notificationLabel;
    
    // Dashboard stats labels
    @FXML private Label totalUsersLabel;
    @FXML private Label totalDonorsLabel;
    @FXML private Label totalCasesLabel;
    @FXML private Label totalDonationsLabel;
    @FXML private Label statusLabel;
    
    // Dashboard tables
    @FXML private TableView<?> pendingVerificationsTable;
    @FXML private TableColumn<?, ?> pendingUserCol;
    @FXML private TableColumn<?, ?> pendingEmailCol;
    @FXML private TableColumn<?, ?> pendingDateCol;
    
    @FXML private TableView<?> recentDonationsTable;
    @FXML private TableColumn<?, ?> recentDonorCol;
    @FXML private TableColumn<?, ?> recentAmountCol;
    @FXML private TableColumn<?, ?> recentCaseCol;
    
    // Content area
    @FXML private javafx.scene.layout.StackPane contentArea;
    
    // View panels
    @FXML private VBox dashboardView;
    @FXML private VBox manageUsersView;
    @FXML private VBox manageCasesView;
    @FXML private VBox allDonationsView;
    @FXML private VBox verifyDonorsView;
    @FXML private VBox reportsView;
    
    // Manage Users elements
    @FXML private TextField userSearchField;
    @FXML private ComboBox<String> userTypeFilterCombo;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> userIdCol;
    @FXML private TableColumn<User, String> userNameCol;
    @FXML private TableColumn<User, String> userEmailCol;
    @FXML private TableColumn<User, String> userTypeCol;
    @FXML private TableColumn<User, String> userStatusCol;
    @FXML private TableColumn<User, Void> userActionsCol;
    
    // Manage Cases elements
    @FXML private ComboBox<String> caseStatusFilterCombo;
    @FXML private TableView<CharityCase> casesTable;
    @FXML private TableColumn<CharityCase, Integer> caseIdCol;
    @FXML private TableColumn<CharityCase, String> caseNameCol;
    @FXML private TableColumn<CharityCase, String> caseReceiverCol;
    @FXML private TableColumn<CharityCase, Double> caseGoalCol;
    @FXML private TableColumn<CharityCase, Double> caseRaisedCol;
    @FXML private TableColumn<CharityCase, String> caseStatusCol;
    @FXML private TableColumn<CharityCase, Void> caseActionsCol;
    @FXML private Button approveCaseBtn;
    @FXML private Button rejectCaseBtn;
    
    // All Donations elements
    @FXML private TableView<Donation> allDonationsTable;
    @FXML private TableColumn<Donation, Integer> donationIdCol;
    @FXML private TableColumn<Donation, String> donationDonorCol;
    @FXML private TableColumn<Donation, String> donationTypeCol;
    @FXML private TableColumn<Donation, Double> donationAmountCol;
    @FXML private TableColumn<Donation, String> donationCaseCol;
    @FXML private TableColumn<Donation, String> donationDateCol;
    @FXML private TableColumn<Donation, String> donationStatusCol;
    
    // Verify Donors elements
    @FXML private TableView<User> unverifiedDonorsTable;
    @FXML private TableColumn<User, Integer> unverifiedIdCol;
    @FXML private TableColumn<User, String> unverifiedNameCol;
    @FXML private TableColumn<User, String> unverifiedEmailCol;
    @FXML private TableColumn<User, String> unverifiedPhoneCol;
    @FXML private TableColumn<User, String> unverifiedDateCol;
    
    // Reports labels
    @FXML private Label reportTotalDonations;
    @FXML private Label reportAvgDonation;
    @FXML private Label reportDonationCount;
    @FXML private Label reportTotalUsers;
    @FXML private Label reportVerifiedDonors;
    @FXML private Label reportActiveReceivers;
    @FXML private Label reportActiveCases;
    @FXML private Label reportCompletedCases;
    @FXML private Label reportTotalRaised;
    
    private final CharityCaseDAO caseDAO;
    private final DonationDAO donationDAO;
    private final UserDAO userDAO;
    private Admin currentAdmin;
    
    public AdminController() {
        this.caseDAO = new CharityCaseDAO();
        this.donationDAO = new DonationDAO();
        this.userDAO = new UserDAO();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Register as observer
            NotificationManager.getInstance().addObserver(this);
            
            // Initialize table columns
            initializeTableColumns();
            
            // Load user data
            loadUserData();
            
            // Load dashboard statistics
            loadDashboardStats();
            
            // Show dashboard by default
            showDashboard();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing admin dashboard", e);
            // Show dashboard anyway even if data loading fails
            if (dashboardView != null) {
                dashboardView.setVisible(true);
                dashboardView.setManaged(true);
            }
        }
    }
    
    /**
     * Initializes table column bindings.
     */
    private void initializeTableColumns() {
        // Users table columns
        if (userIdCol != null) userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (userNameCol != null) userNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (userEmailCol != null) userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (userTypeCol != null) userTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        if (userStatusCol != null) userStatusCol.setCellValueFactory(new PropertyValueFactory<>("verified"));
        
        // Cases table columns
        if (caseIdCol != null) caseIdCol.setCellValueFactory(new PropertyValueFactory<>("caseId"));
        if (caseNameCol != null) caseNameCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (caseStatusCol != null) caseStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        if (caseGoalCol != null) caseGoalCol.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        if (caseRaisedCol != null) caseRaisedCol.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        
        // Donations table columns
        if (donationIdCol != null) donationIdCol.setCellValueFactory(new PropertyValueFactory<>("donationId"));
        if (donationTypeCol != null) donationTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        if (donationAmountCol != null) donationAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        if (donationDateCol != null) donationDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        // Unverified donors table columns
        if (unverifiedIdCol != null) unverifiedIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (unverifiedNameCol != null) unverifiedNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (unverifiedEmailCol != null) unverifiedEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (unverifiedPhoneCol != null) unverifiedPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
    }
    
    /**
     * Loads current admin data.
     */
    private void loadUserData() {
        User user = HelloApplication.getCurrentUser();
        if (user instanceof Admin) {
            currentAdmin = (Admin) user;
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + currentAdmin.getName());
            }
        }
    }
    
    /**
     * Loads dashboard statistics.
     */
    private void loadDashboardStats() {
        try {
            // Get counts
            ArrayList<User> allUsers = userDAO.getAllUsers();
            int totalUsers = allUsers.size();
            int totalDonors = (int) allUsers.stream().filter(u -> "DONOR".equals(u.getUserType())).count();
            
            int activeCases = caseDAO.getCaseCountByStatus("Accepted - High Priority") + 
                             caseDAO.getCaseCountByStatus("Accepted - Low Priority");
            double totalDonationAmount = donationDAO.getTotalDonationAmount();
            
            // Update labels
            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(totalUsers));
            if (totalDonorsLabel != null) totalDonorsLabel.setText(String.valueOf(totalDonors));
            if (totalCasesLabel != null) totalCasesLabel.setText(String.valueOf(activeCases));
            if (totalDonationsLabel != null) totalDonationsLabel.setText(UtilitySupport.formatCurrency(totalDonationAmount));
            
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading dashboard stats", e);
        }
    }
    
    // ==================== VIEW SWITCHING METHODS ====================
    
    /**
     * Shows the main dashboard view.
     */
    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) {
            dashboardView.setVisible(true);
            dashboardView.setManaged(true);
        }
        loadDashboardStats();
        updateStatus("Dashboard loaded");
    }
    
    /**
     * Shows the manage users view.
     */
    @FXML
    private void showManageUsers() {
        hideAllViews();
        if (manageUsersView != null) {
            manageUsersView.setVisible(true);
            manageUsersView.setManaged(true);
        }
        loadAllUsers();
        updateStatus("Manage Users view loaded");
    }
    
    /**
     * Shows the manage cases view.
     */
    @FXML
    private void showManageCases() {
        hideAllViews();
        if (manageCasesView != null) {
            manageCasesView.setVisible(true);
            manageCasesView.setManaged(true);
        }
        loadAllCases();
        updateStatus("Manage Cases view loaded");
    }
    
    /**
     * Shows all donations view.
     */
    @FXML
    private void showAllDonations() {
        hideAllViews();
        if (allDonationsView != null) {
            allDonationsView.setVisible(true);
            allDonationsView.setManaged(true);
        }
        loadAllDonations();
        updateStatus("All Donations view loaded");
    }
    
    /**
     * Shows verify donors view.
     */
    @FXML
    private void showVerifyDonors() {
        hideAllViews();
        if (verifyDonorsView != null) {
            verifyDonorsView.setVisible(true);
            verifyDonorsView.setManaged(true);
        }
        loadUnverifiedDonors();
        updateStatus("Verify Donors view loaded");
    }
    
    /**
     * Shows reports view.
     */
    @FXML
    private void showReports() {
        hideAllViews();
        if (reportsView != null) {
            reportsView.setVisible(true);
            reportsView.setManaged(true);
        }
        loadReportsData();
        updateStatus("Reports view loaded");
    }
    
    /**
     * Hides all view panels.
     */
    private void hideAllViews() {
        VBox[] views = {dashboardView, manageUsersView, manageCasesView, 
                        allDonationsView, verifyDonorsView, reportsView};
        for (VBox view : views) {
            if (view != null) {
                view.setVisible(false);
                view.setManaged(false);
            }
        }
    }
    
    // ==================== DATA LOADING METHODS ====================
    
    /**
     * Loads all users into the users table.
     */
    private void loadAllUsers() {
        try {
            ArrayList<User> users = userDAO.getAllUsers();
            if (usersTable != null) {
                ObservableList<User> userList = FXCollections.observableArrayList(users);
                usersTable.setItems(userList);
            }
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading users", e);
            showNotification("Error loading users");
        }
    }
    
    /**
     * Loads all charity cases into the cases table.
     */
    private void loadAllCases() {
        try {
            ArrayList<CharityCase> allCases = new ArrayList<>();
            allCases.addAll(caseDAO.getPendingCases());
            allCases.addAll(caseDAO.getAllAcceptedCases());
            
            if (casesTable != null) {
                ObservableList<CharityCase> caseList = FXCollections.observableArrayList(allCases);
                casesTable.setItems(caseList);
            }
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading cases", e);
            showNotification("Error loading cases");
        }
    }

    /**
     * Loads all donations into the donations table.
     */
    private void loadAllDonations() {
        try {
            ArrayList<Donation> donations = donationDAO.getAllDonations();
            if (allDonationsTable != null) {
                ObservableList<Donation> donationList = FXCollections.observableArrayList(donations);
                allDonationsTable.setItems(donationList);
            }
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading donations", e);
            showNotification("Error loading donations");
        }
    }
    
    /**
     * Loads unverified donors into the verification table.
     */
    private void loadUnverifiedDonors() {
        try {
            ArrayList<User> allUsers = userDAO.getAllUsers();
            ArrayList<User> unverified = new ArrayList<>();
            for (User user : allUsers) {
                if ("DONOR".equals(user.getUserType()) && !user.isVerified()) {
                    unverified.add(user);
                }
            }
            
            if (unverifiedDonorsTable != null) {
                ObservableList<User> userList = FXCollections.observableArrayList(unverified);
                unverifiedDonorsTable.setItems(userList);
            }
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading unverified donors", e);
            showNotification("Error loading unverified donors");
        }
    }
    
    /**
     * Loads data for reports view.
     */
    private void loadReportsData() {
        try {
            // Donation stats
            double totalDonationAmount = donationDAO.getTotalDonationAmount();
            int donationCount = donationDAO.getDonationCount();
            double avgDonation = donationCount > 0 ? totalDonationAmount / donationCount : 0;
            
            if (reportTotalDonations != null) reportTotalDonations.setText("Total Donations: " + UtilitySupport.formatCurrency(totalDonationAmount));
            if (reportAvgDonation != null) reportAvgDonation.setText("Average Donation: " + UtilitySupport.formatCurrency(avgDonation));
            if (reportDonationCount != null) reportDonationCount.setText("Number of Donations: " + donationCount);
            
            // User stats
            ArrayList<User> allUsers = userDAO.getAllUsers();
            int totalUsers = allUsers.size();
            int verifiedDonors = (int) allUsers.stream()
                .filter(u -> "DONOR".equals(u.getUserType()) && u.isVerified()).count();
            int activeReceivers = (int) allUsers.stream()
                .filter(u -> "RECEIVER".equals(u.getUserType())).count();
            
            if (reportTotalUsers != null) reportTotalUsers.setText("Total Users: " + totalUsers);
            if (reportVerifiedDonors != null) reportVerifiedDonors.setText("Verified Donors: " + verifiedDonors);
            if (reportActiveReceivers != null) reportActiveReceivers.setText("Active Receivers: " + activeReceivers);
            
            // Case stats
            int activeCases = caseDAO.getCaseCountByStatus("Accepted - High Priority") + 
                             caseDAO.getCaseCountByStatus("Accepted - Low Priority");
            int completedCases = caseDAO.getCaseCountByStatus("Completed");
            
            if (reportActiveCases != null) reportActiveCases.setText("Active Cases: " + activeCases);
            if (reportCompletedCases != null) reportCompletedCases.setText("Completed Cases: " + completedCases);
            if (reportTotalRaised != null) reportTotalRaised.setText("Total Raised: " + UtilitySupport.formatCurrency(totalDonationAmount));
            
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading reports data", e);
            showNotification("Error loading reports");
        }
    }
    
    // ==================== ACTION HANDLERS ====================
    
    /**
     * Handles deleting a user.
     */
    @FXML
    private void handleDeleteUser() {
        if (usersTable == null) return;
        
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showNotification("Please select a user to delete");
            return;
        }
        
        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete User: " + selectedUser.getName() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userDAO.deleteUser(selectedUser.getId());
                showNotification("User deleted successfully");
                loadAllUsers();
                loadDashboardStats();
            } catch (SQLException | DatabaseConnectionException e) {
                LOGGER.log(Level.SEVERE, "Error deleting user", e);
                showNotification("Error deleting user");
            }
        }
    }
    
    /**
     * Handles refreshing users list.
     */
    @FXML
    private void handleRefreshUsers() {
        loadAllUsers();
        showNotification("Users list refreshed");
    }
    
    /**
     * Handles approving a case.
     */
    @FXML
    private void handleApproveCase() {
        if (casesTable == null) return;
        
        CharityCase selectedCase = casesTable.getSelectionModel().getSelectedItem();
        if (selectedCase == null) {
            showNotification("Please select a case to approve");
            return;
        }
        
        try {
            caseDAO.updateCaseStatus(selectedCase.getCaseId(), "Accepted - High Priority", "High");
            NotificationManager.getInstance().notifyCaseStatusChange(selectedCase.getCaseId(), "Approved");
            showNotification("Case approved successfully");
            loadAllCases();
            loadDashboardStats();
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.SEVERE, "Error approving case", e);
            showNotification("Error approving case");
        }
    }
    
    /**
     * Handles rejecting a case.
     */
    @FXML
    private void handleRejectCase() {
        if (casesTable == null) return;
        
        CharityCase selectedCase = casesTable.getSelectionModel().getSelectedItem();
        if (selectedCase == null) {
            showNotification("Please select a case to reject");
            return;
        }
        
        try {
            caseDAO.updateCaseStatus(selectedCase.getCaseId(), "Declined", "None");
            NotificationManager.getInstance().notifyCaseStatusChange(selectedCase.getCaseId(), "Rejected");
            showNotification("Case rejected");
            loadAllCases();
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.SEVERE, "Error rejecting case", e);
            showNotification("Error rejecting case");
        }
    }
    
    /**
     * Handles refreshing cases list.
     */
    @FXML
    private void handleRefreshCases() {
        loadAllCases();
        showNotification("Cases list refreshed");
    }
    
    /**
     * Handles verifying a donor.
     */
    @FXML
    private void handleVerifyDonor() {
        if (unverifiedDonorsTable == null) return;
        
        User selectedDonor = unverifiedDonorsTable.getSelectionModel().getSelectedItem();
        if (selectedDonor == null) {
            showNotification("Please select a donor to verify");
            return;
        }
        
        try {
            userDAO.updateVerificationStatus(selectedDonor.getId(), true);
            showNotification("Donor verified successfully");
            loadUnverifiedDonors();
            loadDashboardStats();
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.SEVERE, "Error verifying donor", e);
            showNotification("Error verifying donor");
        }
    }
    
    /**
     * Handles rejecting donor verification.
     */
    @FXML
    private void handleRejectDonor() {
        if (unverifiedDonorsTable == null) return;
        
        User selectedDonor = unverifiedDonorsTable.getSelectionModel().getSelectedItem();
        if (selectedDonor == null) {
            showNotification("Please select a donor to reject");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reject Verification");
        confirm.setHeaderText("Reject verification for: " + selectedDonor.getName() + "?");
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userDAO.deleteUser(selectedDonor.getId());
                showNotification("Donor verification rejected");
                loadUnverifiedDonors();
            } catch (SQLException | DatabaseConnectionException e) {
                LOGGER.log(Level.SEVERE, "Error rejecting donor", e);
                showNotification("Error rejecting donor");
            }
        }
    }
    
    /**
     * Handles logout.
     */
    @FXML
    private void handleLogout() {
        NotificationManager.getInstance().removeObserver(this);
        LoginController.logout();
        HelloApplication.showLoginScreen();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Updates status bar.
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }
    
    /**
     * Shows notification in sidebar.
     */
    private void showNotification(String message) {
        if (notificationLabel != null) {
            notificationLabel.setText(message);
        }
    }
    
    @Override
    public void update(String message) {
        Platform.runLater(() -> {
            showNotification(message);
            loadDashboardStats();
        });
    }
}
