package org.example.smartinvoicesystem.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.smartinvoicesystem.SmartInvoiceApplication;
import database.ConnectDB;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private Button employeeButton;
    @FXML private Button customerButton;
    @FXML private Button invoiceButton;
    @FXML private Button productButton;

    // Tables for top products and buyers
    @FXML private TableView<TopProduct> topProductsTable;
    @FXML private TableColumn<TopProduct, String> productNameColumn;
    @FXML private TableColumn<TopProduct, Integer> productStockColumn;
    @FXML private TableColumn<TopProduct, Double> productValueColumn;

    @FXML private TableView<TopBuyer> topBuyersTable;
    @FXML private TableColumn<TopBuyer, String> buyerNameColumn;
    @FXML private TableColumn<TopBuyer, String> buyerEmailColumn;
    @FXML private TableColumn<TopBuyer, String> buyerProductsColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize product table columns
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        // Initialize buyer table columns
        buyerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        buyerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        buyerProductsColumn.setCellValueFactory(new PropertyValueFactory<>("products"));

        loadTopSellingProducts();
        loadTopBuyers();
    }

    private void loadTopSellingProducts() {
        ObservableList<TopProduct> productList = FXCollections.observableArrayList();

        String sql = """
            SELECT p.name, p.quantity as stock,
                SUM(ii.quantity * ii.per_unit_price) as value
            FROM products p
            JOIN invoice_items ii ON p.id = ii.product_id
            GROUP BY p.id
            ORDER BY value DESC
            LIMIT 10;
        """;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productList.add(new TopProduct(
                        rs.getString("name"),
                        rs.getInt("stock"),
                        rs.getDouble("value")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        topProductsTable.setItems(productList);
    }

    private void loadTopBuyers() {
        ObservableList<TopBuyer> buyerList = FXCollections.observableArrayList();

        String sql = """
            SELECT c.name, c.email,
                GROUP_CONCAT(DISTINCT p.name) as products
            FROM clients c
            JOIN invoices i ON c.id = i.client_id
            JOIN invoice_items ii ON i.id = ii.invoice_id
            JOIN products p ON ii.product_id = p.id
            GROUP BY c.id
            ORDER BY COUNT(ii.product_id) DESC
            LIMIT 5;
        """;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                buyerList.add(new TopBuyer(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("products")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        topBuyersTable.setItems(buyerList);
    }

    // Navigation methods

    public void handleOnActionButton(ActionEvent event) {
        loadScene(event, "product-view.fxml", "Smart Invoice - Products");
    }

    public void handleEmployee(ActionEvent event) {
        loadScene(event, "employeeList.fxml", "Smart Invoice - Employees");
    }

    public void handleInvoice(ActionEvent event) {
        loadScene(event, "invoice-list.fxml", "Smart Invoice - Invoices");
    }

    public void handleCustomer(ActionEvent event) {
        loadScene(event, "customer-list.fxml", "Smart Invoice - Customers");
    }

    public void onLogoutButton(ActionEvent event) {
        loadScene(event, "login-view.fxml", "Smart Invoice - Login");
    }

    private void loadScene(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SmartInvoiceApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), 1000, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper classes for table data

    public static class TopProduct {
        private final String name;
        private final int stock;
        private final double value;

        public TopProduct(String name, int stock, double value) {
            this.name = name;
            this.stock = stock;
            this.value = value;
        }

        public String getName() { return name; }
        public int getStock() { return stock; }
        public double getValue() { return value; }
    }

    public static class TopBuyer {
        private final String name;
        private final String email;
        private final String products;

        public TopBuyer(String name, String email, String products) {
            this.name = name;
            this.email = email;
            this.products = products;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getProducts() { return products; }
    }
}
