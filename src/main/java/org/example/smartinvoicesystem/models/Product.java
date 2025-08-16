package org.example.smartinvoicesystem.models;

import javafx.beans.property.*;

public class Product {
    private StringProperty name;
    private DoubleProperty price;
    private IntegerProperty quantity;
    private StringProperty category;

    // Constructor
    public Product(String name, double price, int quantity, String category) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.category = new SimpleStringProperty(category);
    }

    // Getter and Setter methods

    public String getName() {
        return name.get();
    }


    public double getPrice() {
        return price.get();
    }


    public int getQuantity() {
        return quantity.get();
    }


    public String getCategory() {
        return category.get();
    }

}
