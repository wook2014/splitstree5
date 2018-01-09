/*
 *  Copyright (C) 2018 Daniel H. Huson
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


import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Command manager
 * Daniel Huson, 12.2017
 */
public class UndoRedoManager {
    private final ObservableList<UndoableRedoableCommand> undoStack = FXCollections.observableArrayList();
    private final ObservableList<UndoableRedoableCommand> redoStack = FXCollections.observableArrayList();

    private final StringProperty undoName = new SimpleStringProperty("Undo");
    private final StringProperty redoName = new SimpleStringProperty("Redo");

    private final IntegerProperty undoStackSize = new SimpleIntegerProperty(0);
    private final IntegerProperty redoStackSize = new SimpleIntegerProperty(0);


    private final BooleanProperty isPerformingUndoOrRedo = new SimpleBooleanProperty(false);

    private boolean recordChanges = true;

    /**
     * default constructor
     */
    public UndoRedoManager() {
        undoStack.addListener((InvalidationListener) (e) -> {
            undoName.set(undoStack.size() > 0 ? "Undo " + peek(undoStack).getName() : "Undo");
            undoStackSize.set(undoStack.size());
        });
        redoStack.addListener((InvalidationListener) (e) -> {
            redoName.set(redoStack.size() > 0 ? "Redo " + peek(redoStack).getName() : "Redo");
            redoStackSize.set(redoStack.size());
        });
    }

    /**
     * clear
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    /**
     * add a command to the undoable stack
     *
     * @param command
     */
    public void add(UndoableRedoableCommand command) {
        if (command.isUndoable())
            push(command, undoStack);
        else
            undoStack.clear();
        redoStack.clear();
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
    public <T> void add(String name, Property<T> property, T oldValue, T newValue) {
        if (isRecordChanges() && !isPerformingUndoOrRedo())
            add(new UndoableChangeProperty<>(name, property, oldValue, newValue));
    }

    /**
     * adds a undoable apply item. If undo is called on this, then all undos up until the previous undoable apply are performed.
     * Then apply is re-run
     *
     * @param runnable the code to be run
     */
    public void addUndoableApply(Runnable runnable) {
        if (isRecordChanges() && !isPerformingUndoOrRedo()) {
            if (undoStack.size() == 0 || !(peek(undoStack) instanceof UndoableApply)) {
                add(new UndoableApply(runnable));
            }
        }
    }

    public ReadOnlyIntegerProperty undoStackSizeProperty() {
        return undoStackSize;
    }

    public ReadOnlyIntegerProperty redoStackSizeProperty() {
        return redoStackSize;
    }

    /**
     * undo the current undoable command
     *
     * @throws IllegalStateException if no current undoable command
     * @throws Exception
     */
    public void undo() {
        if (undoStack.size() == 0)
            throw new IllegalStateException("Undo stack empty");
        final UndoableRedoableCommand command = pop(undoStack);
        if (command.isRedoable())
            push(command, redoStack);
        else
            redoStack.clear();
        try {
            isPerformingUndoOrRedo.set(true);
            command.undo();
        } finally {
            isPerformingUndoOrRedo.set(false);
        }
    }

    /**
     * redo the current redoable event
     *
     * @throws IllegalStateException if no current redoable command
     * @throws Exception
     */
    public void redo() {
        if (redoStack.size() == 0)
            throw new IllegalStateException("Redo stack empty");
        final UndoableRedoableCommand command = pop(redoStack);
        ;
        if (command.isUndoable())
            push(command, undoStack);
        else
            undoStack.clear();
        try {
            isPerformingUndoOrRedo.set(true);
            command.redo();
        } finally {
            isPerformingUndoOrRedo.set(false);
        }
    }

    public ReadOnlyBooleanProperty canUndoProperty() {
        final BooleanProperty canUndo = new SimpleBooleanProperty();
        canUndo.bind(Bindings.isNotEmpty(undoStack).and(isPerformingUndoOrRedo.not()));
        return canUndo;
    }

    public ReadOnlyBooleanProperty canRedoProperty() {
        final BooleanProperty canRedo = new SimpleBooleanProperty();
        canRedo.bind(Bindings.isNotEmpty(redoStack).and(isPerformingUndoOrRedo.not()));
        return canRedo;
    }

    /**
     * get the name of current undoable command
     *
     * @return name property
     */
    public ReadOnlyStringProperty undoNameProperty() {
        return undoName;
    }

    /**
     * get the name of current redoable command
     *
     * @return name property
     */
    public ReadOnlyStringProperty redoNameProperty() {
        return redoName;
    }

    public void setRecordChanges(boolean recordChanges) {
        this.recordChanges = recordChanges;
    }

    public boolean isRecordChanges() {
        return recordChanges;
    }

    public boolean isPerformingUndoOrRedo() {
        return isPerformingUndoOrRedo.get();
    }

    private static <T> void push(T item, ObservableList<T> stack) {
        stack.add(item);
    }

    private static <T> T pop(ObservableList<T> stack) {
        return stack.remove(stack.size() - 1);
    }

    private static <T> T peek(ObservableList<T> stack) {
        return stack.get(stack.size() - 1);
    }

    /**
     * an undoable
     */
    class UndoableApply extends UndoableRedoableCommand {
        private final Runnable runnable;

        public UndoableApply(Runnable runnable) {
            super("Apply");
            this.runnable = runnable;
        }

        @Override
        public void undo() {
            // this command has already been moved to the redo stack
            while (undoStack.size() > 0 && !(peek(undoStack) instanceof UndoableApply)) {
                UndoRedoManager.this.undo();
            }
            runnable.run(); // re-run apply
        }

        @Override
        public void redo() {
            runnable.run();
        }
    }

}
