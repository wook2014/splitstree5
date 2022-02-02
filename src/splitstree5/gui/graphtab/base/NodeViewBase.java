/*
 * NodeViewBase.java Copyright (C) 2022 Daniel H. Huson
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

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.graph.Node;
import jloda.util.BitSetUtils;

import java.util.BitSet;

/**
 * node view
 * Daniel Huson, 10.2017
 */
public abstract class NodeViewBase {
    protected final Group shapeGroup = new Group();
    protected final Group labelGroup = new Group();
    protected RichTextLabel label;
    private BitSet workingTaxa;

    private final Node v;

    /**
     * construct a simple node view
     *
	 */
    public NodeViewBase(Node v, Iterable<Integer> workingTaxonIds) {
        this.v = v;
        if (workingTaxonIds.iterator().hasNext()) {
            this.workingTaxa = BitSetUtils.asBitSet(workingTaxonIds);
        }
    }

    public Group getShapeGroup() {
        return shapeGroup;
    }

    public Group getLabelGroup() {
        return labelGroup;
    }


    public Node getNode() {
        return v;
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

    public abstract Color getFill();

    public abstract double getWidth();

    public abstract double getHeight();


    public abstract void setFill(Color stroke);

    public abstract void setWidth(double width);

    public abstract void setHeight(double height);


    public RichTextLabel getLabel() {
        return label;
    }

    public void setLabel(RichTextLabel label) {
        this.label = label;
    }

    public void setLabel(String text) {
        if (text == null)
            setLabel((RichTextLabel) null);
        else if (getLabel() != null)
            getLabel().setText(text);
        else
            setLabel(new RichTextLabel(text));
    }

    public abstract void showAsSelected(boolean selected);

    public abstract boolean isShownAsSelected();

    public abstract javafx.scene.Node getNodeShape();

    public BitSet getWorkingTaxa() {
        if (workingTaxa == null)
            workingTaxa = new BitSet();
        return workingTaxa;
    }

    public int getNumberOfWorkingTaxonIds() {
        return workingTaxa == null ? 0 : workingTaxa.cardinality();
    }
}
