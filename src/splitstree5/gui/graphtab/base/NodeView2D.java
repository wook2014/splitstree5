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
package splitstree5.gui.graphtab.base;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import jloda.fx.shapes.CircleShape;
import jloda.util.ProgramProperties;
import splitstree5.gui.formattab.FormatItem;
import splitstree5.gui.utils.SelectionEffect;

/**
 * node view
 * Daniel Huson, 10.2107
 */
public class NodeView2D extends NodeViewBase {
    private Shape shape;
    private Point2D location;

    /**
     * construct a simple node view
     *
     * @param location
     * @param text
     * @return
     */
    public NodeView2D(jloda.graph.Node v, Point2D location, String text) {
        super(v);
        setLocation(location);
        CircleShape circle = new CircleShape(2);
        shape = circle;
        shapeGroup.getChildren().add(shape);
        circle.setTranslateX(location.getX());
        circle.setTranslateY(location.getY());

        if (text != null && text.length() > 0) {
            circle.setStroke(Color.BLACK);
            circle.setFill(Color.WHITE);

            label = new Label(text);
            label.setFont(ProgramProperties.getDefaultFont());
            label.setTranslateX(location.getX() + circle.getRadius() + 2);
            label.setTranslateY(location.getY());
            labelGroup.getChildren().add(label);
        } else {
            circle.setStroke(Color.BLACK);
            circle.setFill(Color.WHITE);
            circle.setRadius(0.75);
            label = null;
        }
        updateStuff();
    }

    public void setShape(Shape shape) {
        final Point2D location;
        final Color color;
        if (this.shape != null) {
            location = new Point2D(this.shape.getTranslateX(), this.shape.getTranslateY());
            color = (Color) this.shape.getFill();
            shapeGroup.getChildren().remove(this.shape);
        } else {
            location = getLocation();
            color = Color.WHITE;
        }
        this.shape = shape;
        updateStuff();
        if (this.shape != null) {
            shape.setTranslateX(location.getX());
            shape.setTranslateY(location.getY());
            shape.setFill(color);
            shape.setStroke(Color.BLACK);
            shapeGroup.getChildren().add(this.shape);
        }
    }

    private void updateStuff() {
        if (label != null || shape != null) {
            EventHandler<MouseEvent> mouseEnteredEventHandler = event -> {
                if (shape != null) {
                    shape.setScaleX(2 * shape.getScaleX());
                    shape.setScaleY(2 * shape.getScaleY());
                }
                if (label != null) {
                    label.setScaleX(1.2 * label.getScaleX());
                    label.setScaleY(1.2 * label.getScaleY());
                }

            };
            EventHandler<MouseEvent> mouseExitedEventHandler = event -> {
                if (shape != null) {
                    shape.setScaleX(0.5 * shape.getScaleX());
                    shape.setScaleY(0.5 * shape.getScaleY());
                }
                if (label != null) {
                    label.setScaleX(1.0 / 1.2 * label.getScaleX());
                    label.setScaleY(1.0 / 1.2 * label.getScaleY());
                }
            };
            if (shape != null) {
                shape.setOnMouseEntered(mouseEnteredEventHandler);
                shape.setOnMouseExited(mouseExitedEventHandler);
            }
            if (label != null) {
                label.setOnMouseEntered(mouseEnteredEventHandler);
                label.setOnMouseExited(mouseExitedEventHandler);
            }
            if (shape != null && label != null)
                Tooltip.install(shape, new Tooltip(label.getText()));
        }
    }

    public Shape getShape() {
        return shape;
    }

    @Override
    public void setLabel(Labeled label) {
        final Point2D location;
        final Color color;

        if (this.label != null) {
            location = new Point2D(this.label.getTranslateX(), this.label.getTranslateY());
            labelGroup.getChildren().remove(this.label);
            color = (Color) this.label.getTextFill();
        } else {
            location = getLocation();
            color = Color.BLACK;
        }
        this.label = label;
        updateStuff();
        if (this.label != null) {
            labelGroup.getChildren().add(this.label);
            label.setTranslateX(location.getX());
            label.setTranslateY(location.getY());
            label.setTextFill(color);
        }
    }


    public Point2D getLocation() {
        return location;
    }

    /**
     * translate the coordinates of this node view
     *
     * @param dx
     * @param dy
     */
    public void translateCoordinates(double dx, double dy) {
        if (shape != null) {
            shape.setTranslateX(shape.getTranslateX() + dx);
            shape.setTranslateY(shape.getTranslateY() + dy);
        }
        final Point2D oldLocation = location;
        location = new Point2D(location.getX() + dx, location.getY() + dy);

        if (label != null) {
            Point2D diff = location.subtract(oldLocation);
            label.setTranslateX(label.getTranslateX() + diff.getX());
            label.setTranslateY(label.getTranslateY() + diff.getY());
        }
    }

    /**
     * rotate by given angle
     *
     * @param angle
     */
    public void rotateCoordinates(double angle) {
        final Point2D oldLocation = location;
        location = GeometryUtils.rotate(location, angle);

        if (shape != null) {
            final Point2D pos = GeometryUtils.rotate(shape.getTranslateX(), shape.getTranslateY(), angle);
            shape.setTranslateX(pos.getX());
            shape.setTranslateY(pos.getY());
        }

        if (label != null) {
            Point2D diff = location.subtract(oldLocation);
            label.setTranslateX(label.getTranslateX() + diff.getX());
            label.setTranslateY(label.getTranslateY() + diff.getY());
        }
    }

    /**
     * scale the coordinates of this node view
     *
     * @param factorX
     * @param factorY
     */
    public void scaleCoordinates(double factorX, double factorY) {
        final Point2D oldLocation = location;
        location = new Point2D(location.getX() * factorX, location.getY() * factorY);

        if (shape != null) {
            shape.setTranslateX(shape.getTranslateX() * factorX);
            shape.setTranslateY(shape.getTranslateY() * factorY);
        }

        if (label != null) {
            Point2D diff = location.subtract(oldLocation);
            label.setTranslateX(label.getTranslateX() + diff.getX());
            label.setTranslateY(label.getTranslateY() + diff.getY());
        }
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }


    public void setStroke(Color color) {
        if (shape != null) {
            shape.setStroke(color);
        }
    }

    public Color getStroke() {
        if (shape != null) {
            return (Color) shape.getStroke();
        }
        return null;
    }

    public void setFill(Color color) {
        if (shape != null) {
            shape.setFill(color);
        }
    }

    public Color getFill() {
        if (shape != null) {
            return (Color) shape.getFill();
        }
        return null;
    }

    public void setStrokeWidth(double width) {
        if (shape != null) {
            shape.setStrokeWidth(width);
        }
    }

    public double getStrokeWidth() {
        if (shape != null) {
            return shape.getStrokeWidth();
        }
        return 1;
    }

    public void setStyling(FormatItem styling) {
        setFont(styling.getFont());
        setFill(styling.getNodeColor());
        setTextFill(styling.getLabelColor());
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
    public double getWidth() {
        return shape.getBoundsInLocal().getWidth();
    }

    @Override
    public double getHeight() {
        return shape.getBoundsInLocal().getHeight();
    }

    @Override
    public void setWidth(double width) {
        shape.setScaleX(width / shape.getBoundsInLocal().getWidth());

    }

    @Override
    public void setHeight(double height) {
        shape.setScaleY(height / shape.getBoundsInLocal().getHeight());

    }

    @Override
    public javafx.scene.Node getNodeShape() {
        return shape;
    }
}
