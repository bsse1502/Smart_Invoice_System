package org.example.smartinvoicesystem.controller;

import database.ConnectDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.smartinvoicesystem.models.Employee;

import java.io.IOException;
import java.sql.*;

public class EmployeeController {
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> positionColumn;

    @FXML private TableView<Employee> searchResultTable;
    @FXML private TableColumn<Employee, Integer> searchIdColumn;
    @FXML private TableColumn<Employee, String> searchNameColumn;
    @FXML private TableColumn<Employee, String> searchPositionColumn;

    @FXML private TextField nameField;
    @FXML private TextField positionField;
    @FXML private TextField searchField;
    @FXML private Label resultLabel;

    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private final ObservableList<Employee> searchResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        positionColumn.setCellValueFactory(cell -> cell.getValue().positionProperty());

        searchIdColumn.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        searchNameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        searchPositionColumn.setCellValueFactory(cell -> cell.getValue().positionProperty());

        loadEmployees();
    }

    private void loadEmployees() {
        employeeList.clear();
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {
            while (rs.next()) {
                employeeList.add(new Employee(rs.getInt("id"), rs.getString("name"), rs.getString("position")));
            }
        } catch (SQLException e) {
            resultLabel.setText("Failed to load employees.");
        }
        employeeTable.setItems(employeeList);
    }

    @FXML
    private void onAddEmployee() {
        String name = nameField.getText().trim();
        String position = positionField.getText().trim();

        if (name.isEmpty() || position.isEmpty()) {
            resultLabel.setText("Name and position required.");
            return;
        }

        String sql = "INSERT INTO employees (name, position) VALUES (?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, position);
            stmt.executeUpdate();
            resultLabel.setText("Employee added.");
            loadEmployees();
            nameField.clear();
            positionField.clear();
        } catch (SQLException e) {
            resultLabel.setText("Add failed: " + e.getMessage());
        }
    }

    @FXML
    private void onRemoveEmployee() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            resultLabel.setText("Select an employee to remove.");
            return;
        }

        String sql = "DELETE FROM employees WHERE id = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            resultLabel.setText("Removed.");
            loadEmployees();
        } catch (SQLException e) {
            resultLabel.setText("Remove failed: " + e.getMessage());
        }
    }

    @FXML
    private void onSearchEmployee() {
        String keyword = searchField.getText().trim();
        searchResults.clear();
        if (keyword.isEmpty()) {
            resultLabel.setText("Search field empty.");
            return;
        }

        String sql = "SELECT * FROM employees WHERE name LIKE ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                searchResults.add(new Employee(rs.getInt("id"), rs.getString("name"), rs.getString("position")));
            }
            searchResultTable.setItems(searchResults);
            resultLabel.setText(searchResults.isEmpty() ? "No results." : "");
        } catch (SQLException e) {
            resultLabel.setText("Search error: " + e.getMessage());
        }
    }

    @FXML
    private void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartinvoicesystem/admin-view.fxml")); // change path to your previous scene
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
