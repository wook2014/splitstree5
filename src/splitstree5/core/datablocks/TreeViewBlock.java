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
import javafx.geometry.Dimension2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromTreeView;
import splitstree5.core.algorithms.interfaces.IToTreeView;
import splitstree5.core.algorithms.views.treeview.PhylogeneticEdgeView;
import splitstree5.core.algorithms.views.treeview.PhylogeneticNodeView;

import java.util.HashMap;
import java.util.Map;
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

    private final Map<Node, PhylogeneticNodeView> node2view = new HashMap<>();
    private final Map<Edge, PhylogeneticEdgeView> edge2view = new HashMap<>();

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

    public void show() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (stage == null) {
                        stage = new Stage();
                        stage.setTitle("Tree View");
                        StackPane pane = new StackPane(group);
                        ScrollPane scrollPane = new ScrollPane(pane);
                        rootNode.setCenter(scrollPane);
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
                    node2view.clear();
                    edge2view.clear();

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
        nodeSelectionModel.setItems(tree.getNodes().toArray(new Node[tree.getNumberOfNodes()]));
        edgeSelectionModel.setItems(tree.getEdges().toArray(new Edge[tree.getNumberOfEdges()]));
    }

    public Map<Node, PhylogeneticNodeView> getNode2view() {
        return node2view;
    }

    public Map<Edge, PhylogeneticEdgeView> getEdge2view() {
        return edge2view;
    }

    public Dimension2D getTargetDimensions() {
        return new Dimension2D(scene.getWidth(), scene.getHeight());
    }
}
