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
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromTreeView;
import splitstree5.core.algorithms.interfaces.IToTreeView;
import splitstree5.core.algorithms.views.treeview.NodeLabelLayouter;
import splitstree5.core.algorithms.views.treeview.PhylogeneticEdgeView;
import splitstree5.core.algorithms.views.treeview.PhylogeneticNodeView;

import java.util.concurrent.CountDownLatch;


/**
 * This block represents the view of a tree
 * Daniel Huson, 11.2017
 */
public class TreeViewBlock extends ADataBlock {
    private final Group group = new Group();
    private final Group edgesGroup = new Group();
    private final Group nodesGroup = new Group();
    private final Group edgeLabelsGroup = new Group();
    private final Group nodeLabelsGroup = new Group();

    private PhyloTree tree;
    private NodeArray<PhylogeneticNodeView> node2view;
    private EdgeArray<PhylogeneticEdgeView> edge2view;

    private final ASelectionModel<Node> nodeSelectionModel = new ASelectionModel<>();
    private final ASelectionModel<Edge> edgeSelectionModel = new ASelectionModel<>();

    private Stage stage = null;
    private final BorderPane rootNode = new BorderPane();
    private final Scene scene = new Scene(rootNode, 600, 600);

    /**
     * constructor
     */
    public TreeViewBlock() {
    }

    @Override
    public void clear() {
        super.clear();
    }

    /**
     * initialize datastructures
     *
     * @param tree
     */
    public void init(PhyloTree tree) {
        this.tree = tree;
        node2view = new NodeArray<>(tree);
        edge2view = new EdgeArray<>(tree);
        group.setScaleX(1);
        group.setScaleY(1);

    }

    public void show() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (stage == null) {
                        stage = new Stage();
                        stage.setTitle("Tree View");
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
                            if (tree != null) NodeLabelLayouter.radialLayout(tree, getNode2view(), getEdge2view());
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
    public int size() {
        return edgesGroup.getChildren().size() + nodesGroup.getChildren().size();
    }

    @Override
    public Class getFromInterface() {
        return IFromTreeView.class;
    }

    @Override
    public Class getToInterface() {
        return IToTreeView.class;
    }

    @Override
    public String getInfo() {
        return "a tree view";
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

    public void updateSelectionModels(PhyloTree tree) {
        nodeSelectionModel.setItems(tree.getNodesAsSet().toArray(new Node[tree.getNumberOfNodes()]));
        edgeSelectionModel.setItems(tree.getEdgesAsSet().toArray(new Edge[tree.getNumberOfEdges()]));
    }

    public NodeArray<PhylogeneticNodeView> getNode2view() {
        return node2view;
    }

    public EdgeArray<PhylogeneticEdgeView> getEdge2view() {
        return edge2view;
    }

    public Dimension2D getTargetDimensions() {
        return new Dimension2D(scene.getWidth(), scene.getHeight());
    }

    /**
     * rescale node and edge label fonts so that they maintain the same apparent size during scaling
     *
     * @param factor
     */
    private void rescaleNodesAndEdgesToKeepApparentSizes(double factor) {
        if (tree != null) {
            for (Node v : tree.nodes()) {
                PhylogeneticNodeView nv = node2view.get(v);
                if (nv.getLabels() != null)
                    for (javafx.scene.Node node : nv.getLabels().getChildren()) {
                        if (node instanceof Labeled) {
                            Font font = ((Labeled) node).getFont();
                            double newSize = font.getSize() / factor;
                            node.setStyle(String.format("-fx-font-size: %.3f;", newSize));
                        }
                    }
            }
            for (Edge e : tree.edges()) {
                PhylogeneticEdgeView ev = edge2view.get(e);

                for (javafx.scene.Node node : ev.getParts().getChildren()) {
                    if (node instanceof Shape) {
                        double strokeWidth = ((Shape) node).getStrokeWidth();
                        ((Shape) node).setStrokeWidth(strokeWidth / factor);
                    }
                }
                if (ev.getLabels() != null)
                    for (javafx.scene.Node node : ev.getLabels().getChildren()) {
                        if (node instanceof Labeled) {
                            Font font = ((Labeled) node).getFont();
                            double newSize = font.getSize() / factor;

                            node.setStyle(String.format("-fx-font-size: %.3f;", newSize));
                        }
                    }
            }
        }
    }
}
