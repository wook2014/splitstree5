/*
 *  EdgeView.java Copyright (C) 2020 Daniel H. Huson
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
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.MouseDragClosestNode;
import jloda.graph.Edge;
import splitstree5.tools.phyloedit.actions.TranslateCommand;

import java.util.function.Function;

public class EdgeView {
    final private int id;
    final private CubicCurve curve;
    final private Label label;
    final private Circle circle1;
    final private Circle circle2;
    final private Shape arrowHead;
    private final ObservableList<Node> children;

    /**
     * constructor
     *
     * @param editor
     * @param edge
     * @param aX
     * @param aY
     * @param bX
     * @param bY
     */
    public EdgeView(PhyloEditor editor, Edge edge, ReadOnlyDoubleProperty aX, ReadOnlyDoubleProperty aY, ReadOnlyDoubleProperty bX, ReadOnlyDoubleProperty bY) {
        arrowHead = new Polyline(-5, -3, 5, 0, -5, 3);
        arrowHead.setStroke(Color.BLACK);
        arrowHead.setFill(Color.BLACK);
        arrowHead.setStrokeWidth(2);

        curve = new CubicCurve();
        curve.setFill(Color.TRANSPARENT);
        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(2);
        curve.setPickOnBounds(false);

        curve.startXProperty().bind(aX);
        curve.startYProperty().bind(aY);
        curve.endXProperty().bind(bX);
        curve.endYProperty().bind(bY);

        circle1 = new Circle(3);
        circle1.setFill(Color.RED);
        circle1.translateXProperty().bindBidirectional(curve.controlX1Property());
        circle1.translateYProperty().bindBidirectional(curve.controlY1Property());
        circle1.setTranslateX(0.7 * curve.getStartX() + 0.3 * curve.getEndX());
        circle1.setTranslateY(0.7 * curve.getStartY() + 0.3 * curve.getEndY());

        circle2 = new Circle(3);
        circle1.setFill(Color.GREEN);
        circle2.translateXProperty().bindBidirectional(curve.controlX2Property());
        circle2.translateYProperty().bindBidirectional(curve.controlY2Property());
        circle2.setTranslateX(0.3 * curve.getStartX() + 0.7 * curve.getEndX());
        circle2.setTranslateY(0.3 * curve.getStartY() + 0.7 * curve.getEndY());

        id = edge.getId();

        // reference current translating control
        final Function<Circle, Node> translatingControl = (circle) -> {
            final Edge e = editor.getGraph().searchEdgeId(id);
            final EdgeView edgeView = editor.getEdge2view().get(e);
            if (circle == circle1)
                return edgeView.getCircle1();
            else
                return edgeView.getCircle2();
        };

        MouseDragClosestNode.setup(curve, editor.getNode2shapeAndLabel().get(edge.getSource()).get1(), circle1,
                editor.getNode2shapeAndLabel().get(edge.getTarget()).get1(), circle2,
                (circle, delta) -> {
                    editor.getUndoManager().add(new TranslateCommand("Edge Shape", translatingControl.apply((Circle) circle), delta));
                });

        curve.setOnMouseClicked(c -> {
            if (!c.isShiftDown()) {
                editor.getNodeSelection().clearSelection();
                editor.getEdgeSelection().clearAndSelect(edge);
            } else
                editor.getEdgeSelection().toggleSelection(edge);
        });

        //arrowHead.translateXProperty().bindBidirectional(curve.controlXProperty());
        //arrowHead.translateYProperty().bindBidirectional(curve.controlYProperty());

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

        label = null;

        children = FXCollections.observableArrayList();
        children.add(arrowHead);
        children.add(curve);
    }

    public ObservableList<Node> getChildren() {
        return children;
    }

    public int getId() {
        return id;
    }

    public CubicCurve getCurve() {
        return curve;
    }

    public Label getLabel() {
        return label;
    }

    public Circle getCircle1() {
        return circle1;
    }

    public Circle getCircle2() {
        return circle2;
    }

    public Shape getArrowHead() {
        return arrowHead;
    }

    public void moveControl1(double dx, double dy) {
        circle1.setTranslateX(circle1.getTranslateX() + dx);
        circle1.setTranslateY(circle1.getTranslateY() + dy);
    }

    public void moveControl2(double dx, double dy) {
        circle2.setTranslateX(circle2.getTranslateX() + dx);
        circle2.setTranslateY(circle2.getTranslateY() + dy);
    }

    public void startMoved(double deltaX, double deltaY) {
        final Point2D start = new Point2D(curve.getStartX(), curve.getStartY());
        final Point2D end = new Point2D(curve.getEndX(), curve.getEndY());
        final Point2D newStart = new Point2D(start.getX() + deltaX, start.getY() + deltaY);

        final double oldDistance = start.distance(end);

        if (oldDistance > 0) {
            final double scaleFactor = newStart.distance(end) / oldDistance;
            final double deltaAngle = GeometryUtilsFX.computeObservedAngle(end, start, newStart);


            if (scaleFactor != 0 || deltaAngle != 0) {

                final Point2D oldControl1 = new Point2D(curve.getControlX1(), curve.getControlY1());
                final double newAngle1 = GeometryUtilsFX.computeAngle(oldControl1.subtract(end)) + deltaAngle;

                final Point2D newControl1 = GeometryUtilsFX.translateByAngle(end, newAngle1, scaleFactor * oldControl1.distance(end));
                curve.setControlX1(newControl1.getX());
                curve.setControlY1(newControl1.getY());

                final Point2D oldControl2 = new Point2D(curve.getControlX2(), curve.getControlY2());
                final double newAngle2 = GeometryUtilsFX.computeAngle(oldControl2.subtract(end)) + deltaAngle;

                final Point2D newControl2 = GeometryUtilsFX.translateByAngle(end, newAngle2, scaleFactor * oldControl2.distance(end));
                curve.setControlX2(newControl2.getX());
                curve.setControlY2(newControl2.getY());
            }
        }
    }

    public void endMoved(double deltaX, double deltaY) {
        final Point2D start = new Point2D(curve.getStartX(), curve.getStartY());
        final Point2D end = new Point2D(curve.getEndX(), curve.getEndY());
        final Point2D newEnd = new Point2D(end.getX() + deltaX, end.getY() + deltaY);
        final double oldDistance = end.distance(start);

        if (oldDistance > 0) {
            final double scaleFactor = newEnd.distance(start) / oldDistance;
            final double deltaAngle = GeometryUtilsFX.computeObservedAngle(start, end, newEnd);

            if (scaleFactor != 0 || deltaAngle != 0) {

                final Point2D oldControl1 = new Point2D(curve.getControlX1(), curve.getControlY1());
                final double newAngle1 = GeometryUtilsFX.computeAngle(oldControl1.subtract(start)) + deltaAngle;

                final Point2D newControl1 = GeometryUtilsFX.translateByAngle(start, newAngle1, scaleFactor * oldControl1.distance(start));
                curve.setControlX1(newControl1.getX());
                curve.setControlY1(newControl1.getY());

                final Point2D oldControl2 = new Point2D(curve.getControlX2(), curve.getControlY2());
                final double newAngle2 = GeometryUtilsFX.computeAngle(oldControl2.subtract(start)) + deltaAngle;

                final Point2D newControl2 = GeometryUtilsFX.translateByAngle(start, newAngle2, scaleFactor * oldControl2.distance(start));
                curve.setControlX2(newControl2.getX());
                curve.setControlY2(newControl2.getY());
            }
        }
    }

    public double[] getControlCoordinates() {
        return new double[]{curve.getControlX1(), curve.getControlY1(), curve.getControlX2(), curve.getControlY2()};
    }

    public void setControlCoordinates(double[] coordinates) {
        curve.setControlX1(coordinates[0]);
        curve.setControlY1(coordinates[1]);
        curve.setControlX2(coordinates[2]);
        curve.setControlY2(coordinates[3]);
    }
}
