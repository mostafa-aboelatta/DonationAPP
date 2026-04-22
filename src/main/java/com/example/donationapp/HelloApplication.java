package com.example.donationapp;

import com.example.donationapp.database.DatabaseManager;
import com.example.donationapp.models.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application
{
    
    private static Stage primaryStage;
    private static User currentUser;
    
    @Override
    public void start(Stage stage) throws IOException
    {
        primaryStage = stage;
        
        // Initialize database connection
        try {
            DatabaseManager.getInstance().getConnection();
            System.out.println("Database connection established successfully.");
        } catch (Exception e) {
            System.err.println("Warning: Could not connect to database. " + e.getMessage());
            System.err.println("Application will continue but database features may not work.");
        }
        
        // Load the Login screen
        showLoginScreen();
    }
    

    public static void showLoginScreen()
    {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 450, 550);
            primaryStage.setTitle("Donation Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showError("Could not load login screen: " + e.getMessage());
        }
    }
    

    public static void showSignupScreen()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Signup.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 450, 600);
            primaryStage.setTitle("Donation Management System - Sign Up");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showError("Could not load signup screen: " + e.getMessage());
        }
    }
    

    public static void showDonorDashboard()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("Donor.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setTitle("Donation Management System - Donor Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showError("Could not load donor dashboard: " + e.getMessage());
        }
    }
    

    public static void showReceiverDashboard()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("reciever.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setTitle("Donation Management System - Receiver Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showError("Could not load receiver dashboard: " + e.getMessage());
        }
    }
    

    public static void showAdminDashboard()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 700);
            primaryStage.setTitle("Donation Management System - Admin Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showError("Could not load admin dashboard: " + e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            showError("Error loading admin dashboard: " + e.getMessage());
        }
    }
    

    public static void showVolunteerDashboard()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("volunteer.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setTitle("Donation Management System - Volunteer Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            showError("Could not load volunteer dashboard: " + e.getMessage());
        }
    }

    public static Stage getPrimaryStage() { return primaryStage; }
    

    public static User getCurrentUser() { return currentUser; }
    

    public static void setCurrentUser(User user) { currentUser = user; }
    //show an error message provided as a param
    private static void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void stop()
    {
        // Close database connection when application stops
        try
        {
            DatabaseManager.getInstance().closeConnection();
            System.out.println("Database connection closed.");
        }
        catch (Exception e)
        {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

