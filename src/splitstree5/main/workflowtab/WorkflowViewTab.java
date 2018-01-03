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

package splitstree5.main.workflowtab;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import jloda.fx.ASelectionModel;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.Workflow;
import splitstree5.main.MainWindowController;
import splitstree5.main.ViewerTab;
import splitstree5.undo.UndoRedoManager;
import splitstree5.undo.UndoableChangeList;
import splitstree5.undo.UndoableRedoableCommand;
import splitstree5.utils.SelectionEffect;

import java.util.*;

/**
 * create a connector view
 * Created by huson on 12/31/16.
 */
public class WorkflowViewTab extends ViewerTab {
    private final Document document;
    private final UndoRedoManager undoManager;

    private final Group nodeViews = new Group();
    private final Group edgeViews = new Group();

    private final Map<ANode, WorkflowNodeView> node2NodeView = new HashMap<>();
    private final Map<ANode, List<WorkflowEdgeView>> node2EdgeViews = new HashMap<>();

    private final ZoomableScrollPane scrollPane;
    private final Pane centerPane;

    /**
     * constructor
     */
    public WorkflowViewTab(Document document) {
        this.document = document;
        undoManager = new UndoRedoManager();
        setText("Workflow");

        centerPane = new StackPane(edgeViews, nodeViews);
        centerPane.setPadding(new Insets(5, 5, 5, 5));
        //centerPane.setStyle("-fx-border-color: red");
        scrollPane = new ZoomableScrollPane(centerPane);
        scrollPane.setPadding(new Insets(2, 2, 2, 2));
        setContent(scrollPane);

        centerPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));

        centerPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()));

        getScrollPane().setOnMouseClicked((e) -> {
            if (!e.isShiftDown())
                getWorkflow().getNodeSelectionModel().clearSelection();
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

        final Workflow workflow = document.getWorkflow();

        if (workflow.getTopTaxaNode() == null)
            return; // no data available

        node2NodeView.clear();
        node2NodeView.put(workflow.getTopTaxaNode(), new WorkflowNodeView(this, workflow.getTopTaxaNode()));
        node2NodeView.put(workflow.getTaxaFilter(), new WorkflowNodeView(this, workflow.getTaxaFilter()));

        node2NodeView.put(workflow.getWorkingTaxaNode(), new WorkflowNodeView(this, workflow.getWorkingTaxaNode()));
        node2NodeView.put(workflow.getTopDataNode(), new WorkflowNodeView(this, workflow.getTopDataNode()));
        node2NodeView.put(workflow.getTopFilter(), new WorkflowNodeView(this, workflow.getTopFilter()));
        node2NodeView.put(workflow.getWorkingDataNode(), new WorkflowNodeView(this, workflow.getWorkingDataNode()));

        final double yDelta = 100;
        final double xDelta = 200;
        node2NodeView.get(workflow.getTopTaxaNode()).setXY(20, 20);
        node2NodeView.get(workflow.getTopDataNode()).setXY(20 + xDelta, 20);
        node2NodeView.get(workflow.getTaxaFilter()).setXY(20, 20 + yDelta);
        node2NodeView.get(workflow.getWorkingTaxaNode()).setXY(20, 20 + 2 * yDelta);
        node2NodeView.get(workflow.getTopFilter()).setXY(20 + xDelta, 20 + 2 * yDelta);
        node2NodeView.get(workflow.getWorkingDataNode()).setXY(20 + 2 * xDelta, 20 + yDelta);

        assignNodeViewsAndCoordinatesForChildrenRec(this, workflow.getWorkingDataNode(), node2NodeView, xDelta, yDelta, true);

        final ObservableList<WorkflowEdgeView> edgeViews = FXCollections.observableArrayList();

        for (WorkflowNodeView a : node2NodeView.values()) {
            for (WorkflowNodeView b : node2NodeView.values()) {
                if (a.getANode().getChildren().contains(b.getANode())) {
                    final WorkflowEdgeView edgeView = new WorkflowEdgeView(a, b);
                    edgeViews.add(edgeView);
                    {
                        final List<WorkflowEdgeView> list = node2EdgeViews.computeIfAbsent(a.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                    {
                        final List<WorkflowEdgeView> list = node2EdgeViews.computeIfAbsent(b.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                } else if (a.getANode() == workflow.getWorkingTaxaNode() && b.getANode() == workflow.getTopFilter()) {
                    final WorkflowEdgeView edgeView = new WorkflowEdgeView(a, b);
                    edgeViews.add(edgeView);
                    {
                        final List<WorkflowEdgeView> list = node2EdgeViews.computeIfAbsent(a.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                    {
                        final List<WorkflowEdgeView> list = node2EdgeViews.computeIfAbsent(b.getANode(), k -> new ArrayList<>());
                        list.add(edgeView);
                    }
                }
            }
        }

        getEdgeViews().getChildren().addAll(edgeViews);
        getNodeViews().getChildren().addAll(node2NodeView.values());

        workflow.getNodeSelectionModel().getSelectedItems().addListener((ListChangeListener<ANode>) c -> {
            while (c.next()) {
                for (ANode node : c.getAddedSubList()) {
                    if (node2NodeView.containsKey(node))
                        node2NodeView.get(node).setEffect(SelectionEffect.getInstance());
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
    private void assignNodeViewsAndCoordinatesForChildrenRec(WorkflowViewTab workflowView, ANode v, Map<ANode, WorkflowNodeView> node2nodeView, double xDelta, double yDelta, boolean horizontal) {
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

            final WorkflowNodeView nodeView = node2nodeView.computeIfAbsent(w, k -> new WorkflowNodeView(workflowView, w));
            if (horizontal) {
                nodeView.setXY(x + xDelta, y);
            } else {
                nodeView.setXY(x, y + yDelta);
            }
            assignNodeViewsAndCoordinatesForChildrenRec(workflowView, w, node2nodeView, xDelta, yDelta, horizontal);
            count++;
        }
    }

    public Workflow getWorkflow() {
        return document.getWorkflow();
    }

    public Document getDocument() {
        return document;
    }

    public Pane getCenterPane() {
        return centerPane;
    }

    public ZoomableScrollPane getScrollPane() {
        return scrollPane;
    }

    public Group getNodeViews() {
        return nodeViews;
    }

    public Group getEdgeViews() {
        return edgeViews;
    }

    public UndoRedoManager getUndoManager() {
        return undoManager;
    }


    public WorkflowNodeView getNodeView(ANode node) {
        return node2NodeView.get(node);
    }

    Map<ANode, WorkflowNodeView> getNode2NodeView() {
        return node2NodeView;
    }

    Map<ANode, List<WorkflowEdgeView>> getNode2EdgeViews() {
        return node2EdgeViews;
    }

    /**
     * setup menu items and bind their disable properties
     *
     * @param controller
     */
    @Override
    public void updateMenus(MainWindowController controller) {
        final ASelectionModel<ANode> selectionModel = getWorkflow().getNodeSelectionModel();

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
            deletableSelection.remove(getWorkflow().getTopTaxaNode());
            deletableSelection.remove(getWorkflow().getTopDataNode());
            deletableSelection.remove(getWorkflow().getTaxaFilter());
            deletableSelection.remove(getWorkflow().getTopFilter());
            deletableSelection.remove(getWorkflow().getWorkingTaxaNode());
            deletableSelection.remove(getWorkflow().getWorkingDataNode());

            Set<ANode> toDelete = new HashSet<>();
            for (Node node : nodeViews.getChildren()) {
                if (node instanceof WorkflowNodeView) {
                    final ANode aNode = ((WorkflowNodeView) node).getANode();
                    if (aNode instanceof AConnector) {
                        ANode child = ((AConnector) aNode).getChild();
                        if (deletableSelection.contains(child))
                            toDelete.add(aNode);
                    }
                }
            }
            final ArrayList<UndoableRedoableCommand> list = new ArrayList<>();
            toDelete.addAll(deletableSelection);
            for (ANode node : toDelete) {
                if (node2EdgeViews.keySet().contains(node)) {
                    final ArrayList<WorkflowEdgeView> listOfEdgeView = new ArrayList<>(node2EdgeViews.get(node));
                    for (WorkflowEdgeView edgeView : listOfEdgeView) {
                        edgeViews.getChildren().remove(edgeView);
                        list.add(new UndoableRedoableCommand("Delete") {
                            public void undo() {
                                if (!edgeViews.getChildren().contains(edgeView))
                                    edgeViews.getChildren().add(edgeView);
                            }

                            public void redo() {
                                edgeViews.getChildren().remove(edgeView);
                            }
                        });
                    }
                    list.add(new UndoableRedoableCommand("Delete") {
                        public void undo() {
                            node2EdgeViews.put(node, listOfEdgeView);
                        }

                        public void redo() {
                            node2EdgeViews.remove(node);
                        }
                    });
                }
                final WorkflowNodeView nodeView = node2NodeView.get(node);
                final ObservableList<ANode> children = FXCollections.observableArrayList(node.getChildren());
                final ANode parent = getWorkflow().findParent(node);
                list.add(new UndoableRedoableCommand("Delete") {
                    public void undo() {
                        if (!nodeViews.getChildren().contains(nodeView))
                            nodeViews.getChildren().add(nodeView);
                        node2NodeView.put(node, nodeView);
                        getWorkflow().reconnect(parent, node, children);
                    }

                    public void redo() {
                        nodeViews.getChildren().remove(nodeView);
                        node2EdgeViews.remove(node);
                        getWorkflow().delete(node, true, false);
                    }
                });
                {
                    list.add(new UndoableRedoableCommand("Delete") {
                        public void undo() {
                            getWorkflow().updateSelectionModel();
                            getDocument().updateMethodsText();
                        }

                        public void redo() {
                            getWorkflow().updateSelectionModel();
                            getDocument().updateMethodsText();
                        }
                    });
                }
                selectionModel.clearSelection(node);
                // here we actually delete stuff:
                for (UndoableRedoableCommand change : list) {
                    change.redo();
                }
                getUndoManager().add(new UndoableChangeList("Delete", list));
            }
        });
        controller.getDeleteMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        controller.getZoomInMenuItem().setOnAction((e) -> {
            scrollPane.zoomBy(1.1, 1.1);
        });
        controller.getZoomOutMenuItem().setOnAction((e) -> {
            scrollPane.zoomBy(1.0 / 1.1, 1.0 / 1.1);
        });

        controller.getResetMenuItem().setOnAction((e) -> scrollPane.resetZoom());

    }
}