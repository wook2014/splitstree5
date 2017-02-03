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

package splitstree5.undo;


import javafx.beans.property.*;

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
    private StringProperty undoName = new SimpleStringProperty("Undo");
    private StringProperty redoName = new SimpleStringProperty("Redo");

    private boolean recordChanges = true;

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
     * add a change item, if not currently in an undo or redo execution (in which case we don't want to record a change...)
     * If item equals() current undoable item, then not added
     *
     * @param change
     */
    public void addUndoableChange(UndoableChange change) {
        if (isRecordChanges() && !isPerformingUndoOrRedo() && (currentNode == parentNode || !currentNode.change.equals(change))) {
            final ListNode node = new ListNode(change);
            currentNode.next = node;
            node.prev = currentNode;
            currentNode = node;
            updateProperties();
        }
    }

    /**
     * add an undoable property change
     *
     * @param name     is used in undo/redo menu
     * @param property
     * @param oldValue
     * @param newValue
     * @param <T>
     */
    public <T> void addUndoableChange(String name, Property<T> property, T oldValue, T newValue) {
        if (isRecordChanges() && !isPerformingUndoOrRedo())
            addUndoableChange(new UndoableChangeProperty<>(name, property, oldValue, newValue));
    }

    /**
     * add a pair of undoable property changes
     *
     * @param name
     * @param property1
     * @param oldValue1
     * @param newValue1
     * @param property2
     * @param oldValue2
     * @param newValue2
     * @param <S>
     * @param <T>
     */
    public <S, T> void addUndoableChangePair(String name, Property<S> property1, S oldValue1, S newValue1, Property<T> property2, T oldValue2, T newValue2) {
        if (isRecordChanges() && !isPerformingUndoOrRedo())
            addUndoableChange(new UndoableChangePropertyPair<>(name, property1, oldValue1, newValue1, property2, oldValue2, newValue2));
    }

    /**
     * performs current undo
     *
     * @return true, if undo performed
     */
    public boolean undo() {
        if (!performingUndoOrRedo.get()) {
            performingUndoOrRedo.set(true);
            try {
                if (!canUndo())
                    throw new IllegalStateException("Cannot undo.");
                currentNode.change.undo();
                movePrev();
                updateProperties();
                return true;
            } finally {
                performingUndoOrRedo.set(false);
            }
        }
        return false;
    }

    /**
     * performs current redo
     *
     * @return true, if redo performed
     */
    public boolean redo() {
        if (!performingUndoOrRedo.get()) {
            performingUndoOrRedo.set(true);
            try {
                if (!canRedo())
                    throw new IllegalStateException("Cannot redo.");
                moveNext();
                currentNode.change.redo();
                updateProperties();
                return true;
            } finally {
                performingUndoOrRedo.set(false);
            }
        }
        return false;
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
    public ReadOnlyBooleanProperty canUndoProperty() {
        return ReadOnlyBooleanProperty.readOnlyBooleanProperty(canUndo);
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
    public ReadOnlyBooleanProperty canRedoProperty() {
        return ReadOnlyBooleanProperty.readOnlyBooleanProperty(canRedo);
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
        undoName.set(canUndo() ? "Undo" + (currentNode.change.getName().length() > 0 ? " " + currentNode.change.getName() : "") : "Undo");
        redoName.set(canRedo() ? "Redo" + (currentNode.next.change.getName().length() > 0 ? " " + currentNode.next.change.getName() : "") : "Redo");
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

    public void setRecordChanges(boolean recordChanges) {
        this.recordChanges = recordChanges;
    }

    public boolean isRecordChanges() {
        return recordChanges;
    }

    /**
     * adds a undoable apply item. If undo is called on this, then all undos up until the previous undoable apply are performed.
     * After that, this item is removed from the undo manager, as it is not redoable.
     *
     * @param runnable
     */
    public void addUndoableApply(Runnable runnable) {
        if (currentNode.change == null || !(currentNode.change instanceof UndoableApply)) {
            addUndoableChange(new UndoableApply(currentNode, runnable));
        }
    }

    public class UndoableApply extends UndoableChange {
        private final ListNode prev;
        private final Runnable runnable;

        public UndoableApply(ListNode lastNode, Runnable runnable) {
            super("Apply");
            this.prev = lastNode;
            this.runnable = runnable;
        }

        @Override
        public void undo() {
            for (ListNode a = prev; a != null; a = a.prev) {
                if (a.change instanceof UndoableApply)
                    break;
                if (a.change != null) {
                    a.change.undo();
                }
            }
            runnable.run();
        }

        @Override
        public void redo() {
            runnable.run();
        }
    }
}