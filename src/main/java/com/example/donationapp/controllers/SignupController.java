package com.example.donationapp.controllers;

import com.example.donationapp.HelloApplication;
import com.example.donationapp.dao.UserDAO;
import com.example.donationapp.exceptions.*;
import com.example.donationapp.models.*;
import com.example.donationapp.util.NotificationManager;
import com.example.donationapp.util.UtilitySupport;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Signup screen.
 * Handles user registration for all user types.
 */
public class SignupController implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(SignupController.class.getName());
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private Button signupButton;
    @FXML private Hyperlink loginLink;
    @FXML private Label messageLabel;
    
    private final UserDAO userDAO;
    
    public SignupController() {
        this.userDAO = new UserDAO();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize user type combo box if not already initialized in FXML
        if (userTypeComboBox != null && userTypeComboBox.getItems().isEmpty()) {
            userTypeComboBox.getItems().addAll("Donor", "Receiver", "Volunteer", "Admin");
            userTypeComboBox.setValue("Donor");
        }
        
        // Clear message label
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }
    
    /**
     * Handles the signup button click.
     */
    @FXML
    private void handleSignup() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
        
        // Get input values
        String name = usernameField != null ? usernameField.getText().trim() : "";
        String email = emailField != null ? emailField.getText().trim() : "";
        String phone = phoneField != null ? phoneField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText() : "";
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";
        String userType = userTypeComboBox != null ? userTypeComboBox.getValue() : "Donor";
        
        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showError("All fields are required");
            return;
        }
        
        if (!UtilitySupport.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }
        
        if (!UtilitySupport.isValidPhone(phone)) {
            showError("Please enter a valid phone number");
            return;
        }
        
        if (!UtilitySupport.isValidPassword(password)) {
            showError("Password must be at least 8 characters with at least one letter and one number");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }
        
        try {
            // Create appropriate user object
            User user = createUser(name, email, phone, userType);
            
            // Insert user into database
            int userId = userDAO.insertUser(user, password, userType.toUpperCase());
            
            LOGGER.info("User registered: " + name + " (" + userType + ") with ID: " + userId);
            
            // Notify about new registration
            NotificationManager.getInstance().notifyNewUserRegistration(name, userType);
            
            // Show success message
            showSuccess("Registration successful! Please login.");
            
            // Navigate back to login after a short delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> HelloApplication.showLoginScreen());
                } catch (Exception e) {
                    // Ignore
                }
            }).start();
            
        } catch (DuplicateUserException e) {
            showError("An account with this email already exists");
            LOGGER.warning("Duplicate user: " + e.getMessage());
        } catch (SQLException e) {
            showError("Database error. Please try again later.");
            LOGGER.log(Level.SEVERE, "Database error during registration", e);
        } catch (DatabaseConnectionException e) {
            showError("Could not connect to database. Please check your connection.");
            LOGGER.log(Level.SEVERE, "Database connection error", e);
        }
    }
    
    /**
     * Creates the appropriate user object based on type.
     */
    private User createUser(String name, String email, String phone, String userType) {
        switch (userType) {
            case "Donor":
                return new Donor(name, email, phone);
            case "Receiver":
                return new Receiver(name, email, phone);
            case "Admin":
                return new Admin(name, email, phone, "Default Location");
            case "Volunteer":
                return new Volunteer(name, email, phone, "Default Location");
            default:
                return new Donor(name, email, phone);
        }
    }
    
    /**
     * Handles the back to login link click.
     */
    @FXML
    private void handleBackToLogin() {
        HelloApplication.showLoginScreen();
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
     * Displays a success message.
     */
    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: green;");
        }
    }
    
    /**
     * Clears all input fields.
     */
    @FXML
    private void clearFields() {
        if (usernameField != null) usernameField.clear();
        if (emailField != null) emailField.clear();
        if (phoneField != null) phoneField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (messageLabel != null) messageLabel.setText("");
    }
}
