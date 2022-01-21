/*
 * EdgePoint.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.geometry.Point2D;

/**
 * points used to determine how to draw an edge
 * Daniel Huson, 10.2017
 */
public class EdgePoint {
    public enum Type {StartPoint, EndPoint, Corner, MidPoint, SupportPoint, PrePoint, PostPoint}

    private final Point2D point;
    private final Type type;

    public EdgePoint(Type type, Point2D point) {
        this.point = point;
        this.type = type;
    }

    public Point2D getPoint() {
        return point;
    }

    public double getX() {
        return point.getX();
    }

    public double getY() {
        return point.getY();
    }

    public Type getType() {
        return type;
    }

    /**
     * gets an edge point by type
     *
     * @param type
     * @param edgePoints
     * @return first edge point of given type or null
     */
    public static EdgePoint getByType(Type type, EdgePoint[] edgePoints) {
        for (EdgePoint edgePoint : edgePoints) {
            if (edgePoint.getType() == type)
                return edgePoint;
        }
        return null;
    }
}
