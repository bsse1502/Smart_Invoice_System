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
import org.example.smartinvoicesystem.models.Customer;

import java.io.IOException;
import java.sql.*;

public class CustomerController {

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> idColumn;
    @FXML
    private TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, Double> costColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField costField;

    @FXML
    private TextField searchField;
    @FXML
    private Label resultLabel;

    @FXML
    private TableView<Customer> searchResultTable;
    @FXML
    private TableColumn<Customer, Integer> searchIdColumn;
    @FXML
    private TableColumn<Customer, String> searchNameColumn;
    @FXML
    private TableColumn<Customer, Double> searchCostColumn;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private ObservableList<Customer> searchResults = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        nameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        costColumn.setCellValueFactory(cell -> cell.getValue().costProperty().asObject());

        searchIdColumn.setCellValueFactory(cell -> cell.getValue().idProperty().asObject());
        searchNameColumn.setCellValueFactory(cell -> cell.getValue().nameProperty());
        searchCostColumn.setCellValueFactory(cell -> cell.getValue().costProperty().asObject());

        customerTable.setItems(customerList);
        searchResultTable.setItems(searchResults);

        loadCustomers();
    }

    private void loadCustomers() {
        customerList.clear();
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, phone FROM clients")) {

            while (rs.next()) {
                customerList.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        parseDoubleSafe(rs.getString("phone"))  // Using phone as cost (mock value)
                ));
            }
        } catch (SQLException e) {
            resultLabel.setText("Failed to load customers.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAddCustomer() {
        String name = nameField.getText();
        String phone = costField.getText();  // costField used as phone input now

        if (name.isEmpty() || phone.isEmpty()) {
            resultLabel.setText("Name and Phone are required.");
            return;
        }

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO clients (name, phone) VALUES (?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.executeUpdate();

            resultLabel.setText("Customer added successfully.");
            nameField.clear();
            costField.clear();
            loadCustomers();

        } catch (SQLException e) {
            resultLabel.setText("Error adding customer.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().trim();
        searchResults.clear();

        if (searchText.isEmpty()) {
            resultLabel.setText("Enter a name to search.");
            return;
        }

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, phone FROM clients WHERE name LIKE ?")) {

            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                searchResults.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        parseDoubleSafe(rs.getString("phone")) // using phone as a mock numeric value
                ));
            }

            resultLabel.setText(searchResults.isEmpty()
                    ? "No customer found."
                    : searchResults.size() + " customer(s) found.");

        } catch (SQLException e) {
            resultLabel.setText("Search error.");
            e.printStackTrace();
        }
    }

    // Helper: If phone isn't numeric, set 0
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void handleBack(ActionEvent event) {
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
