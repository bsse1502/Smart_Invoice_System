package org.example.smartinvoicesystem.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HomeController {
    @FXML
    private Button initButton;
    
    @FXML
    private Text statusText;
    
    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    public void initialize() {
        // Initialize components
        statusText.setText("");
        progressIndicator.setVisible(false);
        
        // Add hover effect to button
        initButton.setOnMouseEntered(e -> initButton.setStyle(
            "-fx-background-color: #2980b9; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8;"
        ));
        
        initButton.setOnMouseExited(e -> initButton.setStyle(
            "-fx-background-color: #3498DB; -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 8;"
        ));
    }

    @FXML
    private void goToLoginPage() {
        try {
            // Show loading indicator
            progressIndicator.setVisible(true);
            statusText.setText("Loading login page...");
            statusText.setStyle("-fx-fill: #2ecc71;");

            // Load the login page FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/smartinvoicesystem/login-view.fxml")
            );
            Scene loginScene = new Scene(loader.load());

            // Get current stage
            Stage currentStage = (Stage) initButton.getScene().getWindow();
            
            // Add fade transition
            loginScene.setFill(null);
            currentStage.setScene(loginScene);

            // Update status
            statusText.setText("Successfully navigated to login page");
            
        } catch (Exception e) {
            // Handle any errors
            statusText.setText("Error: " + e.getMessage());
            statusText.setStyle("-fx-fill: #e74c3c;"); // Red color for error
            e.printStackTrace();
            
        } finally {
            // Hide loading indicator
            progressIndicator.setVisible(false);
        }
    }
}