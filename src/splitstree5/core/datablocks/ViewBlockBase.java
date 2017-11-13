/*
 *  Copyright (C) 2016 Daniel H. Huson
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

import javafx.collections.ListChangeListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jloda.fx.ASelectionModel;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.util.Single;
import splitstree5.core.datablocks.view.AEdgeView;
import splitstree5.core.datablocks.view.ANodeView;
import splitstree5.utils.SelectionEffect;

public abstract class ViewBlockBase extends ADataBlock {
    protected final Group group = new Group();
    protected final Group edgesGroup = new Group();
    protected final Group nodesGroup = new Group();
    protected final Group edgeLabelsGroup = new Group();
    protected final Group nodeLabelsGroup = new Group();
    protected final ASelectionModel<Node> nodeSelectionModel = new ASelectionModel<>();
    protected final ASelectionModel<Edge> edgeSelectionModel = new ASelectionModel<>();
    protected final BorderPane rootNode = new BorderPane();
    protected final Scene scene = new Scene(rootNode, 600, 600);
    private PhyloGraph phyloGraph;
    protected NodeArray<ANodeView> node2view;
    protected EdgeArray<AEdgeView> edge2view;
    protected Stage stage = null;

    /**
     * constructor
     */
    public ViewBlockBase() {
        nodeSelectionModel.getSelectedItems().addListener((ListChangeListener<Node>) c -> {
            if (c.next()) {
                for (Node v : c.getAddedSubList()) {
                    if (v.getOwner() != null) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv != null) {
                            if (nv.getLabel() != null)
                                nv.getLabel().setEffect(SelectionEffect.getInstance());
                            if (nv.getShape() != null)
                                nv.getShape().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Node v : c.getRemoved()) {
                    if (v.getOwner() != null) {
                        final ANodeView nv = getNode2view().get(v);
                        if (nv.getLabel() != null)
                            nv.getLabel().setEffect(null);
                        if (nv.getShape() != null)
                            nv.getShape().setEffect(null);
                    }
                }
            }
        });
        edgeSelectionModel.getSelectedItems().addListener((ListChangeListener<Edge>) c -> {
            if (c.next()) {
                for (Edge e : c.getAddedSubList()) {
                    if (e.getOwner() != null) {
                        final AEdgeView nv = getEdge2view().getValue(e);
                        if (nv != null) {
                            if (nv.getLabel() != null)
                                nv.getLabel().setEffect(SelectionEffect.getInstance());
                            if (nv.getShape() != null)
                                nv.getShape().setEffect(SelectionEffect.getInstance());
                        }
                    }
                }
                for (Edge e : c.getRemoved()) {
                    if (e.getOwner() != null) {
                        final AEdgeView ev = getEdge2view().getValue(e);
                        if (ev.getLabel() != null)
                            ev.getLabel().setEffect(null);
                        if (ev.getShape() != null)
                            ev.getShape().setEffect(null);
                    }
                }
            }
        });
    }

    @Override
    public int size() {
        return edgesGroup.getChildren().size() + nodesGroup.getChildren().size();
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

    public void updateSelectionModels(PhyloGraph graph) {
        nodeSelectionModel.setItems(graph.getNodesAsSet().toArray(new Node[graph.getNumberOfNodes()]));
        edgeSelectionModel.setItems(graph.getEdgesAsSet().toArray(new Edge[graph.getNumberOfEdges()]));
    }

    public NodeArray<ANodeView> getNode2view() {
        return node2view;
    }

    public EdgeArray<AEdgeView> getEdge2view() {
        return edge2view;
    }

    public Dimension2D getTargetDimensions() {
        return new Dimension2D(0.6 * scene.getWidth(), 0.9 * scene.getHeight());
    }

    /**
     * rescale node and edge label fonts so that they maintain the same apparent size during scaling
     *
     * @param factor
     */
    protected void rescaleNodesAndEdgesToKeepApparentSizes(double factor) {
        if (phyloGraph != null) {
            for (Node v : phyloGraph.nodes()) {
                ANodeView nv = node2view.getValue(v);
                if (nv.getLabel() != null && nv.getLabel() instanceof Labeled) {
                    Font font = ((Labeled) nv.getLabel()).getFont();
                    double newSize = font.getSize() / factor;
                    nv.getLabel().setStyle(String.format("-fx-font-size: %.3f;", newSize));
                }
                if (nv.getShape() != null && nv.getShape() instanceof Shape) {
                    final Shape shape = (Shape) nv.getShape();
                    //shape.setStrokeWidth(shape.getStrokeWidth() / factor);

                    shape.setScaleX(shape.getScaleX() / factor);
                    shape.setScaleY(shape.getScaleY() / factor);
                }
            }
            for (Edge e : phyloGraph.edges()) {
                AEdgeView ev = edge2view.getValue(e);

                if (ev.getShape() != null && ev.getShape() instanceof Shape) {
                    double strokeWidth = ((Shape) ev.getShape()).getStrokeWidth();
                    ((Shape) ev.getShape()).setStrokeWidth(strokeWidth / factor);
                }

                if (ev.getLabel() != null && ev.getLabel() instanceof Labeled) {
                    Font font = ((Labeled) ev.getLabel()).getFont();
                    double newSize = font.getSize() / factor;

                    ev.getLabel().setStyle(String.format("-fx-font-size: %.3f;", newSize));
                }
            }
        }
    }

    PhyloGraph getPhyloGraph() {
        return phyloGraph;
    }

    @Override
    public void clear() {
        super.clear();
    }

    /**
     * initialize datastructures
     *
     * @param phyloGraph
     */
    public void init(PhyloGraph phyloGraph) {
        this.phyloGraph = phyloGraph;
        node2view = new NodeArray<>(phyloGraph);
        edge2view = new EdgeArray<>(phyloGraph);
        group.setScaleX(1);
        group.setScaleY(1);
    }


    /**
     * create a simple node view
     *
     * @param location
     * @param text
     * @return
     */
    public static ANodeView createNodeView(jloda.graph.Node v, Point2D location, String text, ASelectionModel<jloda.graph.Node> selectionModel) {
        final ANodeView nodeView = new ANodeView(v);
        nodeView.setLocation(location);
        Circle circle = new Circle(location.getX(), location.getY(), 2);
        nodeView.setShape(circle);
        circle.setFill(Color.BLUE);
        Label label;
        if (text != null && text.length() > 0) {
            label = new Label(text);
            label.setLayoutX(location.getX() + circle.getRadius());
            label.setLayoutY(location.getY());
            nodeView.setLabel(label);
        } else
            label = null;
        circle.setOnMouseClicked((e) -> {
            if (!e.isShiftDown())
                selectionModel.clearSelection();
            if (selectionModel.getSelectedItems().contains(v))
                selectionModel.clearSelection(v);
            else
                selectionModel.select(v);
        });
        if (label != null) {
            label.setOnMouseClicked((e) -> {
                if (!e.isShiftDown())
                    selectionModel.clearSelection();
                if (selectionModel.getSelectedItems().contains(v))
                    selectionModel.clearSelection(v);
                else
                    selectionModel.select(v);
            });
            final Single<Point2D> point = new Single<>();
            label.setOnMousePressed((e) -> {
                if (selectionModel.getSelectedItems().contains(v))
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
            });
            label.setOnMouseDragged((e) -> {
                if (point.get() != null) {
                    double deltaX = e.getScreenX() - point.get().getX();
                    double deltaY = e.getScreenY() - point.get().getY();
                    point.set(new Point2D(e.getScreenX(), e.getScreenY()));
                    if (deltaX != 0)
                        label.setLayoutX(label.getLayoutX() + deltaX);
                    if (deltaY != 0)
                        label.setLayoutY(label.getLayoutY() + deltaY);
                }
            });
            label.setOnMouseReleased((e) -> {
                point.set(null);
            });
        }
        return nodeView;
    }

}
