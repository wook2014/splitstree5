/*
 *  PhyloEditor.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.tools.phyloedit;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.graph.GraphFX;
import jloda.fx.shapes.CircleShape;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.MouseDragClosestNode;
import jloda.fx.util.MouseDragToTranslate;
import jloda.fx.util.SelectionEffectBlue;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import jloda.util.Pair;
import jloda.util.Single;
import splitstree5.tools.phyloedit.actions.MoveSelectedNodesCommand;
import splitstree5.tools.phyloedit.actions.NewEdgeAndNodeCommand;
import splitstree5.tools.phyloedit.actions.NodeViewContextMenu;
import splitstree5.tools.phyloedit.actions.TranslateCommand;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * editor
 * Daniel Huson, 1.2020
 */
public class PhyloEditor {
    public enum EdgeType {treeEdge, reticulateEdge}

    private final PhyloTree graph = new PhyloTree();
    private final GraphFX<PhyloTree> graphFX = new GraphFX<>(graph);
    private final UndoManager undoManager = new UndoManager();

    private final static ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font("Helvetica", 12));

    private final PhyloEditorMain main;
    private final NodeArray<Pair<Shape, Label>> node2shapeAndLabel;
    private final EdgeArray<EdgeView> edge2view;

    private final ItemSelectionModel<Node> nodeSelection = new ItemSelectionModel<>();
    private final ItemSelectionModel<Edge> edgeSelection = new ItemSelectionModel<>();

    private final Group world;
    private final Group spacers;
    private final Group graphNodes;
    private final Group graphNodeLabels;
    private final Group graphEdges;
    private final Group graphEdgeLabels;

    /**
     * constructor
     */
    public PhyloEditor(PhyloEditorMain main) {
        this.main = main;

        spacers = new Group();
        graphNodes = new Group();
        graphNodeLabels = new Group();
        graphEdges = new Group();
        graphEdgeLabels = new Group();

        this.world = new Group(spacers, graphEdges, graphNodes, graphEdgeLabels, graphNodeLabels);

        node2shapeAndLabel = new NodeArray<>(graph);
        edge2view = new EdgeArray<>(graph);

        nodeSelection.getSelectedItems().addListener((ListChangeListener<Node>) (e) -> {
            while (e.next()) {
                for (Node v : e.getAddedSubList()) {
                    final Pair<Shape, Label> pair = node2shapeAndLabel.get(v);
                    if (pair != null) {
                        for (Object oNode : pair) {
                            ((javafx.scene.Node) oNode).setEffect(SelectionEffectBlue.getInstance());
                        }
                    }
                }
                for (Node v : e.getRemoved()) {
                    final Pair<Shape, Label> pair = node2shapeAndLabel.get(v);
                    if (pair != null) {
                        for (Object oNode : pair) {
                            ((javafx.scene.Node) oNode).setEffect(null);
                        }
                    }
                }
            }
        });

        edgeSelection.getSelectedItems().addListener((ListChangeListener<Edge>) (e) -> {
            while (e.next()) {
                for (Edge edge : e.getAddedSubList()) {
                    final EdgeView edgeView = edge2view.get(edge);
                    if (edgeView != null) {
                        for (javafx.scene.Node node : edgeView.getChildren())
                            node.setEffect(SelectionEffectBlue.getInstance());
                    }
                }
                for (Edge edge : e.getRemoved()) {
                    final EdgeView edgeView = edge2view.get(edge);
                    if (edgeView != null) {
                        for (javafx.scene.Node node : edgeView.getChildren())
                            node.setEffect(null);
                    }
                }
            }
        });
    }

    public void clear() {
        nodeSelection.clearSelection();
        edgeSelection.clearSelection();
        graph.clear();
        world.getChildren().clear();
    }

    public void addNode(Pane pane, double x, double y, Node v) {
        final Shape shape;
        final Label label;

        shape = new CircleShape(10);
        shape.setStroke(Color.BLACK);
        shape.setFill(Color.WHITE);
        shape.setStrokeWidth(2);

        if (graph.getLabel(v) != null)
            label = new Label(graph.getLabel(v));
        else
            label = new Label();

        shape.setTranslateX(x);
        shape.setTranslateY(y);
        setupMouseInteraction(pane, shape, v);
        graphNodes.getChildren().add(shape);

        label.setFont(getFont());
        label.setLayoutX(10);
        label.translateXProperty().bind(shape.translateXProperty());
        label.translateYProperty().bind(shape.translateYProperty());
        graphNodeLabels.getChildren().add(label);
        setupMouseInteraction(pane, label, v);

        if (true) {  // add spacers to prevent graph from moving when molecules with long names arrive at a node that is on the boundary of the graph
            final Circle spacer = new Circle(100);
            spacer.translateXProperty().bind(shape.translateXProperty());
            spacer.translateYProperty().bind(shape.translateYProperty());
            spacer.setFill(Color.TRANSPARENT);
            spacer.setStroke(Color.TRANSPARENT);
            spacer.setMouseTransparent(true);

            spacers.getChildren().add(spacer);
        }

        label.textProperty().addListener((c, o, n) -> graph.setLabel(v, n));

        shape.setOnContextMenuRequested((c) -> {
            (new NodeViewContextMenu(main.getStage(), undoManager, v, shape, label)).show(main.getStage(), c.getScreenX(), c.getScreenY());
        });

        node2shapeAndLabel.put(v, new Pair<>(shape, label));
    }

    public void removeNode(Node v) {
        for (Edge e : v.adjacentEdges()) {
            graphEdges.getChildren().remove(edge2view.get(e).cubicCurve);
        }
        Pair<Shape, Label> pair = node2shapeAndLabel.get(v);
        if (pair != null) {
            graphNodes.getChildren().remove(pair.get1());
            graphNodeLabels.getChildren().remove(pair.get2());
        }
    }

    public void addEdge(Edge e) {
        final Shape sourceShape = node2shapeAndLabel.get(e.getSource()).getFirst();
        final Shape targetShape = node2shapeAndLabel.get(e.getTarget()).getFirst();

        EdgeView edgeView = createPath(e, sourceShape.translateXProperty(), sourceShape.translateYProperty(), targetShape.translateXProperty(), targetShape.translateYProperty(), EdgeType.reticulateEdge);
        edge2view.setValue(e, edgeView);
        graphEdges.getChildren().add(edgeView.cubicCurve);
    }

    public void removeEdge(Edge e) {
        graphEdges.getChildren().remove(edge2view.get(e));
    }

    enum What {moveNode, growEdge}

    /**
     * setup mouse interaction
     */
    private void setupMouseInteraction(Pane pane, javafx.scene.Node mouseTarget, Node v) {
        mouseTarget.setCursor(Cursor.CROSSHAIR);

        final double[] mouseDownPosition = new double[2];
        final double[] previousMousePosition = new double[2];
        final Single<Boolean> moved = new Single<>(false);
        final Single<What> what = new Single<>(null);
        final Line line = new Line();
        world.getChildren().add(line);

        mouseTarget.setOnMousePressed(c -> {
            previousMousePosition[0] = c.getSceneX();
            previousMousePosition[1] = c.getSceneY();
            mouseDownPosition[0] = c.getSceneX();
            mouseDownPosition[1] = c.getSceneY();
            moved.set(false);
            what.set(c.isShiftDown() ? What.growEdge : What.moveNode);
            if (what.get() == What.growEdge) {
                final Shape shape = node2shapeAndLabel.get(v).get1();
                line.setStartX(shape.getTranslateX());
                line.setStartY(shape.getTranslateY());

                final Point2D location = pane.sceneToLocal(previousMousePosition[0], previousMousePosition[1]);

                line.setEndX(location.getX());
                line.setEndY(location.getY());
                line.setVisible(true);
            }
            c.consume();
        });

        mouseTarget.setOnMouseDragged(c -> {
            final double mouseX = c.getSceneX();
            final double mouseY = c.getSceneY();

            if (what.get() == What.moveNode) {
                getNodeSelection().select(v);

                for (Node u : getNodeSelection().getSelectedItems()) {
                    if (mouseTarget instanceof Shape) {
                        final Shape shape = node2shapeAndLabel.get(u).get1();
                        shape.setTranslateX(shape.getTranslateX() + (mouseX - previousMousePosition[0]));
                        shape.setTranslateY(shape.getTranslateY() + (mouseY - previousMousePosition[1]));
                    } else if (mouseTarget instanceof Label) {
                        final Label label = node2shapeAndLabel.get(u).get2();
                        label.setLayoutX(label.getLayoutX() + (mouseX - previousMousePosition[0]));
                        label.setLayoutY(label.getLayoutY() + (mouseY - previousMousePosition[1]));
                    }
                }
            }
            if (what.get() == What.growEdge) {
                getNodeSelection().clearSelection();
                getNodeSelection().select(v);

                final Point2D location = pane.sceneToLocal(mouseX, mouseY);
                line.setEndX(location.getX());
                line.setEndY(location.getY());
            }

            moved.set(true);
            previousMousePosition[0] = c.getSceneX();
            previousMousePosition[1] = c.getSceneY();
            c.consume();
        });

        mouseTarget.setOnMouseReleased(c -> {
            if (!moved.get()) {
                if (!c.isShiftDown()) {
                    nodeSelection.clearSelection();
                    edgeSelection.clearSelection();
                    nodeSelection.select(v);
                } else {
                    if (nodeSelection.getSelectedItems().contains(v))
                        nodeSelection.clearSelection(v);
                    else
                        nodeSelection.select(v);
                }
            } else {
                if (what.get() == What.moveNode) {
                    // yes, add, not doAndAdd()
                    undoManager.add(new MoveSelectedNodesCommand(previousMousePosition[0] - mouseDownPosition[0], previousMousePosition[1] - mouseDownPosition[1], this, nodeSelection.getSelectedItems()));

                } else if (what.get() == What.growEdge) {
                    final double x = line.getEndX();
                    final double y = line.getEndY();

                    final Node w = findNodeIfHit(c.getScreenX(), c.getScreenY());
                    undoManager.doAndAdd(new NewEdgeAndNodeCommand(pane, this, v, w, x, y));
                }
                moved.set(false);
            }
            line.setVisible(false);
        });
    }

    public void moveNode(Node v, double x, double y) {
        final Pair<Shape, Label> pair = node2shapeAndLabel.get(v);
        pair.getFirst().setTranslateX(pair.getFirst().getTranslateX() + x);
        pair.getFirst().setTranslateY(pair.getFirst().getTranslateY() + y);
    }


    private Node findNodeIfHit(double x, double y) {
        for (Node v : graph.nodes()) {
            final Shape shape = node2shapeAndLabel.get(v).getFirst();
            if (shape.contains(shape.screenToLocal(x, y)))
                return v;
        }
        return null;
    }

    /**
     * create a path to represent an edge
     *
     * @param aX
     * @param aY
     * @param bX
     * @param bY
     * @param edgeType
     * @return path
     */
    private EdgeView createPath(Edge edge, ReadOnlyDoubleProperty aX, ReadOnlyDoubleProperty aY, ReadOnlyDoubleProperty bX, ReadOnlyDoubleProperty bY, EdgeType edgeType) {
        final Shape arrowHead = new Polyline(-5, -3, 5, 0, -5, 3);
        arrowHead.setStroke(Color.BLACK);
        arrowHead.setStrokeWidth(2);

        final CubicCurve curve = new CubicCurve();
        curve.setFill(Color.TRANSPARENT);
        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(2);
        curve.setPickOnBounds(true);

        curve.startXProperty().bind(aX);
        curve.startYProperty().bind(aY);
        curve.endXProperty().bind(bX);
        curve.endYProperty().bind(bY);

        Circle circle1 = new Circle();
        circle1.translateXProperty().bindBidirectional(curve.controlX1Property());
        circle1.translateYProperty().bindBidirectional(curve.controlY1Property());
        circle1.setTranslateX(0.3 * curve.getStartX() + 0.7 * curve.getEndX());
        circle1.setTranslateY(0.3 * curve.getStartY() + 0.7 * curve.getEndY());

        Circle circle2 = new Circle();
        circle2.translateXProperty().bindBidirectional(curve.controlX2Property());
        circle2.translateYProperty().bindBidirectional(curve.controlY2Property());
        circle2.setTranslateX(0.7 * curve.getStartX() + 0.3 * curve.getEndX());
        circle2.setTranslateY(0.7 * curve.getStartY() + 0.3 * curve.getEndY());

        final int id = edge.getId();

        // reference current translating node
        final Function<Circle, javafx.scene.Node> translatingNode = (circle) -> {
            final Edge e = graph.searchEdgeId(id);
            final EdgeView edgeView = edge2view.get(e);
            if (circle == circle1)
                return edgeView.circle1;
            else
                return edgeView.circle2;
        };

        MouseDragClosestNode.setup(curve, node2shapeAndLabel.get(edge.getSource()).get1(), circle1,
                node2shapeAndLabel.get(edge.getTarget()).get1(), circle2,
                (circle, delta) -> {
                    undoManager.add(new TranslateCommand("Edge Shape", translatingNode.apply((Circle) circle), delta));
                });


        //arrowHead.translateXProperty().bindBidirectional(curve.controlXProperty());
        //arrowHead.translateYProperty().bindBidirectional(curve.controlYProperty());

        MouseDragToTranslate.setup(arrowHead);

        final InvalidationListener invalidationListener = (e) -> {
            final double angle = GeometryUtilsFX.computeAngle(new Point2D(curve.getEndX() - curve.getControlX2(), curve.getEndY() - curve.getControlY2()));
            arrowHead.setRotationAxis(new Point3D(0, 0, 1));
            arrowHead.setRotate(angle);

            final Point2D location = GeometryUtilsFX.translateByAngle(new Point2D(curve.getEndX(), curve.getEndY()), angle, -15);
            arrowHead.setTranslateX(location.getX());
            arrowHead.setTranslateY(location.getY());
        };
        invalidationListener.invalidated(null);

        curve.startXProperty().addListener(invalidationListener);
        curve.startYProperty().addListener(invalidationListener);
        curve.endXProperty().addListener(invalidationListener);
        curve.endYProperty().addListener(invalidationListener);
        curve.controlX2Property().addListener(invalidationListener);
        curve.controlY2Property().addListener(invalidationListener);

        curve.startXProperty().addListener((c, o, n) -> curve.setControlX1(curve.getControlX1() + (n.doubleValue() - o.doubleValue())));
        curve.startYProperty().addListener((c, o, n) -> curve.setControlY1(curve.getControlY1() + (n.doubleValue() - o.doubleValue())));

        curve.endXProperty().addListener((c, o, n) -> curve.setControlX2(curve.getControlX2() + (n.doubleValue() - o.doubleValue())));
        curve.endYProperty().addListener((c, o, n) -> curve.setControlY2(curve.getControlY2() + (n.doubleValue() - o.doubleValue())));

        return new EdgeView(id, curve, circle1, circle2);
    }

    /**
     * update the path representing an edge
     *
     * @param ax
     * @param ay
     * @param ex
     * @param ey
     * @param moveToA
     * @param lineToB
     * @param quadCurveToD
     * @param lineToE
     * @param arrowHead
     * @param clockwise
     * @return center point
     */
    private static Point2D updatePath(double ax, double ay, double ex, double ey, Point2D center, MoveTo moveToA, LineTo lineToB, QuadCurveTo quadCurveToD, LineTo lineToE, EdgeType edgeType, Shape arrowHead, boolean clockwise) {
        final double straightSegmentLength = 25;
        final double liftFactor = 0.2;

        final double distance = GeometryUtilsFX.distance(ax, ay, ex, ey);

        moveToA.setX(ax);
        moveToA.setY(ay);

        lineToE.setX(ex);
        lineToE.setY(ey);

        if (false && distance <= 2 * straightSegmentLength) {
            lineToB.setX(ax);
            lineToB.setY(ay);

            quadCurveToD.setX(ax);
            quadCurveToD.setY(ay);
            quadCurveToD.setControlX(ax);
            quadCurveToD.setControlY(ay);

            center = new Point2D(ax, ay);

            arrowHead.setTranslateX(0.5 * (quadCurveToD.getX() + lineToE.getX()));
            arrowHead.setTranslateY(0.5 * (quadCurveToD.getY() + lineToE.getY()));
        } else {
            final double alpha = GeometryUtilsFX.computeAngle(new Point2D(ex - ax, ey - ay));

            final Point2D m = new Point2D(0.5 * (ax + ex), 0.5 * (ay + ey));
            if (center == null) {
                final Point2D c;
                if (!clockwise)
                    center = m.add(-Math.sin(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance, Math.cos(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance);
                else
                    center = m.subtract(-Math.sin(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance, Math.cos(GeometryUtilsFX.deg2rad(alpha)) * liftFactor * distance);
            }

            final double beta = GeometryUtilsFX.computeAngle(center.subtract(ax, ay));

            final Point2D b = new Point2D(ax + straightSegmentLength * Math.cos(GeometryUtilsFX.deg2rad(beta)), ay + straightSegmentLength * Math.sin(GeometryUtilsFX.deg2rad(beta)));

            lineToB.setX(b.getX());
            lineToB.setY(b.getY());

            final double delta = GeometryUtilsFX.computeAngle(center.subtract(ex, ey));
            final Point2D d = new Point2D(ex + straightSegmentLength * Math.cos(GeometryUtilsFX.deg2rad(delta)), ey + straightSegmentLength * Math.sin(GeometryUtilsFX.deg2rad(delta)));

            quadCurveToD.setX(d.getX());
            quadCurveToD.setY(d.getY());
            quadCurveToD.setControlX(center.getX());
            quadCurveToD.setControlY(center.getY());

            arrowHead.setTranslateX(0.75 * d.getX() + 0.25 * lineToE.getX());
            arrowHead.setTranslateY(0.75 * d.getY() + 0.25 * lineToE.getY());
        }

        final double angle = GeometryUtilsFX.computeAngle(new Point2D(lineToE.getX() - quadCurveToD.getX(), lineToE.getY() - quadCurveToD.getY()));
        arrowHead.setRotationAxis(new Point3D(0, 0, 1));
        arrowHead.setRotate(angle);
        return center;

    }

    /**
     * is this edge the second of two edges that both connect the same two nodes?
     * (If so, will flip its bend)
     *
     * @param edge
     * @return true, if second of two edges
     */
    private static boolean isSecondOfTwoEdges(Edge edge) {
        for (Edge f : edge.getSource().adjacentEdges()) {
            if (f.getTarget() == edge.getTarget() && edge.getId() > f.getId())
                return true;
        }
        return false;
    }


    /**
     * find a path in a group
     *
     * @param group
     * @return path, if found
     */
    static Path getPath(Group group) {
        for (javafx.scene.Node child : group.getChildren()) {
            if (child instanceof Path)
                return (Path) child;
        }
        return null;
    }

    public static Font getFont() {
        return font.get();
    }

    public static ObjectProperty<Font> fontProperty() {
        return font;
    }

    public static void setFont(Font font) {
        PhyloEditor.font.set(font);
    }

    public Group getWorld() {
        return world;
    }

    public PhyloTree getGraph() {
        return graph;
    }

    public ItemSelectionModel<Node> getNodeSelection() {
        return nodeSelection;
    }

    public NodeArray<Pair<Shape, Label>> getNode2shapeAndLabel() {
        return node2shapeAndLabel;
    }

    public ItemSelectionModel<Edge> getEdgeSelection() {
        return edgeSelection;
    }

    public GraphFX<PhyloTree> getGraphFX() {
        return graphFX;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    static class EdgeView {
        private int id;
        private CubicCurve cubicCurve;
        private Label label;
        private Circle circle1;
        private Circle circle2;

        public EdgeView(int edgeId, CubicCurve cubicCurve, Circle circle1, Circle circle2) {
            this.id = edgeId;
            this.cubicCurve = cubicCurve;
            this.circle1 = circle1;
            this.circle2 = circle2;
        }

        public ArrayList<javafx.scene.Node> getChildren() {
            final ArrayList<javafx.scene.Node> children = new ArrayList<>();
            if (cubicCurve != null)
                children.add(cubicCurve);
            if (label != null)
                children.add(label);
            return children;
        }


    }
}
