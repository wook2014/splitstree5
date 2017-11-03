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
package splitstree5.core.algorithms.views.treeview;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import splitstree5.core.algorithms.views.TreeDrawer;

/**
 * Edge view
 * Daniel Huson, 10.2017
 */
public class PhylogeneticEdgeView {
    private Group parts;
    private Group labels;

    public PhylogeneticEdgeView() {
    }

    public void addParts(Node... parts) {
        if (this.parts == null)
            this.parts = new Group();
        this.parts.getChildren().addAll(parts);
    }

    public Group getParts() {
        return parts;
    }

    public void addLabels(Node... labels) {
        if (this.labels == null)
            this.labels = new Group();
        this.labels.getChildren().addAll(labels);
    }

    public Group getLabels() {
        return labels;
    }

    /**
     * create a simple edge view
     *
     * @param layout
     * @param shape
     * @param weight
     * @param start
     * @param control1
     * @param mid
     * @param control2
     * @param support
     * @param end
     * @return
     */
    public static PhylogeneticEdgeView createDefaultEdgeView(TreeDrawer.Layout layout, TreeDrawer.EdgeShape shape, Double weight,
                                                             final Point2D start, final Point2D control1, final Point2D mid, final Point2D control2, final Point2D support, final Point2D end) {
        final PhylogeneticEdgeView edgeView = new PhylogeneticEdgeView();

        Shape edgeShape = null;

        switch (shape) {
            case CubicCurve: {
                boolean forJavaCourse = false;

                if (forJavaCourse || layout == TreeDrawer.Layout.Radial) {
                    edgeShape = new CubicCurve(start.getX(), start.getY(), control1.getX(), control1.getY(), control2.getX(), control2.getY(), end.getX(), end.getY());


                    if (forJavaCourse) {
                        Circle circle1 = new Circle(control1.getX(), control1.getY(), 2);
                        circle1.setFill(Color.RED);
                        edgeView.addLabels(circle1);
                        Circle circle2 = new Circle(control2.getX(), control2.getY(), 2);
                        circle2.setFill(Color.BLACK);
                        edgeView.addLabels(circle2);
                    }

                    break;
                } else {
                    if (support != null) {
                        final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                        final CubicCurveTo cubicCurveTo = new CubicCurveTo(control1.getX(), control1.getY(), control2.getX(), control2.getY(), support.getX(), support.getY());
                        final HLineTo hLineTo = new HLineTo(end.getX());

                        edgeShape = new Path(moveTo, cubicCurveTo, hLineTo);

                        // todo: only lineTo if necessary
                        break;
                    }
                } // else fall through to next
            }
            case QuadCurve: {
                if (layout == TreeDrawer.Layout.Radial) {
                    edgeShape = new QuadCurve(start.getX(), start.getY(), mid.getX(), mid.getY(), end.getX(), end.getY());
                    break;
                } else {
                    if (support != null) {
                        final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                        final QuadCurveTo quadCurveTo = new QuadCurveTo(mid.getX(), mid.getY(), support.getX(), support.getY());
                        final LineTo lineTo = new LineTo(end.getX(), end.getY());
                        edgeShape = new Path(moveTo, quadCurveTo, lineTo);
                        break;
                    }
                }  // else fall through to next
            }
            case Angular: {
                if (layout == TreeDrawer.Layout.Radial) {
                    double radius = mid.magnitude();
                    double startAngle = GeometryUtils.computeAngle(start);
                    double endAngle = GeometryUtils.computeAngle(mid);

                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final ArcTo arcTo = new ArcTo(radius, radius, 0, mid.getX(), mid.getY(), false, endAngle > startAngle);
                    final LineTo lineTo = new LineTo(end.getX(), end.getY());
                    edgeShape = new Path(moveTo, arcTo, lineTo);
                } else // rectilinear:
                {
                    edgeShape = new Polyline(start.getX(), start.getY(), mid.getX(), mid.getY(), end.getX(), end.getY());
                }
                break;
            }
            case Straight: {
                if (start != null && end != null) {
                    edgeShape = new Line(start.getX(), start.getY(), end.getX(), end.getY());
                }
                break;
            }
        }

        if (edgeShape != null) {
            edgeShape.setFill(Color.TRANSPARENT);
            edgeShape.setStroke(Color.GREEN.deriveColor(1, 1, 1, 0.3));
            edgeShape.setStrokeLineCap(StrokeLineCap.ROUND);
            edgeShape.setStrokeWidth(5);
            edgeView.addParts(edgeShape);
        }

        if (false && weight != null && start != null && end != null) {
            Label label = new Label("" + weight);
            final Point2D m = (mid != null ? mid : start.add(end).multiply(0.5));
            label.setLayoutX(m.getX());
            label.setLayoutY(m.getY());
            edgeView.addLabels(label);
        }
        return edgeView;
    }
}
