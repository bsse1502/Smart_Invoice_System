package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class InsertDummyData {

    public static void insertData() {
        try (Connection conn = ConnectDB.getConnection()) {

            // Insert Users
            // Insert Users (2 Admins, 3 Employees)
            String userSQL = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(userSQL)) {

                for (int i = 1; i <= 5; i++) {
                    String name, email, password, role;

                    if (i <= 2) {
                        name = "Admin " + i;
                        email = "admin" + i + "@example.com";
                        password = "admin" + (100 + i);
                        role = "admin";
                    } else {
                        name = "Employee " + (i - 2);
                        email = "employee" + (i - 2) + "@example.com";
                        password = "emp" + (100 + i);
                        role = "employee";
                    }

                    pstmt.setString(1, name);
                    pstmt.setString(2, email);
                    pstmt.setString(3, password);
                    pstmt.setString(4, role);
                    pstmt.executeUpdate();
                }
            }


            // Insert Clients
            String clientSQL = "INSERT INTO clients (name, email, phone, address) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(clientSQL)) {
                for (int i = 1; i <= 5; i++) {
                    pstmt.setString(1, "Client " + i);
                    pstmt.setString(2, "client" + i + "@mail.com");
                    pstmt.setString(3, "01710000" + i + i);
                    pstmt.setString(4, "Client Address " + i);
                    pstmt.executeUpdate();
                }
            }

            // Insert Products
            String productSQL = "INSERT INTO products (name, price, tax, discount, category) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(productSQL)) {
                for (int i = 1; i <= 5; i++) {
                    pstmt.setString(1, "Product " + i);
                    pstmt.setDouble(2, 100.0 + i * 10); // base price
                    pstmt.setDouble(3, 5.0);             // 5% tax
                    pstmt.setDouble(4, 2.0);             // 2% discount
                    pstmt.setString(5, (i % 2 == 0) ? "Electronics" : "Groceries"); // example category
                    pstmt.executeUpdate();
                }
            }


            // Insert Invoices
            String invoiceSQL = "INSERT INTO invoices (client_id, user_id, invoice_date, total, payment_status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(invoiceSQL)) {
                for (int i = 1; i <= 5; i++) {
                    pstmt.setInt(1, i); // client_id (1 to 5)
                    pstmt.setInt(2, 1); // admin user id = 1
                    pstmt.setString(3, "2025-07-" + (10 + i)); // invoice_date
                    pstmt.setDouble(4, 500.0 + i * 50); // total
                    pstmt.setString(5, (i % 2 == 0) ? "Paid" : "Unpaid");
                    pstmt.executeUpdate();
                }
            }

            // Insert Invoice Items
            String itemSQL = "INSERT INTO invoice_items (invoice_id, product_id, quantity, per_unit_price) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemSQL)) {
                Random rand = new Random();
                for (int i = 1; i <= 10; i++) {
                    int invoiceId = (i % 5) + 1;  // invoice_id 1-5
                    int productId = (i % 5) + 1;  // product_id 1-5
                    int qty = 1 + rand.nextInt(5); // 1 to 5
                    double unitPrice = 100 + productId * 10;
                    pstmt.setInt(1, invoiceId);
                    pstmt.setInt(2, productId);
                    pstmt.setInt(3, qty);
                    pstmt.setDouble(4, unitPrice);
                    pstmt.executeUpdate();
                }
            }

            System.out.println(" Dummy data inserted successfully.");

        } catch (SQLException e) {
            System.out.println("Error inserting data: " + e.getMessage());
        }
    }
}
