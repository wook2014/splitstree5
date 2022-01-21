/*
 * SplitsView3DTab.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.control.ProgressPane;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.ResourceManagerFX;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloSplitsGraph;
import jloda.util.Pair;
import splitstree5.core.Document;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.graphtab.ISplitsViewTab;
import splitstree5.gui.graphtab.SplitsViewTab;
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
public class SplitsView3DTab extends Graph3DTab<PhyloSplitsGraph> implements ISplitsViewTab {
    private final ItemSelectionModel<Integer> splitsSelectionModel = new ItemSelectionModel<>();
    private boolean inSelection;

    /**
     * constructor
     */
    public SplitsView3DTab() {
        super();
        this.setText("SplitsNetwork3DViewer");
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
    public NodeView3D createNodeView(final Node v, Iterable<Integer> workingTaxonIds, Point2D location, String label) {
        return new NodeView3D(v, workingTaxonIds, new Point3D(location.getX(), location.getY(), 0), label);
    }

    /**
     * setup a node view
     */
    public void setupNodeView(NodeViewBase nv) {
        final NodeView3D nodeView = (NodeView3D) nv;
        final Node v = nodeView.getNode();

        nodeView.getShapeGroup().setOnMousePressed((e) -> {
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
            e.consume();
        });
        nodeView.getShapeGroup().setOnMouseDragged((e) -> {
            e.consume();
            if (!edgeSelectionModel.isEmpty() && nodeSelectionModel.getSelectedItems().contains(nodeView.getNode())) {
                final Pair<Point3D, Point3D> pair = getAnchorAndMover(nodeSelectionModel, edgeSelectionModel, node2view);
                final Point3D anchor = pair.getFirst();
                final Point3D mover = pair.getSecond();
                final Point2D delta = new Point2D(mouseX - e.getScreenX(), mouseY - e.getScreenY());

                //noinspection SuspiciousNameCombination
                final Point3D dragOrthogonalAxis = new Point3D(delta.getY(), -delta.getX(), 0).multiply(-1);
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
        nodeView.getShapeGroup().setOnMouseClicked((x) -> {
            x.consume();
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
        });

        if (nodeView.getLabelGroup() != null) {
            nodeView.getLabelGroup().setOnMouseClicked((e) -> {
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
        addNodeLabelMovementSupport(nodeView);
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
    private Pair<Point3D, Point3D> getAnchorAndMover(ItemSelectionModel<Node> nodeSelectionModel, ItemSelectionModel<Edge> edgeSelectionModel, NodeArray<NodeViewBase> node2view) {
        Edge e = edgeSelectionModel.getSelectedItems().get(0);
        if (!nodeSelectionModel.isSelected(e.getSource()))
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
    public EdgeView3D createEdgeView(final Edge e, final Point2D start, final Point2D end, String text) {
        final EdgeView3D edgeView = new EdgeView3D(e, GeometryUtilsFX.from2Dto3D(start), GeometryUtilsFX.from2Dto3D(end));
        // todo: setLabel(text): not implementyed

        final EventHandler<? super MouseEvent> handler = (EventHandler<MouseEvent>) x -> {
            x.consume();
            if (!x.isShiftDown()) {
                splitsSelectionModel.clearSelection();
                nodeSelectionModel.clearSelection();
            }
            SplitsViewTab.selectBySplit(graph, e, splitsSelectionModel, nodeSelectionModel, x.isControlDown());
        };

        edgeView.getShape().setOnMouseClicked(handler);

        if (edgeView.getLabel() != null) {
            edgeView.getLabel().setOnMouseClicked(handler);
        }
        return edgeView;
    }

    private EmbeddingService service;

    @Override
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);

        controller.getRelaxMenuItem().setOnAction((z) -> {
            controller.getRelaxMenuItem().setDisable(true);
            if (service == null) {
                service = new EmbeddingService();
                service.stateProperty().addListener((c, o, n) -> controller.getRelaxMenuItem().setDisable(n == Worker.State.RUNNING));
                service.setOnSucceeded((x) -> {
                    NodeArray<Point3D> newLocations = service.getValue();
                    for (Node v : graph.nodes()) {
                        ((NodeView3D) node2view.get(v)).setLocation(newLocations.get(v));
                    }
                    for (Edge e : graph.edges()) {
                        ((EdgeView3D) edge2view.get(e)).updateCoordinates(((NodeView3D) node2view.get(e.getSource())).getLocation(), ((NodeView3D) node2view.get(e.getTarget())).getLocation());
                    }
                });
            }
            service.setup(graph, node2view, 5, false, true);
            getDataNode().getDataBlock().getDocument().getMainWindow().getMainWindowController().getBottomPane().getChildren().add(new ProgressPane(service));
            service.restart();
        });
    }

    @Override
    public void setLayout(GraphLayout graphLayout) {
    }

    @Override
    public String getInfo() {
        if (getGraph() != null)
            return "a splits network with " + getGraph().getNumberOfNodes() + " nodes and " + getGraph().getNumberOfEdges() + " edges";
        else return "";
    }
}
