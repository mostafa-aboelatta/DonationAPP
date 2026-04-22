package com.example.donationapp.controllers;

import com.example.donationapp.HelloApplication;
import com.example.donationapp.dao.CharityCaseDAO;
import com.example.donationapp.dao.DonationDAO;
import com.example.donationapp.exceptions.*;
import com.example.donationapp.interfaces.Observer;
import com.example.donationapp.models.*;
import com.example.donationapp.util.NotificationManager;
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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Receiver dashboard.
 * Handles case submission and status tracking.
 */
public class ReceiverController implements Initializable, Observer {
    
    private static final Logger LOGGER = Logger.getLogger(ReceiverController.class.getName());
    
    @FXML private Label welcomeLabel;
    @FXML private Label notificationLabel;
    @FXML private Label statusLabel;
    @FXML private Label activeCasesLabel;
    @FXML private Label totalReceivedLabel;
    @FXML private Label pendingCasesLabel;
    @FXML private Label applyMessageLabel;
    
    // Content views
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private VBox applyForCaseView;
    @FXML private VBox myCasesView;
    @FXML private VBox receivedDonationsView;
    
    // Overview cases table (dashboard)
    @FXML private TableView<CharityCase> overviewCasesTable;
    @FXML private TableColumn<CharityCase, String> caseNameCol;
    @FXML private TableColumn<CharityCase, Double> caseGoalCol;
    @FXML private TableColumn<CharityCase, Double> caseRaisedCol;
    @FXML private TableColumn<CharityCase, String> caseStatusCol;
    @FXML private TableColumn<CharityCase, String> caseProgressCol;
    
    // My cases table
    @FXML private TableView<CharityCase> myCasesTable;
    @FXML private TableColumn<CharityCase, String> myCaseNameCol;
    @FXML private TableColumn<CharityCase, String> myCaseDescCol;
    @FXML private TableColumn<CharityCase, Double> myCaseGoalCol;
    @FXML private TableColumn<CharityCase, Double> myCaseRaisedCol;
    @FXML private TableColumn<CharityCase, String> myCaseStatusCol;
    @FXML private TableColumn<CharityCase, String> myCaseDateCol;
    
    // Received donations table
    @FXML private TableView<Donation> receivedDonationsTable;
    @FXML private TableColumn<Donation, String> donationDateCol;
    @FXML private TableColumn<Donation, String> donationDonorCol;
    @FXML private TableColumn<Donation, String> donationTypeCol;
    @FXML private TableColumn<Donation, Double> donationAmountCol;
    @FXML private TableColumn<Donation, String> donationCaseCol;
    
    // Case submission form
    @FXML private TextField caseTitleField;
    @FXML private TextArea caseDescriptionField;
    @FXML private TextField goalAmountField;
    @FXML private ComboBox<String> categoryComboBox;
    
    @FXML private Button submitCaseBtn;
    @FXML private Button logoutButton;
    
    // Sidebar navigation buttons
    @FXML private Button applyForCaseBtn;
    @FXML private Button myCasesBtn;
    @FXML private Button viewDonationsBtn;
    
    private final CharityCaseDAO caseDAO;
    private final DonationDAO donationDAO;
    private Receiver currentReceiver;
    
    public ReceiverController() {
        this.caseDAO = new CharityCaseDAO();
        this.donationDAO = new DonationDAO();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register as observer
        NotificationManager.getInstance().addObserver(this);
        
        // Initialize category combo box if needed
        if (categoryComboBox != null && categoryComboBox.getItems().isEmpty()) {
            categoryComboBox.getItems().addAll(
                "Medical", "Education", "Emergency", "Housing", "Food", "Other"
            );
        }
        
        // Initialize table columns
        initializeTableColumns();
        
        // Load user data
        loadUserData();
        
        // Load cases
        loadMyCases();
        
        // Show dashboard
        showDashboard();
    }
    
    /**
     * Initializes table column bindings.
     */
    private void initializeTableColumns() {
        // Overview cases table
        if (caseNameCol != null) {
            caseNameCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (caseGoalCol != null) {
            caseGoalCol.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        }
        if (caseRaisedCol != null) {
            caseRaisedCol.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        }
        if (caseStatusCol != null) {
            caseStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
        
        // My cases table
        if (myCaseNameCol != null) {
            myCaseNameCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (myCaseDescCol != null) {
            myCaseDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (myCaseGoalCol != null) {
            myCaseGoalCol.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        }
        if (myCaseRaisedCol != null) {
            myCaseRaisedCol.setCellValueFactory(new PropertyValueFactory<>("currentAmount"));
        }
        if (myCaseStatusCol != null) {
            myCaseStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
        
        // Received donations table
        if (donationDateCol != null) {
            donationDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        }
        if (donationDonorCol != null) {
            donationDonorCol.setCellValueFactory(new PropertyValueFactory<>("donorName"));
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
    }
    
    /**
     * Loads current user data.
     */
    private void loadUserData() {
        User user = HelloApplication.getCurrentUser();
        if (user instanceof Receiver) {
            currentReceiver = (Receiver) user;
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + currentReceiver.getName());
            }
        }
    }
    
    /**
     * Loads cases submitted by the current receiver.
     */
    private void loadMyCases() {
        if (currentReceiver == null) return;
        
        try {
            ArrayList<CharityCase> cases = caseDAO.getCasesByUserId(currentReceiver.getId());
            
            if (overviewCasesTable != null) {
                ObservableList<CharityCase> caseList = FXCollections.observableArrayList(cases);
                overviewCasesTable.setItems(caseList);
            }
            
            if (myCasesTable != null) {
                ObservableList<CharityCase> caseList = FXCollections.observableArrayList(cases);
                myCasesTable.setItems(caseList);
            }
            
            // Update stats
            long activeCases = cases.stream().filter(c -> "Active".equals(c.getStatus())).count();
            long pendingCases = cases.stream().filter(c -> "Pending".equals(c.getStatus())).count();
            double totalReceived = cases.stream().mapToDouble(CharityCase::getCurrentAmount).sum();
            
            if (activeCasesLabel != null) {
                activeCasesLabel.setText(String.valueOf(activeCases));
            }
            if (pendingCasesLabel != null) {
                pendingCasesLabel.setText(String.valueOf(pendingCases));
            }
            if (totalReceivedLabel != null) {
                totalReceivedLabel.setText(String.format("$%.2f", totalReceived));
            }
            
        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading cases", e);
            showError("Could not load your cases");
        }
    }
    
    /**
     * Shows dashboard view.
     */
    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) {
            dashboardView.setVisible(true);
            dashboardView.setManaged(true);
        }
    }
    
    /**
     * Shows apply for case view.
     */
    @FXML
    private void showApplyForCase() {
        hideAllViews();
        if (applyForCaseView != null) {
            applyForCaseView.setVisible(true);
            applyForCaseView.setManaged(true);
        }
    }
    
    /**
     * Shows my cases view.
     */
    @FXML
    private void showMyCases() {
        loadMyCases();
        hideAllViews();
        if (myCasesView != null) {
            myCasesView.setVisible(true);
            myCasesView.setManaged(true);
        }
    }
    
    /**
     * Shows received donations view.
     */
    @FXML
    private void showReceivedDonations() {
        hideAllViews();
        if (receivedDonationsView != null) {
            receivedDonationsView.setVisible(true);
            receivedDonationsView.setManaged(true);
        }
    }
    
    private void hideAllViews() {
        if (dashboardView != null) {
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
        }
        if (applyForCaseView != null) {
            applyForCaseView.setVisible(false);
            applyForCaseView.setManaged(false);
        }
        if (myCasesView != null) {
            myCasesView.setVisible(false);
            myCasesView.setManaged(false);
        }
        if (receivedDonationsView != null) {
            receivedDonationsView.setVisible(false);
            receivedDonationsView.setManaged(false);
        }
    }
    
    /**
     * Handles the submit case button click.
     */
    @FXML
    private void handleSubmitCase() {
        if (applyMessageLabel != null) {
            applyMessageLabel.setText("");
        }
        
        if (currentReceiver == null) {
            showApplyError("User not logged in");
            return;
        }
        
        String title = caseTitleField != null ? caseTitleField.getText().trim() : "";
        String description = caseDescriptionField != null ? caseDescriptionField.getText().trim() : "";
        String goalStr = goalAmountField != null ? goalAmountField.getText().trim() : "";
        String category = categoryComboBox != null ? categoryComboBox.getValue() : "";
        
        // Validate inputs
        if (title.isEmpty() || description.isEmpty() || goalStr.isEmpty()) {
            showApplyError("Please fill in all required fields");
            return;
        }
        
        if (description.length() < 20) {
            showApplyError("Description must be at least 20 characters");
            return;
        }
        
        double goalAmount;
        try {
            goalAmount = Double.parseDouble(goalStr);
            if (goalAmount <= 0) {
                showApplyError("Goal amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            showApplyError("Please enter a valid goal amount");
            return;
        }
        
        try {
            // Create charity case
            CharityCase charityCase = new CharityCase(currentReceiver.getId(), title, description);
            charityCase.setGoalAmount(goalAmount);
            charityCase.setCategory(category);
            
            // Save to database
            int caseId = caseDAO.insertCase(charityCase);
            
            // Notify observers
            NotificationManager.getInstance().notifyNewCaseSubmission(caseId, category);
            
            // Show success
            showApplySuccess("Case submitted successfully! Waiting for approval.");
            
            // Clear form
            clearForm();
            
            // Refresh cases list
            loadMyCases();
            
        } catch (SQLException | DatabaseConnectionException e) {
            showApplyError("Could not submit case. Please try again.");
            LOGGER.log(Level.SEVERE, "Error submitting case", e);
        }
    }
    
    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleLogout() {
        // Remove observer
        NotificationManager.getInstance().removeObserver(this);
        
        // Clear current user
        LoginController.logout();
        
        // Navigate to login
        HelloApplication.showLoginScreen();
    }
    
    /**
     * Clears the form.
     */
    private void clearForm() {
        if (caseTitleField != null) caseTitleField.clear();
        if (caseDescriptionField != null) caseDescriptionField.clear();
        if (goalAmountField != null) goalAmountField.clear();
        if (categoryComboBox != null) categoryComboBox.setValue(null);
    }
    
    private void showApplyError(String message) {
        if (applyMessageLabel != null) {
            applyMessageLabel.setText(message);
            applyMessageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void showApplySuccess(String message) {
        if (applyMessageLabel != null) {
            applyMessageLabel.setText(message);
            applyMessageLabel.setStyle("-fx-text-fill: green;");
        }
    }
    
    /**
     * Shows an error message.
     */
    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
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
            // Refresh cases when there's a notification (in case status changed)
            loadMyCases();
        });
    }
}
