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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import splitstree5.gui.formattab.Styles;

/**
 * node view
 * Daniel Huson, 10.2107
 */
public class ANodeView {
    private static final EventHandler<MouseEvent> mouseEnteredHandler = (x) -> {
        ((Shape) x.getSource()).setScaleX(4 * ((Shape) x.getSource()).getScaleX());
        ((Shape) x.getSource()).setScaleY(4 * ((Shape) x.getSource()).getScaleY());
    };

    private static final EventHandler<MouseEvent> mouseExitedHandler = (x) -> {
        ((Shape) x.getSource()).setScaleX(0.25 * ((Shape) x.getSource()).getScaleX());
        ((Shape) x.getSource()).setScaleY(0.25 * ((Shape) x.getSource()).getScaleY());
    };
    private Node shape;
    private Node label;
    private Point2D location;

    private final jloda.graph.Node v;

    /**
     * construct a simple node view
     *
     * @param location
     * @param text
     * @return
     */
    public ANodeView(jloda.graph.Node v, Point2D location, String text) {
        this.v = v;
        setLocation(location);
        final Circle circle = new Circle();
        circle.setLayoutX(location.getX());
        circle.setLayoutY(location.getY());
        setShape(circle);
        if (text != null && text.length() > 0) {
            circle.setStroke(Color.BLACK);
            circle.setFill(Color.WHITE);
            circle.setRadius(2);

            label = new Label(text);
            label.setLayoutX(location.getX() + circle.getRadius());
            label.setLayoutY(location.getY());

            final Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(((Labeled) label).textProperty());
            Tooltip.install(circle, tooltip);
            Tooltip.install(label, tooltip);
            setLabel(label);

            circle.setOnMouseEntered((x) -> {
                circle.setScaleX(2 * circle.getScaleX());
                circle.setScaleY(2 * circle.getScaleY());
                label.setScaleX(1.2 * label.getScaleX());
                label.setScaleY(1.2 * label.getScaleY());
            });
            label.setOnMouseEntered(circle.getOnMouseEntered());

            circle.setOnMouseExited((x) -> {
                circle.setScaleX(0.5 * circle.getScaleX());
                circle.setScaleY(0.5 * circle.getScaleY());
                label.setScaleX(1.0 / 1.2 * label.getScaleX());
                label.setScaleY(1.0 / 1.2 * label.getScaleY());
            });
            label.setOnMouseExited(circle.getOnMouseExited());

        } else {
            circle.setFill(Color.BLACK);
            circle.setRadius(1);

            circle.setOnMouseEntered(mouseEnteredHandler);
            circle.setOnMouseExited(mouseExitedHandler);

            label = null;
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
            shape.setLayoutX(shape.getLayoutX() + dx);
            shape.setLayoutY(shape.getLayoutY() + dy);
        }
        final Point2D oldLocation = location;
        location = new Point2D(location.getX() + dx, location.getY() + dy);

        if (label != null) {
            Point2D diff = location.subtract(oldLocation);
            label.setLayoutX(label.getLayoutX() + diff.getX());
            label.setLayoutY(label.getLayoutY() + diff.getY());
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
            final Point2D pos = GeometryUtils.rotate(shape.getLayoutX(), shape.getLayoutY(), angle);
            shape.setLayoutX(pos.getX());
            shape.setLayoutY(pos.getY());
        }

        if (label != null) {
            Point2D diff = location.subtract(oldLocation);
            label.setLayoutX(label.getLayoutX() + diff.getX());
            label.setLayoutY(label.getLayoutY() + diff.getY());
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
            shape.setLayoutX(shape.getLayoutX() * factorX);
            shape.setLayoutY(shape.getLayoutY() * factorY);
        }

        if (label != null) {
            Point2D diff = location.subtract(oldLocation);
            label.setLayoutX(label.getLayoutX() + diff.getX());
            label.setLayoutY(label.getLayoutY() + diff.getY());
        }
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public jloda.graph.Node getNode() {
        return v;
    }

    public void setFont(Font font) {
        if (label != null && label instanceof Labeled) {
            ((Labeled) label).setFont(font);
        }
    }

    public Font getFont() {
        if (label != null && label instanceof Labeled) {
            return ((Labeled) label).getFont();
        }
        return null;
    }

    public void setStroke(Paint paint) {
        if (shape != null && shape instanceof Shape) {
            ((Shape) shape).setStroke(paint);
        }
    }

    public Paint getStroke() {
        if (shape != null && shape instanceof Shape) {
            return ((Shape) shape).getStroke();
        }
        return null;
    }

    public void setFill(Paint paint) {
        if (shape != null && shape instanceof Shape) {
            ((Shape) shape).setFill(paint);
        }
    }

    public Paint getFill() {
        if (shape != null && shape instanceof Shape) {
            return ((Shape) shape).getFill();
        }
        return null;
    }

    public void setStrokeWidth(double width) {
        if (shape != null && shape instanceof Shape) {
            ((Shape) shape).setStrokeWidth(width);
        }
    }

    public double getStrokeWidth() {
        if (shape != null && shape instanceof Shape) {
            return ((Shape) shape).getStrokeWidth();
        }
        return 1;
    }

    public void setTextFill(Paint paint) {
        if (label != null && label instanceof Labeled) {
            ((Labeled) label).setTextFill(paint);
        }
    }

    public Paint getTextFill() {
        if (label != null && label instanceof Labeled) {
            return ((Labeled) label).getTextFill();
        }
        return null;
    }

    public void setStyling(Styles styling) {
        setFont(styling.getFont());
        setStroke(styling.getStroke());
        setFill(styling.getFill());
        setTextFill(styling.getTextFill());
        setStrokeWidth(styling.getStrokeWidth());
    }


}
