/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.graph3dtab;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import jloda.fx.ASelectionModel;
import jloda.fx.SelectionEffect;
import jloda.graph.NodeArray;
import splitstree5.gui.graphtab.base.NodeViewBase;

import java.util.HashMap;
import java.util.Map;

/**
 * maintains 2D bounding boxes for 3D node views
 * Daniel Huson, 11.2017
 */
public class BoundingBoxes2D {
    private final Group rectangles = new Group();
    private final Map<jloda.graph.Node, Rectangle> node2rectangle = new HashMap<>();
    private final ListChangeListener<jloda.graph.Node> changeListener; // keep reference

    /**
     * constructor
     *
     * @param node2view
     * @param properties
     */
    public BoundingBoxes2D(Pane bottomPane, NodeArray<NodeViewBase> node2view, ASelectionModel<jloda.graph.Node> nodeSelectionModel, ReadOnlyProperty... properties) {
        changeListener = c -> {
            while (c.next()) {
                for (jloda.graph.Node node : c.getRemoved()) {
                    rectangles.getChildren().remove(node2rectangle.get(node));
                    node2rectangle.remove(node);
                }
                for (jloda.graph.Node node : c.getAddedSubList()) {
                    final NodeView3D nodeView = (NodeView3D) node2view.get(node);
                    final Rectangle rect = createBoundingBoxWithBinding(bottomPane, nodeView.getShape(), properties);
                    node2rectangle.put(node, rect);
                    rectangles.getChildren().add(rect);
                }
            }
        };
        nodeSelectionModel.getSelectedItems().addListener(new WeakListChangeListener<>(changeListener));
    }

    public Group getRectangles() {
        return rectangles;
    }

    /**
     * create a bounding box that is bound to user determined transformations
     */
    private static Rectangle createBoundingBoxWithBinding(Pane pane, Node shape, final ReadOnlyProperty... properties) {
        final Rectangle boundingBox = new Rectangle();
        boundingBox.setStroke(SelectionEffect.getInstance().getColor());
        boundingBox.setStrokeWidth(2);
        boundingBox.setFill(Color.TRANSPARENT);
        boundingBox.setMouseTransparent(true);
        boundingBox.setVisible(true);

        final ObjectBinding<Rectangle> binding = new ObjectBinding<Rectangle>() {
            {
                bind(properties);
                bind(shape.translateXProperty());
                bind(shape.translateYProperty());
                bind(shape.translateZProperty());
                bind(shape.scaleXProperty());
            }

            @Override
            protected Rectangle computeValue() {
                return computeRectangle(pane, shape);
            }
        };

        binding.addListener((c, o, n) -> {
            boundingBox.setX(n.getX());
            boundingBox.setY(n.getY());
            boundingBox.setWidth(n.getWidth());
            boundingBox.setHeight(n.getHeight());
        });
        boundingBox.setUserData(binding);

        binding.invalidate();
        return boundingBox;
    }

    private static Rectangle computeRectangle(Pane pane, Node node) {
        try {
            final Bounds boundsOnScreen = node.localToScreen(node.getBoundsInLocal());
            final Bounds paneBoundsOnScreen = pane.localToScreen(pane.getBoundsInLocal());
            final double xInScene = boundsOnScreen.getMinX() - paneBoundsOnScreen.getMinX();
            final double yInScene = boundsOnScreen.getMinY() - paneBoundsOnScreen.getMinY();
            return new Rectangle(xInScene, yInScene, boundsOnScreen.getWidth(), boundsOnScreen.getHeight());
        } catch (NullPointerException e) {
            return new Rectangle(0, 0, 0, 0);
        }
    }
}
