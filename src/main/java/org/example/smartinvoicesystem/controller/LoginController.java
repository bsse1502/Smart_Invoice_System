package org.example.smartinvoicesystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.smartinvoicesystem.SmartInvoiceApplication;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final String DB_URL = "jdbc:sqlite:mydb.db";

    @FXML
    public void handleLogin(ActionEvent event) {
        validLogin();
    }

    public void validLogin() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        String query = "SELECT * FROM users WHERE name = ? AND email = ? AND password = ?";


        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                if ("admin".equalsIgnoreCase(role)) {
                    loadDashboard("admin-view.fxml", "Admin Dashboard");
                } else if ("employee".equalsIgnoreCase(role)) {
                    loadDashboard("employee-view.fxml", "Employee Dashboard");
                } else {
                    showAlert("Invalid Role", "No dashboard found for role: " + role);
                }
            } else {
                showAlert("Login Failed", "Invalid credentials. Please try again.");
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred: " + e.getMessage());
        }
    }

    private void loadDashboard(String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SmartInvoiceApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(loader.load(), 1000, 600);
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();

        // Close login window
        Stage currentStage = (Stage) usernameField.getScene().getWindow();
        currentStage.close();
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Status");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
