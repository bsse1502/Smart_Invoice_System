package org.example.smartinvoicesystem.models.draft;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.smartinvoicesystem.models.draft.DraftMemento;

public class DraftSale {
    private ObservableList<DraftMemento.DraftItem> currentItems = FXCollections.observableArrayList();

    public void setItems(ObservableList<DraftMemento.DraftItem> items) {
        currentItems = FXCollections.observableArrayList(items);
    }

    public ObservableList<DraftMemento.DraftItem> getItems() {
        return currentItems;
    }

    public DraftMemento saveToMemento() {
        return new DraftMemento(currentItems);
    }

    public void restoreFromMemento(DraftMemento memento) {
        currentItems = FXCollections.observableArrayList(memento.getItemsSnapshot());
    }
}
