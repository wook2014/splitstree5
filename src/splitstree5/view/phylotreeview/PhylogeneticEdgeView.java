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

package splitstree5.view.phylotreeview;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import splitstree5.core.algorithms.views.treeview.GeometryUtils;

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
     * @param shape
     * @param edgePoints
     * @return
     */
    public static PhylogeneticEdgeView createDefaultEdgeView(PhylogeneticTreeView.Layout layout, PhylogeneticTreeView.EdgeShape shape, Float weight, EdgePoint... edgePoints) {
        final PhylogeneticEdgeView edgeView = new PhylogeneticEdgeView();

        final EdgePoint pre = EdgePoint.getByType(EdgePoint.Type.PrePoint, edgePoints);
        final EdgePoint start = EdgePoint.getByType(EdgePoint.Type.StartPoint, edgePoints);
        final EdgePoint mid = EdgePoint.getByType(EdgePoint.Type.MidPoint, edgePoints);
        final EdgePoint support = EdgePoint.getByType(EdgePoint.Type.SupportPoint, edgePoints);
        final EdgePoint end = EdgePoint.getByType(EdgePoint.Type.EndPoint, edgePoints);
        final EdgePoint post = EdgePoint.getByType(EdgePoint.Type.PostPoint, edgePoints);

        Shape edgeShape = null;

        switch (shape) {
            case CubicCurve: {
                boolean forJavaCourse = true;

                if (forJavaCourse || layout == PhylogeneticTreeView.Layout.Circular) {
                    if (start != null && end != null && pre != null && post != null && mid != null) {
                        edgeShape = new CubicCurve(start.getX(), start.getY(), pre.getX(), pre.getY(), post.getX(), post.getY(), end.getX(), end.getY());


                        if (forJavaCourse) {
                            Circle circle1 = new Circle(pre.getX(), pre.getY(), 2);
                            circle1.setFill(Color.RED);
                            edgeView.addLabels(circle1);
                            Circle circle2 = new Circle(post.getX(), post.getY(), 2);
                            circle2.setFill(Color.BLACK);
                            edgeView.addLabels(circle2);
                        }

                        break;
                    }
                } else {

                    if (start != null && end != null && pre != null && post != null && mid != null && support != null) {
                        final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                        final CubicCurveTo cubicCurveTo = new CubicCurveTo(pre.getX(), pre.getY(), post.getX(), post.getY(), support.getX(), support.getY());
                        final HLineTo hLineTo = new HLineTo(end.getX());

                        edgeShape = new Path(moveTo, cubicCurveTo, hLineTo);

                        // todo: only lineTo if necessary
                        break;
                    }
                } // else fall through to next
            }
            case QuadCurve: {
                boolean forJavaCourse = false;

                if (forJavaCourse || layout == PhylogeneticTreeView.Layout.Circular) {
                    if (start != null && end != null && mid != null) {
                        edgeShape = new QuadCurve(start.getX(), start.getY(), mid.getX(), mid.getY(), end.getX(), end.getY());

                        if (forJavaCourse) {
                            Circle circle = new Circle(mid.getX(), mid.getY(), 2);
                            circle.setFill(Color.RED.deriveColor(1, 1, 1, 0.5));
                            edgeView.addLabels(circle);
                        }


                        break;
                    }
                } else {
                    if (start != null && mid != null && support != null && end != null) {
                        final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                        final QuadCurveTo quadCurveTo = new QuadCurveTo(mid.getX(), mid.getY(), support.getX(), support.getY());
                        final LineTo lineTo = new LineTo(end.getX(), end.getY());
                        edgeShape = new Path(moveTo, quadCurveTo, lineTo);
                        break;
                    }
                }  // else fall through to next
            }
            case Rectilinear: {
                if (start != null && mid != null && end != null) {
                    if (layout == PhylogeneticTreeView.Layout.Circular) {
                        double radius = mid.getPoint().magnitude();
                        double startAngle = GeometryUtils.computeAngle(start.getPoint());
                        double endAngle = GeometryUtils.computeAngle(mid.getPoint());

                        final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                        final ArcTo arcTo = new ArcTo(radius, radius, 0, mid.getX(), mid.getY(), false, endAngle > startAngle);
                        final LineTo lineTo = new LineTo(end.getX(), end.getY());
                        edgeShape = new Path(moveTo, arcTo, lineTo);
                    } else // rectilinear:
                    {
                        edgeShape = new Polyline(start.getX(), start.getY(), mid.getX(), mid.getY(), end.getX(), end.getY());
                    }
                    break;
                }  // else fall through to next
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
            final Point2D m = (mid != null ? mid.getPoint() : start.getPoint().add(end.getPoint()).multiply(0.5));
            label.setLayoutX(m.getX());
            label.setLayoutY(m.getY());
            edgeView.addLabels(label);
        }
        return edgeView;
    }
}
