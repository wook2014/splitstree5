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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jloda.fx.ASelectionModel;
import jloda.fx.ExtendedFXMLLoader;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.ANode;
import splitstree5.core.dag.DAG;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.undo.UndoManager;
import splitstree5.undo.UndoableChange;
import splitstree5.undo.UndoableChangeList;

import java.io.IOException;
import java.util.*;

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

    private final Map<ANode, DagNodeView> node2NodeView = new HashMap<>();
    private final Map<ANode, List<DagEdgeView>> node2EdgeViews = new HashMap<>();


    /**
     * constructor
     */
    public DAGView(Document document) throws IOException {
        this.document = document;

        final ExtendedFXMLLoader<DAGViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        undoManager = new UndoManager();

        final ASelectionModel<ANode> selectionModel = getDag().getNodeSelectionModel();

        controller.getCenterPane().getChildren().addAll(edgeViews, nodeViews);

        controller.getCloseMenuItem().setOnAction((e) -> Platform.exit());
        controller.getDoneButton().setOnAction((e) -> Platform.exit());

        controller.getUndoMenuItem().setOnAction((e) -> undoManager.undo());
        controller.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canUndoProperty()));
        controller.getUndoMenuItem().textProperty().bind(undoManager.undoNameProperty());
        controller.getRedoMenuItem().setOnAction((e) -> undoManager.redo());

        controller.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canRedoProperty()));
        controller.getRedoMenuItem().textProperty().bind(undoManager.redoNameProperty());

        controller.getSelectAllMenuItem().setOnAction((e) -> selectionModel.selectAll());
        controller.getSelectNoneMenuItem().setOnAction((e) -> selectionModel.clearSelection());
        controller.getSelectNoneMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        selectionModel.getSelectedItems().addListener((InvalidationListener) (e) -> System.err.println("Selected: " + selectionModel.getSelectedItems().size()));

        controller.getDeleteMenuItem().setOnAction((e) -> {
            Set<ANode> deletableSelection = new HashSet<>(selectionModel.getSelectedItems());
            deletableSelection.remove(getDag().getTopTaxaNode());
            deletableSelection.remove(getDag().getTopDataNode());
            deletableSelection.remove(getDag().getTaxaFilter());
            deletableSelection.remove(getDag().getTopFilter());
            deletableSelection.remove(getDag().getWorkingTaxaNode());
            deletableSelection.remove(getDag().getWorkingDataNode());

            Set<ANode> toDelete = new HashSet<>();
            for (Node node : nodeViews.getChildren()) {
                if (node instanceof DagNodeView) {
                    final ANode aNode = ((DagNodeView) node).getANode();
                    if (aNode instanceof AConnector) {
                        ANode child = ((AConnector) aNode).getChild();
                        if (deletableSelection.contains(child))
                            toDelete.add(aNode);
                    }
                }
            }
            final ArrayList<UndoableChange> list = new ArrayList<>();
            toDelete.addAll(deletableSelection);
            for (ANode node : toDelete) {
                if (node2EdgeViews.keySet().contains(node)) {
                    final ArrayList<DagEdgeView> listOfEdgeView = new ArrayList<>(node2EdgeViews.get(node));
                    for (DagEdgeView edgeView : listOfEdgeView) {
                        edgeViews.getChildren().remove(edgeView);
                        list.add(new UndoableChange("Delete") {
                            public void undo() {
                                if (!edgeViews.getChildren().contains(edgeView))
                                    edgeViews.getChildren().add(edgeView);
                            }

                            public void redo() {
                                edgeViews.getChildren().remove(edgeView);
                            }
                        });
                    }
                    list.add(new UndoableChange("Delete") {
                        public void undo() {
                            node2EdgeViews.put(node, listOfEdgeView);
                        }

                        public void redo() {
                            node2EdgeViews.remove(node);
                        }
                    });
                }
                final DagNodeView nodeView = node2NodeView.get(node);
                final ObservableList<ANode> children = FXCollections.observableArrayList(node.getChildren());
                final ANode parent = getDag().findParent(node);
                list.add(new UndoableChange("Delete") {
                    public void undo() {
                        if (!nodeViews.getChildren().contains(nodeView))
                            nodeViews.getChildren().add(nodeView);
                        node2NodeView.put(node, nodeView);
                        getDag().reconnect(parent, node, children);
                    }

                    public void redo() {
                        nodeViews.getChildren().remove(nodeView);
                        node2EdgeViews.remove(node);
                        getDag().delete(node, true, false);
                    }
                });
                {
                    list.add(new UndoableChange("Delete") {
                        public void undo() {
                            getDag().updateSelectionModel();
                            getDocument().updateMethodsText();
                        }

                        public void redo() {
                            getDag().updateSelectionModel();
                            getDocument().updateMethodsText();
                        }
                    });
                }
                selectionModel.clearSelection(node);
                // here we actually delete stuff:
                for (UndoableChange change : list) {
                    change.redo();
                }
                getUndoManager().addUndoableChange(new UndoableChangeList("Delete", list));
            }
        });
        controller.getDeleteMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        controller.getMethodTextArea().textProperty().bind(document.methodsTextProperty());

        controller.getZoomInMenuItem().setOnAction((e) -> {
            controller.getCenterPane().setScaleX(1.1 * controller.getCenterPane().getScaleX());
            controller.getCenterPane().setScaleY(1.1 * controller.getCenterPane().getScaleY());
        });
        controller.getZoomOutMenuItem().setOnAction((e) -> {
            controller.getCenterPane().setScaleX(1.0 / 1.1 * controller.getCenterPane().getScaleX());
            controller.getCenterPane().setScaleY(1.0 / 1.1 * controller.getCenterPane().getScaleY());
        });

        controller.getCenterScrollPane().setPrefViewportHeight(controller.getCenterPane().getHeight());
        controller.getCenterScrollPane().setPrefViewportWidth(controller.getCenterPane().getWidth());

        controller.getCenterScrollPane().setOnMouseClicked((e) -> {
            if (!e.isShiftDown())
                selectionModel.clearSelection();
        });

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

        node2NodeView.clear();
        node2NodeView.put(dag.getTopTaxaNode(), new DagNodeView(this, dag.getTopTaxaNode()));
        node2NodeView.put(dag.getTaxaFilter(), new DagNodeView(this, dag.getTaxaFilter()));

        node2NodeView.put(dag.getWorkingTaxaNode(), new DagNodeView(this, dag.getWorkingTaxaNode()));
        node2NodeView.put(dag.getTopDataNode(), new DagNodeView(this, dag.getTopDataNode()));
        node2NodeView.put(dag.getTopFilter(), new DagNodeView(this, dag.getTopFilter()));
        node2NodeView.put(dag.getWorkingDataNode(), new DagNodeView(this, dag.getWorkingDataNode()));

        final double yDelta = 150;
        final double xDelta = 250;
        node2NodeView.get(dag.getTopTaxaNode()).setXY(20, 20);
        node2NodeView.get(dag.getTopDataNode()).setXY(20 + xDelta, 20);
        node2NodeView.get(dag.getTaxaFilter()).setXY(20, 20 + yDelta);
        node2NodeView.get(dag.getWorkingTaxaNode()).setXY(20, 20 + 2 * yDelta);
        node2NodeView.get(dag.getTopFilter()).setXY(20 + xDelta, 20 + 2 * yDelta);
        node2NodeView.get(dag.getWorkingDataNode()).setXY(20 + 2 * xDelta, 20 + yDelta);

        assignNodeViewsAndCoordinatesForChildrenRec(this, dag.getWorkingDataNode(), node2NodeView, xDelta, yDelta, true);

        final ObservableList<DagEdgeView> edgeViews = FXCollections.observableArrayList();

        for (DagNodeView a : node2NodeView.values()) {
            for (DagNodeView b : node2NodeView.values()) {
                if (a.getANode().getChildren().contains(b.getANode())) {
                    final DagEdgeView edgeView = new DagEdgeView(a, b);
                    edgeViews.add(edgeView);
                    {
                        final List<DagEdgeView> list = node2EdgeViews.computeIfAbsent(a.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                    {
                        final List<DagEdgeView> list = node2EdgeViews.computeIfAbsent(b.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                }

                else if (a.getANode() == dag.getWorkingTaxaNode() && b.getANode() == dag.getTopFilter()) {
                    final DagEdgeView edgeView = new DagEdgeView(a, b);
                    edgeViews.add(edgeView);
                    {
                        final List<DagEdgeView> list = node2EdgeViews.computeIfAbsent(a.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                    {
                        final List<DagEdgeView> list = node2EdgeViews.computeIfAbsent(b.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                }
            }
        }

        getEdgeViews().getChildren().addAll(edgeViews);
        getNodeViews().getChildren().addAll(node2NodeView.values());

        dag.getNodeSelectionModel().getSelectedItems().addListener((ListChangeListener<ANode>) c -> {
            while (c.next()) {
                for (ANode node : c.getAddedSubList()) {
                    if (node2NodeView.containsKey(node))
                        node2NodeView.get(node).setEffect(new DropShadow(3, Color.RED));
                }
                for (ANode node : c.getRemoved()) {
                    if (node2NodeView.containsKey(node))
                        node2NodeView.get(node).setEffect(null);
                }
            }
        });
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

    public DagNodeView getNodeView(ANode node) {
        return node2NodeView.get(node);
    }

    Map<ANode, DagNodeView> getNode2NodeView() {
        return node2NodeView;
    }

    Map<ANode, List<DagEdgeView>> getNode2EdgeViews() {
        return node2EdgeViews;
    }
}