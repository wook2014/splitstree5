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
package splitstree5.main.graphtab.base;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jloda.graph.Edge;
import jloda.util.Basic;

import java.util.ArrayList;

/**
 * Edge view
 * Daniel Huson, 10.2017
 */
public class AEdgeView {
    public enum EdgeShape {Straight, Angular, QuadCurve, CubicCurve}

    private Node shape;
    private Node label;
    private Point2D referencePoint;

    private final Edge e;

    /**
     * create an edge view
     *
     * @param e
     * @param weight
     * @param start
     * @param end
     * @return edge view
     */
    public AEdgeView(Edge e, Double weight, final Point2D start, final Point2D end) {
        this.e = e;

        Shape edgeShape = null;
        if (start != null && end != null) {
            edgeShape = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        }

        if (edgeShape != null) {
            edgeShape.setFill(Color.TRANSPARENT);
            edgeShape.setStroke(Color.BLACK);
            edgeShape.setStrokeLineCap(StrokeLineCap.ROUND);
            edgeShape.setStrokeWidth(3);
            setShape(edgeShape);
            setReferencePoint(start.add(end).multiply(0.5));
        }

        if (false && weight != null && start != null && end != null) {
            Label label = new Label("" + weight);
            final Point2D m = start.add(end).multiply(0.5);
            label.setLayoutX(m.getX());
            label.setLayoutY(m.getY());
            setLabel(label);
        }
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
    public AEdgeView(Edge e, GraphLayout layout, AEdgeView.EdgeShape shape, Double weight, final Point2D start, final Point2D control1, final Point2D mid, final Point2D control2, final Point2D support, final Point2D end) {
        this.e = e;

        Shape edgeShape = null;

        switch (shape) {
            case CubicCurve: {
                // todo: do we want to use the support node?
                if (true || layout == GraphLayout.Radial) {
                    edgeShape = new CubicCurve(start.getX(), start.getY(), control1.getX(), control1.getY(), control2.getX(), control2.getY(), end.getX(), end.getY());
                    break;
                } else if (support != null) {
                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final CubicCurveTo cubicCurveTo = new CubicCurveTo(control1.getX(), control1.getY(), control2.getX(), control2.getY(), support.getX(), support.getY());
                    final HLineTo hLineTo = new HLineTo(end.getX());
                    edgeShape = new Path(moveTo, cubicCurveTo, hLineTo);
                    break;
                } // else fall through to next
            }
            case QuadCurve: {
                if (layout == GraphLayout.Radial) {
                    edgeShape = new QuadCurve(start.getX(), start.getY(), mid.getX(), mid.getY(), end.getX(), end.getY());
                    break;
                } else if (support != null) {
                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final QuadCurveTo quadCurveTo = new QuadCurveTo(mid.getX(), mid.getY(), support.getX(), support.getY());
                    final LineTo lineTo = new LineTo(end.getX(), end.getY());
                    edgeShape = new Path(moveTo, quadCurveTo, lineTo);
                    break;
                }  // else fall through to next
            }
            case Angular: {
                if (layout == GraphLayout.Radial) {
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
            edgeShape.setStroke(Color.BLACK);
            edgeShape.setStrokeLineCap(StrokeLineCap.ROUND);
            edgeShape.setStrokeWidth(3);
            setShape(edgeShape);
            if (shape == AEdgeView.EdgeShape.Straight)
                setReferencePoint(start.add(end).multiply(0.5));
            else
                setReferencePoint(mid);
        }

        if (false && weight != null && start != null && end != null) {
            Label label = new Label("" + weight);
            final Point2D m = (mid != null ? mid : start.add(end).multiply(0.5));
            label.setLayoutX(m.getX());
            label.setLayoutY(m.getY());
            setLabel(label);
        }
    }

    public void setShape(Node shape) {
        this.shape = shape;
    }

    public Node getShape() {
        return shape;
    }

    public void setLabel(Node label) {
        this.label = label;
    }

    public Node getLabel() {
        return label;
    }

    public Point2D getReferencePoint() {
        return referencePoint;
    }

    public void setReferencePoint(Point2D referencePoint) {
        this.referencePoint = referencePoint;
    }

    public Edge getEdge() {
        return e;
    }

    /**
     * set the coordinates of this edge from locations
     *
     * @param start
     * @param end
     */
    public void setCoordinates(Point2D start, Point2D end) {
        if (shape != null) {
            if (shape instanceof Line) {
                final Line line = (Line) shape;
                line.setStartX(start.getX());
                line.setEndX(end.getX());
                line.setStartY(start.getY());
                line.setEndY(end.getY());
            } else {
                throw new RuntimeException("setCoordinates(): not implemented for shape: " + Basic.getShortName(shape.getClass()));
            }
        }
    }

    /**
     * scale the coordinates of this edge
     *
     * @param factorX
     * @param factorY
     */
    public void scaleCoordinates(double factorX, double factorY) {
        if (shape != null) {
            if (shape instanceof Line) {
                final Line line = (Line) shape;
                line.setStartX(line.getStartX() * factorX);
                line.setEndX(line.getEndX() * factorX);
                line.setStartY(line.getStartY() * factorY);
                line.setEndY(line.getEndY() * factorY);
            } else if (shape instanceof Polyline) {
                final Polyline line = (Polyline) shape;
                ArrayList<Double> newPoints = new ArrayList<>(line.getPoints().size());
                boolean isX = true;
                for (Double value : line.getPoints()) {
                    if (isX)
                        newPoints.add(value * factorX);
                    else
                        newPoints.add(value * factorY);
                    isX = !isX;
                }
                line.getPoints().setAll(newPoints);
            } else if (shape instanceof QuadCurve) {
                QuadCurve quadCurve = (QuadCurve) shape;
                quadCurve.setStartX(quadCurve.getStartX() * factorX);
                quadCurve.setStartY(quadCurve.getStartY() * factorY);
                quadCurve.setEndX(quadCurve.getEndX() * factorX);
                quadCurve.setEndY(quadCurve.getEndY() * factorY);
                quadCurve.setControlX(quadCurve.getControlX() * factorX);
                quadCurve.setControlY(quadCurve.getControlY() * factorY);
            } else if (shape instanceof CubicCurve) {
                CubicCurve quadCurve = (CubicCurve) shape;
                quadCurve.setStartX(quadCurve.getStartX() * factorX);
                quadCurve.setStartY(quadCurve.getStartY() * factorY);
                quadCurve.setEndX(quadCurve.getEndX() * factorX);
                quadCurve.setEndY(quadCurve.getEndY() * factorY);
                quadCurve.setControlX1(quadCurve.getControlX1() * factorX);
                quadCurve.setControlY1(quadCurve.getControlY1() * factorY);
                quadCurve.setControlX2(quadCurve.getControlX2() * factorX);
                quadCurve.setControlY2(quadCurve.getControlY2() * factorY);
            } else if (shape instanceof Path) {
                final Path path = (Path) shape;
                final ArrayList<PathElement> elements = new ArrayList<>(path.getElements().size());
                for (PathElement element : path.getElements()) {
                    if (element instanceof MoveTo) {
                        elements.add(new MoveTo(((MoveTo) element).getX() * factorX, ((MoveTo) element).getY() * factorY));
                    } else if (element instanceof LineTo) {
                        elements.add(new LineTo(((LineTo) element).getX() * factorX, ((LineTo) element).getY() * factorY));
                    } else if (element instanceof QuadCurveTo) {
                        elements.add(new QuadCurveTo(((QuadCurveTo) element).getControlX() * factorX, ((QuadCurveTo) element).getControlY() * factorY, ((QuadCurveTo) element).getX() * factorX, ((QuadCurveTo) element).getY() * factorY));
                    } else if (element instanceof CubicCurveTo) {
                        elements.add(new CubicCurveTo(((CubicCurveTo) element).getControlX1() * factorX, ((CubicCurveTo) element).getControlY1() * factorY, ((CubicCurveTo) element).getControlX2() * factorX, ((CubicCurveTo) element).getControlY2() * factorY, ((CubicCurveTo) element).getX() * factorX, ((CubicCurveTo) element).getY() * factorY));
                    }
                }
                path.getElements().setAll(elements);
            }
        }
        if (label != null) {
            label.setLayoutX(label.getLayoutX() * factorX);
            label.setLayoutY(label.getLayoutY() * factorY);
        }
        referencePoint = new Point2D(referencePoint.getX() * factorX, referencePoint.getY() * factorY);
    }
}
