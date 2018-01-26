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
package splitstree5.gui.graphtab.base;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import jloda.find.FindToolBar;
import jloda.find.NodeLabelSearcher;
import jloda.fx.ASelectionModel;
import jloda.fx.ZoomableScrollPane;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import jloda.util.ProgramProperties;
import jloda.util.Single;
import splitstree5.core.Document;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.gui.ISavesPreviousSelection;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.formattab.FontSizeIncrementCommand;
import splitstree5.gui.formattab.FormatItem;
import splitstree5.gui.graphtab.commands.LayoutLabelsCommand;
import splitstree5.gui.graphtab.commands.MoveLabelCommand;
import splitstree5.gui.graphtab.commands.RotateCommand;
import splitstree5.gui.graphtab.commands.ZoomCommand;
import splitstree5.gui.utils.RubberBandSelection;
import splitstree5.gui.utils.SelectionEffect;
import splitstree5.main.MainWindowManager;
import splitstree5.menu.MenuController;
import splitstree5.undo.CompositeCommand;
import splitstree5.utils.Print;

import java.util.*;
import java.util.concurrent.Executors;

/**
 * tree and split network tab base class
 * Daniel Huson,. 12.2017
 */
public abstract class GraphTab<G extends PhyloGraph> extends ViewerTab implements ISavesPreviousSelection {
    protected ZoomableScrollPane scrollPane;

    protected final Group group = new Group();
    protected final Group edgesGroup = new Group();
    protected final Group nodesGroup = new Group();
    protected final Group edgeLabelsGroup = new Group();
    protected final Group nodeLabelsGroup = new Group();
    protected final ASelectionModel<Node> nodeSelectionModel = new ASelectionModel<>();
    protected final ASelectionModel<Edge> edgeSelectionModel = new ASelectionModel<>();

    protected final BorderPane rootNode = new BorderPane();
    protected final Label label = new Label("GraphTab");
    private G graph;

    private final NodeLabelSearcher nodeLabelSearcher;

    protected NodeArray<ANodeView> node2view;
    protected EdgeArray<AEdgeView> edge2view;

    private final StackPane centerPane = new StackPane();

    private final BooleanProperty sparseLabels = new SimpleBooleanProperty(false);

    private DoubleProperty scaleChangeX = new SimpleDoubleProperty(1); // keep track of scale changes, used for reset
    private DoubleProperty scaleChangeY = new SimpleDoubleProperty(1);
    private DoubleProperty angleChange = new SimpleDoubleProperty(0);

    private ObjectProperty<GraphLayout> layout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);

    private final RubberBandSelection rubberBandSelection;

    private Map<String, FormatItem> nodeLabel2Style;

    private ListChangeListener<Node> nodeSelectionChangeListener;
    private ListChangeListener<Taxon> documentTaxonSelectionChangeListener;

    /**
     * constructor
     */
    public GraphTab() {
        setContent(centerPane); // first need to set this here and then later set to center of root node...

        // setup find / replace tool bar:
        {
            nodeLabelSearcher = new NodeLabelSearcher(graph, nodeSelectionModel);
            nodeLabelSearcher.addLabelChangedListener(v -> Platform.runLater(() -> {
                ANodeView nv = node2view.get(v);
                if (nv.getLabel() != null)
                    nv.getLabel().setText(graph.getLabel(v));
                else {
                    Label label = new Label(graph.getLabel(v));
                    label.setFont(ProgramProperties.getDefaultFont());
                    nv.setLabel(label);
                }
            }));
            findToolBar = new FindToolBar(nodeLabelSearcher);
            //findToolBar.setShowReplaceToolBar(true);
            rootNode.setTop(findToolBar);
        }

        // centerPane.setStyle("-fx-border-color: red");

        nodeSelectionModel.getSelectedItems().addListener((ListChangeListener<Node>) c -> {
            while (c.next()) {
                for (Node v : c.getAddedSubList()) {
                    if (v.getOwner() == getGraph()) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv != null) {
                            if (nv.getLabel() != null) {
                                nv.getLabel().setEffect(SelectionEffect.getInstance());
                            }
                            nv.getShapeGroup().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Node v : c.getRemoved()) {
                    if (v.getOwner() == getGraph()) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv != null) {
                            if (nv.getLabel() != null) {
                                nv.getLabel().setEffect(null);
                            }
                            nv.getShapeGroup().setEffect(null);
                        }
                    }
                }
            }
        });
        edgeSelectionModel.getSelectedItems().addListener((ListChangeListener<Edge>) c -> {
            while (c.next()) {
                for (Edge e : c.getAddedSubList()) {
                    if (e.getOwner() == getGraph()) {
                        final AEdgeView ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            if (ev.getLabel() != null) {
                                ev.getLabel().setEffect(SelectionEffect.getInstance());
                            }
                            if (ev.getShape() != null)
                                ev.getShape().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Edge e : c.getRemoved()) {
                    if (e.getOwner() == getGraph()) {
                        final AEdgeView ev = getEdge2view().getValue(e);
                        if (ev != null) {
                            if (ev.getLabel() != null) {
                                ev.getLabel().setEffect(null);
                            }
                            if (ev.getShape() != null)
                                ev.getShape().setEffect(null);
                        }
                    }
                }
            }
        });

        rubberBandSelection = new RubberBandSelection(centerPane, group, createRubberBandSelectionHandler());
        setClosable(false);
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

    public void updateSelectionModels(G graph, TaxaBlock taxaBlock, Document document) {
        try {
            nodeSelectionModel.setSuspendListeners(true);
            edgeSelectionModel.setSuspendListeners(true);
            nodeSelectionModel.setItems(graph.getNodesAsSet().toArray(new Node[graph.getNumberOfNodes()]));
            edgeSelectionModel.setItems(graph.getEdgesAsSet().toArray(new Edge[graph.getNumberOfEdges()]));
        } finally {
            nodeSelectionModel.setSuspendListeners(false);
            edgeSelectionModel.setSuspendListeners(false);
        }

        nodeSelectionChangeListener = c -> {
            while (c.next()) {
                for (Node node : c.getAddedSubList()) {
                    if (node.getOwner() == graph) {
                        String name = graph.getLabel(node);
                        if (name != null) {
                            Taxon taxon = taxaBlock.get(name);
                            if (taxon != null)
                                document.getTaxaSelectionModel().select(taxon);
                        }
                    }
                }
                for (Node node : c.getRemoved()) {
                    if (node.getOwner() == graph) {
                        String name = graph.getLabel(node);
                        if (name != null) {
                            Taxon taxon = taxaBlock.get(name);
                            if (taxon != null)
                                document.getTaxaSelectionModel().clearSelection(taxon);
                        }
                    }
                }
            }
        };
        nodeSelectionModel.getSelectedItems().addListener(new WeakListChangeListener<>(nodeSelectionChangeListener));

        documentTaxonSelectionChangeListener = c -> {
            while (c.next()) {
                for (Taxon taxon : c.getAddedSubList()) {
                    String label = taxon.getName();
                    for (Node v : this.graph.nodes()) {
                        if (this.graph.getLabel(v) != null && label.equals(this.graph.getLabel(v))) {
                            nodeSelectionModel.select(v);
                        }
                    }
                }
                for (Taxon taxon : c.getRemoved()) {
                    String label = taxon.getName();
                    for (Node v : this.graph.nodes()) {
                        if (this.graph.getLabel(v) != null && label.equals(this.graph.getLabel(v)) && nodeSelectionModel.getSelectedItems().contains(v)) {
                            nodeSelectionModel.clearSelection(v);
                        }
                    }
                }
            }
        };
        document.getTaxaSelectionModel().getSelectedItems().addListener(new WeakListChangeListener<>(documentTaxonSelectionChangeListener));

        if (!document.getTaxaSelectionModel().isEmpty()) {
            final Set<String> selectedNames = new HashSet<>();
            for (Taxon taxon : document.getTaxaSelectionModel().getSelectedItems()) {
                selectedNames.add(taxon.getName());
            }
            for (Node v : graph.nodes()) {
                if (graph.getLabel(v) != null && selectedNames.contains(graph.getLabel(v))) {
                    nodeSelectionModel.select(v);
                }
            }
        }
    }

    public NodeArray<ANodeView> getNode2view() {
        return node2view;
    }

    public EdgeArray<AEdgeView> getEdge2view() {
        return edge2view;
    }

    public Dimension2D getTargetDimensions() {
        return new Dimension2D(0.6 * centerPane.getWidth(), 0.9 * centerPane.getHeight());
    }

    public G getGraph() {
        return graph;
    }

    /**
     * initialize data structures
     *
     * @param phyloGraph
     */
    public void init(G phyloGraph) {
        this.graph = phyloGraph;
        node2view = new NodeArray<>(phyloGraph);
        edge2view = new EdgeArray<>(phyloGraph);
        nodeLabelSearcher.setGraph(graph);
        Platform.runLater(() -> {
            group.setScaleX(1);
            group.setScaleY(1);
            scaleChangeX.set(1);
            scaleChangeY.set(1);
            angleChange.set(0);
        });
        Platform.runLater(() -> getUndoManager().clear());
    }

    public void setName(String name) {
        label.setText(name);
    }

    public String getName() {
        return label.getText();
    }

    /**
     * show the viewer
     */
    public void show() {
        Platform.runLater(() -> {
            try {
                if (centerPane.getChildren().size() == 0) {
                    final Group world = new Group();
                    world.getChildren().add(group);
                    centerPane.getChildren().add(world);

                    centerPane.setOnMouseClicked((e) -> {
                        if (!e.isShiftDown()) {
                            nodeSelectionModel.clearSelection();
                            edgeSelectionModel.clearSelection();
                        }
                    });
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
                Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    Platform.runLater(() -> layoutLabels(sparseLabels.get()));
                });
            }
            if (!(rootNode.getCenter() instanceof ScrollPane)) {
                setContent(rootNode);
                scrollPane = new ZoomableScrollPane(centerPane) {
                    @Override // override node scaling to use coordinate scaling
                    public void updateScale() {
                        if (layout.get() == GraphLayout.Radial) {
                            getUndoManager().doAndAdd(new ZoomCommand(getZoomFactorY(), getZoomFactorY(), GraphTab.this));
                        } else {
                            getUndoManager().doAndAdd(new ZoomCommand(getZoomFactorX(), getZoomFactorY(), GraphTab.this));
                        }
                    }
                };
                scrollPane.lockAspectRatioProperty().bind(layout.isEqualTo(GraphLayout.Radial));
                centerPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
                centerPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                        scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

                rootNode.setCenter(scrollPane);

                /* this works once window is open, but first time around...
                scrollPane.layout();
                System.err.print("Center: "+scrollPane.getVvalue());
                scrollPane.setVvalue(0.5);
                System.err.println(" -> "+scrollPane.getVvalue());

                scrollPane.setHvalue(0.5);
                */


            }
        });
    }

    public GraphLayout getLayout() {
        return layout.get();
    }

    public void setLayout(GraphLayout layout) {
        this.layout.set(layout);
    }

    public void layoutLabels(boolean sparseLabels) {
        if (getGraph() != null) {
            if (getLayout() == GraphLayout.Radial)
                NodeLabelLayouter.radialLayout(sparseLabels, getGraph(), getNode2view(), getEdge2view());
            else {
                if (getGraph() instanceof PhyloTree) {
                    NodeLabelLayouter.leftToRightLayout(sparseLabels, getGraph(), ((PhyloTree) getGraph()).getRoot(), getNode2view(), getEdge2view());
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
     * select nodes  labels
     *
     * @param set
     */
    public void selectNodesByLabel(Collection<String> set, boolean select) {
        for (Node node : getGraph().nodes()) {
            String label = getGraph().getLabel(node);
            if (label != null && set.contains(label))
                if (select)
                    nodeSelectionModel.select(node);
                else
                    nodeSelectionModel.clearSelection(node);
        }
    }

    /**
     * select nodes and edges by labels
     *
     * @param set
     */
    public void selectByLabel(Collection<String> set) {
        for (Node node : getGraph().nodes()) {
            String label = getGraph().getLabel(node);
            if (label != null && set.contains(label))
                nodeSelectionModel.select(node);
        }
        for (Edge edge : getGraph().edges()) {
            String label = getGraph().getLabel(edge);
            if (label != null && set.contains(label))
                edgeSelectionModel.select(edge);
        }
    }

    public void saveAsPreviousSelection() {
        MainWindowManager.getInstance().getPreviousSelection().clear();
        if (nodeSelectionModel.getSelectedItems().size() > 0) {
            for (Node node : nodeSelectionModel.getSelectedItems()) {
                if (node.getOwner() == getGraph()) {
                    final String label = getGraph().getLabel(node);
                    if (label != null)
                        MainWindowManager.getInstance().getPreviousSelection().add(label);
                }
            }
        }
        if (edgeSelectionModel.getSelectedItems().size() > 0) {
            for (Edge edge : edgeSelectionModel.getSelectedItems()) {
                if (edge.getOwner() == getGraph()) {
                    final String label = getGraph().getLabel(edge);
                    if (label != null)
                        MainWindowManager.getInstance().getPreviousSelection().add(label);
                }
            }
        }
    }

    public void addNodeLabelMovementSupport(ANodeView nodeView) {
        final Labeled label = nodeView.getLabel();
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
                        getUndoManager().doAndAdd(new MoveLabelCommand(label, oldLocation.get(), newLocation, nodeView));
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

            for (Node node : graph.nodes()) {
                final ANodeView nodeView = node2view.get(node);
                {
                    final Bounds bounds = nodeView.getShapeGroup().localToScene(nodeView.getShapeGroup().getBoundsInLocal());
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
                for (Edge edge : graph.edges()) {
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

    @Override
    public void updateMenus(MenuController controller) {
        controller.getPageSetupMenuItem().setOnAction((e) -> Print.showPageLayout(getMainWindow().getStage()));
        controller.getPrintMenuitem().setOnAction((e) -> Print.print(getMainWindow().getStage(), centerPane));

        if (getUndoManager() != null) {
            controller.getUndoMenuItem().setOnAction((e) -> {
                getUndoManager().undo();
            });
            controller.getUndoMenuItem().disableProperty().bind(getUndoManager().canUndoProperty().not());
            controller.getUndoMenuItem().textProperty().bind(getUndoManager().undoNameProperty());

            controller.getRedoMenuItem().setOnAction((e) -> {
                getUndoManager().redo();
            });
            controller.getRedoMenuItem().disableProperty().bind(getUndoManager().canRedoProperty().not());
            controller.getRedoMenuItem().textProperty().bind(getUndoManager().redoNameProperty());
        }

        controller.getSelectAllMenuItem().setOnAction((e) -> {
            nodeSelectionModel.selectAll();
            edgeSelectionModel.selectAll();
        });
        // controller.getSelectAllMenuItem().disableProperty().bind(Bindings.size(nodeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfNodes())
        // .and(Bindings.size(edgeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfEdges()))); // todo: breaks if number of nodes or edges changes...

        controller.getSelectNoneMenuItem().setOnAction((e) -> {
            nodeSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();
        });

        if (graph != null) {
            controller.getSelectAllNodesMenuItem().setOnAction((e) -> nodeSelectionModel.selectAll());
            controller.getSelectAllNodesMenuItem().disableProperty().bind(Bindings.size(nodeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfNodes()));

            controller.getSelectAllEdgesMenuItem().setOnAction((e) -> edgeSelectionModel.selectAll());
            controller.getSelectAllEdgesMenuItem().disableProperty().bind(Bindings.size(edgeSelectionModel.getSelectedItems()).isEqualTo(graph.getNumberOfEdges()));
        }

        controller.getFindMenuItem().setOnAction((e) -> findToolBar.setShowFindToolBar(true));
        controller.getFindAgainMenuItem().setOnAction((e) -> findToolBar.findAgain());
        controller.getFindAgainMenuItem().disableProperty().bind(findToolBar.canFindAgainProperty().not());

        controller.getReplaceMenuItem().setOnAction((e) -> findToolBar.setShowReplaceToolBar(true));

        controller.getSelectAllLabeledNodesMenuItem().setOnAction((e) -> {
            for (Node v : getGraph().nodes()) {
                if (getGraph().getLabel(v) != null && getGraph().getLabel(v).length() > 0)
                    nodeSelectionModel.select(v);
            }
        });

        controller.getInvertNodeSelectionMenuItem().setOnAction((e) -> nodeSelectionModel.invertSelection());
        controller.getInvertEdgeSelectionMenuItem().setOnAction((e) -> edgeSelectionModel.invertSelection());

        controller.getDeselectAllNodesMenuItem().setOnAction((e) -> nodeSelectionModel.clearSelection());
        controller.getDeselectAllNodesMenuItem().disableProperty().bind(nodeSelectionModel.emptyProperty());
        controller.getDeselectEdgesMenuItem().setOnAction((e) -> edgeSelectionModel.clearSelection());
        controller.getDeselectEdgesMenuItem().disableProperty().bind(edgeSelectionModel.emptyProperty());

        controller.getSelectFromPreviousMenuItem().setOnAction((e) -> {
            selectByLabel(MainWindowManager.getInstance().getPreviousSelection());

        });
        controller.getSelectFromPreviousMenuItem().disableProperty().bind(Bindings.isEmpty(MainWindowManager.getInstance().getPreviousSelection()));

        controller.getZoomInMenuItem().setOnAction((e) -> scrollPane.zoomBy(1.1, 1.1));
        controller.getZoomOutMenuItem().setOnAction((e) -> scrollPane.zoomBy(1 / 1.1, 1 / 1.1));

        controller.getRotateLeftMenuItem().setOnAction((e) -> getUndoManager().doAndAdd(new RotateCommand(-10, GraphTab.this)));
        controller.getRotateLeftMenuItem().disableProperty().bind(layout.isNotEqualTo(GraphLayout.Radial));
        controller.getRotateRightMenuItem().setOnAction((e) -> getUndoManager().doAndAdd(new RotateCommand(10, GraphTab.this)));
        controller.getRotateRightMenuItem().disableProperty().bind(layout.isNotEqualTo(GraphLayout.Radial));


        controller.getResetMenuItem().setOnAction((e) -> {
            getUndoManager().doAndAdd(new CompositeCommand("Reset",
                    new ZoomCommand(1 / scaleChangeX.get(), 1 / scaleChangeY.get(), GraphTab.this),
                    new RotateCommand(-angleChange.get(), GraphTab.this),
                    new LayoutLabelsCommand(layout.get(), sparseLabels.get(), graph, (graph instanceof PhyloTree ? ((PhyloTree) graph).getRoot() : null), node2view, edge2view)));
            //scrollPane.resetZoom();
        });
        controller.getResetMenuItem().disableProperty().bind(scaleChangeX.isEqualTo(1).and(scaleChangeY.isEqualTo(1)).and(angleChange.isEqualTo(0)));

        controller.getIncreaseFontSizeMenuItem().setOnAction((x) -> {
            if (nodeSelectionModel.getSelectedItems().size() > 0 || edgeSelectionModel.getSelectedItems().size() > 0) {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(2, nodeSelectionModel.getSelectedItems(),
                        node2view, edgeSelectionModel.getSelectedItems(), edge2view));
            } else {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(2, Arrays.asList(nodeSelectionModel.getItems()),
                        node2view, Arrays.asList(edgeSelectionModel.getItems()), edge2view));
            }
        });

        controller.getDecreaseFontSizeMenuItem().setOnAction((x) -> {
            if (nodeSelectionModel.getSelectedItems().size() > 0 || edgeSelectionModel.getSelectedItems().size() > 0) {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(-2, nodeSelectionModel.getSelectedItems(),
                        node2view, edgeSelectionModel.getSelectedItems(), edge2view));
            } else {
                getUndoManager().doAndAdd(new FontSizeIncrementCommand(-2, Arrays.asList(nodeSelectionModel.getItems()),
                        node2view, Arrays.asList(edgeSelectionModel.getItems()), edge2view));
            }
        });


        controller.getFormatNodesMenuItem().setOnAction((e) -> getMainWindow().showFormatTab());
        controller.getFormatNodesMenuItem().setDisable(false);

        controller.getLayoutLabelsMenuItem().setOnAction((e) -> {
            Node root = (graph instanceof PhyloTree ? ((PhyloTree) graph).getRoot() : null);
            getUndoManager().doAndAdd(new LayoutLabelsCommand(layout.get(), sparseLabels.get(), graph, root, node2view, edge2view));
        });
        controller.getSparseLabelsCheckMenuItem().selectedProperty().bindBidirectional(sparseLabels);
        controller.getSparseLabelsCheckMenuItem().setOnAction(controller.getLayoutLabelsMenuItem().getOnAction());
    }

    public void setNodeLabel2Style(Map<String, FormatItem> nodeLabel2Style) {
        this.nodeLabel2Style = nodeLabel2Style;
    }

    public Map<String, FormatItem> getNodeLabel2Style() {
        return nodeLabel2Style;
    }

    public boolean isSparseLabels() {
        return sparseLabels.get();
    }

    public BooleanProperty sparseLabelsProperty() {
        return sparseLabels;
    }

    public void setSparseLabels(boolean sparseLabels) {
        this.sparseLabels.set(sparseLabels);
    }

    public FindToolBar getFindToolBar() {
        return findToolBar;
    }

}
