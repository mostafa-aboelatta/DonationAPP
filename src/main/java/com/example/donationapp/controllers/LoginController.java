package com.example.donationapp.controllers;

import com.example.donationapp.HelloApplication;
import com.example.donationapp.dao.UserDAO;
import com.example.donationapp.exceptions.*;
import com.example.donationapp.models.*;
import com.example.donationapp.util.UtilitySupport;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Login screen.
 * Handles user authentication for all user types.
 */
public class LoginController implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    
    // Store current logged in user
    private static User currentUser;
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private Button loginButton;
    @FXML private Hyperlink signupLink;
    @FXML private Label messageLabel;
    
    private final UserDAO userDAO;
    
    public LoginController() {
        this.userDAO = new UserDAO();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize user type combo box
        if (userTypeComboBox != null) {
            userTypeComboBox.getItems().addAll("Donor", "Receiver", "Volunteer", "Admin");
            userTypeComboBox.setValue("Donor");
        }
        
        // Clear message label
        if (messageLabel != null) {
            messageLabel.setText("");
        }
        
        // Add enter key handler for login
        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }
    }
    
    /**
     * Handles the login button click.
     */
    @FXML
    private void handleLogin() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
        
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String userType = userTypeComboBox.getValue();
        
        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }
        
        if (!UtilitySupport.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }
        
        try {
            // Validate login
            User user = userDAO.validateLogin(email, password);
            
            // Check if user type matches
            String actualUserType = userDAO.getUserType(email);
            if (!actualUserType.equalsIgnoreCase(userType)) {
                showError("Invalid user type. Please select the correct user type.");
                return;
            }
            
            LOGGER.info("User logged in: " + user.getName() + " (" + userType + ")");
            
            // Store current user
            currentUser = user;
            
            // Navigate to appropriate dashboard
            navigateToDashboard(user, userType);
            
        } catch (InvalidCredentialsException e) {
            showError("Invalid email or password");
            LOGGER.warning("Login failed: " + e.getMessage());
        } catch (UserNotFoundException e) {
            showError("User not found. Please sign up first.");
            LOGGER.warning("User not found: " + e.getMessage());
        } catch (SQLException e) {
            showError("Database error. Please try again later.");
            LOGGER.log(Level.SEVERE, "Database error during login", e);
        } catch (DatabaseConnectionException e) {
            showError("Could not connect to database. Please check your connection.");
            LOGGER.log(Level.SEVERE, "Database connection error", e);
        }
    }
    
    /**
     * Handles the signup link click.
     */
    @FXML
    private void handleSignup() {
        HelloApplication.showSignupScreen();
    }
    
    /**
     * Navigates to the appropriate dashboard based on user type.
     */
    private void navigateToDashboard(User user, String userType) {
        currentUser = user;
        HelloApplication.setCurrentUser(user);  // Set user in HelloApplication too
        
        switch (userType.toUpperCase()) {
            case "DONOR":
                HelloApplication.showDonorDashboard();
                break;
            case "RECEIVER":
                HelloApplication.showReceiverDashboard();
                break;
            case "ADMIN":
                HelloApplication.showAdminDashboard();
                break;
            case "VOLUNTEER":
                HelloApplication.showVolunteerDashboard();
                break;
            default:
                showError("Unknown user type");
        }
    }
    
    /**
     * Gets the currently logged in user.
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the currently logged in user.
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    /**
     * Clears the current user session.
     */
    public static void logout() {
        currentUser = null;
    }
    
    /**
     * Displays an error message.
     */
    private void showError(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * Clears all input fields.
     */
    @FXML
    private void clearFields() {
        if (emailField != null) emailField.clear();
        if (passwordField != null) passwordField.clear();
        if (messageLabel != null) messageLabel.setText("");
    }
}
