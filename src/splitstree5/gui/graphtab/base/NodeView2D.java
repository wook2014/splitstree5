/*
 * NodeView2D.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.graphtab.base;

import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.NodeShape;
import jloda.fx.util.GeometryUtilsFX;
import jloda.fx.util.SelectionEffect;
import jloda.util.ProgramProperties;
import splitstree5.gui.formattab.FormatItem;

/**
 * node view
 * Daniel Huson, 10.2107
 */
public class NodeView2D extends NodeViewBase {
    private Shape shape;
    private Point2D location;

    private double oldScaleX;
    private double oldScaleY;

    private Rectangle selectionRectangle;

    /**
     * construct a simple node view
     *
	 */
    public NodeView2D(jloda.graph.Node v, Iterable<Integer> workingTaxonIds, Point2D location, NodeShape nodeShape, double shapeWidth, double shapeHeight, String text) {
        super(v, workingTaxonIds);
        setLocation(location);
        if (nodeShape == null)
            shape = NodeShape.create(NodeShape.Circle, 2.0, 2.0);
        else {
            shape = NodeShape.create(nodeShape, shapeWidth, shapeHeight);
            shapeWidth = 1;
        }
        shapeGroup.getChildren().add(shape);
        shapeGroup.setTranslateX(location.getX());
        shapeGroup.setTranslateY(location.getY());

        if (text != null && text.length() > 0) {
            shape.setStroke(Color.BLACK);
            shape.setFill(Color.WHITE);
            label = new RichTextLabel(text);

            label.setStyle("");
            label.setFont(ProgramProperties.getDefaultFontFX());
            label.setTranslateX(location.getX() + shapeWidth + 2);
            label.setTranslateY(location.getY());

            labelGroup.getChildren().add(label);
            (v.getOwner()).setLabel(v, text);
        } else {
            if (nodeShape == null) {
                ((Circle) shape).setRadius(0.75);
            }
            shape.setStroke(Color.BLACK);
            shape.setFill(Color.WHITE);
            label = null;
        }
        updateStuff();
    }

    public void setShape(Shape shape) {
        // final Point2D location;
        final Color color;
        if (this.shape != null) {
            //location = new Point2D(this.shape.getTranslateX(), this.shape.getTranslateY());
            color = (Color) this.shape.getFill();
            shapeGroup.getChildren().remove(this.shape);
        } else {
            //location = getLocation();
            color = Color.WHITE;
        }
        this.shape = shape;
        updateStuff();
        if (this.shape != null) {
            //shape.setTranslateX(location.getX());
            //shape.setTranslateY(location.getY());
            shape.setFill(color);
            shape.setStroke(Color.BLACK);
            shapeGroup.getChildren().add(this.shape);
        }
    }

    private void updateStuff() {
        if (label != null || shape != null) {
            final EventHandler<MouseEvent> mouseEnteredEventHandler = event -> {
                if (shapeGroup != null) {
                    oldScaleX = shapeGroup.getScaleX();
                    oldScaleY = shapeGroup.getScaleY();

                    final Bounds bounds = shapeGroup.getBoundsInLocal();
                    final double factorX = (bounds.getWidth() + 10) / bounds.getWidth();
                    final double factorY = (bounds.getHeight() + 10) / bounds.getHeight();
                    shapeGroup.setScaleX(factorX * shapeGroup.getScaleX());
                    shapeGroup.setScaleY(factorY * shapeGroup.getScaleY());
                }
                if (false && label != null) {
                    label.setScaleX(1.2 * label.getScaleX());
                    label.setScaleY(1.2 * label.getScaleY());
                }

            };
            final EventHandler<MouseEvent> mouseExitedEventHandler = event -> {
                if (shapeGroup != null) {
                    shapeGroup.setScaleX(oldScaleX);
                    shapeGroup.setScaleY(oldScaleY);
                }
                if (false && label != null) {
                    label.setScaleX(1.0 / 1.2 * label.getScaleX());
                    label.setScaleY(1.0 / 1.2 * label.getScaleY());
                }
            };
            if (shapeGroup != null) {
                shapeGroup.setOnMouseEntered(mouseEnteredEventHandler);
                shapeGroup.setOnMouseExited(mouseExitedEventHandler);
            }
            if (label != null) {
                label.setOnMouseEntered(mouseEnteredEventHandler);
                label.setOnMouseExited(mouseExitedEventHandler);
            }
            if (shapeGroup != null && label != null && label.getText() != null)
                Tooltip.install(shapeGroup, new Tooltip(label.getText()));
        }
    }

    public Shape getShape() {
        return shape;
    }

    @Override
    public void setLabel(RichTextLabel label) {
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
        (getNode().getOwner()).setLabel(getNode(), label != null ? label.getText() : null);
    }

    public void translateLabelX(double x) {
        label.setTranslateX(label.getTranslateX() - location.getX() + x);
    }

    public Point2D getLocation() {
        return location;
    }

    /**
     * translate the coordinates of this node view
     *
	 */
    public void translateCoordinates(double dx, double dy) {
        if (shapeGroup != null) {
            shapeGroup.setTranslateX(shapeGroup.getTranslateX() + dx);
            shapeGroup.setTranslateY(shapeGroup.getTranslateY() + dy);
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
	 */
    public void rotateCoordinates(double angle) {
        final Point2D oldLocation = location;
        location = GeometryUtilsFX.rotate(location, angle);

        if (shapeGroup != null) {
            final Point2D pos = GeometryUtilsFX.rotate(shapeGroup.getTranslateX(), shapeGroup.getTranslateY(), angle);
            shapeGroup.setTranslateX(pos.getX());
            shapeGroup.setTranslateY(pos.getY());
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
	 */
    public void scaleCoordinates(double factorX, double factorY) {
        var oldLocation = location;
        location = new Point2D(location.getX() * factorX, location.getY() * factorY);

        if (shapeGroup != null) {
            shapeGroup.setTranslateX(shapeGroup.getTranslateX() * factorX);
            shapeGroup.setTranslateY(shapeGroup.getTranslateY() * factorY);
        }

        if (label != null) {
            var diff = location.subtract(oldLocation);
            label.setTranslateX(label.getTranslateX() * factorX); // this is so that align leaf label works
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
            if (shapeGroup.getChildren().size() == 1 && shape != null && shape.getScaleX() == 1) {
                shape.setEffect(SelectionEffect.getInstance());
            } else {
                selectionRectangle = new Rectangle();
                selectionRectangle.setFill(Color.TRANSPARENT);
                final Bounds bounds = shapeGroup.getBoundsInLocal();
                selectionRectangle.setStroke(SelectionEffect.getInstance().getColor());
                selectionRectangle.setX(bounds.getMinX());
                selectionRectangle.setY(bounds.getMinY());
                selectionRectangle.setWidth(bounds.getWidth());
                selectionRectangle.setHeight(bounds.getHeight());
                selectionRectangle.setStrokeWidth(2);
                shapeGroup.getChildren().add(selectionRectangle);
            }
        } else {
            if (label != null)
                label.setEffect(null);
            if (shape != null)
                shape.setEffect(null);
            if (selectionRectangle != null) {
                shapeGroup.getChildren().remove(selectionRectangle);
                selectionRectangle = null;
            }
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
