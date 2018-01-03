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
package splitstree5.main.graphtab.base;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import jloda.util.Single;
import splitstree5.core.project.ProjectManager;
import splitstree5.main.ISavesPreviousSelection;
import splitstree5.main.MainWindowController;
import splitstree5.main.ViewerTab;
import splitstree5.undo.UndoRedoManager;
import splitstree5.undo.UndoableRedoableCommand;
import splitstree5.utils.RubberBandSelection;
import splitstree5.utils.SelectionEffect;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * tree and split network tab base class
 * Daniel Huson,. 12.2017
 */
public abstract class GraphTab extends ViewerTab implements ISavesPreviousSelection {
    protected final Group group = new Group();
    protected final Group edgesGroup = new Group();
    protected final Group nodesGroup = new Group();
    protected final Group edgeLabelsGroup = new Group();
    protected final Group nodeLabelsGroup = new Group();
    protected final ASelectionModel<Node> nodeSelectionModel = new ASelectionModel<>();
    protected final ASelectionModel<Edge> edgeSelectionModel = new ASelectionModel<>();
    protected final BorderPane rootNode = new BorderPane();
    private PhyloGraph phyloGraph;
    protected NodeArray<ANodeView> node2view;
    protected EdgeArray<AEdgeView> edge2view;

    private final StackPane pane = new StackPane();

    private DoubleProperty scaleChangeX = new SimpleDoubleProperty(1); // keep track of scale changes, used for reset
    private DoubleProperty scaleChangeY = new SimpleDoubleProperty(1);
    private DoubleProperty angleChange = new SimpleDoubleProperty(0);

    private final StringProperty title = new SimpleStringProperty("");
    private ObjectProperty<GraphLayout> layout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);

    private final RubberBandSelection rubberBandSelection;

    /**
     * constructor
     */
    public GraphTab() {
        setContent(pane);
        // pane.setStyle("-fx-border-color: red");

        nodeSelectionModel.getSelectedItems().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                for (Node v : c.getAddedSubList()) {
                    if (v.getOwner() != null) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv != null) {
                            if (nv.getLabel() != null)
                                nv.getLabel().setEffect(SelectionEffect.getInstance());
                            if (nv.getShape() != null)
                                nv.getShape().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Node v : c.getRemoved()) {
                    if (v.getOwner() != null) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv != null) {
                            if (nv.getLabel() != null)
                                nv.getLabel().setEffect(null);
                            if (nv.getShape() != null)
                                nv.getShape().setEffect(null);
                        }
                    }
                }
            }
        });
        edgeSelectionModel.getSelectedItems().addListener((ListChangeListener<Edge>) c -> {
            while (c.next()) {
                for (Edge e : c.getAddedSubList()) {
                    if (e.getOwner() != null) {
                        final AEdgeView ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            if (ev.getLabel() != null)
                                ev.getLabel().setEffect(SelectionEffect.getInstance());
                            if (ev.getShape() != null)
                                ev.getShape().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Edge e : c.getRemoved()) {
                    if (e.getOwner() != null) {
                        final AEdgeView ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            if (ev.getLabel() != null)
                                ev.getLabel().setEffect(null);
                            if (ev.getShape() != null)
                                ev.getShape().setEffect(null);
                        }
                    }
                }
            }
        });

        rubberBandSelection = new RubberBandSelection(pane, group, createRubberBandSelectionHandler());

    }

    public int size() {
        return edgesGroup.getChildren().size() + nodesGroup.getChildren().size();
    }

    public Group getGroup() {
        return group;
    }

    public Group getEdgesGroup() {
        return edgesGroup;
    }

    public Group getNodesGroup() {
        return nodesGroup;
    }

    public Group getEdgeLabelsGroup() {
        return edgeLabelsGroup;
    }

    public Group getNodeLabelsGroup() {
        return nodeLabelsGroup;
    }

    public ASelectionModel<Node> getNodeSelectionModel() {
        return nodeSelectionModel;
    }

    public ASelectionModel<Edge> getEdgeSelectionModel() {
        return edgeSelectionModel;
    }

    public void updateSelectionModels(PhyloGraph graph) {
        nodeSelectionModel.setItems(graph.getNodesAsSet().toArray(new Node[graph.getNumberOfNodes()]));
        edgeSelectionModel.setItems(graph.getEdgesAsSet().toArray(new Edge[graph.getNumberOfEdges()]));
    }

    public NodeArray<ANodeView> getNode2view() {
        return node2view;
    }

    public EdgeArray<AEdgeView> getEdge2view() {
        return edge2view;
    }

    public Dimension2D getTargetDimensions() {
        return new Dimension2D(0.6 * pane.getWidth(), 0.9 * pane.getHeight());
    }

    public PhyloGraph getPhyloGraph() {
        return phyloGraph;
    }

    /**
     * initialize data structures
     *
     * @param phyloGraph
     */
    public void init(PhyloGraph phyloGraph) {
        this.phyloGraph = phyloGraph;
        node2view = new NodeArray<>(phyloGraph);
        edge2view = new EdgeArray<>(phyloGraph);
        Platform.runLater(() -> {
            group.setScaleX(1);
            group.setScaleY(1);
            scaleChangeX.set(1);
            scaleChangeY.set(1);
            angleChange.set(0);
        });
    }

    /**
     * show the viewer
     */
    public void show() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                if (pane.getChildren().size() == 0) {
                    final Group world = new Group();
                    world.getChildren().add(group);
                    pane.getChildren().add(world);

                    final ScrollPane scrollPane = new ScrollPane(pane);
                    pane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                            scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
                    pane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                            scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

                    pane.setOnMouseClicked((e) -> {
                        if (!e.isShiftDown()) {
                            nodeSelectionModel.clearSelection();
                            edgeSelectionModel.clearSelection();
                        }
                    });

                    pane.setOnScroll(event -> {
                        event.consume();
                        final double SCALE_DELTA = 1.1;

                        if (getLayout() == GraphLayout.Radial) {
                            final double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
                            scale(scaleFactor, scaleFactor);
                        } else {
                            if (Math.abs(event.getDeltaY()) > Math.abs(event.getDeltaX())) {
                                final double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
                                scale(1, scaleFactor);
                            } else {
                                final double scaleFactor = (event.getDeltaX() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA;
                                scale(scaleFactor, 1);
                            }
                        }
                    });

                    rootNode.setCenter(pane);
                }

                group.getChildren().clear();
                group.getChildren().addAll(edgesGroup.getChildren());
                group.getChildren().addAll(nodesGroup.getChildren());
                group.getChildren().addAll(edgeLabelsGroup.getChildren());
                group.getChildren().addAll(nodeLabelsGroup.getChildren());

                // empty all of these for the next computation
                edgesGroup.getChildren().clear();
                nodesGroup.getChildren().clear();
                edgeLabelsGroup.getChildren().clear();
                nodeLabelsGroup.getChildren().clear();
                nodeSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
            } finally {
                countDownLatch.countDown();
                (new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(this::layoutLabels);

                })).start();
            }
            if (!(getContent() instanceof ScrollPane)) {
                ScrollPane scrollPane = new ScrollPane(pane);
                setContent(scrollPane);

                pane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));

                pane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()));
            }
        });
    }

    public GraphLayout getLayout() {
        return layout.get();
    }

    public void setLayout(GraphLayout layout) {
        this.layout.set(layout);
    }

    public void layoutLabels() {
        if (getPhyloGraph() != null) {
            if (getLayout() == GraphLayout.Radial)
                NodeLabelLayouter.radialLayout(getPhyloGraph(), getNode2view(), getEdge2view());
            else {
                if (getPhyloGraph() instanceof PhyloTree) {
                    NodeLabelLayouter.leftToRightLayout(getPhyloGraph(), ((PhyloTree) getPhyloGraph()).getRoot(), getNode2view(), getEdge2view());
                }
            }
        }
    }

    /**
     * change scale by the given factors
     *
     * @param xFactor
     * @param yFactor
     */
    public void scale(double xFactor, double yFactor) {
        scaleChangeX.set(scaleChangeX.get() * xFactor);
        scaleChangeY.set(scaleChangeY.get() * yFactor);

        for (ANodeView nodeView : getNode2view()) {
            nodeView.scaleCoordinates(xFactor, yFactor);
        }
        for (AEdgeView edgeView : getEdge2view()) {
            edgeView.scaleCoordinates(xFactor, yFactor);
        }
    }

    /**
     * rotate by given angle
     *
     * @param angle
     */
    public void rotate(double angle) {
        angleChange.set(angleChange.get() + angle);
        for (ANodeView nodeView : getNode2view()) {
            nodeView.rotateCoordinates(angle);
        }
        for (AEdgeView edgeView : getEdge2view()) {
            edgeView.rotateCoordinates(angle);
        }
    }


    /**
     * select nodes and edges by labels
     *
     * @param set
     */
    public void selectByLabel(Set<String> set) {
        for (Node node : getPhyloGraph().nodes()) {
            String label = getPhyloGraph().getLabel(node);
            if (label != null && set.contains(label))
                nodeSelectionModel.select(node);
        }
        for (Edge edge : getPhyloGraph().edges()) {
            String label = getPhyloGraph().getLabel(edge);
            if (label != null && set.contains(label))
                edgeSelectionModel.select(edge);
        }
    }

    public void saveAsPreviousSelection() {
        if (nodeSelectionModel.getSelectedItems().size() > 0 || edgeSelectionModel.getSelectedItems().size() > 0) {
            ProjectManager.getInstance().getPreviousSelection().clear();
            if (nodeSelectionModel.getSelectedItems().size() > 0) {
                for (Node node : nodeSelectionModel.getSelectedItems()) {
                    final String label = getPhyloGraph().getLabel(node);
                    if (label != null)
                        ProjectManager.getInstance().getPreviousSelection().add(label);
                }
            }
            if (edgeSelectionModel.getSelectedItems().size() > 0) {
                for (Edge edge : edgeSelectionModel.getSelectedItems()) {
                    final String label = getPhyloGraph().getLabel(edge);
                    if (label != null)
                        ProjectManager.getInstance().getPreviousSelection().add(label);
                }
            }
        }
    }

    public void addNodeLabelMovementSupport(ANodeView nodeView) {
        final javafx.scene.Node label = nodeView.getLabel();
        if (label != null) {
            final Single<Point2D> oldLocation = new Single<>();
            final Single<Point2D> point = new Single<>();
            label.setOnMousePressed((e) -> {
                if (nodeSelectionModel.getSelectedItems().contains(nodeView.getNode())) {
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
                    oldLocation.set(new Point2D(label.getLayoutX(), label.getLayoutY()));
                }
                e.consume();
            });
            label.setOnMouseDragged((e) -> {
                if (point.get() != null) {
                    double deltaX = e.getScreenX() - point.get().getX();
                    double deltaY = e.getScreenY() - point.get().getY();
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
                    if (deltaX != 0)
                        label.setLayoutX(label.getLayoutX() + deltaX);
                    if (deltaY != 0)
                        label.setLayoutY(label.getLayoutY() + deltaY);
                    e.consume();
                }
            });
            label.setOnMouseReleased((e) -> {
                if (oldLocation.get() != null) {
                    final Point2D newLocation = new Point2D(label.getLayoutX(), label.getLayoutY());
                    if (!newLocation.equals(oldLocation.get())) {
                        undoRedoManager.add(new UndoableRedoableCommand("Move Label") {
                            private Node v = nodeView.getNode();
                            double oldX = oldLocation.get().getX();
                            double oldY = oldLocation.get().getY();
                            double newX = newLocation.getX();
                            double newY = newLocation.getY();

                            @Override
                            public void undo() {
                                label.setLayoutX(oldX);
                                label.setLayoutY(oldY);
                            }

                            @Override
                            public void redo() {
                                label.setLayoutX(newX);
                                label.setLayoutY(newY);
                            }

                            @Override
                            public boolean isUndoable() {
                                return getNode2view().get(v) == nodeView; // still contained in graph
                            }

                            @Override
                            public boolean isRedoable() {
                                return getNode2view().get(v) == nodeView; // still contained in graph
                            }
                        });
                    }
                }
                point.set(null);
                e.consume();
            });
        }
    }

    private RubberBandSelection.Handler createRubberBandSelectionHandler() {
        return (rectangle, extendSelection) -> {
            if (!extendSelection) {
                nodeSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
            }
            final Set<Node> previouslySelectedNodes = new HashSet<>(nodeSelectionModel.getSelectedItems());
            final Set<Edge> previouslySelectedEdges = new HashSet<>(edgeSelectionModel.getSelectedItems());

            for (Node node : phyloGraph.nodes()) {
                final ANodeView nodeView = node2view.get(node);
                if (nodeView.getShape() != null) {
                    final Bounds bounds = nodeView.getShape().localToScene(nodeView.getShape().getBoundsInLocal());
                    if (rectangle.contains(bounds.getMinX(), bounds.getMinY()) && rectangle.contains(bounds.getMaxX(), bounds.getMaxY())) {
                        if (previouslySelectedNodes.contains(node))
                            nodeSelectionModel.clearSelection(node);
                        else
                            nodeSelectionModel.select(node);
                    }
                }
                if (nodeView.getLabel() != null) {
                    final Bounds bounds = nodeView.getLabel().localToScene(nodeView.getLabel().getBoundsInLocal());
                    if (rectangle.contains(bounds.getMinX(), bounds.getMinY()) && rectangle.contains(bounds.getMaxX(), bounds.getMaxY())) {
                        if (previouslySelectedNodes.contains(node))
                            nodeSelectionModel.clearSelection(node);
                        else
                            nodeSelectionModel.select(node);
                    }
                }
                for (Edge edge : phyloGraph.edges()) {
                    final AEdgeView edgeView = edge2view.get(edge);
                    if (edgeView.getShape() != null) {
                        final Bounds bounds = edgeView.getShape().localToScene(edgeView.getShape().getBoundsInLocal());
                        if (rectangle.contains(bounds.getMinX(), bounds.getMinY()) && rectangle.contains(bounds.getMaxX(), bounds.getMaxY())) {
                            if (previouslySelectedEdges.contains(edge))
                                edgeSelectionModel.clearSelection(edge);
                            else
                                edgeSelectionModel.select(edge);
                        }
                    }
                    if (edgeView.getLabel() != null) {
                        final Bounds bounds = edgeView.getLabel().localToScene(edgeView.getLabel().getBoundsInLocal());
                        if (rectangle.contains(bounds.getMinX(), bounds.getMinY()) && rectangle.contains(bounds.getMaxX(), bounds.getMaxY())) {
                            if (previouslySelectedEdges.contains(edge))
                                edgeSelectionModel.clearSelection(edge);
                            else
                                edgeSelectionModel.select(edge);
                        }
                    }
                }
            }
        };
    }

    public UndoRedoManager getUndoRedoManager() {
        return undoRedoManager;
    }

    @Override
    public void updateMenus(MainWindowController controller) {
        controller.getUndoMenuItem().setOnAction((e) -> {
            undoRedoManager.undo();
        });
        controller.getUndoMenuItem().disableProperty().bind(undoRedoManager.canUndoProperty().not());
        controller.getUndoMenuItem().textProperty().bind(undoRedoManager.undoNameProperty());

        controller.getRedoMenuItem().setOnAction((e) -> {
            undoRedoManager.redo();
        });
        controller.getRedoMenuItem().disableProperty().bind(undoRedoManager.canRedoProperty().not());
        controller.getRedoMenuItem().textProperty().bind(undoRedoManager.redoNameProperty());

        controller.getSelectAllMenuItem().setOnAction((e) -> {
            nodeSelectionModel.selectAll();
            edgeSelectionModel.selectAll();
        });
        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            nodeSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();
        });
        controller.getSelectAllNodesMenuItem().setOnAction((e) -> nodeSelectionModel.selectAll());
        controller.getSelectAllEdgeMenuItem().setOnAction((e) -> edgeSelectionModel.selectAll());

        controller.getSelectAllLabeledNodesMenuItem().setOnAction((e) -> {
            for (Node v : getPhyloGraph().nodes()) {
                if (getPhyloGraph().getLabel(v) != null && getPhyloGraph().getLabel(v).length() > 0)
                    nodeSelectionModel.select(v);
            }
        });

        controller.getSelectFromPreviousMenuItem().setOnAction((e) -> {
            selectByLabel(ProjectManager.getInstance().getPreviousSelection());

        });
        controller.getSelectFromPreviousMenuItem().disableProperty().bind(Bindings.isEmpty(ProjectManager.getInstance().getPreviousSelection()));

        controller.getZoomInMenuItem().setOnAction((e) -> scale(1.1, 1.1));
        controller.getZoomOutMenuItem().setOnAction((e) -> scale(1 / 1.1, 1 / 1.1));

        controller.getRotateLeftMenuItem().setOnAction((e) -> rotate(-10));
        controller.getRotateLeftMenuItem().disableProperty().bind(layout.isNotEqualTo(GraphLayout.Radial));
        controller.getRotateRightMenuItem().setOnAction((e) -> rotate(10));
        controller.getRotateRightMenuItem().disableProperty().bind(layout.isNotEqualTo(GraphLayout.Radial));


        controller.getResetMenuItem().setOnAction((e) -> {
            scale(1 / scaleChangeX.get(), 1 / scaleChangeY.get());
            rotate(-angleChange.get());
            layoutLabels();
        });
        controller.getResetMenuItem().disableProperty().bind(scaleChangeX.isEqualTo(1).and(scaleChangeY.isEqualTo(1)).and(angleChange.isEqualTo(0)));

        controller.getIncreaseFontSizeMenuItem().setOnAction((x) -> {
            boolean hasSelected = nodeSelectionModel.getSelectedItems().size() > 0 || edgeSelectionModel.getSelectedItems().size() > 0;
            for (Node v : (hasSelected ? nodeSelectionModel.getSelectedItems() : phyloGraph.nodes())) {
                javafx.scene.Node label = node2view.get(v).getLabel();
                if (label instanceof Labeled) {
                    Labeled labeled = (Labeled) label;
                    labeled.setStyle("-fx-font-size: " + (labeled.getFont().getSize() + 2) + ";");
                }
            }
            for (Edge e : (hasSelected ? edgeSelectionModel.getSelectedItems() : phyloGraph.edges())) {
                javafx.scene.Node label = edge2view.get(e).getLabel();
                if (label instanceof Labeled) {
                    Labeled labeled = (Labeled) label;
                    labeled.setStyle("-fx-font-size: " + (labeled.getFont().getSize() + 2) + ";");
                }
            }
        });
        controller.getDecreaseFontSizeMenuItem().setOnAction((x) -> {
            boolean hasSelected = nodeSelectionModel.getSelectedItems().size() > 0 || edgeSelectionModel.getSelectedItems().size() > 0;
            for (Node v : (hasSelected ? nodeSelectionModel.getSelectedItems() : phyloGraph.nodes())) {
                javafx.scene.Node label = node2view.get(v).getLabel();
                if (label instanceof Labeled) {
                    Labeled labeled = (Labeled) label;
                    if (labeled.getFont().getSize() > 2)
                        labeled.setStyle("-fx-font-size: " + (labeled.getFont().getSize() - 2) + ";");
                }
            }
            for (Edge e : (hasSelected ? edgeSelectionModel.getSelectedItems() : phyloGraph.edges())) {
                javafx.scene.Node label = edge2view.get(e).getLabel();
                if (label instanceof Labeled) {
                    Labeled labeled = (Labeled) label;
                    if (labeled.getFont().getSize() > 2)
                        labeled.setStyle("-fx-font-size: " + (labeled.getFont().getSize() - 2) + ";");
                }
            }
        });

        controller.getLayoutLabelsMenuItem().setOnAction((e) -> layoutLabels());
    }

}
