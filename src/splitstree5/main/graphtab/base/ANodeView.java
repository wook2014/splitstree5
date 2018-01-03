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
package splitstree5.main.graphtab.base;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jloda.fx.ASelectionModel;

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
    public ANodeView(jloda.graph.Node v, Point2D location, String text, ASelectionModel<jloda.graph.Node> selectionModel) {
        this.v = v;
        setLocation(location);
        final Circle circle = new Circle(5);
        circle.setLayoutX(location.getX());
        circle.setLayoutY(location.getY());
        setShape(circle);
        circle.setFill(Color.BLUE);
        final Label label;
        if (text != null && text.length() > 0) {
            label = new Label(text);
            label.setLayoutX(location.getX() + circle.getRadius());
            label.setLayoutY(location.getY());
            setLabel(label);
        } else
            label = null;
        circle.setOnMouseClicked((e) -> {
            if (!e.isShiftDown())
                selectionModel.clearSelection();
            if (selectionModel.getSelectedItems().contains(v))
                selectionModel.clearSelection(v);
            else
                selectionModel.select(v);
            e.consume();
        });
        if (label != null) {
            label.setOnMouseClicked((e) -> {
                if (!e.isShiftDown())
                    selectionModel.clearSelection();
                if (selectionModel.getSelectedItems().contains(v))
                    selectionModel.clearSelection(v);
                else
                    selectionModel.select(v);
                e.consume();
            });
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
}
