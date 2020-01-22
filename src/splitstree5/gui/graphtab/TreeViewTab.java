/*
 *  TreeViewTab.java Copyright (C) 2020 Daniel H. Huson
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


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.fx.shapes.NodeShape;
import jloda.fx.util.ResourceManagerFX;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.gui.graphlabels.LabelsEditor;
import splitstree5.gui.graphtab.base.EdgeView2D;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.menu.MenuController;

import java.util.*;

/**
 * The tree viewer tab
 * Daniel Huson, 11.2017
 */
public class TreeViewTab extends Graph2DTab<PhyloTree> {
    /**
     * constructor
     */
    public TreeViewTab() {
        this(true);
    }

    public TreeViewTab(boolean withScrollPane) {
        super(withScrollPane);
        setText("TreeViewer");
        setGraphic(new ImageView(ResourceManagerFX.getIcon("TreeViewer16.gif")));
    }

    /**
     * show the tree
     */
    public void show() {
        super.show();
    }

    /**
     * creates a node view
     *
     * @param v
     * @param location
     * @param shape
     * @param shapeWidth
     * @param shapeHeight
     * @param text
     * @return node view
     */
    @Override
    public NodeView2D createNodeView(Node v, Point2D location, NodeShape shape, double shapeWidth, double shapeHeight, String text) {
        final NodeView2D nodeView = new NodeView2D(v, location, shape, shapeWidth, shapeHeight, text);
        if (nodeView.getShapeGroup() != null) {
            nodeView.getShapeGroup().setOnMouseClicked((x) -> {
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
                x.consume();
            });
        }

        if (nodeView.getLabel() != null) {
            nodeView.getLabel().setOnMouseClicked((x) -> {
                edgeSelectionModel.clearSelection();
                if (!x.isShiftDown())
                    nodeSelectionModel.clearSelection();
                if (nodeSelectionModel.getSelectedItems().contains(v))
                    nodeSelectionModel.clearSelection(v);
                else
                    nodeSelectionModel.select(v);

                // call label editor on double click
                if(x.getClickCount() == 2){
                    LabelsEditor labelsEditor = new LabelsEditor(getMainWindow(), nodeView.getLabel(), nodeLabelSearcher, this);
                    labelsEditor.show();
                }

                x.consume();
            });
        }
        addNodeLabelMovementSupport(nodeView);
        return nodeView;
    }


    /**
     * create an edge view
     *
     * @param e
     * @param layout
     * @param shape
     * @param start
     * @param control1
     * @param mid
     * @param control2
     * @param support
     * @param end
     * @param text
     * @return node view
     */
    @Override
    public EdgeView2D createEdgeView(Edge e, GraphLayout layout, EdgeView2D.EdgeShape shape, Point2D start, Point2D control1, Point2D mid, Point2D control2, Point2D support, Point2D end, String text) {

        final EdgeView2D edgeView = new EdgeView2D(e, layout, shape, start, control1, mid, control2, support, end, text);

        if (edgeView.getShape() != null) {
            edgeView.getShape().setOnMouseClicked((x) -> { // todo: need to use shape group here
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
        addEdgeLabelMovementSupport(edgeView);
        return edgeView;
    }

    @Override
    public EdgeView2D createEdgeView(Edge e, Point2D start, Point2D end, String label) {
        return new EdgeView2D(e, start, end, label);
    }


    /**
     * select all nodes below the given one
     *
     * @param v
     * @param nodeSelectionModel
     */
    private void selectAllBelowRec(Node v, AMultipleSelectionModel<Node> nodeSelectionModel, AMultipleSelectionModel<Edge> edgeSelectionModel) {
        for (Edge e : v.outEdges()) {
            nodeSelectionModel.select(e.getTarget());
            edgeSelectionModel.select(e);
            selectAllBelowRec(e.getTarget(), nodeSelectionModel, edgeSelectionModel);
        }
    }

    @Override
    public void updateMenus(MenuController controller) {
        super.updateMenus(controller);

        final EventHandler<ActionEvent> copyLabels = controller.getCopyMenuItem().getOnAction();
        controller.getCopyMenuItem().setOnAction((e) -> {
            if (nodeSelectionModel.isEmpty() && edgeSelectionModel.isEmpty()) {
                Map<String, String> translate = new HashMap<>();
                for (Node v : graph.nodes()) {
                    if (v.isLeaf() && graph.getLabel(v) != null) {
                        translate.put(graph.getLabel(v), node2view.get(v).getLabel().getText());
                    }
                }
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                // todo: need to implement
                //content.putString(graph.toString(true, translate) + ";");
                clipboard.setContent(content);
            } else copyLabels.handle(e);
        });
        controller.getCopyMenuItem().disableProperty().unbind();
        controller.getCopyMenuItem().setDisable(false);


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

    @Override
    public String getInfo() {
        if (graph != null)
            return "a " + (getLayout() == GraphLayout.Radial ? "unrooted" : "rooted") + " tree drawing with "
                    + getGraph().getNumberOfNodes() + " nodes and "
                    + getGraph().getNumberOfEdges() + " edge";
        else return "";
    }

}
