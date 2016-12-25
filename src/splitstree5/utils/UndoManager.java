/*
 *  Copyright (C) 2016 Daniel H. Huson
 *  
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.utils;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Undoable change manager
 * Created by huson on 12/25/16.
 */
public class UndoManager {
    private ListNode parentNode = new ListNode();
    private ListNode currentNode = null;

    private BooleanProperty performingUndoOrRedo = new SimpleBooleanProperty(false);
    private BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private BooleanProperty canRedo = new SimpleBooleanProperty(false);
    private StringProperty undoName = new SimpleStringProperty();
    private StringProperty redoName = new SimpleStringProperty();


    /**
     * default constructor
     */
    public UndoManager() {
        currentNode = parentNode;
    }

    /**
     * copy constructor
     *
     * @param other
     */
    public UndoManager(UndoManager other) {
        this();
        currentNode = other.currentNode;
        updateProperties();
    }

    /**
     * forget history
     */
    public void clear() {
        currentNode = parentNode;
        parentNode.next = null;
        updateProperties();
    }

    /**
     * add a change item
     *
     * @param changeable
     */
    public void addChangeable(UndoableChange changeable) {
        final ListNode node = new ListNode(changeable);
        currentNode.next = node;
        node.prev = currentNode;
        currentNode = node;
        updateProperties();
    }

    /**
     * performs current undo
     */
    public void undo() {
        if (!performingUndoOrRedo.get()) {
            performingUndoOrRedo.set(true);
            try {
                if (!canUndo())
                    throw new IllegalStateException("Cannot undo.");
                currentNode.change.undo();
                movePrev();
                updateProperties();
            } finally {
                performingUndoOrRedo.set(false);
            }
        }
    }


    /**
     * performs current redo
     */
    public void redo() {
        if (!performingUndoOrRedo.get()) {
            performingUndoOrRedo.set(true);
            try {
                if (!canRedo())
                    throw new IllegalStateException("Cannot redo.");
                moveNext();
                currentNode.change.redo();
                updateProperties();
            } finally {
                performingUndoOrRedo.set(false);
            }
        }
    }

    /**
     * is a undo or redo currently being performed?
     *
     * @return true, if currently performing undo or redo
     */
    public boolean isPerformingUndoOrRedo() {
        return performingUndoOrRedo.get();
    }

    /**
     * can undo
     *
     * @return true, if can undo
     */
    public boolean canUndo() {
        return currentNode != parentNode;
    }

    /**
     * gets the can undo property
     *
     * @return property
     */
    public BooleanProperty canUndoProperty() {
        return canUndo;
    }

    /**
     * can redo
     *
     * @return true, if can redo
     */
    public boolean canRedo() {
        return currentNode.next != null;
    }

    /**
     * gets the can redo property
     *
     * @return property
     */
    public BooleanProperty canRedoProperty() {
        return canRedo;
    }

    /**
     * get the current undo name or ""
     *
     * @return undo name
     */
    public String getUndoName() {
        return undoName.get();
    }

    /**
     * get the undo name property
     *
     * @return name property
     */
    public StringProperty undoNameProperty() {
        return undoName;
    }

    /**
     * get the redo name or ""
     *
     * @return redo name
     */
    public String getRedoName() {
        return redoName.get();
    }

    /**
     * get the redo name property
     *
     * @return name property
     */
    public StringProperty redoNameProperty() {
        return redoName;
    }

    /**
     * update the state of properties
     */
    private void updateProperties() {
        canUndo.set(canUndo());
        canRedo.set(canRedo());
        undoName.set(canUndo() ? currentNode.change.getName() : "");
        redoName.set(canRedo() ? currentNode.next.change.getName() : "");

    }

    /**
     * move to previous item
     */
    private void movePrev() {
        if (currentNode.prev == null)
            throw new IllegalStateException("Cannot move prev.");
        currentNode = currentNode.prev;
    }


    /**
     * move to next
     */
    private void moveNext() {
        if (currentNode.next == null)
            throw new IllegalStateException("Cannot move next.");
        currentNode = currentNode.next;
    }

    /**
     * linked list node
     */
    private class ListNode {
        private ListNode prev = null;
        private ListNode next = null;
        private final UndoableChange change;

        public ListNode(UndoableChange c) {
            change = c;
        }

        public ListNode() {
            change = null;
        }
    }
}