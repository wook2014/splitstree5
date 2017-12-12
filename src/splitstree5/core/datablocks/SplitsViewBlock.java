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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import jloda.fx.ASelectionModel;
import jloda.graph.*;
import jloda.phylo.PhyloGraph;
import splitstree5.core.Document;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToSplitsView;
import splitstree5.main.graphtab.SplitsViewTab;
import splitstree5.main.graphtab.base.AEdgeView;
import splitstree5.main.graphtab.base.ANodeView;
import splitstree5.main.graphtab.base.GeometryUtils;
import splitstree5.main.graphtab.base.GraphLayout;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This block represents the view of a split network
 * Daniel Huson, 11.2017
 */
public class SplitsViewBlock extends ADataBlock {
    private final ASelectionModel<Integer> splitsSelectionModel = new ASelectionModel<>();
    private final SplitsViewTab splitsViewTab;

    /**
     * constructor
     */
    public SplitsViewBlock() {
        super();
        setTitle("Split Network Viewer");
        splitsViewTab = new SplitsViewTab();

        splitsViewTab.setLayout(GraphLayout.Radial);

        splitsSelectionModel.getSelectedItems().addListener((ListChangeListener<Integer>) c -> {
            final Set<Integer> addedSplits = new HashSet<>();
            final Set<Integer> removedSplits = new HashSet<>();
            while (c.next()) {
                addedSplits.addAll(c.getAddedSubList());
                removedSplits.addAll(c.getRemoved());
            }
            final PhyloGraph graph = splitsViewTab.getPhyloGraph();
            for (Edge e : graph.edges()) {
                if (addedSplits.contains(graph.getSplit(e)))
                    splitsViewTab.getEdgeSelectionModel().select(e);
                if (removedSplits.contains(graph.getSplit(e)))
                    splitsViewTab.getEdgeSelectionModel().clearSelection(e);
            }
        });

    }

    @Override
    public void setDocument(Document document) {
        if (getDocument() == null) {
            super.setDocument(document);
            if (document.getMainWindow() != null) {
                Platform.runLater(() -> { // setup tab
                    document.getMainWindow().add(splitsViewTab);
                });
            } else { // this is for testing only: this opens the view in a standalone window
                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    final TabPane tabPane = new TabPane(splitsViewTab);
                    stage.setScene(new Scene(tabPane));
                    stage.setWidth(800);
                    stage.setHeight(800);
                    stage.show();
                });
            }
        }
    }

    public SplitsViewTab getSplitsViewTab() {
        return splitsViewTab;
    }

    /**
     * show the splits network
     */
    public void show() {
        splitsViewTab.show();
    }

    public void updateSelectionModels(PhyloGraph graph) {
        splitsViewTab.updateSelectionModels(graph);
        splitsSelectionModel.setItems(graph.getSplitIds());
    }

    @Override
    public Class getFromInterface() {
        return IFromSplits.class;
    }

    @Override
    public Class getToInterface() {
        return IToSplitsView.class;
    }


    /**
     * create a node view
     *
     * @param v
     * @param location
     * @param label
     * @return node view
     */
    public ANodeView createNodeView(Node v, Point2D location, String label) {
        ASelectionModel<Node> nodeSelectionModel = splitsViewTab.getNodeSelectionModel();
        ASelectionModel<Edge> edgeSelectionModel = splitsViewTab.getEdgeSelectionModel();
        final Group group = splitsViewTab.getGroup();

        final ANodeView nodeView = new ANodeView(v, location, label, nodeSelectionModel);

        if (nodeView.getShape() != null) {
            nodeView.getShape().setOnMousePressed((e) -> {
                mouseX = e.getScreenX();
                mouseY = e.getScreenY();
            });
            nodeView.getShape().setOnMouseDragged((e) -> {
                if (!splitsSelectionModel.isEmpty() && nodeSelectionModel.getSelectedItems().contains(nodeView.getNode())) {
                    final HashSet<Node> selectedNodesSet = new HashSet<>(nodeSelectionModel.getSelectedItems());
                    final Point2D center = computeAnchorCenter(edgeSelectionModel.getSelectedItems(), selectedNodesSet, splitsViewTab.getNode2view());
                    final Point2D prevPoint = group.localToParent(group.screenToLocal(mouseX, mouseY));
                    final Point2D newPoint = group.localToParent(group.screenToLocal(e.getScreenX(), e.getScreenY()));
                    final double angle = GeometryUtils.computeObservedAngle(center, prevPoint, newPoint);
                    applySplitRotation(angle, edgeSelectionModel.getSelectedItems(), selectedNodesSet, splitsViewTab.getNode2view(), splitsViewTab.getEdge2view());
                }
                mouseX = e.getScreenX();
                mouseY = e.getScreenY();
            });
            nodeView.getShape().setOnMouseClicked((e) -> {
                splitsSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
                if (!e.isShiftDown())
                    nodeSelectionModel.clearSelection();
                nodeSelectionModel.select(nodeView.getNode());
            });
        }
        if (nodeView.getLabel() != null) {
            nodeView.getLabel().setOnMouseClicked((e) -> {
                splitsSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
                if (!e.isShiftDown())
                    nodeSelectionModel.clearSelection();
                nodeSelectionModel.select(nodeView.getNode());
            });
        }

        return nodeView;
    }

    /**
     * compute the anchor center for rotating splits
     *
     * @param edges
     * @param selectedNodes
     * @param node2view
     * @return anchor center
     */
    private Point2D computeAnchorCenter(Collection<Edge> edges, HashSet<Node> selectedNodes, NodeArray<ANodeView> node2view) {
        double x = 0;
        double y = 0;
        if (edges.size() > 0) {
            for (Edge edge : edges) {
                final ANodeView nodeView;
                if (selectedNodes.contains(edge.getSource()))
                    nodeView = node2view.get(edge.getTarget());
                else
                    nodeView = node2view.get(edge.getSource());
                x += nodeView.getLocation().getX();
                y += nodeView.getLocation().getY();

            }
            x /= edges.size();
            y /= edges.size();
        }
        return new Point2D(x, y);
    }

    /**
     * rotate split by given angle
     *
     * @param angle
     * @param selectedEdges must contain all and only edges of one split
     * @param selectedNodes must contain all and only nodes on one side of split
     * @param node2view
     * @param edge2view
     */
    private void applySplitRotation(double angle, ObservableList<Edge> selectedEdges, HashSet<Node> selectedNodes, NodeArray<ANodeView> node2view, EdgeArray<AEdgeView> edge2view) {
        final Edge e = selectedEdges.get(0);
        final Node anchorNode;
        final Node selectedNode;
        if (selectedNodes.contains(e.getSource())) {
            anchorNode = e.getTarget();
            selectedNode = e.getSource();

        } else {
            anchorNode = e.getSource();
            selectedNode = e.getTarget();
        }
        final Point2D anchorPoint = node2view.get(anchorNode).getLocation();
        final Point2D selectedPoint = node2view.get(selectedNode).getLocation();
        Point2D newSelectedPoint = GeometryUtils.rotateAbout(selectedPoint, angle, anchorPoint);
        Point2D translate = newSelectedPoint.subtract(selectedPoint);

        for (Node v : selectedNodes) {
            node2view.get(v).translateCoordinates(translate.getX(), translate.getY());
        }
        for (Node v : selectedNodes) {
            for (Edge edge : v.adjacentEdges()) {
                if (v == edge.getTarget() || !selectedNodes.contains(edge.getTarget())) {
                    edge2view.get(edge).setCoordinates(node2view.get(edge.getSource()).getLocation(), node2view.get(edge.getTarget()).getLocation());
                }
            }
        }
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
    public AEdgeView createEdgeView(PhyloGraph graph, Edge e, Double weight, final Point2D start, final Point2D end) {
        final AEdgeView edgeView = new AEdgeView(e,weight,start,end);

        final EventHandler<? super MouseEvent> handler = (EventHandler<MouseEvent>) event -> {
            final Integer splitId = graph.getSplit(e); // must be Integer, not int, otherwise it will be confused with an index
            if (!splitsSelectionModel.getSelectedItems().contains(splitId)) {
                splitsSelectionModel.clearSelection();
                splitsSelectionModel.select(splitId);
                for (Edge f : graph.edges()) {
                    if (graph.getSplit(f) == splitId) {
                        selectAllNodesOnSmallerSide(graph, e, splitsViewTab.getNodeSelectionModel());
                    }
                }
            } else if (event.isShiftDown() && splitsSelectionModel.getSelectedItems().contains(splitId)) {
                splitsSelectionModel.clearSelection();
                splitsViewTab.getNodeSelectionModel().clearSelection();
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
    public int size() {
        return splitsViewTab.size();
    }

    @Override
    public String getInfo() {
        if (splitsViewTab != null && splitsViewTab.getPhyloGraph() != null) {
            return "a split network with " + splitsViewTab.getPhyloGraph().getNumberOfNodes() + " nodes and " + splitsViewTab.getPhyloGraph().getNumberOfEdges() + " edges";
        } else
            return "a split network";
    }

    private static double mouseX;
    private static double mouseY;
}
