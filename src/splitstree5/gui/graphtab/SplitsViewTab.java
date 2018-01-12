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
package splitstree5.gui.graphtab;


import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import jloda.fx.ASelectionModel;
import jloda.graph.*;
import jloda.phylo.PhyloGraph;
import jloda.util.Pair;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.graphtab.base.*;
import splitstree5.menu.MenuController;

import java.util.*;

/**
 * The split network view tab
 * Daniel Huson, 11.2017
 */
public class SplitsViewTab extends GraphTab {
    private final ASelectionModel<Integer> splitsSelectionModel = new ASelectionModel<>();

    /**
     * constructor
     */
    public SplitsViewTab() {
        super();
        final Label label = new Label("Splits Network");
        label.setGraphic(new ImageView(ResourceManager.getIcon("SplitsNetworkView16.gif")));
        setText("");
        setGraphic(label);

        setLayout(GraphLayout.Radial);

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
     * show the splits network
     */
    public void show() {
        super.show();
    }

    @Override
    public void updateSelectionModels(PhyloGraph graph, TaxaBlock taxa, Document document) {
        super.updateSelectionModels(graph, taxa, document);
        splitsSelectionModel.setItems(graph.getSplitIds());
    }

    private double mouseX;
    private double mouseY;

    /**
     * create a node view
     */
    public ANodeView createNodeView(final Node v, Point2D location, String label) {
        final ANodeView nodeView = new ANodeView(v, location, label);

        if (nodeView.getShape() != null) {
            nodeView.getShape().setOnMousePressed((e) -> {
                mouseX = e.getScreenX();
                mouseY = e.getScreenY();
                e.consume();
            });
            nodeView.getShape().setOnMouseDragged((e) -> {
                if (!splitsSelectionModel.isEmpty() && nodeSelectionModel.getSelectedItems().contains(nodeView.getNode())) {
                    final HashSet<Node> selectedNodesSet = new HashSet<>(nodeSelectionModel.getSelectedItems());
                    final Point2D center = computeAnchorCenter(edgeSelectionModel.getSelectedItems(), selectedNodesSet, getNode2view());
                    final Point2D prevPoint = group.localToParent(group.screenToLocal(mouseX, mouseY));
                    final Point2D newPoint = group.localToParent(group.screenToLocal(e.getScreenX(), e.getScreenY()));
                    final double angle = GeometryUtils.computeObservedAngle(center, prevPoint, newPoint);
                    applySplitRotation(angle, edgeSelectionModel.getSelectedItems(), selectedNodesSet, getNode2view(), getEdge2view());
                }
                mouseX = e.getScreenX();
                mouseY = e.getScreenY();
                e.consume();
            });
            nodeView.getShape().setOnMouseClicked((x) -> {
                splitsSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
                if (!x.isShiftDown())
                    nodeSelectionModel.clearSelection();
                if (nodeSelectionModel.getSelectedItems().contains(v))
                    nodeSelectionModel.clearSelection(v);
                else
                    nodeSelectionModel.select(v);
                if (x.getClickCount() >= 2) {
                    ArrayList<Edge> edges = getAdjacentEdgesSortedByDecreasingWeight(v);
                    int index = Math.min(edges.size() - 1, x.getClickCount() - 2);
                    selectBySplit(edges.get(index));
                }
                x.consume();
            });
        }
        if (nodeView.getLabel() != null) {
            nodeView.getLabel().setOnMouseClicked((x) -> {
                splitsSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
                if (!x.isShiftDown())
                    nodeSelectionModel.clearSelection();
                if (nodeSelectionModel.getSelectedItems().contains(v))
                    nodeSelectionModel.clearSelection(v);
                else
                    nodeSelectionModel.select(v);
                x.consume();
            });
        }
        addNodeLabelMovementSupport(nodeView);
        return nodeView;
    }

    /**
     * get list of adjacent edges sorted by decreasing weight
     *
     * @param v
     * @return adjacent edges
     */
    private ArrayList<Edge> getAdjacentEdgesSortedByDecreasingWeight(Node v) {
        final Pair<Double, Edge>[] list = new Pair[v.getDegree()];
        int i = 0;
        for (Edge e : v.adjacentEdges()) {
            list[i++] = new Pair<>(getPhyloGraph().getWeight(e), e);
        }

        Arrays.sort(list, (o1, o2) -> -o1.getFirst().compareTo(o2.getFirst()));
        final ArrayList<Edge> result = new ArrayList<>(list.length);
        for (Pair<Double, Edge> pair : list) {
            result.add(pair.getSecond());
        }
        return result;
    }

    /**
     * create an edge view
     */
    public AEdgeView createEdgeView(final PhyloGraph graph, final Edge e, final Double weight, final Point2D start, final Point2D end) {
        final AEdgeView edgeView = new AEdgeView(e, weight, start, end);
        final int splitId = graph.getSplit(e);

        final EventHandler<? super MouseEvent> handler = (EventHandler<MouseEvent>) x -> {
            if (!splitsSelectionModel.getSelectedItems().contains(splitId)) {
                selectBySplit(e);
            } else if (x.isShiftDown() && splitsSelectionModel.getSelectedItems().contains(splitId)) {
                splitsSelectionModel.clearSelection();
                nodeSelectionModel.clearSelection();
            }
            x.consume();
        };

        if (edgeView.getShape() != null) {
            edgeView.getShape().setOnMouseClicked(handler);
        }

        if (edgeView.getLabel() != null) {
            edgeView.getLabel().setOnMouseClicked(handler);
        }
        return edgeView;
    }

    /**
     * select by split associated with given edge
     */
    public void selectBySplit(Edge e) {
        final int splitId = getPhyloGraph().getSplit(e);
        if (!splitsSelectionModel.getSelectedItems().contains(splitId)) {
            splitsSelectionModel.clearSelection();
            splitsSelectionModel.select((Integer) splitId);
            selectAllNodesOnSmallerSide(getPhyloGraph(), e, nodeSelectionModel);
        }
    }

    /**
     * compute the anchor center for rotating splits
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
     * select all nodes on smaller side of graph separated by e
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
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);
    }
}
