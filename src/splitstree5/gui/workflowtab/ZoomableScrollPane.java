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

package splitstree5.gui.workflowtab;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.transform.Scale;

/**
 * zoomable scroll pane
 * Adapted from here: https://pixelduke.wordpress.com/2012/09/16/zooming-inside-a-scrollpane/
 * Daniel Huson, 12.2017
 */
public class ZoomableScrollPane extends ScrollPane {
    private final Scale scaleTransform;
    private final Group contentGroup;

    public ZoomableScrollPane(Node content) {
        contentGroup = new Group();
        final Group zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(content);
        setContent(contentGroup);
        scaleTransform = new Scale(1, 1, 0, 0);
        zoomGroup.getTransforms().add(scaleTransform);
    }

    public void zoomBy(double byX, double byY) {
        scaleTransform.setX(byX * scaleTransform.getX());
        scaleTransform.setY(byY * scaleTransform.getY());
    }

    public void setZoom(double x, double y) {
        scaleTransform.setX(x);
        scaleTransform.setY(y);
    }

    public void resetZoom() {
        setZoom(1, 1);
    }

    public Group getContentGroup() {
        return contentGroup;
    }
}