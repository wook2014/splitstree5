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

package splitstree5.gui.graphtab.base;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import jloda.graph.Edge;

public abstract class EdgeViewBase {
    protected final Group shapeGroup = new Group();
    protected final Group labelGroup = new Group();
    protected Labeled label;

    private final Edge e;

    /**
     * construct a simple node view
     *
     * @param e
     */
    public EdgeViewBase(Edge e) {
        this.e = e;
    }


    public Group getShapeGroup() {
        return shapeGroup;
    }

    public Group getLabelGroup() {
        return labelGroup;
    }


    public Edge getEdge() {
        return e;
    }

    public void setFont(Font font) {
        if (label != null) {
            label.setFont(font);
        }
    }

    public Font getFont() {
        if (label != null) {
            return label.getFont();
        }
        return null;
    }

    public void setTextFill(Paint paint) {
        if (label != null) {
            label.setTextFill(paint);
        }
    }

    public Paint getTextFill() {
        if (label != null) {
            return label.getTextFill();
        }
        return null;
    }

    public Labeled getLabel() {
        return label;
    }

    public void setLabel(Labeled label) {
        this.label = label;
    }

    public abstract void showAsSelected(boolean selected);

    public abstract boolean isShownAsSelected();

    public abstract Color getStroke();

    public abstract double getStrokeWidth();

    public abstract void setStroke(Color stroke);

    public abstract void setStrokeWidth(double width);

    public abstract Node getEdgeShape();

}