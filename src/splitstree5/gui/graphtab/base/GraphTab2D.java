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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import jloda.fx.ZoomableScrollPane;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import splitstree5.gui.graphtab.commands.LayoutLabelsCommand;
import splitstree5.gui.graphtab.commands.RotateCommand;
import splitstree5.gui.graphtab.commands.ZoomCommand;
import splitstree5.gui.utils.RubberBandSelection;
import splitstree5.menu.MenuController;
import splitstree5.undo.CompositeCommand;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * tree and split network two-dimensional graph
 * Daniel Huson,. 12.2017
 */
public abstract class GraphTab2D<G extends PhyloGraph> extends GraphTabBase<G> {

    protected final RubberBandSelection rubberBandSelection;
    protected DoubleProperty scaleChangeX = new SimpleDoubleProperty(1); // keep track of scale changes, used for reset
    protected DoubleProperty scaleChangeY = new SimpleDoubleProperty(1);
    protected DoubleProperty angleChange = new SimpleDoubleProperty(0);
    protected ObjectProperty<GraphLayout> layout = new SimpleObjectProperty<>(GraphLayout.LeftToRight);

    public GraphTab2D() {
        super();
        rubberBandSelection = new RubberBandSelection(centerPane, group, createRubberBandSelectionHandler());
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
        edgeLabelSearcher.setGraph(graph);
        Platform.runLater(() -> {
            group.setScaleX(1);
            group.setScaleY(1);
            scaleChangeX.set(1);
            scaleChangeY.set(1);
            angleChange.set(0);
        });
        Platform.runLater(() -> getUndoManager().clear());
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
                            getUndoManager().doAndAdd(new ZoomCommand(getZoomFactorY(), getZoomFactorY(), GraphTab2D.this));
                        } else {
                            getUndoManager().doAndAdd(new ZoomCommand(getZoomFactorX(), getZoomFactorY(), GraphTab2D.this));
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

    /**
     * change scale by the given factors
     *
     * @param xFactor
     * @param yFactor
     */
    public void scale(double xFactor, double yFactor) {
        scaleChangeX.set(scaleChangeX.get() * xFactor);
        scaleChangeY.set(scaleChangeY.get() * yFactor);

        for (NodeViewBase nv : getNode2view()) {
            ((NodeView2D) nv).scaleCoordinates(xFactor, yFactor);
        }
        for (EdgeViewBase ev : getEdge2view()) {
            ((EdgeView2D) ev).scaleCoordinates(xFactor, yFactor);
        }
    }

    /**
     * rotate by given angle
     *
     * @param angle
     */
    public void rotate(double angle) {
        angleChange.set(angleChange.get() + angle);
        for (NodeViewBase nv : getNode2view()) {
            ((NodeView2D) nv).rotateCoordinates(angle);
        }
        for (EdgeViewBase ev : getEdge2view()) {
            ((EdgeView2D) ev).rotateCoordinates(angle);
        }
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

    private RubberBandSelection.Handler createRubberBandSelectionHandler() {
        return (rectangle, extendSelection) -> {
            if (!extendSelection) {
                nodeSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
            }
            final Set<Node> previouslySelectedNodes = new HashSet<>(nodeSelectionModel.getSelectedItems());
            final Set<Edge> previouslySelectedEdges = new HashSet<>(edgeSelectionModel.getSelectedItems());

            for (Node node : graph.nodes()) {
                final NodeView2D nodeView = (NodeView2D) node2view.get(node);
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
                    final EdgeView2D edgeView = (EdgeView2D) edge2view.get(edge);
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
        super.updateMenus(controller);

        controller.getRotateLeftMenuItem().setOnAction((e) -> getUndoManager().doAndAdd(new RotateCommand(-10, GraphTab2D.this)));
        controller.getRotateLeftMenuItem().disableProperty().bind(layout.isNotEqualTo(GraphLayout.Radial));
        controller.getRotateRightMenuItem().setOnAction((e) -> getUndoManager().doAndAdd(new RotateCommand(10, GraphTab2D.this)));
        controller.getRotateRightMenuItem().disableProperty().bind(layout.isNotEqualTo(GraphLayout.Radial));


        controller.getResetMenuItem().setOnAction((e) -> {
            getUndoManager().doAndAdd(new CompositeCommand("Reset",
                    new ZoomCommand(1 / scaleChangeX.get(), 1 / scaleChangeY.get(), GraphTab2D.this),
                    new RotateCommand(-angleChange.get(), GraphTab2D.this),
                    new LayoutLabelsCommand(layout.get(), sparseLabels.get(), graph, (graph instanceof PhyloTree ? ((PhyloTree) graph).getRoot() : null), node2view, edge2view)));
            //scrollPane.resetZoom();
        });

        controller.getLayoutLabelsMenuItem().setOnAction((e) -> {
            Node root = (graph instanceof PhyloTree ? ((PhyloTree) graph).getRoot() : null);
            getUndoManager().doAndAdd(new LayoutLabelsCommand(layout.get(), sparseLabels.get(), graph, root, node2view, edge2view));
        });
        controller.getSparseLabelsCheckMenuItem().selectedProperty().bindBidirectional(sparseLabels);
        controller.getSparseLabelsCheckMenuItem().setOnAction(controller.getLayoutLabelsMenuItem().getOnAction());

        controller.getResetMenuItem().disableProperty().bind(scaleChangeX.isEqualTo(1).and(scaleChangeY.isEqualTo(1)).and(angleChange.isEqualTo(0)));
    }
}