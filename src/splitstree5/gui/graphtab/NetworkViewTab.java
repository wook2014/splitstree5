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


import com.sun.javafx.charts.Legend;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.ResourceManager;
import splitstree5.gui.graphtab.base.EdgeView2D;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.gui.graphtab.commands.MoveNodesCommand;
import splitstree5.menu.MenuController;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * The network viewer tab
 * Daniel Huson, 1.2018
 */
public class NetworkViewTab extends Graph2DTab<PhyloGraph> {
    private final ObjectProperty<Legend> legend = new SimpleObjectProperty<>();

    /**
     * constructor
     */
    public NetworkViewTab() {
        super();
        label.setText("NetworkView");
        label.setGraphic(new ImageView(ResourceManager.getIcon("NetworkView16.gif")));
        setText("");
        setGraphic(label);
    }


    /**
     * show the tree
     */
    public void show() {
        super.show();
        Platform.runLater(() -> {
            if (getLegend() != null) {
                borderPane.setBottom(getLegend());
            } else borderPane.setBottom(null);
        });
    }

    private double mouseX;
    private double mouseY;

    /**
     * create a node view
     *
     * @param v
     * @param location
     * @param text
     * @return
     */
    public NodeView2D createNodeView(Node v, Point2D location, String text) {
        final NodeView2D nodeView = new NodeView2D(v, location, text);

        nodeView.getShapeGroup().setOnMousePressed((e) -> {
            e.consume();
            mouseX = e.getScreenX();
            mouseY = e.getScreenY();
        });

        nodeView.getShapeGroup().setOnMouseDragged((e) -> {
            e.consume();
            final double deltaX = e.getScreenX() - mouseX;
            final double deltaY = e.getScreenY() - mouseY;
            if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                if (nodeSelectionModel.getSelectedItems().contains(nodeView.getNode())) {
                    getUndoManager().doAndAdd(new MoveNodesCommand(node2view, edge2view, nodeSelectionModel.getSelectedItems(), deltaX, deltaY));
                }
                mouseX = e.getScreenX();
                mouseY = e.getScreenY();
            }
        });

        if (nodeView.getShapeGroup() != null) {
            nodeView.getShapeGroup().setOnMouseClicked((x) -> {
                x.consume();
                edgeSelectionModel.clearSelection();
                if (!x.isShiftDown())
                    nodeSelectionModel.clearSelection();
                if (nodeSelectionModel.getSelectedItems().contains(v))
                    nodeSelectionModel.clearSelection(v);
                else {
                    nodeSelectionModel.select(v);
                    if (nodeSelectionModel.getSelectedItems().contains(v) && x.getClickCount() == 2) {
                        selectAllBelowRec(v, nodeSelectionModel, edgeSelectionModel);
                    }
                }
            });
        }

        if (nodeView.getLabel() != null) {
            nodeView.getLabel().setOnMouseClicked((x) -> {
                x.consume();
                edgeSelectionModel.clearSelection();
                if (!x.isShiftDown())
                    nodeSelectionModel.clearSelection();
                if (nodeSelectionModel.getSelectedItems().contains(v))
                    nodeSelectionModel.clearSelection(v);
                else
                    nodeSelectionModel.select(v);
            });
        }

        addNodeLabelMovementSupport(nodeView);
        return nodeView;
    }


    /**
     * create an edge view
     */
    public EdgeView2D createEdgeView(final Edge e, final Point2D start, final Point2D end) {
        final EdgeView2D edgeView = new EdgeView2D(e, 1.0, start, end);

        if (edgeView.getShape() != null) {
            edgeView.getShape().setOnMouseClicked((x) -> {
                if (!x.isShiftDown()) {
                    edgeSelectionModel.clearSelection();
                    nodeSelectionModel.clearSelection();
                }
                if (edgeSelectionModel.getSelectedItems().contains(e))
                    edgeSelectionModel.clearSelection(e);
                else {
                    edgeSelectionModel.select(e);
                    if (edgeSelectionModel.getSelectedItems().contains(e) && x.getClickCount() == 2) {
                        selectAllBelowRec(e.getTarget(), nodeSelectionModel, edgeSelectionModel);
                    }
                }
                x.consume();
            });
        }

        if (edgeView.getLabel() != null) {
            edgeView.getLabel().setOnMouseClicked((x) -> {
                if (!x.isShiftDown()) {
                    edgeSelectionModel.clearSelection();
                    nodeSelectionModel.clearSelection();
                }
                if (edgeSelectionModel.getSelectedItems().contains(e))
                    edgeSelectionModel.clearSelection(e);
                else
                    edgeSelectionModel.select(e);
                x.consume();
            });
        }

        return edgeView;
    }

    /**
     * select all nodes below the given one
     *
     * @param v
     * @param nodeSelectionModel
     */
    private void selectAllBelowRec(Node v, ASelectionModel<Node> nodeSelectionModel, ASelectionModel<Edge> edgeSelectionModel) {
        for (Edge e : v.outEdges()) {
            nodeSelectionModel.select(e.getTarget());
            edgeSelectionModel.select(e);
            selectAllBelowRec(e.getTarget(), nodeSelectionModel, edgeSelectionModel);
        }
    }

    public Legend getLegend() {
        return legend.get();
    }

    public ObjectProperty<Legend> legendProperty() {
        return legend;
    }

    public void setLegend(Legend legend) {
        this.legend.set(legend);
    }

    @Override
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);
        controller.getSelectAllBelowMenuItem().setOnAction((e) -> {
            final Stack<Node> stack = new Stack<>();
            final Set<Node> nodesToSelect = new HashSet<>();
            stack.addAll(nodeSelectionModel.getSelectedItems());
            while (stack.size() > 0) {
                final Node v = stack.pop();
                for (Edge edge : v.outEdges()) {
                    final Node w = edge.getTarget();
                    stack.push(w);
                    nodesToSelect.add(w);
                }
            }
            nodeSelectionModel.selectItems(nodesToSelect);
        });
        controller.getSelectAllBelowMenuItem().disableProperty().bind(nodeSelectionModel.emptyProperty());

        controller.getSelectAllEdgesBelowMenuItem().setOnAction((e) -> {
            final Stack<Node> stack = new Stack<>();
            final Set<Edge> edgesToSelect = new HashSet<>();
            stack.addAll(nodeSelectionModel.getSelectedItems());
            while (stack.size() > 0) {
                final Node v = stack.pop();
                for (Edge edge : v.outEdges()) {
                    final Node w = edge.getTarget();
                    stack.push(w);
                    edgesToSelect.add(edge);
                }
            }
            edgeSelectionModel.selectItems(edgesToSelect);
        });
        controller.getSelectAllEdgesBelowMenuItem().disableProperty().bind(nodeSelectionModel.emptyProperty());

    }


}
