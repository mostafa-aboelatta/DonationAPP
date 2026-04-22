package com.example.donationapp.controllers;

import com.example.donationapp.HelloApplication;
import com.example.donationapp.dao.CharityCaseDAO;
import com.example.donationapp.dao.UserDAO;
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
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Volunteer dashboard.
 * Handles volunteer activities and hour logging.
 */
public class VolunteerController implements Initializable, Observer {

    private static final Logger LOGGER = Logger.getLogger(VolunteerController.class.getName());

    // Content views
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;
    @FXML private VBox casesView;
    @FXML private VBox assignmentsView;
    @FXML private VBox helpReceiversView;
    @FXML private VBox logHoursView;

    // Sidebar navigation buttons
    @FXML private Button viewCasesBtn;
    @FXML private Button myAssignmentsBtn;
    @FXML private Button helpReceiversBtn;
    @FXML private Button logHoursBtn;

    // Stats labels
    @FXML private Label assignedCasesLabel;
    @FXML private Label hoursLoggedLabel;
    @FXML private Label peopleHelpedLabel;

    // Top bar and info labels
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label hoursLabel;
    @FXML private Label locationLabel;
    @FXML private Label notificationLabel;
    @FXML private Label statusLabel;

    // Input fields
    @FXML private TextField hoursField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private ComboBox<String> assignmentComboBox;
    @FXML private TextArea notesArea;
    @FXML private DatePicker datePicker;

    // Tables
    @FXML private TableView<CharityCase> availableCasesTable;
    @FXML private TableView<CharityCase> allCasesTable;
    @FXML private TableView<?> myAssignmentsTable;
    @FXML private TableView<?> receiversTable;

    // Table columns for available cases (dashboard)
    @FXML private TableColumn<CharityCase, String> caseNameCol;
    @FXML private TableColumn<CharityCase, String> caseDescCol;
    @FXML private TableColumn<CharityCase, String> caseCategoryCol;
    @FXML private TableColumn<CharityCase, String> caseUrgencyCol;

    // Table columns for all cases
    @FXML private TableColumn<CharityCase, String> allCaseNameCol;
    @FXML private TableColumn<CharityCase, String> allCaseDescCol;
    @FXML private TableColumn<CharityCase, String> allCaseReceiverCol;
    @FXML private TableColumn<CharityCase, Double> allCaseGoalCol;
    @FXML private TableColumn<CharityCase, String> allCaseProgressCol;

    // Assignment table columns
    @FXML private TableColumn<?, ?> assignmentCaseCol;
    @FXML private TableColumn<?, ?> assignmentReceiverCol;
    @FXML private TableColumn<?, ?> assignmentTaskCol;
    @FXML private TableColumn<?, ?> assignmentStatusCol;
    @FXML private TableColumn<?, ?> assignmentDateCol;

    // Receiver table columns
    @FXML private TableColumn<?, ?> receiverNameCol;
    @FXML private TableColumn<?, ?> receiverCaseCol;
    @FXML private TableColumn<?, ?> receiverNeedCol;
    @FXML private TableColumn<?, ?> receiverContactCol;

    // Labels and buttons
    @FXML private Label errorLabel;
    @FXML private Button logHoursButton;
    @FXML private Button updateRoleButton;
    @FXML private Button logoutButton;

    private CharityCaseDAO caseDAO;
    private final UserDAO userDAO;
    private Volunteer currentVolunteer;

    public VolunteerController() {
        this.userDAO = new UserDAO();
        try {
            this.caseDAO = new CharityCaseDAO();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing CharityCaseDAO", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Register as observer
        NotificationManager.getInstance().addObserver(this);

        // Initialize combo boxes
        initializeComboBoxes();

        // Initialize table columns
        initializeTableColumns();

        // Load user data
        loadUserData();

        // Load available cases
        loadAvailableCases();

        // Update stats labels
        updateStatsLabels();
    }

    /**
     * Initializes combo boxes with default values.
     */
    private void initializeComboBoxes() {
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll(
                    "Unassigned",
                    "Case Coordinator",
                    "Donation Handler",
                    "Community Outreach",
                    "Administrative Support",
                    "Event Organizer"
            );
        }

        if (assignmentComboBox != null) {
            assignmentComboBox.getItems().addAll(
                    "General Volunteering",
                    "Food Distribution",
                    "Administrative Tasks",
                    "Community Outreach",
                    "Event Support"
            );
        }
    }

    /**
     * Initializes table column bindings.
     */
    private void initializeTableColumns() {
        // Available cases table (dashboard)
        if (caseNameCol != null) {
            caseNameCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (caseDescCol != null) {
            caseDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (caseCategoryCol != null) {
            caseCategoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        }
        if (caseUrgencyCol != null) {
            caseUrgencyCol.setCellValueFactory(new PropertyValueFactory<>("urgency"));
        }

        // All cases table
        if (allCaseNameCol != null) {
            allCaseNameCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        }
        if (allCaseDescCol != null) {
            allCaseDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        }
        if (allCaseReceiverCol != null) {
            allCaseReceiverCol.setCellValueFactory(new PropertyValueFactory<>("receiverName"));
        }
        if (allCaseGoalCol != null) {
            allCaseGoalCol.setCellValueFactory(new PropertyValueFactory<>("goalAmount"));
        }
        if (allCaseProgressCol != null) {
            allCaseProgressCol.setCellValueFactory(cellData -> {
                CharityCase charityCase = cellData.getValue();
                double progress = (charityCase.getCurrentAmount() / charityCase.getGoalAmount()) * 100;
                return new javafx.beans.property.SimpleStringProperty(
                        String.format("%.1f%%", progress)
                );
            });
        }
    }

    /**
     * Loads current volunteer data.
     */
    private void loadUserData() {
        User user = HelloApplication.getCurrentUser();
        if (user instanceof Volunteer) {
            currentVolunteer = (Volunteer) user;
            updateDisplay();
        }
    }

    /**
     * Loads available charity cases.
     */
    private void loadAvailableCases() {
        if (caseDAO == null) {
            LOGGER.log(Level.WARNING, "CharityCaseDAO is null, cannot load cases");
            return;
        }

        try {
            // Load all accepted cases (same as donor)
            ArrayList<CharityCase> cases = caseDAO.getAllAcceptedCases();

            LOGGER.log(Level.INFO, "Loaded {0} charity cases for volunteer", cases.size());

            // Populate dashboard table
            if (availableCasesTable != null) {
                ObservableList<CharityCase> caseList = FXCollections.observableArrayList(cases);
                availableCasesTable.setItems(caseList);
            }

            // Populate all cases table
            if (allCasesTable != null) {
                ObservableList<CharityCase> caseList = FXCollections.observableArrayList(cases);
                allCasesTable.setItems(caseList);
            }

            updateStatusLabel("Loaded " + cases.size() + " cases");

        } catch (SQLException | DatabaseConnectionException e) {
            LOGGER.log(Level.WARNING, "Error loading cases", e);
            showError("Could not load charity cases");
        }
    }

    /**
     * Updates the display with current volunteer information.
     */
    private void updateDisplay() {
        if (currentVolunteer == null) return;

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentVolunteer.getName());
        }

        if (roleLabel != null) {
            roleLabel.setText("Role: " + currentVolunteer.getRole());
        }

        if (hoursLabel != null) {
            hoursLabel.setText("Total Hours: " + currentVolunteer.getWorkingHours());
        }

        if (locationLabel != null) {
            locationLabel.setText("Location: " + currentVolunteer.getLocation());
        }

        if (roleComboBox != null) {
            roleComboBox.setValue(currentVolunteer.getRole());
        }

        updateStatsLabels();
    }

    /**
     * Updates the statistics labels on the dashboard.
     */
    private void updateStatsLabels() {
        if (currentVolunteer != null) {
            if (assignedCasesLabel != null) {
                assignedCasesLabel.setText("0"); // TODO: Implement actual count
            }
            if (hoursLoggedLabel != null) {
                hoursLoggedLabel.setText(String.valueOf(currentVolunteer.getWorkingHours()));
            }
            if (peopleHelpedLabel != null) {
                peopleHelpedLabel.setText("0"); // TODO: Implement actual count
            }
        }
    }

    /**
     * Shows the dashboard view.
     */
    @FXML
    private void showDashboard() {
        hideAllViews();
        if (dashboardView != null) {
            dashboardView.setVisible(true);
            dashboardView.setManaged(true);
        }
        loadAvailableCases(); // Refresh cases when showing dashboard
        updateStatusLabel("Dashboard loaded");
    }

    /**
     * Shows active cases view.
     */
    @FXML
    private void showActiveCases() {
        hideAllViews();
        if (casesView != null) {
            casesView.setVisible(true);
            casesView.setManaged(true);
        }
        loadAvailableCases(); // Refresh cases when showing cases view
        updateStatusLabel("Viewing active cases");
    }

    /**
     * Shows my assignments view.
     */
    @FXML
    private void showMyAssignments() {
        hideAllViews();
        if (assignmentsView != null) {
            assignmentsView.setVisible(true);
            assignmentsView.setManaged(true);
        }
        updateStatusLabel("Viewing assignments");
    }

    /**
     * Shows help receivers view.
     */
    @FXML
    private void showHelpReceivers() {
        hideAllViews();
        if (helpReceiversView != null) {
            helpReceiversView.setVisible(true);
            helpReceiversView.setManaged(true);
        }
        updateStatusLabel("Viewing receivers");
    }

    /**
     * Shows log hours view.
     */
    @FXML
    private void showLogHours() {
        hideAllViews();
        if (logHoursView != null) {
            logHoursView.setVisible(true);
            logHoursView.setManaged(true);
        }
        updateStatusLabel("Log your hours");
    }

    /**
     * Hides all content views.
     */
    private void hideAllViews() {
        if (dashboardView != null) {
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);
        }
        if (casesView != null) {
            casesView.setVisible(false);
            casesView.setManaged(false);
        }
        if (assignmentsView != null) {
            assignmentsView.setVisible(false);
            assignmentsView.setManaged(false);
        }
        if (helpReceiversView != null) {
            helpReceiversView.setVisible(false);
            helpReceiversView.setManaged(false);
        }
        if (logHoursView != null) {
            logHoursView.setVisible(false);
            logHoursView.setManaged(false);
        }
    }

    /**
     * Handles volunteering for a case.
     */
    @FXML
    private void handleVolunteerForCase() {
        if (allCasesTable != null && allCasesTable.getSelectionModel().getSelectedItem() != null) {
            CharityCase selectedCase = allCasesTable.getSelectionModel().getSelectedItem();
            showSuccess("Successfully volunteered for: " + selectedCase.getTitle());
            updateStatusLabel("Volunteered for case");
            // TODO: Implement actual volunteer assignment logic
        } else {
            showError("Please select a case first");
        }
    }

    /**
     * Handles marking an assignment as complete.
     */
    @FXML
    private void handleMarkComplete() {
        if (myAssignmentsTable != null && myAssignmentsTable.getSelectionModel().getSelectedItem() != null) {
            showSuccess("Assignment marked as complete!");
            updateStatusLabel("Assignment completed");
            // TODO: Implement actual completion logic
        } else {
            showError("Please select an assignment first");
        }
    }

    /**
     * Handles contacting a receiver.
     */
    @FXML
    private void handleContactReceiver() {
        if (receiversTable != null && receiversTable.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Contact Receiver");
            alert.setHeaderText("Contact Information");
            alert.setContentText("Contact details will be displayed here.\n\nTODO: Implement receiver contact system");
            alert.showAndWait();
            updateStatusLabel("Contact information displayed");
        } else {
            showError("Please select a receiver first");
        }
    }

    /**
     * Handles submitting volunteer hours.
     */
    @FXML
    private void handleSubmitHours() {
        clearErrorLabel();

        if (currentVolunteer == null) {
            showError("User not logged in");
            return;
        }

        // Validate assignment selection
        String assignment = assignmentComboBox != null ? assignmentComboBox.getValue() : null;
        if (assignment == null || assignment.isEmpty()) {
            showError("Please select an assignment");
            return;
        }

        // Validate hours
        String hoursStr = hoursField != null ? hoursField.getText().trim() : "";
        if (hoursStr.isEmpty()) {
            showError("Please enter hours to log");
            return;
        }

        int hours;
        try {
            hours = Integer.parseInt(hoursStr);
            if (hours <= 0 || hours > 24) {
                showError("Please enter a valid number of hours (1-24)");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
            return;
        }

        // Validate date
        LocalDate date = datePicker != null ? datePicker.getValue() : null;
        if (date == null) {
            showError("Please select a date");
            return;
        }

        if (date.isAfter(LocalDate.now())) {
            showError("Date cannot be in the future");
            return;
        }

        // Get notes (optional)
        String notes = notesArea != null ? notesArea.getText().trim() : "";

        try {
            // Update working hours
            currentVolunteer.addWorkingHours(hours);
            userDAO.updateUser(currentVolunteer);

            // Update display
            updateDisplay();

            // Clear fields
            if (hoursField != null) {
                hoursField.clear();
            }
            if (notesArea != null) {
                notesArea.clear();
            }
            if (datePicker != null) {
                datePicker.setValue(null);
            }
            if (assignmentComboBox != null) {
                assignmentComboBox.setValue(null);
            }

            // Show success
            showSuccess("Logged " + hours + " hours for " + assignment + ". Total: " + currentVolunteer.getWorkingHours());
            updateStatusLabel("Hours logged successfully");

            // Log the activity
            LOGGER.log(Level.INFO, "Volunteer {0} logged {1} hours for {2} on {3}",
                    new Object[]{currentVolunteer.getName(), hours, assignment, date});

        } catch (SQLException | DatabaseConnectionException e) {
            showError("Could not log hours. Please try again.");
            LOGGER.log(Level.SEVERE, "Error logging hours", e);
        }
    }

    /**
     * Handles logging volunteer hours (legacy method).
     */
    @FXML
    private void handleLogHours() {
        clearErrorLabel();

        if (currentVolunteer == null) {
            showError("User not logged in");
            return;
        }

        String hoursStr = hoursField != null ? hoursField.getText().trim() : "";

        if (hoursStr.isEmpty()) {
            showError("Please enter hours to log");
            return;
        }

        int hours;
        try {
            hours = Integer.parseInt(hoursStr);
            if (hours <= 0 || hours > 24) {
                showError("Please enter a valid number of hours (1-24)");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number");
            return;
        }

        try {
            // Update working hours
            currentVolunteer.addWorkingHours(hours);
            userDAO.updateUser(currentVolunteer);

            // Update display
            updateDisplay();

            // Clear field
            if (hoursField != null) {
                hoursField.clear();
            }

            // Show success
            showSuccess("Logged " + hours + " hours. Total: " + currentVolunteer.getWorkingHours());
            updateStatusLabel("Hours logged");

        } catch (SQLException | DatabaseConnectionException e) {
            showError("Could not log hours. Please try again.");
            LOGGER.log(Level.SEVERE, "Error logging hours", e);
        }
    }

    /**
     * Handles updating volunteer role.
     */
    @FXML
    private void handleUpdateRole() {
        clearErrorLabel();

        if (currentVolunteer == null) {
            showError("User not logged in");
            return;
        }

        String newRole = roleComboBox != null ? roleComboBox.getValue() : null;

        if (newRole == null || newRole.isEmpty()) {
            showError("Please select a role");
            return;
        }

        try {
            // Update role
            currentVolunteer.setRole(newRole);
            userDAO.updateUser(currentVolunteer);

            // Update display
            updateDisplay();

            // Show success
            showSuccess("Role updated to: " + newRole);
            updateStatusLabel("Role updated");

        } catch (SQLException | DatabaseConnectionException e) {
            showError("Could not update role. Please try again.");
            LOGGER.log(Level.SEVERE, "Error updating role", e);
        }
    }

    /**
     * Handles viewing volunteer statistics.
     */
    @FXML
    private void handleViewStats() {
        if (currentVolunteer == null) {
            showError("User not logged in");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Volunteer Statistics");
        alert.setHeaderText("Your Volunteer Summary");
        alert.setContentText(
                "Name: " + currentVolunteer.getName() + "\n" +
                        "Role: " + currentVolunteer.getRole() + "\n" +
                        "Total Hours: " + currentVolunteer.getWorkingHours() + "\n" +
                        "Location: " + currentVolunteer.getLocation() + "\n\n" +
                        "Thank you for your dedication!"
        );
        alert.showAndWait();
    }

    /**
     * Handles refresh.
     */
    @FXML
    private void handleRefresh() {
        try {
            // Reload user from database
            User user = userDAO.getUserById(currentVolunteer.getId());
            if (user instanceof Volunteer) {
                currentVolunteer = (Volunteer) user;
                HelloApplication.setCurrentUser(currentVolunteer);
                updateDisplay();
            }

            // Reload cases
            loadAvailableCases();

            showSuccess("Data refreshed");
            updateStatusLabel("Data refreshed");

        } catch (SQLException | UserNotFoundException | DatabaseConnectionException e) {
            showError("Could not refresh data");
            LOGGER.log(Level.WARNING, "Error refreshing data", e);
        }
    }

    /**
     * Handles logout.
     */
    @FXML
    private void handleLogout() {
        NotificationManager.getInstance().removeObserver(this);
        HelloApplication.setCurrentUser(null);
        HelloApplication.showLoginScreen();
    }

    /**
     * Clears the error label.
     */
    private void clearErrorLabel() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    /**
     * Shows a success message.
     */
    private void showSuccess(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: green;");
        }
    }

    /**
     * Updates the status label.
     */
    private void updateStatusLabel(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
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