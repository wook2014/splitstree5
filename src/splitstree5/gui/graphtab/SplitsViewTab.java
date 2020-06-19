/*
 * SplitsViewTab.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.graphtab;


import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.shapes.NodeShape;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.ResourceManagerFX;
import jloda.graph.*;
import jloda.phylo.PhyloSplitsGraph;
import jloda.util.Pair;
import splitstree5.core.Document;
import splitstree5.core.algorithms.views.SplitsNetworkAlgorithm;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.ViewerBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.gui.graphtab.base.*;
import splitstree5.gui.graphtab.commands.MoveNodesCommand;
import splitstree5.menu.MenuController;

import java.util.*;

/**
 * The split network view tab
 * Daniel Huson, 11.2017
 */
public class SplitsViewTab extends Graph2DTab<PhyloSplitsGraph> implements ISplitsViewTab {
    private final ItemSelectionModel<Integer> splitsSelectionModel = new ItemSelectionModel<>();
    private boolean inSelection;

    /**
     * constructor
     */
    public SplitsViewTab() {
        setText("SplitsNetworkViewer");
        setGraphic(new ImageView(ResourceManagerFX.getIcon("SplitsNetworkViewer16.gif")));

        setLayout(GraphLayout.Radial);

        splitsSelectionModel.getSelectedItems().addListener((ListChangeListener<Integer>) c -> {
            if (!inSelection) {
                try {
                    inSelection = true;
                    final Set<Integer> addedSplits = new HashSet<>();
                    final Set<Integer> removedSplits = new HashSet<>();
                    while (c.next()) {
                        addedSplits.addAll(c.getAddedSubList());
                        removedSplits.addAll(c.getRemoved());
                    }
                    final PhyloSplitsGraph graph = getGraph();
                    for (Edge e : graph.edges()) {
                        if (addedSplits.contains(graph.getSplit(e)))
                            edgeSelectionModel.select(e);
                        if (removedSplits.contains(graph.getSplit(e)))
                            edgeSelectionModel.clearSelection(e);
                    }
                } finally {
                    inSelection = false;
                }
            }
        });

        edgeSelectionModel.getSelectedItems().addListener((ListChangeListener<Edge>) c -> {
            if (!inSelection) {
                inSelection = true;
                try {
                    while (c.next()) {
                        for (Edge e : c.getAddedSubList()) {
                            if (e.getOwner() == getGraph()) {
                                final Integer splitId = getGraph().getSplit(e); // must be Integer, not int!
                                splitsSelectionModel.select(splitId);
                            }
                        }
                        for (Edge e : c.getRemoved()) {
                            if (e.getOwner() == getGraph()) {
                                final Integer splitId = getGraph().getSplit(e); // must be Integer, not int!
                                splitsSelectionModel.clearSelection(splitId);
                            }
                        }
                    }
                } finally {
                    inSelection = false;
                }
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
    public void updateSelectionModels(PhyloSplitsGraph graph, TaxaBlock taxa, Document document) {
        super.updateSelectionModels(graph, taxa, document);
        splitsSelectionModel.clearSelection();

        document.getTaxaSelectionModel().getSelectedItems().addListener((InvalidationListener) (c) -> {
            if (!inSelection)
                splitsSelectionModel.clearSelection();
        });
    }

    private double mouseX;
    private double mouseY;

    /**
     * create a node view
     */
    @Override
    public NodeView2D createNodeView(final Node v, Point2D location, String label) {
        return createNodeView(v, location, null, 0, 0, label);
    }

    @Override
    public NodeView2D createNodeView(Node v, Point2D location, NodeShape shape, double shapeWidth, double shapeHeight, String label) {
        return new NodeView2D(v, location, shape, shapeWidth, shapeHeight, label);
    }

    /**
     * setup a node view
     *
     * @param nv
     */
    @Override
    public void setupNodeView(NodeViewBase nv) {
        final NodeView2D nodeView = (NodeView2D) nv;
        final Node v = nodeView.getNode();

        nodeView.getShapeGroup().setOnMousePressed((e) -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        });
        nodeView.getShapeGroup().setOnMouseDragged((e) -> {
            if (!splitsSelectionModel.isEmpty() && nodeSelectionModel.getSelectedItems().contains(nodeView.getNode())) {
                final HashSet<Node> selectedNodesSet = new HashSet<>(nodeSelectionModel.getSelectedItems());
                final Point2D center = computeAnchorCenter(edgeSelectionModel.getSelectedItems(), selectedNodesSet, (NodeArray) getNode2view());
                final Point2D prevPoint = group.localToParent(group.screenToLocal(mouseX, mouseY));
                final Point2D newPoint = group.localToParent(group.screenToLocal(e.getScreenX(), e.getScreenY()));
                final double angle = GeometryUtilsFX.computeObservedAngle(center, prevPoint, newPoint);
                applySplitRotation(angle, edgeSelectionModel.getSelectedItems(), selectedNodesSet, (NodeArray) getNode2view(), (EdgeArray) getEdge2view());
            }
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        });
        nodeView.getShapeGroup().setOnMouseClicked((x) -> {
            splitsSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();
            if (!x.isShiftDown())
                nodeSelectionModel.clearSelection();
            if (nodeSelectionModel.getSelectedItems().contains(v))
                nodeSelectionModel.clearSelection(v);
            else
                nodeSelectionModel.select(v);
            if (x.getClickCount() >= 2) {
                final ArrayList<Edge> edges = getAdjacentEdgesSortedByDecreasingWeight(v);
                if (edges.size() > 0) {
                    int index = Math.min(edges.size() - 1, x.getClickCount() - 2);
                    SplitsViewTab.selectBySplit(graph, edges.get(index), splitsSelectionModel, nodeSelectionModel, x.isControlDown());
                }
            }
            x.consume();
        });

        if (nodeView.getLabelGroup() != null) {
            nodeView.getLabelGroup().setOnMouseClicked((x) -> {
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
            list[i++] = new Pair<>(getGraph().getWeight(e), e);
        }

        Arrays.sort(list, (o1, o2) -> -o1.getFirst().compareTo(o2.getFirst()));
        final ArrayList<Edge> result = new ArrayList<>(list.length);
        for (Pair<Double, Edge> pair : list) {
            result.add(pair.getSecond());
        }
        return result;
    }

    /**
     * compute the anchor center for rotating splits
     */
    private Point2D computeAnchorCenter(Collection<Edge> edges, HashSet<Node> selectedNodes, NodeArray<NodeView2D> node2view) {
        double x = 0;
        double y = 0;
        if (edges.size() > 0) {
            for (Edge edge : edges) {
                if (edge.getOwner() == getGraph()) {
                    final NodeView2D nodeView;
                    if (selectedNodes.contains(edge.getSource()))
                        nodeView = node2view.get(edge.getTarget());
                    else
                        nodeView = node2view.get(edge.getSource());
                    x += nodeView.getLocation().getX();
                    y += nodeView.getLocation().getY();

                }
            }
            x /= edges.size();
            y /= edges.size();
        }
        return new Point2D(x, y);
    }

    /**
     * rotate split by given angle
     */
    private void applySplitRotation(double angle, ObservableList<Edge> selectedEdges, HashSet<Node> selectedNodes, NodeArray<NodeView2D> node2view, EdgeArray<EdgeView2D> edge2view) {
        final Edge e = selectedEdges.get(0);
        if (e.getOwner() == getGraph()) {
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
            Point2D newSelectedPoint = GeometryUtilsFX.rotateAbout(selectedPoint, angle, anchorPoint);
            Point2D translate = newSelectedPoint.subtract(selectedPoint);

            getUndoManager().doAndAdd(new MoveNodesCommand(getPolygons(), node2view, edge2view, selectedNodes, translate.getX(), translate.getY()));
        }
    }

    /**
     * create an edge view
     */
    public EdgeView2D createEdgeView(final Edge e, final Point2D start, final Point2D end, String text) {
        final EdgeView2D edgeView = new EdgeView2D(e, start, end, text);

        final EventHandler<? super MouseEvent> handler = (EventHandler<MouseEvent>) x -> {
            x.consume();
            if (!x.isShiftDown()) {
                splitsSelectionModel.clearSelection();
                nodeSelectionModel.clearSelection();
            }
            selectBySplit(graph, e, splitsSelectionModel, nodeSelectionModel, x.isControlDown());
        };

        if (edgeView.getShape() != null)
            edgeView.getShape().setOnMouseClicked(handler); // todo: need to use shape group here

        if (edgeView.getLabel() != null) {
            edgeView.getLabel().setOnMouseClicked(handler);
        }
        addEdgeLabelMovementSupport(edgeView);
        return edgeView;
    }

    @Override
    public EdgeView2D createEdgeView(Edge e, GraphLayout graphLayout, EdgeView2D.EdgeShape shape, Point2D start, Point2D control1, Point2D mid, Point2D control2, Point2D support, Point2D end, String label) {
        throw new RuntimeException("createEdgeView(): not implemented");
    }


    /**
     * select by split associated with given edge
     */
    public static void selectBySplit(PhyloSplitsGraph graph, Edge e, ItemSelectionModel<Integer> splitsSelectionModel, ItemSelectionModel<Node> nodeSelectionModel, boolean useLargerSide) {
        final int splitId = graph.getSplit(e);
        selectAllNodesOnOneSide(graph, e, nodeSelectionModel, useLargerSide);
        splitsSelectionModel.select((Integer) splitId);
    }


    /**
     * select all nodes on smaller side of graph separated by e
     */
    private static void selectAllNodesOnOneSide(PhyloSplitsGraph graph, Edge e, ItemSelectionModel<Node> nodeSelectionModel, boolean useLargerSide) {
        nodeSelectionModel.clearSelection();
        final NodeSet visited = new NodeSet(graph);
        visitRec(graph, e.getSource(), null, graph.getSplit(e), visited);
        int sourceSize = visited.size();
        int targetSize = graph.getNumberOfNodes() - sourceSize;
        if ((sourceSize > targetSize) == useLargerSide) {
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
    private static void visitRec(PhyloSplitsGraph graph, Node v, Edge e, int splitId, NodeSet visited) {
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

        controller.getShow3DViewerMenuItem().setOnAction((e) -> {
            DataNode dataNode = getDataNode();
            if (dataNode != null && dataNode.getParent() != null && dataNode.getParent().getParent() != null
                    && dataNode.getParent().getParent().getDataBlock() instanceof SplitsBlock) {
                final DataNode<SplitsBlock> splitsNode = dataNode.getParent().getParent();
                for (Connector<SplitsBlock, ? extends DataBlock> child : splitsNode.getChildren()) {
                    if (child.getChild().getDataBlock() instanceof ViewerBlock.SplitsNetwork3DViewerBlock) {
                        getMainWindow().showDataView(child.getChild());
                        return;
                    }
                }
                // no 3d viewer found, set one up
                final Workflow workflow = controller.getMainWindow().getWorkflow();
                final DataNode<ViewerBlock> viewNode = workflow.createDataNode(new ViewerBlock.SplitsNetwork3DViewerBlock());
                workflow.createConnector(splitsNode, viewNode, new SplitsNetworkAlgorithm()).forceRecompute();
                controller.getMainWindow().getWorkflowTab().recompute();
            }
        });
    }

    @Override
    public String getInfo() {
        if (getGraph() != null)
            return "a splits network with " + getGraph().getNumberOfNodes() + " nodes and " + getGraph().getNumberOfEdges() + " edges";
        else
            return "";
    }

    public BitSet getSelectedSplits() {
        BitSet bits = new BitSet();
        for (Edge e : edgeSelectionModel.getSelectedItems()) {
            bits.set(graph.getSplit(e));
        }
        return bits;
    }

    public BitSet getSelectedTaxa() {
        BitSet bits = new BitSet();
        for (Node v : nodeSelectionModel.getSelectedItems()) {
            for (int t : graph.getTaxa(v))
                bits.set(t);
        }
        return bits;
    }
}
