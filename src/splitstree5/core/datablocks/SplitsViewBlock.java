/*
 *  Copyright (C) 2017 Daniel H. Huson
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
package splitstree5.core.datablocks;


import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloGraph;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToNone;
import splitstree5.core.datablocks.view.AEdgeView;
import splitstree5.core.datablocks.view.NodeLabelLayouter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * This block represents the view of a split network
 * Daniel Huson, 11.2017
 */
public class SplitsViewBlock extends ViewBlockBase {
    private final ASelectionModel<Integer> splitsSelectionModel = new ASelectionModel<>();

    /**
     * constructor
     */
    public SplitsViewBlock() {
        super();

        splitsSelectionModel.getSelectedItems().addListener((ListChangeListener<Integer>) c -> {
            final Set<Integer> addedSplits = new HashSet<>();
            final Set<Integer> removedSplits = new HashSet<>();
            while (c.next()) {
                addedSplits.addAll(c.getAddedSubList());
                removedSplits.addAll(c.getRemoved());
            }
            final PhyloGraph graph = getPhyloGraph();
            for (Edge e : graph.edges()) {
                if (addedSplits.contains(graph.getSplit(e)))
                    edgeSelectionModel.select(e);
                if (removedSplits.contains(graph.getSplit(e)))
                    edgeSelectionModel.clearSelection(e);
            }
        });
    }

    /**
     * show the phyloGraph or network
     */
    public void show() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (stage == null) {
                        stage = new Stage();
                        stage.setTitle("Split network");
                        final Group world = new Group();
                        world.getChildren().add(group);
                        StackPane stackPane = new StackPane(world);
                        ScrollPane scrollPane = new ScrollPane(stackPane);
                        stackPane.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                                scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()).subtract(20));
                        stackPane.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                                scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()).subtract(20));

                        BorderPane borderPane = new BorderPane(scrollPane);
                        Button layoutLabels = new Button("Layout Labels");
                        layoutLabels.setOnAction((e) -> {
                            if (getPhyloGraph() != null) {
                                NodeLabelLayouter.radialLayout(getPhyloGraph(), getNode2view(), getEdge2view());
                            }
                        });

                        Button zoomIn = new Button("Zoom In");
                        zoomIn.setOnAction((e) -> {
                                    final double factor = 1.1;
                                    group.setScaleX(factor * group.getScaleX());
                                    group.setScaleY(factor * group.getScaleY());
                                    rescaleNodesAndEdgesToKeepApparentSizes(factor);
                                }
                        );
                        Button zoomOut = new Button("Zoom Out");
                        zoomOut.setOnAction((e) -> {
                                    final double factor = 1.0 / 1.1;
                                    group.setScaleX(factor * group.getScaleX());
                                    group.setScaleY(factor * group.getScaleY());
                                    rescaleNodesAndEdgesToKeepApparentSizes(factor);
                                }
                        );

                        borderPane.setRight(new VBox(layoutLabels, zoomIn, zoomOut));
                        rootNode.setCenter(borderPane);
                        stage.setScene(scene);
                        stage.sizeToScene();
                        stage.show();
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

                    stage.setIconified(false);
                    stage.toFront();
                } finally {
                    countDownLatch.countDown();
                }
            }
        });

        try {
            countDownLatch.await(); // wait for the JavaFX update to take place
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateSelectionModels(PhyloGraph graph) {
        super.updateSelectionModels(graph);
        splitsSelectionModel.setItems(graph.getSplitIds());
    }

    @Override
    public Class getFromInterface() {
        return IFromSplits.class;
    }

    @Override
    public Class getToInterface() {
        return IToNone.class;
    }

    /**
     * create an edge view
     *
     * @param graph
     * @param e
     * @param weight
     * @param start
     * @param end
     * @return edge view
     */
    public static AEdgeView createEdgeView(PhyloGraph graph, Edge e, Double weight, final Point2D start, final Point2D end, ASelectionModel<Node> nodeSelectionModel, ASelectionModel<Integer> splitSelectionModel) {
        final AEdgeView edgeView = new AEdgeView(e);

        Shape edgeShape = null;
        if (start != null && end != null) {
            edgeShape = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        }

        if (edgeShape != null) {
            edgeShape.setFill(Color.TRANSPARENT);
            edgeShape.setStroke(Color.BLACK);
            edgeShape.setStrokeLineCap(StrokeLineCap.ROUND);
            edgeShape.setStrokeWidth(3);
            edgeView.setShape(edgeShape);
            edgeView.setReferencePoint(start.add(end).multiply(0.5));
        }

        if (false && weight != null && start != null && end != null) {
            Label label = new Label("" + weight);
            final Point2D m = start.add(end).multiply(0.5);
            label.setLayoutX(m.getX());
            label.setLayoutY(m.getY());
            edgeView.setLabel(label);
        }

        final EventHandler<? super MouseEvent> handler = (EventHandler<MouseEvent>) event -> {
            final Integer splitId = graph.getSplit(e); // must be Integer, not int, otherwise it will be confused with an index
            if (!splitSelectionModel.getSelectedItems().contains(splitId)) {
                splitSelectionModel.clearSelection();
                splitSelectionModel.select(splitId);
                for (Edge f : graph.edges()) {
                    if (graph.getSplit(f) == splitId) {
                        selectAllNodesOnSmallerSide(graph, e, nodeSelectionModel);
                    }
                }
            } else if (event.isShiftDown() && splitSelectionModel.getSelectedItems().contains(splitId)) {
                splitSelectionModel.clearSelection();
                nodeSelectionModel.clearSelection();
            }
        };

        if (edgeView.getShape() != null) {
            edgeView.getShape().setOnMouseClicked(handler);
        }

        if (edgeView.getLabel() != null) {
            edgeView.getLabel().setOnMouseClicked(handler);
        }
        return edgeView;
    }

    public ASelectionModel<Integer> getSplitsSelectionModel() {
        return splitsSelectionModel;
    }

    /**
     * select all nodes on smaller side of graph separated by e
     *
     * @param graph
     * @param e
     * @param nodeSelectionModel
     */
    private static void selectAllNodesOnSmallerSide(PhyloGraph graph, Edge e, ASelectionModel<Node> nodeSelectionModel) {
        nodeSelectionModel.clearSelection();
        final NodeSet visited = new NodeSet(graph);
        visitRec(graph, e.getSource(), null, graph.getSplit(e), visited);
        int sourceSize = visited.size();
        int targetSize = graph.getNumberOfNodes() - sourceSize;
        if (sourceSize <= targetSize) {
            nodeSelectionModel.selectItems(visited);
        } else {
            final NodeSet others = graph.getNodesAsSet();
            others.removeAll(visited);
            nodeSelectionModel.selectItems(others);
        }
    }

    /**
     * recursively visit all nodes on one side of a given split
     *
     * @param v
     * @param e
     * @param splitId
     * @param visited
     */
    private static void visitRec(PhyloGraph graph, Node v, Edge e, int splitId, NodeSet visited) {
        if (!visited.contains(v)) {
            visited.add(v);
            for (Edge f : v.adjacentEdges()) {
                if (graph.getSplit(f) != splitId && f != e)
                    visitRec(graph, f.getOpposite(v), f, splitId, visited);
            }
        }
    }

    @Override
    public String getInfo() {
        return "Split network";
    }
}
