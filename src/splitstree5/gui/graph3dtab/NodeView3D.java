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
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import jloda.graph.Node;
import jloda.util.ProgramProperties;
import splitstree5.gui.formattab.FormatItem;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.gui.utils.SelectionEffect;

/**
 * a 3D node view
 * Daniel Huson, 1.2018
 */
public class NodeView3D extends NodeViewBase {
    private Shape3D shape;
    private Point3D location;
    private Color color;


    public NodeView3D(Node v, Point3D location, String text) {
        super(v);

        setLocation(location);
        Sphere sphere = new Sphere(2);
        shape = sphere;
        shapeGroup.getChildren().add(shape);
        sphere.setTranslateX(location.getX());
        sphere.setTranslateY(location.getY());
        sphere.setTranslateZ(location.getZ());

        if (text != null && text.length() > 0) {
            color = Color.GOLDENROD;
            label = new Label(text);
            label.setFont(ProgramProperties.getDefaultFont());
            label.setLayoutX(location.getX() + +2);
            label.setLayoutY(location.getY());
            labelGroup.getChildren().add(label);
        } else {
            color = Color.BLACK;
            sphere.setRadius(0.75);
            label = null;
        }
        final PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(Color.WHITE);
        sphere.setMaterial(material);
        updateStuff();

    }

    private void updateStuff() {
    }

    public Point3D getLocation() {
        return location;
    }

    public void setLocation(Point3D location) {
        this.location = location;
    }

    public Shape3D getShape() {
        return shape;
    }

    public void setShape(Shape3D shape) {
        if (this.shape != null)
            shapeGroup.getChildren().remove(this.shape);
        this.shape = shape;
        if (this.shape != null)
            shapeGroup.getChildren().add(this.shape);
    }

    public void setLabel(Labeled label) {
        if (this.label != null)
            labelGroup.getChildren().remove(this.label);
        this.label = label;
        if (this.label != null)
            labelGroup.getChildren().add(this.label);
    }


    public void setFill(Color color) {
        this.color = color;
        if (shape != null) {
            PhongMaterial material = (PhongMaterial) shape.getMaterial();
            if (material != null && material.getDiffuseColor() != null && !material.getDiffuseColor().equals(SelectionEffect.getInstance().getColor()))
                material.setDiffuseColor(color);
        }
    }

    public Color getFill() {
        return color;
    }

    public void setStrokeWidth(double width) {

    }

    public double getStrokeWidth() {
        return 0;
    }

    public void setStyling(FormatItem styling) {

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
    public boolean isShownAsSelected() {
        if (label != null)
            return label.getEffect() != null;
        else
            return shape != null && shape.getEffect() != null;
    }

    @Override
    public double getWidth() {
        return shape.getScaleX() * shape.getBoundsInLocal().getWidth();
    }

    @Override
    public double getHeight() {
        return shape.getScaleY() * shape.getBoundsInLocal().getHeight();
    }

    @Override
    public void setWidth(double width) {
        shape.setScaleX(width / shape.getBoundsInLocal().getWidth());
        shape.setScaleY(width / shape.getBoundsInLocal().getWidth());
        shape.setScaleZ(width / shape.getBoundsInLocal().getWidth());

    }

    @Override
    public void setHeight(double height) {
        shape.setScaleY(height / shape.getBoundsInLocal().getHeight());
        shape.setScaleY(height / shape.getBoundsInLocal().getWidth());
        shape.setScaleZ(height / shape.getBoundsInLocal().getWidth());

    }

    @Override
    public javafx.scene.Node getNodeShape() {
        return shape;
    }
}
