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

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import splitstree5.gui.style.Style;

/**
 * node view
 * Daniel Huson, 10.2107
 */
public class ANodeView {
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
        final Circle circle = new Circle(5);
        circle.setLayoutX(location.getX());
        circle.setLayoutY(location.getY());
        setShape(circle);
        circle.setFill(Color.WHITE);
        circle.setStroke(Color.BLACK);
        if (text != null && text.length() > 0) {
            label = new Label(text);
            label.setLayoutX(location.getX() + circle.getRadius());
            label.setLayoutY(location.getY());
            setLabel(label);
        } else
            label = null;
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

    public void setStyling(Style styling) {
        setFont(styling.getFont());
        setStroke(styling.getStroke());
        setFill(styling.getFill());
        setTextFill(styling.getTextFill());
        setStrokeWidth(styling.getStrokeWidth());
    }


}
