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

package splitstree5.gui.graph3dtab;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import jloda.graph.Edge;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.utils.SelectionEffect;

/**
 * a 3D edge view
 * Daniel Huson, 1.2018
 */
public class EdgeView3D extends EdgeViewBase {
    private Line3D shape;
    private Color color;

    public EdgeView3D(Edge e) {
        super(e);
    }

    public EdgeView3D(Edge e, double weight, Point3D start, Point3D end) {
        this(e);
        color = Color.DARKGRAY;
        setShape(new Line3D(start, end, color));
    }

    public Line3D getShape() {
        return shape;
    }

    public void setShape(Line3D shape) {
        if (this.shape != null)
            shapeGroup.getChildren().remove(this.shape);
        this.shape = shape;
        if (this.shape != null)
            shapeGroup.getChildren().add(this.shape);
    }

    @Override
    public void showAsSelected(boolean selected) {
        if (selected) {
            if (label != null)
                label.setEffect(SelectionEffect.getInstance());
            if (shape != null)
                ((PhongMaterial) shape.getMaterial()).setDiffuseColor(SelectionEffect.getInstance().getColor());
        } else {
            if (label != null)
                label.setEffect(null);
            if (shape != null)
                ((PhongMaterial) shape.getMaterial()).setDiffuseColor(color);

        }
    }

    @Override
    public void setLabel(Labeled label) {
        super.setLabel(label);
    }


    @Override
    public boolean isShownAsSelected() {
        if (label != null)
            return label.getEffect() != null;
        else
            return shape != null && shape.getEffect() != null;
    }

    @Override
    public Color getStroke() {
        return color;
    }

    @Override
    public double getStrokeWidth() {
        return shape.getRadius();
    }

    @Override
    public void setStroke(Color color) {
        this.color = color;
        if (shape != null) {
            PhongMaterial material = (PhongMaterial) shape.getMaterial();
            if (material != null && material.getDiffuseColor() != null && !material.getDiffuseColor().equals(SelectionEffect.getInstance().getColor()))
                material.setDiffuseColor(this.color);
        }
    }

    @Override
    public void setStrokeWidth(double width) {
        shape.setRadius(0.5 * width);

    }

    @Override
    public Node getEdgeShape() {
        return getShape();
    }
}
