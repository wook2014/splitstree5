/*
 * EdgeView3D.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import jloda.fx.util.SelectionEffect;
import jloda.graph.Edge;
import splitstree5.gui.graphtab.base.EdgeViewBase;

/**
 * a 3D edge view
 * Daniel Huson, 1.2018
 */
public class EdgeView3D extends EdgeViewBase {
    private static final EventHandler<MouseEvent> mouseEnteredHandler = (x) -> ((Line3D) x.getSource()).setLineWidth(2 * ((Line3D) x.getSource()).getLineWidth());
    private static final EventHandler<MouseEvent> mouseExitedHandler = (x) -> ((Line3D) x.getSource()).setLineWidth(0.5 * ((Line3D) x.getSource()).getLineWidth());

    private final Line3D line;
    private Color color;

    public EdgeView3D(Edge e, Point3D start, Point3D end) {
        super(e);
        color = Color.SILVER;
        line = new Line3D(start, end);
        line.setColor(color);
        shapeGroup.getChildren().add(line);
        line.setOnMouseEntered(mouseEnteredHandler);
        line.setOnMouseExited(mouseExitedHandler);
    }

    public Line3D getShape() {
        return line;
    }

    public void setShape(Line3D shape) {
        System.err.println("EdgeView3D.setShape(): Not supported");
    }

    @Override
    public void showAsSelected(boolean selected) {
        if (selected) {
            if (label != null)
                label.setEffect(SelectionEffect.getInstance());
            if (line != null)
                ((PhongMaterial) line.getMaterial()).setDiffuseColor(SelectionEffect.getInstance().getColor());
        } else {
            if (label != null)
                label.setEffect(null);
            if (line != null)
                ((PhongMaterial) line.getMaterial()).setDiffuseColor(color);

        }
    }

    @Override
    public void setLabel(Labeled label) {
        if (this.label != null)
            getLabelGroup().getChildren().remove(this.label);
        super.setLabel(label);
        if (this.label != null && !getLabelGroup().getChildren().contains(this.label))
            getLabelGroup().getChildren().add(this.label);
    }


    @Override
    public boolean isShownAsSelected() {
        if (label != null)
            return label.getEffect() != null;
        else
            return line != null && line.getEffect() != null;
    }

    @Override
    public Color getStroke() {
        return color;
    }

    @Override
    public double getStrokeWidth() {
        return line.getLineWidth();
    }

    @Override
    public void setStroke(Color color) {
        this.color = color;
        if (!line.getColor().equals(SelectionEffect.getInstance().getColor()))
            line.setColor(this.color);
    }

    @Override
    public void setStrokeWidth(double width) {
        line.setLineWidth(width);

    }

    @Override
    public Node getEdgeShape() {
        return getShape();
    }

    public void updateCoordinates(Point3D sourceLocation, Point3D targetLocation) {
        line.setCoordinates(sourceLocation, targetLocation);
    }
}
