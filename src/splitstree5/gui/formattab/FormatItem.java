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

package splitstree5.gui.formattab;

import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.shapes.ISized;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import splitstree5.gui.graphtab.base.AEdgeView;
import splitstree5.gui.graphtab.base.ANodeView;

import java.util.Collection;

/**
 * contains format style attributes associated with a node or edge
 * Daniel Huson, 1.2018
 */
public class FormatItem implements Cloneable {
    private boolean labelFontSet = false;
    private boolean labelColorSet = false;

    private boolean nodeShapeSet = false;
    private boolean nodeSizeSet = false;

    private boolean nodeColorSet = false;

    private boolean edgeWidthSet = false;
    private boolean edgeColorSet = false;

    private Font font;
    private Color labelColor = null;

    private NodeShape nodeShape;
    private Integer nodeWidth;
    private Integer nodeHeight;

    private Color nodeColor = null;

    private Integer edgeWidth = null;
    private Color edgeColor = null;

    public FormatItem() {
    }

    public Font getFont() {
        return font;
    }

    public Color getEdgeColor() {
        return edgeColor;
    }

    public Color getNodeColor() {
        return nodeColor;
    }

    public NodeShape getNodeShape() {
        return nodeShape;
    }

    public Integer getNodeWidth() {
        return nodeWidth;
    }

    public Integer getNodeHeight() {
        return nodeHeight;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public Integer getEdgeWidth() {
        return edgeWidth;
    }

    public void clear() {
        labelFontSet = false;
        labelColorSet = false;

        nodeShapeSet = false;
        nodeSizeSet = false;
        nodeColorSet = false;

        edgeColorSet = false;
        edgeWidthSet = false;

        font = null;
        labelColor = null;

        nodeShape = null;
        nodeWidth = null;
        nodeHeight = null;
        nodeColor = null;

        edgeColor = null;
        edgeWidth = null;
    }

    /**
     * adds the font to the fonts seen
     *
     * @param font
     */
    public void addFont(Font font) {
        if (this.font == null) {
            this.font = font;
            labelFontSet = true;
        } else if (labelFontSet && !this.font.equals(font))
            this.font = null;
    }

    /**
     * adds the edgeColor to the edgeColors seen
     *
     * @param edgeColor
     */
    public void addEdgeColor(Color edgeColor) {
        if (this.edgeColor == null) {
            this.edgeColor = edgeColor;
            edgeColorSet = true;
        } else if (edgeColorSet && !this.edgeColor.equals(edgeColor))
            this.edgeColor = null;
    }

    /**
     * adds the nodeColor to the nodeColors seen
     *
     * @param nodeColor
     */
    public void addNodeColor(Color nodeColor) {
        if (this.nodeColor == null) {
            this.nodeColor = nodeColor;
            nodeColorSet = true;
        } else if (nodeColorSet && !this.nodeColor.equals(nodeColor))
            this.nodeColor = null;
    }

    /**
     * adds the labelColor to the labelColors seen
     *
     * @param labelColor
     */
    public void addLabelColor(Color labelColor) {
        if (this.labelColor == null) {
            this.labelColor = labelColor;
            labelColorSet = true;
        } else if (labelColorSet && !this.labelColor.equals(labelColor))
            this.labelColor = null;
    }

    /**
     * adds the edgeWidth to the edgeWidths seen
     *
     * @param edgeWidth
     */
    public void addEdgeWidth(Integer edgeWidth) {
        if (this.edgeWidth == null) {
            this.edgeWidth = edgeWidth;
            edgeWidthSet = true;
        } else if (edgeWidthSet && !this.edgeWidth.equals(edgeWidth))
            this.edgeWidth = null;
    }

    public void addNodeShape(NodeShape nodeShape) {
        if (this.nodeShape == null) {
            this.nodeShape = nodeShape;
            nodeShapeSet = true;
        } else if (nodeShapeSet && !this.nodeShape.equals(nodeShape))
            this.nodeShape = null;
    }

    public void addNodeSize(Integer width, Integer height) {
        if (this.nodeWidth == null && this.nodeHeight == null) {
            this.nodeWidth = width;
            this.nodeHeight = height;
            nodeSizeSet = true;
        } else if (nodeSizeSet && (this.nodeWidth != width || this.nodeHeight != height)) {
            this.nodeWidth = null;
            this.nodeHeight = null;
        }
    }

    public boolean isLabelFontSet() {
        return labelFontSet;
    }

    public boolean isEdgeColorSet() {
        return edgeColorSet;
    }

    public boolean isNodeColorSet() {
        return nodeColorSet;
    }

    public boolean isLabelColorSet() {
        return labelColorSet;
    }

    public boolean isEdgeWidthSet() {
        return edgeWidthSet;
    }

    public boolean isNodeShapeSet() {
        return nodeShapeSet;
    }

    public boolean isNodeSizeSet() {
        return nodeSizeSet;
    }

    /**
     * determines the current format item based on the current selection
     *
     * @param nodes
     * @param node2view
     * @param edges
     * @param edge2view
     * @return current format item
     */
    public static FormatItem createFromSelection(Collection<Node> nodes, NodeArray<ANodeView> node2view,
                                                 Collection<Edge> edges, EdgeArray<AEdgeView> edge2view) {
        FormatItem formatItem = new FormatItem();

        if (nodes != null && node2view != null) {
            for (Node v : nodes) {
                final ANodeView nv = node2view.get(v);
                if (nv.getLabel() != null && nv.getLabel() instanceof Labeled) {
                    formatItem.addFont(nv.getLabel().getFont());
                    formatItem.addLabelColor((Color) nv.getLabel().getTextFill());
                }
                if (nv.getShape() != null && nv.getShape() instanceof Shape) {
                    formatItem.addNodeShape((NodeShape.valueOf(nv.getShape())));
                    formatItem.addNodeSize((int) getWidth(nv.getShape()), (int) getHeight(nv.getShape()));
                    formatItem.addNodeColor((Color) (nv.getShape()).getFill());
                }
            }
        }
        if (edges != null && edge2view != null) {
            for (Edge e : edges) {
                final AEdgeView nv = edge2view.get(e);
                if (nv.getLabel() != null && nv.getLabel() instanceof Labeled) {
                    formatItem.addFont(nv.getLabel().getFont());
                    formatItem.addLabelColor((Color) nv.getLabel().getTextFill());
                }
                if (nv.getShape() != null && nv.getShape() instanceof Shape) {
                    formatItem.addEdgeWidth((int) (nv.getShape()).getStrokeWidth());
                    formatItem.addEdgeColor((Color) (nv.getShape()).getStroke());
                }
            }
        }
        return formatItem;
    }

    /**
     * apply this format item to the selected nodes and edges
     *
     * @param nodes
     * @param node2view
     * @param edges
     * @param edge2view
     */
    public void apply(Collection<Node> nodes, NodeArray<ANodeView> node2view,
                      Collection<Edge> edges, EdgeArray<AEdgeView> edge2view) {

        if ((isLabelFontSet() || isLabelColorSet() || isNodeSizeSet() || isNodeShapeSet() || isNodeColorSet())
                && nodes != null && node2view != null) {
            for (Node v : nodes) {
                final ANodeView nv = node2view.get(v);
                if (nv.getLabel() != null) {
                    if (isLabelFontSet())
                        nv.getLabel().setFont(font);
                    if (isLabelColorSet())
                        nv.getLabel().setTextFill(getLabelColor());
                }

                if (nv.getShape() != null) {
                    if (isNodeShapeSet()) {
                        nv.setShape(NodeShape.create(getNodeShape(), (int) getWidth(nv.getShape())));
                    }
                    if (isNodeSizeSet()) {
                        double width = (getNodeWidth() != null ? getNodeWidth() : -1);
                        double height = (getNodeHeight() != null ? getNodeHeight() : -1);
                        if (nv.getShape() instanceof ISized) {
                            if (width == -1)
                                width = ((ISized) nv.getShape()).getWidth();
                            if (height == -1)
                                height = ((ISized) nv.getShape()).getHeight();
                            ((ISized) nv.getShape()).setSize(width, height);
                        }
                    }
                    if (isNodeColorSet()) {
                        nv.getShape().setFill(getNodeColor());
                    }
                }
            }
        }

        if ((isLabelFontSet() || isLabelColorSet() || isEdgeColorSet() || isEdgeWidthSet())
                && edges != null && edge2view != null) {
            for (Edge e : edges) {
                final AEdgeView nv = edge2view.get(e);
                if (nv.getLabel() != null) {
                    if (isLabelFontSet())
                        nv.getLabel().setFont(font);
                    if (isLabelColorSet())
                        nv.getLabel().setTextFill(getLabelColor());
                }
                if (nv.getShape() != null) {
                    if (isEdgeWidthSet())
                        (nv.getShape()).setStrokeWidth(getEdgeWidth());
                    if (isEdgeColorSet())
                        (nv.getShape()).setStroke(getEdgeColor());
                }
            }
        }
    }


    private static double getWidth(Shape shape) {
        if (shape instanceof ISized)
            return ((ISized) shape).getWidth();
        else
            return shape.getLayoutBounds().getWidth();
    }

    private static double getHeight(Shape shape) {
        if (shape instanceof ISized)
            return ((ISized) shape).getHeight();
        else
            return shape.getLayoutBounds().getHeight();
    }

    public FormatItem clone() {
        try {
            return (FormatItem) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getName() {
        String name = null;
        if (labelFontSet)
            name = "Font";
        if (labelColorSet) {
            if (name == null)
                name = "Label Color";
            else
                return "Format";
        }
        if (nodeShapeSet) {
            if (name == null)
                name = "Node Shape";
            else
                return "Format";
        }
        if (nodeSizeSet) {
            if (name == null)
                name = "Node Size";
            else
                return "Format";
        }
        if (nodeColorSet) {
            if (name == null)
                name = "Node Color";
            else
                return "Format";
        }

        if (edgeWidthSet) {
            if (name == null)
                name = "Edge Width";
            else
                return "Format";
        }
        if (edgeColorSet) {
            if (name == null)
                name = "Edge Color";
            else
                return "Format";
        }
        if (name == null)
            return "None";
        else
            return name;
    }
}
