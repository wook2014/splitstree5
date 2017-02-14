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

package splitstree5.gui.dagview;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import splitstree5.core.Document;
import splitstree5.core.dag.ANode;
import splitstree5.core.dag.DAG;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.undo.UndoManager;
import splitstree5.utils.Option;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * create a connector view
 * Created by huson on 12/31/16.
 */
public class DAGView {
    private final Document document;
    private final Parent root;
    private final DAGViewController controller;
    private final UndoManager undoManager;
    private Stage stage;

    private final Group nodeViews = new Group();
    private final Group edgeViews = new Group();

    private final ArrayList<Option> options = new ArrayList<>();

    /**
     * constructor
     */
    public DAGView(Document document) throws IOException {
        this.document = document;

        final ExtendedFXMLLoader<DAGViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        undoManager = new UndoManager();

        controller.getCenterPane().getChildren().addAll(edgeViews, nodeViews);

        controller.getUndoMenuItem().setOnAction((e) -> {
            undoManager.undo();
        });
        controller.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canUndoProperty()));
        controller.getUndoMenuItem().textProperty().bind(undoManager.undoNameProperty());
        controller.getRedoMenuItem().setOnAction((e) -> undoManager.redo());
        controller.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canRedoProperty()));
        controller.getRedoMenuItem().textProperty().bind(undoManager.redoNameProperty());

        recompute();
    }

    /**
     * clear and then recompute the view
     */
    public void recompute() {
        undoManager.clear();

        getEdgeViews().getChildren().clear();
        getNodeViews().getChildren().clear();

        final DAG dag = document.getDag();

        final Map<ANode, DagNodeView> node2nodeView = new HashMap<>();

        node2nodeView.put(dag.getTopTaxaNode(), new DagNodeView(this, dag.getTopTaxaNode()));
        node2nodeView.put(dag.getTaxaFilter(), new DagNodeView(this, dag.getTaxaFilter()));

        node2nodeView.put(dag.getWorkingTaxaNode(), new DagNodeView(this, dag.getWorkingTaxaNode()));
        node2nodeView.put(dag.getTopDataNode(), new DagNodeView(this, dag.getTopDataNode()));
        node2nodeView.put(dag.getTopFilter(), new DagNodeView(this, dag.getTopFilter()));
        node2nodeView.put(dag.getWorkingDataNode(), new DagNodeView(this, dag.getWorkingDataNode()));

        final double yDelta = 150;
        final double xDelta = 250;
        node2nodeView.get(dag.getTopTaxaNode()).setXY(20, 20);
        node2nodeView.get(dag.getTopDataNode()).setXY(20 + xDelta, 20);
        node2nodeView.get(dag.getTaxaFilter()).setXY(20, 20 + yDelta);
        node2nodeView.get(dag.getWorkingTaxaNode()).setXY(20, 20 + 2 * yDelta);
        node2nodeView.get(dag.getTopFilter()).setXY(20 + xDelta, 20 + 2 * yDelta);
        node2nodeView.get(dag.getWorkingDataNode()).setXY(20 + 2 * xDelta, 20 + yDelta);

        assignNodeViewsAndCoordinatesForChildrenRec(this, dag.getWorkingDataNode(), node2nodeView, xDelta, yDelta, true);

        final ObservableList<DagEdgeView> edgeViews = FXCollections.observableArrayList();

        for (DagNodeView a : node2nodeView.values()) {
            for (DagNodeView b : node2nodeView.values()) {
                if (a.getANode().getChildren().contains(b.getANode()))
                    edgeViews.add(new DagEdgeView(a, b));
                else if (a.getANode() == dag.getWorkingTaxaNode() && b.getANode() == dag.getTopFilter()) {
                    edgeViews.add(new DagEdgeView(a, b));
                }
            }
        }

        getEdgeViews().getChildren().addAll(edgeViews);
        getNodeViews().getChildren().addAll(node2nodeView.values());
    }

    /**
     * recursively
     *
     * @param v
     * @param node2nodeView
     * @param xDelta
     * @param yDelta
     * @param horizontal
     */
    private void assignNodeViewsAndCoordinatesForChildrenRec(DAGView dagView, ANode v, Map<ANode, DagNodeView> node2nodeView, double xDelta, double yDelta, boolean horizontal) {
        double x = node2nodeView.get(v).xProperty().get();
        double y = node2nodeView.get(v).yProperty().get();

        int count = 0;
        for (ANode w : v.getChildren()) {
            if (count == 1)
                horizontal = !horizontal;
            else if (count == 2) {
                x += (count - 1) * xDelta;
                y += (count - 1) * yDelta;
            }

            final DagNodeView nodeView = node2nodeView.computeIfAbsent(w, k -> new DagNodeView(dagView, w));
            if (horizontal) {
                nodeView.setXY(x + xDelta, y);
            } else {
                nodeView.setXY(x, y + yDelta);
            }
            assignNodeViewsAndCoordinatesForChildrenRec(dagView, w, node2nodeView, xDelta, yDelta, horizontal);
            count++;
        }
    }

    public DAG getDag() {
        return document.getDag();
    }

    public Document getDocument() {
        return document;
    }

    public Pane getCenterPane() {
        return controller.getCenterPane();
    }

    public Group getNodeViews() {
        return nodeViews;
    }

    public Group getEdgeViews() {
        return edgeViews;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * show this view
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("DAG Viewer - SplitsTree5");
        stage.setScene(new Scene(root, 600, 400));

        stage.setX(100 + ConnectorView.windowCount * 40);
        stage.setY(200 + ConnectorView.windowCount * 40);
        ConnectorView.windowCount++;

        stage.show();
    }
}