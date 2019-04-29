/*
 *  EdgeView2D.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.graphtab.base;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.ProgramPropertiesFX;
import jloda.fx.util.SelectionEffect;
import jloda.graph.Edge;
import jloda.util.Basic;

import java.util.ArrayList;

/**
 * Edge view
 * Daniel Huson, 10.2017
 */
public class EdgeView2D extends EdgeViewBase {
    private static final EventHandler<MouseEvent> mouseEnteredHandler = (x) -> ((Shape) x.getSource()).setStrokeWidth(4 * ((Shape) x.getSource()).getStrokeWidth());
    private static final EventHandler<MouseEvent> mouseExitedHandler = (x) -> ((Shape) x.getSource()).setStrokeWidth(0.25 * ((Shape) x.getSource()).getStrokeWidth());

    public enum EdgeShape {Straight, Angular, QuadCurve, CubicCurve}

    private Shape shape;
    private Point2D referencePoint;

    /**
     * create an edge view
     *
     * @param e
     * @param start
     * @param end
     * @return edge view
     */
    public EdgeView2D(Edge e, final Point2D start, final Point2D end, String text) {
        super(e);

        Shape edgeShape = null;
        if (start != null && end != null) {
            edgeShape = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        }

        if (edgeShape != null) {
            edgeShape.setFill(Color.TRANSPARENT);
            edgeShape.setStroke(Color.BLACK);
            edgeShape.setStrokeLineCap(StrokeLineCap.ROUND);
            edgeShape.setStrokeWidth(1);
            setShape(edgeShape);
            setReferencePoint(start.add(end).multiply(0.5));
        }

        if (text != null) {
            final Label label = new Label(text);
            label.setStyle("");
            label.setFont(ProgramPropertiesFX.getDefaultFont());
            if (start != null && end != null) {
                final Point2D m = start.add(end).multiply(0.5);
                label.setTranslateX(m.getX());
                label.setTranslateY(m.getY());
            }
            setLabel(label);
        }
        getShape().setOnMouseEntered(mouseEnteredHandler);
        getShape().setOnMouseExited(mouseExitedHandler);
    }

    /**
     * create a simple edge view
     */
    public EdgeView2D(Edge e, GraphLayout layout, EdgeShape shape, final Point2D start, final Point2D control1, final Point2D mid, final Point2D control2, final Point2D support, final Point2D end, String text) {
        super(e);

        Shape edgeShape = null;

        switch (shape) {
            case CubicCurve: {
                // todo: do we want to use the support node?
                if (true || layout == GraphLayout.Radial) {
                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final CubicCurveTo cubicCurveTo = new CubicCurveTo(control1.getX(), control1.getY(), control2.getX(), control2.getY(), end.getX(), end.getY());
                    edgeShape = new Path(moveTo, cubicCurveTo);
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
                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final QuadCurveTo quadCurveTo = new QuadCurveTo(mid.getX(), mid.getY(), end.getX(), end.getY());
                    edgeShape = new Path(moveTo, quadCurveTo);
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
                    double startAngle = GeometryUtilsFX.computeAngle(start);
                    double endAngle = GeometryUtilsFX.computeAngle(mid);

                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final ArcTo arcTo = new ArcTo(radius, radius, 0, mid.getX(), mid.getY(), false, endAngle > startAngle);
                    final LineTo lineTo = new LineTo(end.getX(), end.getY());
                    edgeShape = new Path(moveTo, arcTo, lineTo);
                } else // rectilinear:
                {
                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final LineTo lineTo = new LineTo(mid.getX(), mid.getY());
                    final LineTo lineTo2 = new LineTo(end.getX(), end.getY());
                    edgeShape = new Path(moveTo, lineTo, lineTo2);
                }
                break;
            }
            case Straight: {
                if (start != null && end != null) {
                    final MoveTo moveTo = new MoveTo(start.getX(), start.getY());
                    final LineTo lineTo = new LineTo(end.getX(), end.getY());
                    edgeShape = new Path(moveTo, lineTo);
                }
                break;
            }
        }

        if (edgeShape != null) {
            edgeShape.setPickOnBounds(false);
            edgeShape.setFill(Color.TRANSPARENT);
            edgeShape.setStroke(Color.BLACK);
            
            edgeShape.setStrokeLineCap(StrokeLineCap.ROUND);
            edgeShape.setStrokeWidth(1);
            setShape(edgeShape);
            if (shape == EdgeView2D.EdgeShape.Straight)
                setReferencePoint(start.add(end).multiply(0.5));
            else
                setReferencePoint(mid);

            edgeShape.setOnMouseEntered(mouseEnteredHandler);
            edgeShape.setOnMouseExited(mouseExitedHandler);
        }

        if (text != null) {
            Label label = new Label(text);
            final Point2D m;
            if (mid != null)
                m = mid;
            else if (start != null && end != null)
                m = start.add(end).multiply(0.5);
            else
                m = null;
            if (m != null) {
                label.setTranslateX(m.getX());
                label.setTranslateY(m.getY());
            }
            setLabel(label);
        }
    }

    /**
     * create an edge view
     */
    public EdgeView2D(Edge e, ArrayList<PathElement> elements, String text) {
        super(e);
        if (elements != null && elements.size() >= 2) {
            Shape path = new Path(elements);

            path.setPickOnBounds(false);
            path.setFill(Color.TRANSPARENT);
            path.setStroke(Color.BLACK);
            path.setStrokeLineCap(StrokeLineCap.ROUND);
            path.setStrokeWidth(1);
            setShape(path);

            // todo: debug this:
            final Bounds bounds = path.getLayoutBounds();
            final Point2D mid = new Point2D(0.5 * (bounds.getMinX() + bounds.getMaxX()), 0.5 * (bounds.getMinY() + bounds.getMaxY()));
            setReferencePoint(mid);

            path.setOnMouseEntered(mouseEnteredHandler);
            path.setOnMouseExited(mouseExitedHandler);
        }

        if (text != null) {
            Label label = new Label(text);
            final Point2D m = getReferencePoint();
            if (m != null) {
                label.setTranslateX(m.getX());
                label.setTranslateY(m.getY());
            }
            setLabel(label);
        }
    }

    public void setShape(Shape shape) {
        if (this.shape != null)
            shapeGroup.getChildren().remove(this.shape);
        this.shape = shape;
        if (this.shape != null)
            shapeGroup.getChildren().add(this.shape);
    }

    public Shape getShape() {
        return shape;
    }


    public Point2D getReferencePoint() {
        return referencePoint;
    }

    public void setReferencePoint(Point2D referencePoint) {
        this.referencePoint = referencePoint;
    }

    /**
     * set the coordinates of this edge from locations as a straight line
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
     * rotate by given angle
     *
     * @param angle
     */
    public void rotateCoordinates(double angle) {
        if (shape != null) {
            if (shape instanceof Path) {
                final Path path = (Path) shape;
                final ArrayList<PathElement> elements = new ArrayList<>(path.getElements().size());
                for (PathElement element : path.getElements()) {
                    if (element instanceof MoveTo) {
                        final Point2D p = GeometryUtilsFX.rotate(((MoveTo) element).getX(), ((MoveTo) element).getY(), angle);
                        elements.add(new MoveTo(p.getX(), p.getY()));
                    } else if (element instanceof LineTo) {
                        final Point2D p = GeometryUtilsFX.rotate(((LineTo) element).getX(), ((LineTo) element).getY(), angle);
                        elements.add(new LineTo(p.getX(), p.getY()));
                    } else if (element instanceof ArcTo) {
                        final ArcTo arcTo = (ArcTo) element;
                        final Point2D p = GeometryUtilsFX.rotate(arcTo.getX(), arcTo.getY(), angle);
                        final ArcTo newArcTo = new ArcTo(arcTo.getRadiusX(), arcTo.getRadiusY(), 0, p.getX(), p.getY(), arcTo.isLargeArcFlag(), arcTo.isSweepFlag());
                        elements.add(newArcTo);
                    } else if (element instanceof QuadCurveTo) {
                        final QuadCurveTo quadCurveTo = (QuadCurveTo) element;
                        final Point2D c = GeometryUtilsFX.rotate(quadCurveTo.getControlX(), quadCurveTo.getControlY(), angle);
                        final Point2D p = GeometryUtilsFX.rotate(quadCurveTo.getX(), quadCurveTo.getY(), angle);
                        elements.add(new QuadCurveTo(c.getX(), c.getY(), p.getX(), p.getY()));
                    } else if (element instanceof CubicCurveTo) {
                        final CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
                        final Point2D c1 = GeometryUtilsFX.rotate(cubicCurveTo.getControlX1(), cubicCurveTo.getControlY1(), angle);
                        final Point2D c2 = GeometryUtilsFX.rotate(cubicCurveTo.getControlX2(), cubicCurveTo.getControlY2(), angle);
                        final Point2D p = GeometryUtilsFX.rotate(cubicCurveTo.getX(), cubicCurveTo.getY(), angle);
                        elements.add(new CubicCurveTo(c1.getX(), c1.getY(), c2.getX(), c2.getY(), p.getX(), p.getY()));
                    }
                }
                path.getElements().setAll(elements);
            } else if (shape instanceof Line) {
                final Point2D start = GeometryUtilsFX.rotate(((Line) shape).getStartX(), ((Line) shape).getStartY(), angle);
                final Point2D end = GeometryUtilsFX.rotate(((Line) shape).getEndX(), ((Line) shape).getEndY(), angle);
                final Line line = (Line) shape;
                line.setStartX(start.getX());
                line.setEndX(end.getX());
                line.setStartY(start.getY());
                line.setEndY(end.getY());
            } else {
                throw new RuntimeException("rotateCoordinates()(): Unsupported edge shape: " + Basic.getShortName(shape.getClass()));
            }
        }
        if (label != null) {
            Point2D p = GeometryUtilsFX.rotate(label.getTranslateX(), label.getTranslateY(), angle);
            label.setTranslateX(p.getX());
            label.setTranslateY(p.getY());
        }
        referencePoint = GeometryUtilsFX.rotate(referencePoint, angle);
    }

    /**
     * scale the coordinates of this edge
     *
     * @param factorX
     * @param factorY
     */
    public void scaleCoordinates(double factorX, double factorY) {
        if (shape != null) {
            if (shape instanceof Path) {
                final Path path = (Path) shape;
                final ArrayList<PathElement> elements = new ArrayList<>(path.getElements().size());
                for (PathElement element : path.getElements()) {
                    if (element instanceof MoveTo) {
                        elements.add(new MoveTo(((MoveTo) element).getX() * factorX, ((MoveTo) element).getY() * factorY));
                    } else if (element instanceof LineTo) {
                        elements.add(new LineTo(((LineTo) element).getX() * factorX, ((LineTo) element).getY() * factorY));
                    } else if (element instanceof ArcTo) {
                        final ArcTo arcTo = (ArcTo) element;
                        final ArcTo newArcTo = new ArcTo(arcTo.getRadiusX() * factorX, arcTo.getRadiusY() * factorY, 0, arcTo.getX() * factorX, arcTo.getY() * factorY, arcTo.isLargeArcFlag(), arcTo.isSweepFlag());
                        elements.add(newArcTo);
                    } else if (element instanceof QuadCurveTo) {
                        elements.add(new QuadCurveTo(((QuadCurveTo) element).getControlX() * factorX, ((QuadCurveTo) element).getControlY() * factorY, ((QuadCurveTo) element).getX() * factorX, ((QuadCurveTo) element).getY() * factorY));
                    } else if (element instanceof CubicCurveTo) {
                        elements.add(new CubicCurveTo(((CubicCurveTo) element).getControlX1() * factorX, ((CubicCurveTo) element).getControlY1() * factorY, ((CubicCurveTo) element).getControlX2() * factorX, ((CubicCurveTo) element).getControlY2() * factorY, ((CubicCurveTo) element).getX() * factorX, ((CubicCurveTo) element).getY() * factorY));
                    }
                }
                path.getElements().setAll(elements);
            } else if (shape instanceof Line) {
                final Line line = (Line) shape;
                line.setStartX(line.getStartX() * factorX);
                line.setEndX(line.getEndX() * factorY);
                line.setStartY(line.getStartY() * factorX);
                line.setEndY(line.getEndY() * factorY);
            } else
                throw new RuntimeException("scaleCoordinates(): Unsupported edge shape: " + Basic.getShortName(shape.getClass()));
        }
        if (label != null) {
            label.setTranslateX(label.getTranslateX() * factorX);
            label.setTranslateY(label.getTranslateY() * factorY);
        }
        referencePoint = new Point2D(referencePoint.getX() * factorX, referencePoint.getY() * factorY);
    }

    @Override
    public void showAsSelected(boolean selected) {
        if (selected) {
            if (label != null)
                label.setEffect(SelectionEffect.getInstance());
            if (shape != null)
                shape.setEffect(SelectionEffect.getInstance());
        } else {
            if (label != null)
                label.setEffect(null);
            if (shape != null)
                shape.setEffect(null);
        }
    }

    @Override
    public boolean isShownAsSelected() {
        if (label != null)
            return label.getEffect() != null;
        else
            return shape != null && shape.getEffect() != null;
    }

    @Override
    public Color getStroke() {
        if (shape != null)
            return (Color) shape.getStroke();
        else
            return null;
    }

    @Override
    public double getStrokeWidth() {
        if (shape != null)
            return shape.getStrokeWidth();
        else
            return 1;
    }

    @Override
    public void setStroke(Color stroke) {
        if (shape != null)
            shape.setStroke(stroke);
    }

    @Override
    public void setStrokeWidth(double width) {
        if (shape != null)
            shape.setStrokeWidth(width);

    }

    @Override
    public Node getEdgeShape() {
        return shape;
    }

    @Override
    public void setLabel(Labeled label) {
        super.setLabel(label);
        label.setLayoutX(referencePoint.getX());
        label.setLayoutY(referencePoint.getY());
    }
}
