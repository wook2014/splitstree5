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
package jloda.find;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MultipleSelectionModel;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.phylo.PhyloGraph;

import java.util.LinkedList;
import java.util.Objects;

/**
 * Class for finding and replacing edge labels in a labeled graph
 * Daniel Huson, 7.2008, 1.2018
 */
public class EdgeLabelSearcher implements IObjectSearcher<Edge> {
    private final String name;
    private PhyloGraph graph;
    private final ASelectionModel<Edge> edgeSelectionModel;
    private Edge current = null;

    private final ObjectProperty<Edge> found = new SimpleObjectProperty<>();

    public static final String SEARCHER_NAME = "Edges";


    /**
     * constructor
     *
     * @param
     * @param graph
     */
    public EdgeLabelSearcher(PhyloGraph graph, ASelectionModel<Edge> edgeSelectionModel) {
        this(SEARCHER_NAME, graph, edgeSelectionModel);
    }

    /**
     * constructor
     *
     * @param
     * @param graph
     */
    public EdgeLabelSearcher(String name, PhyloGraph graph, ASelectionModel<Edge> edgeSelectionModel) {
        this.graph = graph;
        this.name = name;
        this.edgeSelectionModel = edgeSelectionModel;
    }

    /**
     * get the name for this type of search
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * goto the first object
     */
    public boolean gotoFirst() {
        current = graph.getFirstEdge();
        return isCurrentSet();
    }

    /**
     * goto the next object
     */
    public boolean gotoNext() {
        if (current == null || current.getOwner() == null)
            gotoFirst();
        else
            current = current.getNext();
        return isCurrentSet();
    }

    /**
     * goto the last object
     */
    public boolean gotoLast() {
        current = graph.getLastEdge();
        return isCurrentSet();
    }

    /**
     * goto the previous object
     */
    public boolean gotoPrevious() {
        if (current == null)
            gotoLast();
        else
            current = current.getPrev();
        return isCurrentSet();
    }

    /**
     * is the current object selected?
     *
     * @return true, if selected
     */
    public boolean isCurrentSelected() {
        return isCurrentSet() && edgeSelectionModel.getSelectedItems().contains(current);
    }

    /**
     * set selection state of current object
     *
     * @param select
     */
    public void setCurrentSelected(boolean select) {
        if (current != null) {
            final Edge toSelect = current;
            Platform.runLater(() -> {
                if (select) {
                    edgeSelectionModel.select(toSelect);
                    found.set(toSelect);
                } else {
                    edgeSelectionModel.clearSelection(toSelect);
                    found.set(null);
                }
            });
        }
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        Platform.runLater(() -> {
            if (select)
                edgeSelectionModel.selectAll();
            else
                edgeSelectionModel.clearSelection();
        });
    }

    /**
     * get the label of the current object
     *
     * @return label
     */
    public String getCurrentLabel() {
        if (current == null)
            return null;
        else
            return graph.getLabel(current);
    }

    /**
     * set the label of the current object
     *
     * @param newLabel
     */
    public void setCurrentLabel(String newLabel) {
        if (current != null && !Objects.equals(newLabel, graph.getLabel(current))) {
            if (newLabel == null || newLabel.length() == 0) {
                graph.setLabel(current, null);
            } else {
                graph.setLabel(current, newLabel);
            }
            fireLabelChangedListeners(current);
        }
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public boolean isGlobalFindable() {
        return graph.getNumberOfEdges() > 0;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public boolean isSelectionFindable() {
        return edgeSelectionModel.getSelectedItems().size() > 0;
    }

    /**
     * is the current object set?
     *
     * @return true, if set
     */
    public boolean isCurrentSet() {
        return current != null;
    }

    /**
     * something has been changed or selected, update view
     */
    public void updateView() {
    }

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    public boolean canFindAll() {
        return true;
    }

    private final java.util.List<LabelChangedListener> labelChangedListeners = new LinkedList<>();

    /**
     * fire the label changed listener
     *
     * @param v
     */
    private void fireLabelChangedListeners(Edge v) {
        for (LabelChangedListener listener : labelChangedListeners) {
            listener.doLabelHasChanged(v);
        }
    }

    /**
     * add a label changed listener
     *
     * @param listener
     */
    public void addLabelChangedListener(LabelChangedListener listener) {
        labelChangedListeners.add(listener);
    }

    /**
     * remove a label changed listener
     *
     * @param listener
     */
    public void removeLabelChangedListener(LabelChangedListener listener) {
        labelChangedListeners.remove(listener);
    }

    /**
     * label changed listener
     */
    public interface LabelChangedListener {
        void doLabelHasChanged(Edge v);
    }

    /**
     * how many objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfObjects() {
        return graph.getNumberOfEdges();
    }

    /**
     * how many selected objects are there?
     *
     * @return number of objects or -1
     */
    public int numberOfSelectedObjects() {
        return edgeSelectionModel.getSelectedItems().size();
    }

    public Edge getFound() {
        return found.get();
    }

    public ReadOnlyObjectProperty<Edge> foundProperty() {
        return found;
    }

    @Override
    public MultipleSelectionModel<Edge> getSelectionModel() {
        return edgeSelectionModel;
    }

    public PhyloGraph getGraph() {
        return graph;
    }

    public void setGraph(PhyloGraph graph) {
        this.graph = graph;
    }
}
