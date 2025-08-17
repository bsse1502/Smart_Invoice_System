package org.example.smartinvoicesystem.models.draft;

import java.util.Stack;

public class DraftManager {
    private final Stack<DraftMemento> history = new Stack<>();

    public void save(DraftMemento memento) {
        history.push(memento);
    }

    public DraftMemento undo() {
        if (!history.isEmpty()) {
            return history.pop();
        }
        return null;
    }
}
