package com.example.donationapp.controllers;

import com.example.donationapp.HelloApplication;
import com.example.donationapp.dao.CharityCaseDAO;
import com.example.donationapp.dao.DonationDAO;
import com.example.donationapp.dao.UserDAO;
import com.example.donationapp.exceptions.*;
import com.example.donationapp.factory.DonationFactory;
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
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Donor dashboard.
 * Handles donation operations and case viewing.
 */
public class DonorController implements Initializable, Observer {
    
    private static final Logger LOGGER = Logger.getLogger(DonorController.class.getName());
    
    @FXML private Label welcomeLabel;
    @FXML private Label verificationStatusLabel;
    @FXML private Label totalDonationsLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label notificationLabel;
    @FXML private Label statusLabel;
    @FXML private Label donationMessageLabel;
    
    // Content area views
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private VBox makeDonationView;
    @FXML private VBox myDonationsView;
    @FXML private VBox charityCasesView;
    
    // Recent cases table (dashboard)
    @FXML private TableView<CharityCase> recentCasesTable;
    @FXML private TableColumn<CharityCase, String> caseNameCol;
    @FXML private TableColumn<CharityCase, String> caseDescCol;
    @FXML private TableColumn<CharityCase, Double> caseGoalCol;
    @FXML private TableColumn<CharityCase, Double> caseRaisedCol;
    
    // My donations table
    @FXML private TableView<Donation> myDonationsTable;
    @FXML private TableColumn<Donation, String> donationDateCol;
    @FXML private TableColumn<Donation, String> donationTypeCol;
    @FXML private TableColumn<Donation, Double> donationAmountCol;
    @FXML private TableColumn<Donation, String> donationCaseCol;
    @FXML private TableColumn<Donation, String> donationStatusCol;
    
    // All cases table
    @FXML private TableView<CharityCase> allCasesTable;
    @FXML private TableColumn<CharityCase, String> allCaseNameCol;
    @FXML private TableColumn<CharityCase, String> allCaseDescCol;
    @FXML private TableColumn<CharityCase, Double> allCaseGoalCol;
    @FXML private TableColumn<CharityCase, Double> allCaseRaisedCol;
    @FXML private TableColumn<CharityCase, String> allCaseProgressCol;
    
    // Donation form
    @FXML private ComboBox<String> donationTypeComboBox;
    @FXML private ComboBox<CharityCase> charityCaseComboBox;
    @FXML private TextField amountField;
    @FXML private TextArea descriptionField;
    
    @FXML private Button submitDonationBtn;
    @FXML private Button logoutButton;
    @FXML private Button verifyAccountBtn;
    @FXML private Button makeDonationBtn;
    @FXML private Button viewDonationsBtn;
    @FXML private Button viewCasesBtn;
    
    private CharityCaseDAO caseDAO;
    private DonationDAO donationDAO;
    private UserDAO userDAO;
    private Donor currentDonor;
    
    public DonorController() {
        try {
            this.caseDAO = new CharityCaseDAO();
            this.donationDAO = new DonationDAO();
            this.userDAO = new UserDAO();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing DAOs", e);
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register as observer for notifications
        NotificationManager.getInstance().addObserver(this);
        
        // Initialize donation type combo box
        if (donationTypeComboBox != null && donationTypeComboBox.getItems().isEmpty()) {
            donationTypeComboBox.getItems().addAll("Financial", "Medical", "Other");
            donationTypeComboBox.setValue("Financial");
        }
        
        // Initialize table columns
        initializeTableColumns();
        
        // Load current user data
        loadUserData();
        
        // Load available cases
        loadAvailableCases();
        
        // Load donation history
        loadDonationHistory();
        
        // Show dashboard by default
        showDashboard();
    }
    
    /**
     * Initializes table column bindings.
     */
    private void initializeTableColumns() {
        // Recent cases table (dashboard)
        if (caseNameCol != null) {
            caseNameCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (caseDescCol != null) {
            caseDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (caseGoalCol != null) {
            caseGoalCol.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        }
        if (caseRaisedCol != null) {
            caseRaisedCol.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        }
        
        // My donations table
        if (donationDateCol != null) {
            donationDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        }
        if (donationTypeCol != null) {
            donationTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (donationAmountCol != null) {
            donationAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        }
        if (donationCaseCol != null) {
            donationCaseCol.setCellValueFactory(new PropertyValueFactory<>("caseName"));
        }
        if (donationStatusCol != null) {
            donationStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
        
        // All cases table
        if (allCaseNameCol != null) {
            allCaseNameCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (allCaseDescCol != null) {
            allCaseDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (allCaseGoalCol != null) {
            allCaseGoalCol.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        }
        if (allCaseRaisedCol != null) {
            allCaseRaisedCol.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        }
    }
    
    /**
     * Loads the current user's data.
     */
    private void loadUserData() {
        User user = HelloApplication.getCurrentUser();
        if (user instanceof Donor) {
            currentDonor = (Donor) user;
            
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + currentDonor.getName());
            }
            
            if (verificationStatusLabel != null) {
                if (currentDonor.isVerified()) {
                    verificationStatusLabel.setText("✓ Verified");
                    verificationStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                    if (verifyAccountBtn != null) {
                        verifyAccountBtn.setVisible(false);
                    }
                } else {
                    verificationStatusLabel.setText("⚠ Unverified");
                    verificationStatusLabel.setStyle("-fx-text-fill: #f39c12;");
                }
            }
        }
    }
    
    /**
     * Loads available charity cases.
     */
    private void loadAvailableCases() {
        if (caseDAO == null) return;
        
        try {
            ArrayList<CharityCase> cases = caseDAO.getAllAcceptedCases();
            
            if (recentCasesTable != null) {
                ObservableList<CharityCase> caseList = FXCollections.observableArrayList(cases);
                recentCasesTable.setItems(caseList);
            }
            
            if (allCasesTable != null) {
                ObservableList<CharityCase> caseList = FXCollections.observableArrayList(cases);
                allCasesTable.setItems(caseList);
            }
            
            if (charityCaseComboBox != null) {
                charityCaseComboBox.getItems().clear();
                charityCaseComboBox.getItems().addAll(cases);
            }
            
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading cases", e);
            showError("Could not load charity cases");
        }
    }
    
    /**
     * Loads donation history for the current donor.
     */
    private void loadDonationHistory() {
        if (currentDonor == null || donationDAO == null) return;
        
        try {
            ArrayList<Donation> donations = donationDAO.getDonationsByDonorId(currentDonor.getId());
            
            if (myDonationsTable != null) {
                ObservableList<Donation> donationList = FXCollections.observableArrayList(donations);
                myDonationsTable.setItems(donationList);
            }
            
            // Calculate totals
            int count = donations.size();
            double total = donations.stream().mapToDouble(Donation::getAmount).sum();
            
            if (totalDonationsLabel != null) {
                totalDonationsLabel.setText(String.valueOf(count));
            }
            if (totalAmountLabel != null) {
                totalAmountLabel.setText(UtilitySupport.formatCurrency(total));
            }
            
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading donations", e);
        }
    }
    
    /**
     * Handles the verify button click.
     */
    @FXML
    private void handleVerifyAccount() {
        if (currentDonor == null || userDAO == null) return;
        
        try {
            // Generate verification code
            String code = UtilitySupport.generateUniqueVerificationCode();
            
            // Send verification email
            UtilitySupport.sendVerificationEmail(currentDonor.getEmail(), code);
            
            // For demo purposes, auto-verify
            userDAO.updateVerificationStatus(currentDonor.getId(), true);
            currentDonor.setVerified(true);
            
            // Update UI
            if (verificationStatusLabel != null) {
                verificationStatusLabel.setText("✓ Verified");
                verificationStatusLabel.setStyle("-fx-text-fill: #27ae60;");
            }
            if (verifyAccountBtn != null) {
                verifyAccountBtn.setVisible(false);
            }
            
            showSuccess("Verification successful!");
            
        } catch (SQLException | DatabaseConnectionException e) {
            showError("Could not verify account");
            LOGGER.log(Level.SEVERE, "Error verifying account", e);
        }
    }
    
    /**
     * Shows the dashboard view.
     */
    @FXML
    private void showDashboard() {
        if (dashboardView != null) {
            dashboardView.setVisible(true);
            dashboardView.setManaged(true);
        }
        if (makeDonationView != null) {
            makeDonationView.setVisible(false);
            makeDonationView.setManaged(false);
        }
        if (myDonationsView != null) {
            myDonationsView.setVisible(false);
            myDonationsView.setManaged(false);
        }
        if (charityCasesView != null) {
            charityCasesView.setVisible(false);
            charityCasesView.setManaged(false);
        }
    }
    
    /**
     * Shows the make donation view.
     */
    @FXML
    private void showMakeDonation() {
        if (dashboardView != null) {
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
        }
        if (makeDonationView != null) {
            makeDonationView.setVisible(true);
            makeDonationView.setManaged(true);
        }
        if (myDonationsView != null) {
            myDonationsView.setVisible(false);
            myDonationsView.setManaged(false);
        }
        if (charityCasesView != null) {
            charityCasesView.setVisible(false);
            charityCasesView.setManaged(false);
        }
    }
    
    /**
     * Shows my donations view.
     */
    @FXML
    private void showMyDonations() {
        loadDonationHistory();
        if (dashboardView != null) {
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
        }
        if (makeDonationView != null) {
            makeDonationView.setVisible(false);
            makeDonationView.setManaged(false);
        }
        if (myDonationsView != null) {
            myDonationsView.setVisible(true);
            myDonationsView.setManaged(true);
        }
        if (charityCasesView != null) {
            charityCasesView.setVisible(false);
            charityCasesView.setManaged(false);
        }
    }
    
    /**
     * Shows charity cases view.
     */
    @FXML
    private void showCharityCases() {
        loadAvailableCases();
        if (dashboardView != null) {
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
        }
        if (makeDonationView != null) {
            makeDonationView.setVisible(false);
            makeDonationView.setManaged(false);
        }
        if (myDonationsView != null) {
            myDonationsView.setVisible(false);
            myDonationsView.setManaged(false);
        }
        if (charityCasesView != null) {
            charityCasesView.setVisible(true);
            charityCasesView.setManaged(true);
        }
    }
    
    /**
     * Handles submit donation button.
     */
    @FXML
    private void handleSubmitDonation() {
        if (donationMessageLabel != null) {
            donationMessageLabel.setText("");
            donationMessageLabel.setStyle("-fx-text-fill: red;");
        }
        
        // Check if donor is verified
        if (currentDonor != null && !currentDonor.isVerified()) {
            if (donationMessageLabel != null) {
                donationMessageLabel.setText("Please verify your account before making donations");
            }
            return;
        }
        
        String donationType = donationTypeComboBox != null ? donationTypeComboBox.getValue() : "Financial";
        CharityCase selectedCase = charityCaseComboBox != null ? charityCaseComboBox.getValue() : null;
        
        if (selectedCase == null) {
            if (donationMessageLabel != null) {
                donationMessageLabel.setText("Please select a charity case");
            }
            return;
        }
        
        try {
            Map<String, Object> params = new HashMap<>();
            String amountStr = amountField != null ? amountField.getText().trim() : "";
            
            if (amountStr.isEmpty()) {
                if (donationMessageLabel != null) {
                    donationMessageLabel.setText("Please enter an amount");
                }
                return;
            }
            
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    if (donationMessageLabel != null) {
                        donationMessageLabel.setText("Amount must be greater than 0");
                    }
                    return;
                }
            } catch (NumberFormatException e) {
                if (donationMessageLabel != null) {
                    donationMessageLabel.setText("Please enter a valid amount");
                }
                return;
            }
            
            params.put("amount", amount);
            if (descriptionField != null && !descriptionField.getText().isEmpty()) {
                params.put("description", descriptionField.getText());
            }
            
            Donation donation = DonationFactory.createDonation(donationType, params);
            
            // Save to database
            int donationId = donationDAO.insertDonation(donation, currentDonor.getId(), selectedCase.getCaseId());
            
            // Notify observers
            NotificationManager.getInstance().notifyNewDonation(
                currentDonor.getName(), 
                donation.getAmount(), 
                selectedCase.getCaseId()
            );
            
            // Show success message
            if (donationMessageLabel != null) {
                donationMessageLabel.setText("Donation successful! Thank you.");
                donationMessageLabel.setStyle("-fx-text-fill: green;");
            }
            
            // Clear form
            if (amountField != null) amountField.clear();
            if (descriptionField != null) descriptionField.clear();
            if (charityCaseComboBox != null) charityCaseComboBox.setValue(null);
            
            // Refresh data
            loadDonationHistory();
            loadAvailableCases();
            
        } catch (InvalidDonationException e) {
            if (donationMessageLabel != null) {
                donationMessageLabel.setText(e.getMessage());
            }
        } catch (SQLException | DatabaseConnectionException e) {
            if (donationMessageLabel != null) {
                donationMessageLabel.setText("Could not process donation. Please try again.");
            }
            LOGGER.log(Level.SEVERE, "Error processing donation", e);
        }
    }
    
    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        loadAvailableCases();
        loadDonationHistory();
        showSuccess("Data refreshed");
    }
    
    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleLogout() {
        // Remove observer
        NotificationManager.getInstance().removeObserver(this);
        
        // Clear current user
        HelloApplication.setCurrentUser(null);
        
        // Navigate to login
        HelloApplication.showLoginScreen();
    }
    
    /**
     * Shows an error message.
     */
    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
        if (donationMessageLabel != null) {
            donationMessageLabel.setText(message);
            donationMessageLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }
    
    /**
     * Shows a success message.
     */
    private void showSuccess(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: #27ae60;");
        }
    }
    
    @Override
    public void update(String message) {
        Platform.runLater(() -> {
            if (notificationLabel != null) {
                notificationLabel.setText(message);
            }
        });
    }
}
