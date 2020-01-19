/*
 * ReactionGraphView.java Copyright (C) 2019. Daniel H. Huson
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

package splitstree5.treebased.editor;


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
import jloda.fx.shapes.CircleShape;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.SelectionEffectBlue;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import jloda.util.Pair;
import jloda.util.Single;


/**
 * maintains the visualization of a model
 * Daniel Huson, 7.2019
 */
public class PhyloTreeView {
    public enum EdgeType {treeEdge, reticulateEdge}

    private final static ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.font("Helvetica", 12));

    private final PhyloTree graph;
    private final NodeArray<Pair<Shape, Label>> node2shapeAndLabel;
    private final EdgeArray<Group> edge2group;

    private final ItemSelectionModel<Node> nodeSelection = new ItemSelectionModel<>();
    private final ItemSelectionModel<Edge> edgeSelection = new ItemSelectionModel<>();

    private final Group world;
    private final Group spacers;
    private final Group graphNodes;
    private final Group graphNodeLabels;
    private final Group graphEdges;

    private final UndoManager undoManager;

    /**
     * construct a graph view
     */
    public PhyloTreeView(PhyloTree graph, UndoManager undoManager) {
        this.graph = graph;
        this.undoManager = undoManager;

        spacers = new Group();
        graphNodes = new Group();
        graphNodeLabels = new Group();
        graphEdges = new Group();
        this.world = new Group(spacers, graphEdges, graphNodes, graphNodeLabels);

        node2shapeAndLabel = new NodeArray<>(graph);
        edge2group = new EdgeArray<>(graph);
        ;

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
                    final Group group = edge2group.get(edge);
                    if (group != null) {
                        for (javafx.scene.Node node : group.getChildren())
                            node.setEffect(SelectionEffectBlue.getInstance());
                    }
                }
                for (Edge edge : e.getRemoved()) {
                    final Group group = edge2group.get(edge);
                    if (group != null) {
                        for (javafx.scene.Node node : group.getChildren())
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
        final Label text;

        shape = new CircleShape(10);
        shape.setStroke(Color.BLACK);
        shape.setFill(Color.WHITE);
        shape.setStrokeWidth(2);

        if (graph.getLabel(v) != null)
            text = new Label(graph.getLabel(v));
        else
            text = new Label();

        shape.setTranslateX(x);
        shape.setTranslateY(y);
        setupMouseInteraction(pane, shape, v);
        graphNodes.getChildren().add(shape);

        text.setFont(getFont());
        text.setLayoutX(10);
        text.translateXProperty().bind(shape.translateXProperty());
        text.translateYProperty().bind(shape.translateYProperty());
        graphNodeLabels.getChildren().add(text);
        setupMouseInteraction(pane, text, v);

        if (true) {  // add spacers to prevent graph from moving when molecules with long names arrive at a node that is on the boundary of the graph
            final Circle spacer = new Circle(100);
            spacer.translateXProperty().bind(shape.translateXProperty());
            spacer.translateYProperty().bind(shape.translateYProperty());
            spacer.setFill(Color.TRANSPARENT);
            spacer.setStroke(Color.TRANSPARENT);
            spacer.setMouseTransparent(true);

            spacers.getChildren().add(spacer);
        }

        node2shapeAndLabel.put(v, new Pair<>(shape, text));
    }

    public void removeNode(Node v) {
        for (Edge e : v.adjacentEdges()) {
            graphEdges.getChildren().remove(edge2group.get(e));
        }
        Pair<Shape, Label> pair = node2shapeAndLabel.get(v);
        if (pair != null) {
            graphNodes.getChildren().remove(pair.get1());
            graphNodeLabels.getChildren().remove(pair.get2());
        }
    }

    public void addEdge(Edge e) {
        final Shape sourceShape = node2shapeAndLabel.get(e.getSource()).getFirst();
        final Shape targetShape = node2shapeAndLabel.get(e.getSource()).getFirst();

        Group group = (createPath(e, sourceShape.translateXProperty(), sourceShape.translateYProperty(), targetShape.translateXProperty(), targetShape.translateYProperty(), EdgeType.reticulateEdge));
        edge2group.setValue(e, group);
        graphEdges.getChildren().add(group);
    }

    enum What {moveNode, growEdge}

    ;

    /**
     * setup mouse interaction
     */
    private void setupMouseInteraction(Pane pane, javafx.scene.Node mouseTarget, Node v) {
        mouseTarget.setCursor(Cursor.CROSSHAIR);

        final double[] mouseDown = new double[2];
        final Single<Boolean> moved = new Single<>(false);
        final Single<What> what = new Single<>(null);
        final Line line = new Line();
        world.getChildren().add(line);

        mouseTarget.setOnMousePressed(c -> {
            mouseDown[0] = c.getSceneX();
            mouseDown[1] = c.getSceneY();
            moved.set(false);
            what.set(c.isShiftDown() ? What.growEdge : What.moveNode);
            if (what.get() == What.growEdge) {
                final Shape shape = node2shapeAndLabel.get(v).get1();
                line.setStartX(shape.getTranslateX());
                line.setStartY(shape.getTranslateY());

                final Point2D location = pane.sceneToLocal(mouseDown[0], mouseDown[1]);

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
                        shape.setTranslateX(shape.getTranslateX() + (mouseX - mouseDown[0]));
                        shape.setTranslateY(shape.getTranslateY() + (mouseY - mouseDown[1]));
                    } else if (mouseTarget instanceof Label) {
                        final Label label = node2shapeAndLabel.get(u).get2();
                        label.setLayoutX(label.getLayoutX() + (mouseX - mouseDown[0]));
                        label.setLayoutY(label.getLayoutY() + (mouseY - mouseDown[1]));
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
            mouseDown[0] = c.getSceneX();
            mouseDown[1] = c.getSceneY();
            c.consume();
        });

        mouseTarget.setOnMouseReleased(c -> {
            if (!moved.get()) {
                if (!c.isControlDown()) {
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
                if (what.get() == What.growEdge) {
                    final double x = line.getEndX();
                    final double y = line.getEndY();
                    undoManager.doAndAdd(createAddEdgeCommand(pane, v, null, x, y));
                    line.setVisible(false);
                }
                moved.set(false);
            }
        });
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
    private Group createPath(Edge edge, ReadOnlyDoubleProperty aX, ReadOnlyDoubleProperty aY, ReadOnlyDoubleProperty bX, ReadOnlyDoubleProperty bY, EdgeType edgeType) {
        final Shape arrowHead = new Polyline(-5, -3, 5, 0, -5, 3);
        arrowHead.setStroke(Color.BLACK);

        final MoveTo moveToA = new MoveTo();

        final LineTo lineToB = new LineTo();

        final QuadCurveTo quadCurveToD = new QuadCurveTo();

        final LineTo lineToE = new LineTo();

        final CircleShape circleShape = new CircleShape(3);

        final InvalidationListener invalidationListener = (e) -> {
            final Point2D lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge));

            if (lineCenter != null) {
                circleShape.setTranslateX(lineCenter.getX());
                circleShape.setTranslateY(lineCenter.getY());
            }
        };

        aX.addListener(invalidationListener);
        aY.addListener(invalidationListener);
        bX.addListener(invalidationListener);
        bY.addListener(invalidationListener);

        {
            final Point2D lineCenter = updatePath(aX.get(), aY.get(), bX.get(), bY.get(), null, moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge));
            circleShape.setTranslateX(lineCenter.getX());
            circleShape.setTranslateY(lineCenter.getY());
            circleShape.translateXProperty().addListener((c, o, n) ->
                    updatePath(aX.get(), aY.get(), bX.get(), bY.get(), new Point2D(circleShape.getTranslateX(), circleShape.getTranslateY()), moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge)));
            circleShape.translateYProperty().addListener((c, o, n) ->
                    updatePath(aX.get(), aY.get(), bX.get(), bY.get(), new Point2D(circleShape.getTranslateX(), circleShape.getTranslateY()), moveToA, lineToB, quadCurveToD, lineToE, edgeType, arrowHead, isSecondOfTwoEdges(edge)));
            // setupMouseInteraction(circleShape,circleShape);
            circleShape.setFill(Color.TRANSPARENT);
            circleShape.setStroke(Color.TRANSPARENT);
        }


        final Path path = new Path(moveToA, lineToB, quadCurveToD, lineToE);
        path.setStrokeWidth(2);

        //setupMouseInteraction(path, circleShape, null, edge);

        return new Group(path, arrowHead, circleShape);
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
        PhyloTreeView.font.set(font);
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

    public ItemSelectionModel<Edge> getEdgeSelection() {
        return edgeSelection;
    }

    UndoableRedoableCommand createAddNodeCommand(Pane pane, double x, double y) {
        final Single<Node> v = new Single<>();
        return new UndoableRedoableCommand("Add Node") {
            @Override
            public void undo() {
                if (v.get() != null) {
                    removeNode(v.get());
                    graph.deleteNode(v.get());
                }
            }

            @Override
            public boolean isUndoable() {
                return v.get() != null;
            }

            @Override
            public void redo() {
                v.set(graph.newNode());
                addNode(pane, x, y, v.get());
            }
        };
    }

    UndoableRedoableCommand createAddEdgeCommand(Pane pane, Node a, Node b, double x, double y) {
        final Single<Node> w = new Single<>();
        return new UndoableRedoableCommand("Add Edge") {
            @Override
            public void undo() {
                if (w.get() != null) {
                    removeNode(w.get());
                    graph.deleteNode(w.get());
                }
            }

            @Override
            public boolean isUndoable() {
                return w.get() != null;
            }

            @Override
            public void redo() {
                if (b == null) {
                    w.set(graph.newNode());
                    addNode(pane, x, y, w.get());
                } else
                    w.set(b);
                if (a.getCommonEdge(w.get()) == null) {
                    addEdge(graph.newEdge(a, w.get()));
                }
            }
        };
    }
}
