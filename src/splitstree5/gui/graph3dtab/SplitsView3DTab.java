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
package splitstree5.gui.graph3dtab;


import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.NodeSet;
import jloda.phylo.SplitsGraph;
import jloda.util.Pair;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.menu.MenuController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The split network view tab for 3D viewer
 * Daniel Huson, 11.2017
 */
public class SplitsView3DTab extends Graph3DTab<SplitsGraph> implements ISplitsViewTab {
    private final ASelectionModel<Integer> splitsSelectionModel = new ASelectionModel<>();
    private boolean inSelection;
    private DataNode dataNode;

    /**
     * constructor
     */
    public SplitsView3DTab() {
        super();
        label.setText("Splits Network 3D");
        label.setGraphic(new ImageView(ResourceManager.getIcon("SplitsNetworkView16.gif")));
        setText("");
        setGraphic(label);

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
                    final SplitsGraph graph = getGraph();
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
    public void updateSelectionModels(SplitsGraph graph, TaxaBlock taxa, Document document) {
        super.updateSelectionModels(graph, taxa, document);
        splitsSelectionModel.setItems(graph.getSplitIds());

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
    public NodeView3D createNodeView(final Node v, Point2D location, String label) {
        final NodeView3D nv = new NodeView3D(v, new Point3D(location.getX(), location.getY(), 0), label);

        nv.getShapeGroup().setOnMousePressed((e) -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        });
        nv.getShapeGroup().setOnMouseDragged((e) -> {
            e.consume();
            if (!edgeSelectionModel.isEmpty() && nodeSelectionModel.getSelectedItems().contains(nv.getNode())) {
                final Pair<Point3D, Point3D> pair = getAnchorAndMover(nodeSelectionModel, edgeSelectionModel, node2view);
                final Point3D anchor = pair.getFirst();
                final Point3D mover = pair.getSecond();
                final Point2D delta = new Point2D(mouseX - e.getScreenX(), mouseY - e.getScreenY());

                //noinspection SuspiciousNameCombination
                final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0);
                final Rotate rotate = new Rotate(0.25 * delta.magnitude(), anchor.getX(), anchor.getY(), anchor.getZ(), dragOrthogonalAxis);
                final Point3D translateVector = rotate.transform(mover).subtract(mover);

                // move stuff:
                for (Node w : nodeSelectionModel.getSelectedItems()) {
                    ((NodeView3D) node2view.get(w)).translate(translateVector);
                }
                for (Edge f : graph.edges()) {
                    ((EdgeView3D) edge2view.get(f)).updateCoordinates(((NodeView3D) node2view.get(f.getSource())).getLocation(), ((NodeView3D) node2view.get(f.getTarget())).getLocation());
                }
            }
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
        });
        nv.getShapeGroup().setOnMouseClicked((e) -> {
            e.consume();
            splitsSelectionModel.clearSelection();
            edgeSelectionModel.clearSelection();
            if (!e.isShiftDown())
                nodeSelectionModel.clearSelection();
            if (nodeSelectionModel.getSelectedItems().contains(v))
                nodeSelectionModel.clearSelection(v);
            else
                nodeSelectionModel.select(v);
            if (e.getClickCount() >= 2) {
                ArrayList<Edge> edges = getAdjacentEdgesSortedByDecreasingWeight(v);
                int index = Math.min(edges.size() - 1, e.getClickCount() - 2);
                selectBySplit(edges.get(index));
            }
        });

        if (nv.getLabelGroup() != null) {
            nv.getLabelGroup().setOnMouseClicked((e) -> {
                e.consume();
                splitsSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
                if (!e.isShiftDown())
                    nodeSelectionModel.clearSelection();
                if (nodeSelectionModel.getSelectedItems().contains(v))
                    nodeSelectionModel.clearSelection(v);
                else
                    nodeSelectionModel.select(v);
            });
        }
        addNodeLabelMovementSupport(nv);

        return nv;
    }

    /**
     * gets the location of the anchor and mover nodes
     * Assumes there is an selected edge and that every selected edge is incident to precisely one not selected node
     *
     * @param nodeSelectionModel
     * @param edgeSelectionModel
     * @param node2view
     * @return anchor and mover
     */
    private Pair<Point3D, Point3D> getAnchorAndMover(ASelectionModel<Node> nodeSelectionModel, ASelectionModel<Edge> edgeSelectionModel, NodeArray<NodeViewBase> node2view) {
        Edge e = edgeSelectionModel.getSelectedItem();
        if (!nodeSelectionModel.getSelectedItems().contains(e.getSource()))
            return new Pair<>(((NodeView3D) node2view.get(e.getSource())).getLocation(), ((NodeView3D) node2view.get(e.getTarget())).getLocation());
        else
            return new Pair<>(((NodeView3D) node2view.get(e.getTarget())).getLocation(), ((NodeView3D) node2view.get(e.getSource())).getLocation());
    }

    public void addNodeLabelMovementSupport(NodeView3D nodeView) {
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
     * create an edge view
     */
    public EdgeView3D createEdgeView(final SplitsGraph graph, final Edge e, final Double weight, final Point2D start, final Point2D end) {
        final EdgeView3D edgeView = new EdgeView3D(e, weight, from2to3D(start), from2to3D(end));
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

        edgeView.getShape().setOnMouseClicked(handler);

        if (edgeView.getLabel() != null) {
            edgeView.getLabel().setOnMouseClicked(handler);
        }
        return edgeView;
    }

    /**
     * select by split associated with given edge
     */
    public void selectBySplit(Edge e) {
        final int splitId = getGraph().getSplit(e);
        if (!splitsSelectionModel.getSelectedItems().contains(splitId)) {
            splitsSelectionModel.clearSelection();
            selectAllNodesOnSmallerSide(getGraph(), e, nodeSelectionModel);
            splitsSelectionModel.select((Integer) splitId);
        }
    }

    /**
     * select all nodes on smaller side of graph separated by e
     */
    private static void selectAllNodesOnSmallerSide(SplitsGraph graph, Edge e, ASelectionModel<Node> nodeSelectionModel) {
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
    private static void visitRec(SplitsGraph graph, Node v, Edge e, int splitId, NodeSet visited) {
        if (!visited.contains(v)) {
            visited.add(v);
            for (Edge f : v.adjacentEdges()) {
                if (graph.getSplit(f) != splitId && f != e)
                    visitRec(graph, f.getOpposite(v), f, splitId, visited);
            }
        }
    }

    private EmbeddingService service;
    private int iterations;

    @Override
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);

        controller.getRelaxMenuItem().setOnAction((z) -> {
            iterations = 50;

            controller.getRelaxMenuItem().setDisable(true);
            if (service == null) {
                service = new EmbeddingService();
                service.setOnSucceeded((x) -> {
                    NodeArray<Point3D> newLocations = service.getValue();
                    for (Node v : graph.nodes()) {
                        ((NodeView3D) node2view.get(v)).setLocation(newLocations.get(v));
                    }
                    for (Edge e : graph.edges()) {
                        ((EdgeView3D) edge2view.get(e)).updateCoordinates(((NodeView3D) node2view.get(e.getSource())).getLocation(), ((NodeView3D) node2view.get(e.getTarget())).getLocation());
                    }
                    if (--iterations >= 0) {
                        service.restart();
                    }
                });
                service.stateProperty().addListener((c, o, n) -> {
                    if (iterations == 0)
                        controller.getRelaxMenuItem().setDisable(n != Worker.State.RUNNING);
                });
            }
            service.setup(graph, node2view, 5, false, true);
            service.restart();
        });
    }

    private static Point3D from2to3D(Point2D point) {
        return new Point3D(point.getX(), point.getY(), 0);
    }

    private static Point2D from3to2D(Point3D point) {
        return new Point2D(point.getX(), point.getY());
    }

    @Override
    public void setLayout(GraphLayout graphLayout) {

    }

    @Override
    public ViewerTab getTab() {
        return this;
    }

    @Override
    public DataNode getDataNode() {
        return dataNode;
    }

    @Override
    public void setDataNode(DataNode dataNode) {
        this.dataNode = dataNode;
    }
}
