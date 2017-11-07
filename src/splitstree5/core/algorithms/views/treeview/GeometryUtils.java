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
package splitstree5.core.algorithms.views.treeview;

import javafx.geometry.Point2D;

public class GeometryUtils {
    private final static double RAD_TO_DEG_FACTOR = 180.0 / Math.PI;
    private final static double DEG_TO_RAD_FACTOR = Math.PI / 180.0;

    /**
     * Computes the angle of a two-dimensional vector.
     *
     * @param p Point2D
     * @return angle in degrees
     */
    public static double computeAngle(Point2D p) {
        if (p.getX() != 0) {
            double x = Math.abs(p.getX());
            double y = Math.abs(p.getY());
            double a = Math.atan(y / x);

            if (p.getX() > 0) {
                if (p.getY() > 0)
                    return rad2deg(a);
                else
                    return rad2deg(2.0 * Math.PI - a);
            } else // p.getX()<0
            {
                if (p.getY() > 0)
                    return rad2deg(Math.PI - a);
                else
                    return rad2deg(Math.PI + a);
            }
        } else if (p.getY() > 0)
            return rad2deg(0.5 * Math.PI);
        else // p.y<0
            return rad2deg(-0.5 * Math.PI);
    }

    public static double rad2deg(double angle) {
        angle *= RAD_TO_DEG_FACTOR;
        while (angle > 360)
            angle -= 360;
        while (angle < 0)
            angle += 360;
        return angle;
    }

    public static double deg2rad(double deg) {
        return DEG_TO_RAD_FACTOR * deg;
    }

    /**
     * Rotates a point by angle alpha around a second point
     *
     * @param src    the point to be rotated
     * @param alpha  the angle
     * @param anchor the anchor point
     * @return the rotated point
     */
    public static Point2D rotateAbout(Point2D src, double alpha, Point2D anchor) {
        Point2D tar = new Point2D(src.getX() - anchor.getX(), src.getY() - anchor.getY());
        tar = rotate(tar, alpha);
        tar = new Point2D(tar.getX() + anchor.getX(), tar.getY() + anchor.getY());
        return tar;
    }

    /**
     * Translate a point in the direction specified by an angle.
     *
     * @param apt   Point2D
     * @param alpha double in degrees
     * @param dist  double
     * @return Point2D
     */
    public static Point2D translateByAngle(Point2D apt, double alpha, double dist) {
        double dx = dist * Math.cos(DEG_TO_RAD_FACTOR * alpha);
        double dy = dist * Math.sin(DEG_TO_RAD_FACTOR * alpha);
        if (Math.abs(dx) < 0.000000001)
            dx = 0;
        if (Math.abs(dy) < 0.000000001)
            dy = 0;
        return new Point2D(apt.getX() + dx, apt.getY() + dy);
    }

    /**
     * Rotates a two-dimensional vector by the angle alpha.
     *
     * @param p     point
     * @param alpha angle in degree
     * @return q point rotated around origin
     */
    public static Point2D rotate(Point2D p, double alpha) {
        double sina = Math.sin(DEG_TO_RAD_FACTOR * alpha);
        double cosa = Math.cos(DEG_TO_RAD_FACTOR * alpha);
        return new Point2D(p.getX() * cosa - p.getY() * sina, p.getX() * sina + p.getY() * cosa);
    }
}
