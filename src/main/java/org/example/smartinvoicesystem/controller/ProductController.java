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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.smartinvoicesystem.models.Product;

import java.io.IOException;
import java.sql.*;

public class ProductController {
    @FXML
    private TableColumn<Product, String> nameColumn;
    @FXML
    private TableColumn<Product, Double> priceColumn;
    @FXML
    private TableColumn<Product, Integer> quantityColumn;
    @FXML
    private TableColumn<Product, String> categoryColumn;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TextField productNameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField categoryField;

    // Newly added for search and buttons
    @FXML
    private TextField searchNameField;
    @FXML
    private TextField searchCategoryField;

    @FXML
    private Button searchButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button addButton;

    // Called automatically when FXML loads
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        loadProductsFromDatabase();

        // Initial button states
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);

        // Listen for table row selection to fill form
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                productNameField.setText(newSelection.getName());
                priceField.setText(String.valueOf(newSelection.getPrice()));
                quantityField.setText(String.valueOf(newSelection.getQuantity()));
                categoryField.setText(newSelection.getCategory());

                // Enable update & delete when selecting a row
                updateButton.setDisable(false);
                deleteButton.setDisable(false);

                // Disable add button (because product already exists)
                addButton.setDisable(true);
            } else {
                // No selection, disable update/delete and enable add
                updateButton.setDisable(true);
                deleteButton.setDisable(true);
                addButton.setDisable(false);
            }
        });
    }

    private void loadProductsFromDatabase() {
        ObservableList<Product> productList = FXCollections.observableArrayList();

        String query = "SELECT * FROM products";

        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String category = rs.getString("category");

                Product product = new Product(name, price, quantity, category);
                productList.add(product);
            }

            productTable.setItems(productList);

        } catch (SQLException e) {
            System.out.println("Error loading products: " + e.getMessage());
        }
    }

    @FXML
    public void onAddButton() {
        String productName = productNameField.getText().trim();
        String productPrice = priceField.getText().trim();
        String productQuantity = quantityField.getText().trim();
        String category = categoryField.getText().trim();

        if (productName.isEmpty() || productPrice.isEmpty() || productQuantity.isEmpty() || category.isEmpty()) {
            System.out.println("Please fill in all fields.");
            return;
        }

        try {
            double price = Double.parseDouble(productPrice);
            int quantity = Integer.parseInt(productQuantity);

            try (Connection conn = ConnectDB.getConnection()) {
                // Step 1: Check if product with same name already exists
                String checkSql = "SELECT COUNT(*) FROM products WHERE name = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, productName);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Product with this name already exists.");
                        return;
                    }
                }

                // Step 2: Insert if not exists
                String insertSql = "INSERT INTO products (name, price, quantity, category) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, productName);
                    insertStmt.setDouble(2, price);
                    insertStmt.setInt(3, quantity);
                    insertStmt.setString(4, category);
                    insertStmt.executeUpdate();

                    System.out.println("New product added successfully.");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            loadProductsFromDatabase();
            clearFormFields();
        }
    }

    // Search button action
    @FXML
    public void onSearchButton() {
        String name = searchNameField.getText().trim();
        String category = searchCategoryField.getText().trim();

        ObservableList<Product> filteredList = FXCollections.observableArrayList();

        String sql = "SELECT * FROM products WHERE name LIKE ? AND category LIKE ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + name + "%");
            pstmt.setString(2, "%" + category + "%");

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String pname = rs.getString("name");
                double price = rs.getDouble("price");
                int quantity = rs.getInt("quantity");
                String pcategory = rs.getString("category");

                filteredList.add(new Product(pname, price, quantity, pcategory));
            }

            productTable.setItems(filteredList);

        } catch (SQLException e) {
            System.out.println("Search error: " + e.getMessage());
        }
    }

    // Reset search fields and reload table
    @FXML
    public void onResetButton() {
        searchNameField.clear();
        searchCategoryField.clear();
        loadProductsFromDatabase();
    }

    // Clear form fields
    @FXML
    public void onClearButton() {
        clearFormFields();
    }

    @FXML
    public void onUpdateButton() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            System.out.println("Please select a product to update");
            return;
        }

        String productName = productNameField.getText();
        String productPrice = priceField.getText();
        String productQuantity = quantityField.getText();
        String category = categoryField.getText();

        if (productName.isEmpty() || productPrice.isEmpty() || productQuantity.isEmpty() || category.isEmpty()) {
            System.out.println("Please fill in all fields");
            return;
        }

        try {
            double price = Double.parseDouble(productPrice);
            int quantity = Integer.parseInt(productQuantity);

            String sql = "UPDATE products SET name = ?, price = ?, quantity = ?, category = ? WHERE name = ?";

            try (Connection conn = ConnectDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, productName);
                pstmt.setDouble(2, price);
                pstmt.setInt(3, quantity);
                pstmt.setString(4, category);
                pstmt.setString(5, selectedProduct.getName());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Product updated successfully");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            loadProductsFromDatabase();
            clearFormFields();
        }
    }

    @FXML
    public void onDeleteButton() {
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            System.out.println("Please select a product to delete");
            return;
        }

        try {
            String sql = "DELETE FROM products WHERE name = ?";

            try (Connection conn = ConnectDB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, selectedProduct.getName());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Product deleted successfully");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            loadProductsFromDatabase();
            clearFormFields();
        }
    }

    @FXML
    private void onBackButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartinvoicesystem/admin-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearFormFields() {
        productNameField.clear();
        priceField.clear();
        quantityField.clear();
        categoryField.clear();

        // Reset button states
        addButton.setDisable(false);
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }
}
