/*
 *  WorkflowViewTab.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.workflowtab;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.fx.control.ZoomableScrollPane;
import jloda.fx.undo.UndoableChangeList;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.Print;
import jloda.fx.util.ResourceManagerFX;
import jloda.fx.util.SelectionEffect;
import splitstree5.core.Document;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowNode;
import splitstree5.gui.ViewerTab;
import splitstree5.menu.MenuController;

import java.util.*;

/**
 * The workflow tab
 * Daniel Huson, 12/31/16.
 */
public class WorkflowViewTab extends ViewerTab {
    private final Document document;
    private final Group nodeViews = new Group();
    private final Group edgeViews = new Group();

    private final Map<WorkflowNode, WorkflowNodeView> node2NodeView = new HashMap<>();
    private final Map<WorkflowNode, ArrayList<WorkflowEdgeView>> node2EdgeViews = new HashMap<>();

    private final Label noDataLabel = new Label("No data - open or import data from a file");

    private final ZoomableScrollPane scrollPane;
    private final Pane centerPane;

    /**
     * constructor
     */
    public WorkflowViewTab(Document document) {
        this.document = document;
        setText("Workflow");
        setGraphic(new ImageView(ResourceManagerFX.getIcon("sun/Preferences16.gif")));
        setClosable(false);

        centerPane = new Pane(new StackPane(edgeViews, nodeViews));

        centerPane.setPadding(new Insets(5, 5, 5, 5));
        //centerPane.setStyle("-fx-border-color: red");
        scrollPane = new ZoomableScrollPane(centerPane);
        scrollPane.setLockAspectRatio(true);
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

        scrollPane.allowZoomProperty().bind(Bindings.size(nodeViews.getChildren()).greaterThan(1));

        getWorkflow().getNodeSelectionModel().getSelectedItems().addListener((ListChangeListener<WorkflowNode>) c -> {
            while (c.next()) {
                for (WorkflowNode node : c.getAddedSubList()) {
                    if (node2NodeView.containsKey(node))
                        node2NodeView.get(node).setEffect(SelectionEffect.getInstance());
                }
                for (WorkflowNode node : c.getRemoved()) {
                    if (node2NodeView.containsKey(node))
                        node2NodeView.get(node).setEffect(null);
                }
            }
        });

        getWorkflow().getTopologyChanged().addListener((c) -> {
            if (getWorkflow().getNumberOfDataNodes() == 0) {
                if (!nodeViews.getChildren().contains(noDataLabel))
                    nodeViews.getChildren().add(noDataLabel);
            } else {
                recompute();
            }
        });

        noDataLabel.setTextFill(Color.DARKGRAY);
    }

    public void clear() {
        getUndoManager().clear();

        getEdgeViews().getChildren().clear();
        getNodeViews().getChildren().clear();
        node2NodeView.clear();
        node2EdgeViews.clear();
    }

    /**
     * clear and then recompute the view
     */
    public void recompute() {
        if (getWorkflow().getWorkingDataNode() == null)
            return;

        getEdgeViews().getChildren().clear();
        getNodeViews().getChildren().clear();
        node2NodeView.clear();
        node2EdgeViews.clear();

        final Workflow workflow = document.getWorkflow();

        if (workflow.getTopTaxaNode() == null)
            return; // no data available

        node2NodeView.put(workflow.getTopTaxaNode(), new WorkflowNodeView(this, workflow.getTopTaxaNode()));
        node2NodeView.put(workflow.getTaxaFilter(), new WorkflowNodeView(this, workflow.getTaxaFilter()));

        node2NodeView.put(workflow.getWorkingTaxaNode(), new WorkflowNodeView(this, workflow.getWorkingTaxaNode()));
        node2NodeView.put(workflow.getTopDataNode(), new WorkflowNodeView(this, workflow.getTopDataNode()));
        node2NodeView.put(workflow.getTopFilter(), new WorkflowNodeView(this, workflow.getTopFilter()));
        node2NodeView.put(workflow.getWorkingDataNode(), new WorkflowNodeView(this, workflow.getWorkingDataNode()));

        final double yDelta = 100;
        final double xDelta = 200;


        node2NodeView.get(workflow.getTopTaxaNode()).setXY(0, -2 * yDelta);
        node2NodeView.get(workflow.getTaxaFilter()).setXY(0, -yDelta);
        node2NodeView.get(workflow.getWorkingTaxaNode()).setXY(0, 0);

        node2NodeView.get(workflow.getTopDataNode()).setXY(xDelta, -2 * yDelta);
        node2NodeView.get(workflow.getTopFilter()).setXY(xDelta, 0);
        node2NodeView.get(workflow.getWorkingDataNode()).setXY(2 * xDelta, 0);

        assignNodeViewsAndCoordinatesForChildrenRec(this, workflow.getWorkingDataNode(), node2NodeView, xDelta, yDelta, 0);

        final ObservableList<WorkflowEdgeView> edgeViews = FXCollections.observableArrayList();

        for (WorkflowNodeView a : node2NodeView.values()) {
            for (WorkflowNodeView b : node2NodeView.values()) {
                if (a.getANode() != null && b.getANode() != null && a.getANode().getChildren().contains(b.getANode())) {
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
    }

    /**
     * recursively
     *
     * @param v
     * @param node2nodeView
     * @param xDelta
     * @param yDelta
     * @param leavesVisited
     */
    private int assignNodeViewsAndCoordinatesForChildrenRec(WorkflowViewTab workflowView, WorkflowNode v, Map<WorkflowNode, WorkflowNodeView> node2nodeView, double xDelta, double yDelta, int leavesVisited) {
        if (v.getChildren().size() == 0) {
            return leavesVisited + 1;
        } else {
            final double x = node2nodeView.get(v).xProperty().get() + xDelta;

            for (WorkflowNode w : v.getChildren()) {
                final WorkflowNodeView nodeView = node2nodeView.computeIfAbsent(w, k -> new WorkflowNodeView(workflowView, w));
                nodeView.setX(x);
                nodeView.setY(leavesVisited * yDelta);
                leavesVisited = assignNodeViewsAndCoordinatesForChildrenRec(workflowView, w, node2nodeView, xDelta, yDelta, leavesVisited);
            }
            return leavesVisited;
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

    public WorkflowNodeView getNodeView(WorkflowNode node) {
        return node2NodeView.get(node);
    }

    Map<WorkflowNode, WorkflowNodeView> getNode2NodeView() {
        return node2NodeView;
    }

    Map<WorkflowNode, ArrayList<WorkflowEdgeView>> getNode2EdgeViews() {
        return node2EdgeViews;
    }

    /**
     * setup menu items and bind their disable properties
     *
     * @param controller
     */
    @Override
    public void updateMenus(MenuController controller) {
        setOnClosed(null);
        setClosable(false);

        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(getMainWindow().getStage()));
        controller.getPrintMenuitem().setOnAction((e) -> Print.print(getMainWindow().getStage(), centerPane));

        final AMultipleSelectionModel<WorkflowNode> selectionModel = getWorkflow().getNodeSelectionModel();

        controller.getUndoMenuItem().setOnAction((e) -> getUndoManager().undo());
        controller.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(getUndoManager().canUndoProperty()));
        controller.getUndoMenuItem().textProperty().bind(getUndoManager().undoNameProperty());
        controller.getRedoMenuItem().setOnAction((e) -> getUndoManager().redo());

        controller.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(getUndoManager().canRedoProperty()));
        controller.getRedoMenuItem().textProperty().bind(getUndoManager().redoNameProperty());

        controller.getCopyImageMenuItem().setOnAction((x) -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            WritableImage writableImage = new WritableImage((int) centerPane.getWidth() + 1,
                    (int) centerPane.getHeight() + 1);
            centerPane.snapshot(null, writableImage);
            content.putImage(writableImage);
            clipboard.setContent(content);
        });
        controller.getCopyImageMenuItem().disableProperty().bind(getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().not());

        controller.getSelectAllMenuItem().setOnAction((e) -> selectionModel.selectAll());
        controller.getSelectAllMenuItem().disableProperty().bind(Bindings.size(selectionModel.getSelectedItems()).isEqualTo(getWorkflow().getNumberOfNodes()));

        controller.getSelectNoneMenuItem().setOnAction((e) -> selectionModel.clearSelection());
        controller.getSelectNoneMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        controller.getSelectAllNodesMenuItem().setOnAction((e) -> selectionModel.selectAll());
        controller.getSelectAllNodesMenuItem().disableProperty().bind(Bindings.size(selectionModel.getSelectedItems()).isEqualTo(getWorkflow().getNumberOfNodes()));

        controller.getDeselectAllNodesMenuItem().setOnAction((e) -> selectionModel.clearSelection());
        controller.getDeselectAllNodesMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        controller.getInvertNodeSelectionMenuItem().setOnAction((e) -> selectionModel.invertSelection());
        controller.getInvertNodeSelectionMenuItem().disableProperty().bind(getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().not());

        controller.getSelectAllBelowMenuItem().setOnAction((e) -> {
            getWorkflow().selectAllBelow(selectionModel.getSelectedItems(), true);
        });
        controller.getSelectAllBelowMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        // delete command:
        controller.getDeleteMenuItem().setOnAction((e) -> {
            controller.getSelectAllBelowMenuItem().fire();

            final Set<WorkflowNode> deletableSelection = new HashSet<>(selectionModel.getSelectedItems());
            final Set<WorkflowNode> toDelete = new HashSet<>();

            {
                deletableSelection.removeAll(getWorkflow().getTopNodes());
                deletableSelection.removeAll(getWorkflow().getWorkingNodes());

                for (Node node : nodeViews.getChildren()) {
                    if (node instanceof WorkflowNodeView) {
                        final WorkflowNode workflowNode = ((WorkflowNodeView) node).getANode();
                        if (workflowNode instanceof Connector) {
                            WorkflowNode child = ((Connector) workflowNode).getChild();
                            if (deletableSelection.contains(child)) {
                                toDelete.add(workflowNode);
                            }
                        }
                    }
                }
            }

            final ArrayList<UndoableRedoableCommand> list = new ArrayList<>();
            toDelete.addAll(deletableSelection);
            for (final WorkflowNode node : toDelete) {
                if (node2EdgeViews.keySet().contains(node)) {
                    final ArrayList<WorkflowEdgeView> listOfEdgeView = new ArrayList<>(node2EdgeViews.get(node));
                    for (final WorkflowEdgeView edgeView : listOfEdgeView) {
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
                list.add(new UndoableRedoableCommand("Delete") {
                    final WorkflowNodeView nodeView = node2NodeView.get(node);
                    final ObservableList<WorkflowNode> children = FXCollections.observableArrayList(node.getChildren());

                    public void undo() {
                        if (nodeView != null) {
                            if (!nodeViews.getChildren().contains(nodeView))
                                nodeViews.getChildren().add(nodeView);
                            node2NodeView.put(node, nodeView);
                        }
                        getWorkflow().reconnect(node.getParent(), node, children);
                    }

                    public void redo() {
                        if (nodeView != null) {
                            nodeViews.getChildren().remove(nodeView);
                            node2NodeView.remove(node);
                        }
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
            }

            selectionModel.clearSelection(toDelete);
            getUndoManager().doAndAdd(new UndoableChangeList("Delete", list));

            /*
            getUndoManager().doAndAdd(new UndoableRedoableCommand("Delete") {
                @Override
                public void undo() {
                    for (WorkflowNode node : toDelete) {
                            for (WorkflowEdgeView edgeView : saveNode2EdgeViews.get(node)) {
                                edgeViews.getChildren().add(edgeView);
                            }
                        final WorkflowNodeView nodeView = saveNode2NodeView.get(node);
                        if (nodeView != null) {
                            nodeViews.getChildren().add(nodeView);
                            if (!nodeViews.getChildren().contains(nodeView))
                                nodeViews.getChildren().add(nodeView);
                            final ObservableList<WorkflowNode> children = FXCollections.observableArrayList(node.getChildren());
                            final WorkflowNode parent = node.getParent();
                            getWorkflow().reconnect(parent, node, children);
                        }
                    }
                }

                @Override
                public void redo() {
                    for (WorkflowNode node : toDelete) {
                            for (WorkflowEdgeView edgeView : saveNode2EdgeViews.get(node)) {
                                edgeViews.getChildren().remove(edgeView);
                            }
                        if(saveNode2NodeView.get(node)!=null)
                            nodeViews.getChildren().remove(saveNode2NodeView.get(node));
                        getWorkflow().delete(node, true, false);
                    }
                    selectionModel.clearSelection(toDelete);
                }
            });
            */
        });
        controller.getDeleteMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        controller.getDuplicateMenuItem().setOnAction((e) -> callDuplicateCommand());
        controller.getDuplicateMenuItem().disableProperty().bind(selectionModel.emptyProperty());

        controller.getZoomInMenuItem().setOnAction((e) -> {
            scrollPane.zoomBy(1.1, 1.1);
        });
        controller.getZoomInMenuItem().disableProperty().bind(getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().not());

        controller.getZoomOutMenuItem().setOnAction((e) -> {
            scrollPane.zoomBy(1.0 / 1.1, 1.0 / 1.1);
        });
        controller.getZoomOutMenuItem().disableProperty().bind(getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().not());

        controller.getResetMenuItem().setOnAction((e) -> scrollPane.resetZoom());
        controller.getResetMenuItem().disableProperty().bind(getWorkflow().hasWorkingTaxonNodeForFXThreadProperty().not());
    }

    /**
     * duplicate all selected nodes
     */
    public void callDuplicateCommand() {
        getUndoManager().doAndAdd(new UndoableRedoableCommand("Duplicate") {
            final ArrayList<WorkflowNode> selected = new ArrayList<>(getWorkflow().getNodeSelectionModel().getSelectedItems());
            final Collection<WorkflowNode> newNodes = new ArrayList<>();

            @Override
            public void undo() {
                getWorkflow().delete(newNodes);
                newNodes.clear();
                recompute();
            }

            @Override
            public void redo() {
                newNodes.clear();
                newNodes.addAll(getWorkflow().duplicate(selected));
                recompute();
                getWorkflow().recomputeTop(newNodes);
            }

            @Override
            public boolean isUndoable() {
                return newNodes.size() > 0;
            }

            @Override
            public boolean isRedoable() {
                return selected.size() > 0;
            }
        });
    }
}