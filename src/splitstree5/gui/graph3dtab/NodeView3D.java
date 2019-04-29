/*
 *  NodeView3D.java Copyright (C) 2019 Daniel H. Huson
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

import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import jloda.fx.util.SelectionEffect;
import jloda.graph.Node;
import splitstree5.gui.formattab.FormatItem;
import splitstree5.gui.graphtab.base.NodeViewBase;

/**
 * a 3D node view
 * Daniel Huson, 1.2018
 */
public class NodeView3D extends NodeViewBase {
    private Shape3D shape;
    private Color color;
    private Rectangle selectionRectangle;

    /**
     * constructor
     *
     * @param v
     * @param location
     * @param text
     */
    public NodeView3D(Node v, Point3D location, String text) {
        super(v);

        setLocation(location);
        Sphere sphere = new Sphere(2);
        setShape(sphere);

        if (text != null && text.length() > 0) {
            color = Color.GOLD;
            setLabel(new Label(text));
            getLabel().setVisible(false);
            getLabel().setStyle("");
        } else {
            color = Color.SILVER;
            sphere.setRadius(1);
        }

        final PhongMaterial material = new PhongMaterial(color);
        material.setSpecularColor(color.brighter());
        sphere.setMaterial(material);
    }

    private void updateStuff() {
        if (label != null || shape != null) {
            EventHandler<MouseEvent> mouseEnteredEventHandler = event -> {
                if (shape != null) {
                    shape.setScaleX(2 * shape.getScaleX());
                    shape.setScaleY(2 * shape.getScaleY());
                    shape.setScaleZ(2 * shape.getScaleZ());
                }
                if (label != null) {
                    label.setScaleX(1.2 * label.getScaleX());
                    label.setScaleY(1.2 * label.getScaleY());
                }

            };
            EventHandler<MouseEvent> mouseExitedEventHandler = event -> {
                if (shape != null) {
                    shape.setScaleX(0.5 * shape.getScaleX());
                    shape.setScaleY(0.5 * shape.getScaleY());
                    shape.setScaleZ(0.5 * shape.getScaleZ());
                }
                if (label != null) {
                    label.setScaleX(1.0 / 1.2 * label.getScaleX());
                    label.setScaleY(1.0 / 1.2 * label.getScaleY());
                }
            };
            if (shape != null) {
                shape.setOnMouseEntered(mouseEnteredEventHandler);
                shape.setOnMouseExited(mouseExitedEventHandler);
            }
            if (label != null) {
                label.setOnMouseEntered(mouseEnteredEventHandler);
                label.setOnMouseExited(mouseExitedEventHandler);
            }
            if (shape != null && label != null)
                Tooltip.install(shape, new Tooltip(label.getText()));
        }
    }


    public Point3D getLocation() {
        return new Point3D(shapeGroup.getTranslateX(), shapeGroup.getTranslateY(), shapeGroup.getTranslateZ());
    }

    public void setLocation(Point3D location) {
        shapeGroup.setTranslateX(location.getX());
        shapeGroup.setTranslateY(location.getY());
        shapeGroup.setTranslateZ(location.getZ());
        if (selectionRectangle != null && selectionRectangle.getUserData() instanceof ChangeListener)
            ((ChangeListener) selectionRectangle.getUserData()).changed(null, null, null);
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
        updateStuff();
    }

    public void setLabel(Labeled label) {
        if (this.label != null) {
            labelGroup.getChildren().remove(this.label);
        }
        this.label = label;
        if (this.label != null)
            labelGroup.getChildren().add(this.label);
        bindLabel();
        updateStuff();

    }

    public void setFill(Color color) {
        this.color = color;
        if (shape != null) {
            final PhongMaterial material = (PhongMaterial) shape.getMaterial();
            if (material != null) {
                material.setDiffuseColor(color);
                material.setSpecularColor(color.brighter());
            }
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
        if (selectionRectangle != null) {
            if (selected) {
                if (!labelGroup.getChildren().contains(selectionRectangle))
                    labelGroup.getChildren().add(selectionRectangle);
                if (label != null)
                    label.setTextFill(SelectionEffect.getInstance().getColor());
            } else {
                if (labelGroup.getChildren().contains(selectionRectangle))
                    labelGroup.getChildren().remove(selectionRectangle);
                if (label != null)
                    label.setTextFill(Color.BLACK);
            }
        }
    }

    @Override
    public boolean isShownAsSelected() {
        return selectionRectangle != null && labelGroup.getChildren().contains(selectionRectangle);
    }

    @Override
    public double getWidth() {
        return shapeGroup.getScaleX() * shapeGroup.getBoundsInLocal().getWidth();
    }

    @Override
    public double getHeight() {
        return shapeGroup.getScaleY() * shapeGroup.getBoundsInLocal().getHeight();
    }

    @Override
    public void setWidth(double width) {
        shapeGroup.setScaleX(width / shapeGroup.getBoundsInLocal().getWidth());
        shapeGroup.setScaleY(width / shapeGroup.getBoundsInLocal().getWidth());
        shapeGroup.setScaleZ(width / shapeGroup.getBoundsInLocal().getWidth());

    }

    @Override
    public void setHeight(double height) {
        shapeGroup.setScaleY(height / shapeGroup.getBoundsInLocal().getHeight());
        shapeGroup.setScaleY(height / shapeGroup.getBoundsInLocal().getWidth());
        shapeGroup.setScaleZ(height / shapeGroup.getBoundsInLocal().getWidth());

    }

    @Override
    public javafx.scene.Node getNodeShape() {
        return shape;
    }

    private void bindLabel() {
        if (this.label != null && selectionRectangle != null) {
            label.layoutXProperty().bind(selectionRectangle.xProperty().add(selectionRectangle.widthProperty()).add(4));
            label.layoutYProperty().bind(selectionRectangle.yProperty().add(selectionRectangle.heightProperty().multiply(-0.5)));
        }
    }

    /**
     * setup the selection rectangle
     *
     * @param pane        that contains 3D shapes
     * @param viewChanged
     */
    public void setupSelectionRectangle(Pane pane, Binding viewChanged) {
        if (selectionRectangle != null)
            labelGroup.getChildren().remove(selectionRectangle);

        selectionRectangle = createBoundingRectangleWithBinding(pane, shapeGroup, viewChanged);
        if (label != null)
            label.setVisible(true);
        bindLabel();
    }

    /**
     * create a bounding box that is bound to user determined transformations
     */
    private Rectangle createBoundingRectangleWithBinding(Pane pane, javafx.scene.Node shape, final Binding viewChanged) {
        final Rectangle rectangle = new Rectangle();
        rectangle.setStroke(SelectionEffect.getInstance().getColor());
        rectangle.setEffect(new DropShadow(1, 1, 1, Color.DARKGRAY));
        rectangle.setStrokeWidth(2);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setMouseTransparent(true);
        rectangle.setVisible(true);

        final ChangeListener changeListener = (c, o, n) -> {
            BoundingBox boundingBox = computeBoundingBox(pane, shape);
            rectangle.setX(boundingBox.getMinX());
            rectangle.setY(boundingBox.getMinY());
            rectangle.setWidth(boundingBox.getWidth());
            rectangle.setHeight(boundingBox.getHeight());
        };
        rectangle.setUserData(changeListener);
        viewChanged.addListener(new WeakChangeListener(changeListener));

        return rectangle;
    }

    private static BoundingBox computeBoundingBox(Pane pane, javafx.scene.Node node) {
        try {
            final Bounds boundsOnScreen = node.localToScreen(node.getBoundsInLocal());
            final Bounds paneBoundsOnScreen = pane.localToScreen(pane.getBoundsInLocal());
            final double xInScene = boundsOnScreen.getMinX() - paneBoundsOnScreen.getMinX();
            final double yInScene = boundsOnScreen.getMinY() - paneBoundsOnScreen.getMinY();
            return new BoundingBox(xInScene - 2, yInScene - 2, boundsOnScreen.getWidth() + 4, boundsOnScreen.getHeight() + 4);
        } catch (NullPointerException e) {
            return new BoundingBox(0, 0, 0, 0);
        }
    }

    public void translate(Point3D translateVector) {
        setLocation(getLocation().add(translateVector));
    }
}
