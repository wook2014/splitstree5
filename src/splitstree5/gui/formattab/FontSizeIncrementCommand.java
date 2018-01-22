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
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import splitstree5.gui.graphtab.base.AEdgeView;
import splitstree5.gui.graphtab.base.ANodeView;
import splitstree5.undo.UndoableRedoableCommand;

import java.util.ArrayList;
import java.util.Collection;

/**
 * undoable font size increment
 */
public class FontSizeIncrementCommand extends UndoableRedoableCommand {
    private final double increment;

    private final ArrayList<Node> nodes;
    private final NodeArray<ANodeView> node2view;
    private final ArrayList<Edge> edges;
    private final EdgeArray<AEdgeView> edge2view;

    /**
     * constructor
     *
     * @param increment
     * @param nodes
     * @param node2view
     * @param edges
     * @param edge2view
     */
    public FontSizeIncrementCommand(double increment, Collection<Node> nodes, NodeArray<ANodeView> node2view, Collection<Edge> edges, EdgeArray<AEdgeView> edge2view) {
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
        if (nodes != null && node2view != null) {
            for (Node v : nodes) {
                Labeled label = node2view.get(v).getLabel();
                if (label != null)
                    label.setStyle("-fx-font-size: " + (label.getFont().getSize() - increment) + ";");
            }
        }
        if (edges != null && edge2view != null) {
            for (Edge v : edges) {
                Labeled label = edge2view.get(v).getLabel();
                if (label != null)
                    label.setStyle("-fx-font-size: " + (label.getFont().getSize() - increment) + ";");
            }
        }
    }

    @Override
    public void redo() {
        if (nodes != null && node2view != null) {
            for (Node v : nodes) {
                Labeled label = node2view.get(v).getLabel();
                if (label != null)
                    label.setStyle("-fx-font-size: " + (label.getFont().getSize() + increment) + ";");
            }
        }
        if (edges != null && edge2view != null) {
            for (Edge v : edges) {
                Labeled label = edge2view.get(v).getLabel();
                if (label != null)
                    label.setStyle("-fx-font-size: " + (label.getFont().getSize() + increment) + ";");
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
