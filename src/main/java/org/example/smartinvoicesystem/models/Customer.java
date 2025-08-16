package org.example.smartinvoicesystem.models;

import javafx.beans.property.*;

public class Customer {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty cost;

    public Customer(int id, String name, double cost) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.cost = new SimpleDoubleProperty(cost);
    }

    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getCost() { return cost.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty costProperty() { return cost; }
}
