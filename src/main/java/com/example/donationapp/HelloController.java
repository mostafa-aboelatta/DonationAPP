package com.example.donationapp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;

public class HelloController
{
    @FXML
    private Button Login;

    @FXML
    private Button Signup;

    @FXML
    private Button Back_to_Login;

    // Method to handle Signup button click (from login view)
    @FXML
    public void Signup_onbutton_click(ActionEvent event) throws IOException
    {
        // Get the current stage from the event source
        Stage currentStage = (Stage) ((Button)event.getSource()).getScene().getWindow();

        // Load the signup FXML
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Signup.fxml"));
        Scene signupScene = new Scene(fxmlLoader.load(), 400, 400);

        // Set the new scene on the same stage
        currentStage.setTitle("SIGNUP");
        currentStage.setScene(signupScene);
    }

    // Method to handle Back to Login button click (from signup view to go back)
    @FXML
    public void getBack_to_Login(ActionEvent event) throws IOException
    {
        // Get the current stage from the event source
        Stage currentStage = (Stage) ((Button)event.getSource()).getScene().getWindow();

        // Load the login FXML - FIXED: changed from "hello-view.fxml" to "Login.fxml"
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Login.fxml"));
        Scene loginScene = new Scene(fxmlLoader.load(), 350, 400);

        // Set the new scene on the same stage
        currentStage.setTitle("LOGIN");
        currentStage.setScene(loginScene);
    }
}