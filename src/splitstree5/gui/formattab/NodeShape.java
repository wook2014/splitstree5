/*
 *  NodeShape.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.gui.formattab;

import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import jloda.fx.shapes.*;
import jloda.util.Basic;

public enum NodeShape {
    Square, Circle, TriangleUp, TriangleDown, Diamond, Hexagon, Rectangle, Oval, Other, None;

    /**
     * determines the node shape of a shape
     *
     * @param shape
     * @return node shape
     */
    public static NodeShape valueOf(Node shape) {
        if (shape == null)
            return None;
        if (shape instanceof Circle)
            return Circle;
        else if (shape instanceof SquareShape)
            return Square;
        else if (shape instanceof DiamondShape)
            return Diamond;
        else if (shape instanceof HexagonShape)
            return Hexagon;
        else if (shape instanceof TriangleUpShape)
            return TriangleUp;
        else if (shape instanceof TriangleDownShape)
            return TriangleDown;
        else if (shape instanceof RectangleShape)
            return Rectangle;
        else if (shape instanceof OvalShape)
            return Oval;
        else
            return Other;
    }

    /**
     * creates a shape
     *
     * @param name
     * @param size
     * @return shape
     */
    public static Shape create(String name, int size) {
        return create(name, size, size);
    }

    /**
     * creates a shape
     *
     * @param nodeShape
     * @param size
     * @return shape
     */
    public static Shape create(NodeShape nodeShape, int size) {
        return create(nodeShape, size, size);
    }

    /**
     * creates a shape
     *
     * @param name
     * @param width
     * @param height
     * @return
     */
    public static Shape create(String name, int width, int height) {
        final NodeShape nodeShape = Basic.valueOfIgnoreCase(NodeShape.class, name);
        if (nodeShape != null)
            return create(nodeShape, width, height);
        else
            return null;
    }

    /**
     * create a shape for a node shape
     *
     * @param nodeShape
     * @param width
     * @param height
     * @return shape
     */
    public static Shape create(NodeShape nodeShape, int width, int height) {
        switch (nodeShape) {
            case Square:
                return new SquareShape(width);
            case Rectangle:
                return new RectangleShape(width, height);
            default:
            case Circle:
                if (width == height)
                    return new CircleShape(0.5 * width);
            case Oval:
                return new OvalShape(0.5 * width, 0.5 * height);
            case TriangleUp:
                return new TriangleUpShape(width, height);
            case TriangleDown:
                return new TriangleDownShape(width, height);
            case Diamond:
                return new DiamondShape(width, height);
            case Hexagon:
                return new HexagonShape(width, height);
        }
    }

    // todo: implement different shapes here
    public static Shape3D create3D(Node shape, int width) {
        ((Sphere) shape).setRadius(0.5 * width);
        return (Sphere) shape;
    }

    public static int[] getSize(Shape shape) {
        return new int[]{(int) Math.round(shape.getBoundsInLocal().getWidth()), (int) Math.round(shape.getBoundsInLocal().getHeight())};
    }
}
