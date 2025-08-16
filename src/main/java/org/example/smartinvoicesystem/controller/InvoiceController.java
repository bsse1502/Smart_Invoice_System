package org.example.smartinvoicesystem.controller;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import database.ConnectDB;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class InvoiceController {

    @FXML private ComboBox<String> clientIdField;
    @FXML private ComboBox<String> userIdField;
    @FXML private TextField dateField;
    @FXML private ComboBox<String> paymentStatusBox;
    @FXML private TextField totalField;

    @FXML private ComboBox<String> productIdField;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, Integer> productIdColumn;
    @FXML private TableColumn<Item, Integer> quantityColumn;
    @FXML private TableColumn<Item, Double> priceColumn;

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private Label statusLabel;

    private final ObservableList<Item> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        productIdColumn.setCellValueFactory(data -> data.getValue().productIdProperty().asObject());
        quantityColumn.setCellValueFactory(data -> data.getValue().quantityProperty().asObject());
        priceColumn.setCellValueFactory(data -> data.getValue().unitPriceProperty().asObject());

        itemTable.setItems(items);

        paymentStatusBox.setItems(FXCollections.observableArrayList("Paid", "Unpaid", "Partial"));
        paymentStatusBox.setValue("Unpaid");

        dateField.setText(LocalDate.now().toString());

        loadComboBoxData("clients", clientIdField);
        loadComboBoxData("users", userIdField);
        loadComboBoxData("products", productIdField);
    }

    private void loadComboBoxData(String table, ComboBox<String> comboBox) {
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM " + table);
             ResultSet rs = stmt.executeQuery()) {

            ObservableList<String> ids = FXCollections.observableArrayList();
            while (rs.next()) {
                ids.add(String.valueOf(rs.getInt("id")));
            }
            comboBox.setItems(ids);
        } catch (SQLException e) {
            statusLabel.setText("Error loading " + table);
            e.printStackTrace();
        }
    }

    @FXML
    private void onClientSelected() {
        String selectedClientId = clientIdField.getValue();
        if (selectedClientId == null) return;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, phone FROM clients WHERE id = ?")) {
            stmt.setInt(1, Integer.parseInt(selectedClientId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                phoneField.setText(rs.getString("phone"));
                nameField.setEditable(false);
                phoneField.setEditable(false);
            } else {
                nameField.clear();
                phoneField.clear();
                nameField.setEditable(true);
                phoneField.setEditable(true);
            }
        } catch (SQLException e) {
            statusLabel.setText("Error loading client data.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCustomer() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("Enter both name and phone.");
            return;
        }

        try (Connection conn = ConnectDB.getConnection()) {
            // Check if customer already exists
            String checkSql = "SELECT id FROM clients WHERE name = ? AND phone = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, name);
                checkStmt.setString(2, phone);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    statusLabel.setText("Customer already exists with ID: " + rs.getInt("id"));
                    clientIdField.setValue(String.valueOf(rs.getInt("id")));
                    onClientSelected(); // auto-fill fields
                    return;
                }
            }

            // Insert new customer
            String insertSql = "INSERT INTO clients (name, phone) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, name);
                insertStmt.setString(2, phone);
                int affected = insertStmt.executeUpdate();

                if (affected > 0) {
                    ResultSet keys = insertStmt.getGeneratedKeys();
                    if (keys.next()) {
                        String newId = String.valueOf(keys.getInt(1));
                        clientIdField.getItems().add(newId);
                        clientIdField.setValue(newId);
                        statusLabel.setText("Customer added!");
                        onClientSelected(); // auto-fill fields
                    }
                }
            }

        } catch (SQLException e) {
            statusLabel.setText("Error adding customer.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetCustomer() {
        nameField.clear();
        phoneField.clear();
    }

    @FXML
    private void onProductSelected() {
        String productId = productIdField.getValue();
        if (productId == null) return;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT price FROM products WHERE id = ?")) {

            stmt.setInt(1, Integer.parseInt(productId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                unitPriceField.setText(String.format("%.2f", rs.getDouble("price")));
            }

        } catch (SQLException e) {
            statusLabel.setText("Error fetching product price.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onCreateInvoice() {
        String sqlInvoice = "INSERT INTO invoices (client_id, user_id, invoice_date, total, payment_status) VALUES (?, ?, ?, ?, ?)";
        String sqlItem = "INSERT INTO invoice_items (invoice_id, product_id, quantity, per_unit_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement invoiceStmt = conn.prepareStatement(sqlInvoice, Statement.RETURN_GENERATED_KEYS)) {

            invoiceStmt.setInt(1, Integer.parseInt(clientIdField.getValue()));
            invoiceStmt.setInt(2, Integer.parseInt(userIdField.getValue()));
            invoiceStmt.setString(3, dateField.getText());
            invoiceStmt.setDouble(4, Double.parseDouble(totalField.getText()));
            invoiceStmt.setString(5, paymentStatusBox.getValue());

            int rows = invoiceStmt.executeUpdate();
            if (rows == 0) throw new SQLException("Invoice creation failed.");

            ResultSet keys = invoiceStmt.getGeneratedKeys();
            if (keys.next()) {
                int invoiceId = keys.getInt(1);

                try (PreparedStatement itemStmt = conn.prepareStatement(sqlItem)) {
                    for (Item item : items) {
                        itemStmt.setInt(1, invoiceId);
                        itemStmt.setInt(2, item.getProductId());
                        itemStmt.setInt(3, item.getQuantity());
                        itemStmt.setDouble(4, item.getUnitPrice());
                        itemStmt.addBatch();
                    }
                    itemStmt.executeBatch();
                }

                statusLabel.setText("Invoice created!");
                items.clear();
                itemTable.refresh();
                totalField.clear();
            }

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClick(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/smartinvoicesystem/admin-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Item {
        private final IntegerProperty productId;
        private final IntegerProperty quantity;
        private final DoubleProperty unitPrice;

        public Item(int productId, int quantity, double unitPrice) {
            this.productId = new SimpleIntegerProperty(productId);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
        }

        public int getProductId() { return productId.get(); }
        public int getQuantity() { return quantity.get(); }
        public double getUnitPrice() { return unitPrice.get(); }

        public IntegerProperty productIdProperty() { return productId; }
        public IntegerProperty quantityProperty() { return quantity; }
        public DoubleProperty unitPriceProperty() { return unitPrice; }
    }


    @FXML
    private void handleAddItem() {
        String productIdStr = productIdField.getValue();
        String quantityStr = quantityField.getText();
        String priceStr = unitPriceField.getText();

        if (productIdStr == null || quantityStr.isEmpty() || priceStr.isEmpty()) {
            statusLabel.setText("Select product and enter quantity.");
            return;
        }

        try {
            int productId = Integer.parseInt(productIdStr);
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);

            // Add item to list
            items.add(new Item(productId, quantity, price));

            // Clear input for next product
            productIdField.setValue(null);
            quantityField.clear();
            unitPriceField.clear();

            statusLabel.setText("Product added to list.");
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid quantity or price.");
        }
    }

    @FXML
    private void handleCalculateTotal() {
        double total = 0;
        for (Item item : items) {
            total += item.getQuantity() * item.getUnitPrice();
        }
        totalField.setText(String.format("%.2f", total));
        statusLabel.setText("Total calculated.");
    }



}
