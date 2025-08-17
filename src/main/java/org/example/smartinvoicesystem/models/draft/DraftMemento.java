package org.example.smartinvoicesystem.models.draft;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DraftMemento {
    private final ObservableList<DraftItem> itemsSnapshot;

    public DraftMemento(ObservableList<DraftItem> items) {
        // Deep copy
        this.itemsSnapshot = FXCollections.observableArrayList();
        for (DraftItem item : items) {
            this.itemsSnapshot.add(new DraftItem(item.getProductId(), item.getQuantity(), item.getUnitPrice()));
        }
    }

    public ObservableList<DraftItem> getItemsSnapshot() {
        return itemsSnapshot;
    }

    public static class DraftItem {
        private final int productId;
        private final int quantity;
        private final double unitPrice;

        public DraftItem(int productId, int quantity, double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public int getProductId() { return productId; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
    }
}
