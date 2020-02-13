/*
 * FontSizeIncrementCommand.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.formattab;

import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.FontUtils;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.NodeViewBase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * undoable font size increment
 */
public class FontSizeIncrementCommand extends UndoableRedoableCommand {
    private final double increment;

    private final ArrayList<Node> nodes;
    private final NodeArray<NodeViewBase> node2view;
    private final ArrayList<Edge> edges;
    private final EdgeArray<EdgeViewBase> edge2view;

    /**
     * constructor
     *
     * @param increment
     * @param nodes
     * @param node2view
     * @param edges
     * @param edge2view
     */
    public FontSizeIncrementCommand(double increment, Collection<Node> nodes, NodeArray<NodeViewBase> node2view, Collection<Edge> edges, EdgeArray<EdgeViewBase> edge2view) {
        super(increment > 0 ? "Font Increase" : "Font Decrease");
        this.increment = increment;

        if (nodes != null)
            this.nodes = new ArrayList<>(nodes);
        else
            this.nodes = null;
        this.node2view = node2view;

        if (edges != null)
            this.edges = new ArrayList<>(edges);
        else
            this.edges = null;
        this.edge2view = edge2view;
    }

    @Override
    public void undo() {
        FontWeight fontWeight = null;
        FontPosture fontPosture = null;
        Font font = null;

        if (nodes != null && node2view != null) {
            for (Node v : nodes) {
                Labeled label = node2view.get(v).getLabel();
                if (label != null) {
                    final double size = (label.getFont().getSize() - increment);
                    if (size >= 0) {
                        if (font == null || !label.getFont().equals(font)) {
                            font = label.getFont();
                            fontWeight = FontUtils.getWeight(font);
                            fontPosture = FontUtils.getPosture(font);
                        }
                        label.setFont(Font.font(font.getFamily(), fontWeight, fontPosture, size));
                    }
                }
            }
        }
        if (edges != null && edge2view != null) {
            for (Edge v : edges) {
                Labeled label = edge2view.get(v).getLabel();
                if (label != null) {
                    final double size = (label.getFont().getSize() - increment);
                    if (size >= 0) {
                        if (font == null || !label.getFont().equals(font)) {
                            font = label.getFont();
                            fontWeight = FontUtils.getWeight(font);
                            fontPosture = FontUtils.getPosture(font);
                        }
                        label.setFont(Font.font(font.getFamily(), fontWeight, fontPosture, size));
                    }
                }
            }
        }
    }

    @Override
    public void redo() {
        FontWeight fontWeight = null;
        FontPosture fontPosture = null;
        Font font = null;

        if (nodes != null && node2view != null) {
            for (Node v : nodes) {
                Labeled label = node2view.get(v).getLabel();
                if (label != null) {
                    final double size = (label.getFont().getSize() + increment);
                    if (font == null || !label.getFont().equals(font)) {
                        font = label.getFont();
                        fontWeight = FontUtils.getWeight(font);
                        fontPosture = FontUtils.getPosture(font);
                    }
                    label.setFont(Font.font(font.getFamily(), fontWeight, fontPosture, size));
                }
            }
        }
        if (edges != null && edge2view != null) {
            for (Edge v : edges) {
                Labeled label = edge2view.get(v).getLabel();
                if (label != null) {
                    final double size = (label.getFont().getSize() + increment);
                    if (font == null || !label.getFont().equals(font)) {
                        font = label.getFont();
                        fontWeight = FontUtils.getWeight(font);
                        fontPosture = FontUtils.getPosture(font);
                    }
                    label.setFont(Font.font(font.getFamily(), fontWeight, fontPosture, size));
                }
            }
        }

    }

    @Override
    public boolean isUndoable() {
        return (nodes != null && nodes.size() > 0 && nodes.get(0).getOwner() != null) ||
                (edges != null && edges.size() > 0 && edges.get(0).getOwner() != null);
    }

    @Override
    public boolean isRedoable() {
        return (nodes != null && nodes.size() > 0 && nodes.get(0).getOwner() != null) ||
                (edges != null && edges.size() > 0 && edges.get(0).getOwner() != null);
    }
}
