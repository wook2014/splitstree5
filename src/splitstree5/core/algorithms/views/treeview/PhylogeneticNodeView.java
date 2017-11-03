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
 *  Copyright (C) 2017 Daniel H. Huson
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
import javafx.scene.shape.Circle;

/**
 * node view
 * Daniel Huson, 10.2107
 */
public class PhylogeneticNodeView {
    private Group parts;
    private Group labels;
    private Point2D location;

    public PhylogeneticNodeView() {
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

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    /**
     * create a simple node view
     *
     * @param location
     * @param text
     * @return
     */
    public static PhylogeneticNodeView createDefaultNodeView(Point2D location, String text) {
        final PhylogeneticNodeView nodeView = new PhylogeneticNodeView();
        nodeView.setLocation(location);
        Circle circle = new Circle(location.getX(), location.getY(), 2);
        nodeView.addParts(circle);
        circle.setFill(Color.BLUE);
        if (text != null && text.length() > 0) {
            Label label = new Label(text);
            label.setLayoutX(location.getX() + circle.getRadius() + 3);
            label.setLayoutY(location.getY());
            nodeView.addLabels(label);
        }
        return nodeView;
    }
}
