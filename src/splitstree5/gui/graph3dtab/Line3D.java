/*
 * Line3D.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.gui.graph3dtab;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;

/**
 * a 3D line implemented as a cylinder
 * Daniel Huson, 11.2017
 */
public class Line3D extends Cylinder {
    private static final Point3D yAxis = new Point3D(0, 100, 0);

    /**
     * constructor
     */
    public Line3D(Point3D start, Point3D end) {
        super(0.5, 100, 8);

        final PhongMaterial material = new PhongMaterial();
        setMaterial(material);
        setCoordinates(start, end);
    }

    /**
     * change the coordinates
     *
	 */
    public void setCoordinates(Point3D start, Point3D end) {
        final Point3D midpoint = start.midpoint(end);
        final Point3D direction = end.subtract(start);

        final Point3D perpendicularAxis = yAxis.crossProduct(direction);
        final double angle = yAxis.angle(direction);
        setRotationAxis(perpendicularAxis);
        setRotate(angle);

        setTranslateX(midpoint.getX());
        setTranslateY(midpoint.getY());
        setTranslateZ(midpoint.getZ());

        setScaleY(start.distance(end) / getHeight());
    }

    public void setColor(Color color) {
        final PhongMaterial material = (PhongMaterial) getMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(color.brighter());
    }

    public Color getColor() {
        final PhongMaterial material = (PhongMaterial) getMaterial();
        return material.getDiffuseColor();
    }


    public void setLineWidth(double width) {
        setRadius(0.5 * width);
    }

    public double getLineWidth() {
        return 2 * getRadius();
    }

    /**
     * constructor
     *
	 */
    public Line3D(DoubleProperty startXProperty, DoubleProperty startYProperty, DoubleProperty startZProperty,
                  DoubleProperty endXProperty, DoubleProperty endYProperty, DoubleProperty endZProperty, Color color) {
        super(1, 100, 8);

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(color.brighter());
        setMaterial(material);

        final InvalidationListener listener = createInvalidationListener(startXProperty, startYProperty, startZProperty, endXProperty, endYProperty, endZProperty);
        startXProperty.addListener(listener);
        startYProperty.addListener(listener);
        startZProperty.addListener(listener);
        endXProperty.addListener(listener);
        endYProperty.addListener(listener);
        endZProperty.addListener(listener);

        listener.invalidated(null); // fire now
    }

    /**
     * updates cylinder when location of start or end changes
     *
	 */
    private InvalidationListener createInvalidationListener(DoubleProperty startX, DoubleProperty startY, DoubleProperty startZ, DoubleProperty endX, DoubleProperty endY, DoubleProperty endZ) {
        return observable -> {
            final Point3D start = new Point3D(startX.get(), startY.get(), startZ.get());
            final Point3D end = new Point3D(endX.get(), endY.get(), endZ.get());
            final Point3D midpoint = start.midpoint(end);
            final Point3D direction = end.subtract(start);

            final Point3D perpendicularAxis = yAxis.crossProduct(direction);
            final double angle = yAxis.angle(direction);
            setRotationAxis(perpendicularAxis);
            setRotate(angle);

            setTranslateX(midpoint.getX());
            setTranslateY(midpoint.getY());
            setTranslateZ(midpoint.getZ());

            setScaleY(start.distance(end) / getHeight());

        };
    }
}
