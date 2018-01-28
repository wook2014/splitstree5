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

package jloda.fx;


import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.MultipleSelectionModel;
import jloda.util.Basic;

import java.util.*;

/**
 * Selection model
 * Daniel Huson, 12.2015
 */
public class ASelectionModel<T> extends MultipleSelectionModel<T> {
    private final ObservableSet<Integer> selectedIndices = FXCollections.synchronizedObservableSet(FXCollections.observableSet());
    private final ObservableList<Integer> selectedIndicesAsList = FXCollections.observableArrayList();
    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final ObservableList<Integer> unmodifiableSelectedIndices = FXCollections.unmodifiableObservableList(selectedIndicesAsList);
    private final ObservableList<T> unmodifiableSelectedItems = FXCollections.unmodifiableObservableList(selectedItems);

    private final ReadOnlyBooleanProperty empty = (new SimpleSetProperty<>(selectedIndices).emptyProperty());

    private T[] items; // need a copy of this array to map indices to objects, when required

    private int focusIndex = -1; // focus index

    private boolean suspendListeners = false;

    private boolean inUpdate = false;


    /**
     * Constructor
     *
     * @param items 0 or more items
     */
    @SafeVarargs
    public ASelectionModel(T... items) {
        this.items = Arrays.copyOf(items, items.length);  // use copy for safety

        // setup unmodifiable lists
        {
            // first setup observable array lists that listen for changes of the selectedIndices set

            selectedIndices.addListener((SetChangeListener<Integer>) c -> {
                if (!inUpdate) {
                    try {
                        inUpdate = true;
                        if (!suspendListeners) {
                            if (c.wasAdded()) {
                                selectedIndicesAsList.add(c.getElementAdded());
                                selectedItems.add(ASelectionModel.this.getItems()[c.getElementAdded()]);
                            } else if (c.wasRemoved()) {
                                selectedIndicesAsList.remove(c.getElementRemoved());
                                final T item = ASelectionModel.this.getItems()[c.getElementRemoved()];
                                if (selectedItems.size() > 0 && selectedItems.contains(item)) {
                                    try {
                                        selectedItems.remove(item);
                                    } catch (Exception ex) {
                                        Basic.caught(ex);
                                    }
                                }
                            }
                        }
                    } finally {
                        inUpdate = false;
                    }
                }
            });
        }
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return unmodifiableSelectedIndices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return unmodifiableSelectedItems;
    }

    @Override
    public void selectIndices(int index, int... indices) {
        select(index);
        for (int i : indices) {
            select(i);
        }
    }

    @Override
    public void selectAll() {
        focusIndex = -1;
        final ArrayList<Integer> indicesToSelect = new ArrayList<>(items.length);
        for (int index = 0; index < items.length; index++) {
            if (!selectedIndices.contains(index))
                indicesToSelect.add(index);
        }
        selectedIndices.addAll(indicesToSelect);
    }

    @Override
    public void clearAndSelect(int index) {
        clearSelection();
        select(index);
    }

    @Override
    public void select(int index) {
        if (index >= 0 && index < items.length) {
            focusIndex = index;
            if (!selectedIndices.contains(index))
                selectedIndices.add(index);
        }
    }

    @Override
    public void select(T item) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(item) && !selectedIndices.contains(i)) {
                focusIndex = i;
                selectedIndices.add(i);
                return;
            }
        }
    }

    public void selectItems(Collection<? extends T> toSelect) {
        final ArrayList<Integer> indicesToSelect = new ArrayList<>(toSelect.size());
        for (int i = 0; i < items.length; i++) {
            if (!selectedIndices.contains(i) && toSelect.contains(items[i])) {
                indicesToSelect.add(i);
            }
        }
        if (indicesToSelect.size() > 0) {
            selectedIndices.addAll(indicesToSelect);
            focusIndex = indicesToSelect.get(0);
        }
    }

    public void clearSelection(T item) {
        for (int i : selectedIndices) {
            if (items[i].equals(item)) {
                if (i == focusIndex)
                    focusIndex = -1;
                selectedIndices.remove(i);
                return;
            }
        }
    }

    public void clearSelection(Collection<? extends T> toClear) {
        final ArrayList<Integer> indicesToClear = new ArrayList<>(toClear.size());
        for (int i : selectedIndices) {
            if (toClear.contains(items[i])) {
                indicesToClear.add(i);
            }
        }
        if (indicesToClear.size() > 0)
            selectedIndices.removeAll(indicesToClear);
        if (indicesToClear.contains(focusIndex))
            focusIndex = -1;
    }

    @Override
    public void clearSelection(int index) {
        if (index >= 0 && index < items.length) {
            selectedIndices.remove(index);
        }
        if (index == focusIndex)
            focusIndex = -1;
    }


    @Override
    public void clearSelection() {
        focusIndex = -1;
        selectedIndices.clear();
    }

    @Override
    public boolean isSelected(int index) {
        return index >= 0 && index < items.length && selectedIndices.contains(index);
    }

    @Override
    public boolean isEmpty() {
        return empty.get();
    }

    public ReadOnlyBooleanProperty emptyProperty() {
        return empty;
    }

    @Override
    public void selectFirst() {
        if (items.length > 0) {
            select(0);
        }
    }

    @Override
    public void selectLast() {
        if (items.length > 0) {
            select(items.length - 1);
        }
    }

    @Override
    public void selectPrevious() {
        select(focusIndex - 1);
    }

    @Override
    public void selectNext() {
        select(focusIndex + 1);
    }

    /**
     * get the current array of items.
     *
     * @return items
     */
    public T[] getItems() {
        return items;
    }

    /**
     * clear selection and set list of items
     *
     * @param items
     */
    public void setItems(T[] items) {
        clearSelection();
        selectedItems.clear();
        selectedIndices.clear();
        selectedIndicesAsList.clear();
        this.items = Arrays.copyOf(items, items.length);  // use copy for safety
    }

    /**
     * clear selection and set list of items
     *
     * @param items
     */
    public void setItems(Collection<T> items) {
        setItems(Basic.toArray(items));
    }

    /**
     * clear selection and set list of items
     *
     * @param items1
     * @param items2
     */
    public void setItems(Collection<? extends T> items1, Collection<? extends T> items2) {
        final Collection<T> all = new ArrayList<>(items1.size() + items2.size());
        all.addAll(items1);
        all.addAll(items2);
        setItems(Basic.toArray(all));
    }

    /**
     * invert the current selection
     */
    public void invertSelection() {
        focusIndex = -1;
        final Set<Integer> toSelect = new HashSet<>();
        for (int index = 0; index < items.length; index++) {
            if (!selectedIndices.contains(index))
                toSelect.add(index);
        }
        selectedIndices.clear();
        selectedIndices.addAll(toSelect);
    }

    /**
     * gets the focus index or -1
     */
    public int getFocusIndex() {
        return focusIndex;
    }

    /**
     * iterate over all selected items, or all items, if none selected
     */
    public Iterable<T> getSelectedOrAll() {
        return () -> {
            if (getSelectedItems().size() > 0) {
                return getSelectedItems().iterator();
            } else {
                return new Iterator<T>() {
                    private int i = 0;

                    @Override
                    public boolean hasNext() {
                        return i < items.length;
                    }

                    @Override
                    public T next() {
                        return items[i++];
                    }
                };
            }
        };
    }

    public boolean isSuspendListeners() {
        return suspendListeners;
    }

    public void setSuspendListeners(boolean suspendListeners) {
        this.suspendListeners = suspendListeners;
    }
}
