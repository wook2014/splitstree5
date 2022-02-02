/*
 * FormatChangeCommand.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.gui.formattab;

import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.util.Pair;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.NodeViewBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * an undoable-redoable format change
 */
public class FormatChangeCommand extends UndoableRedoableCommand {
    private final FormatItem formatItem;
    private final ArrayList<Node> nodes;
    private final NodeArray<NodeViewBase> node2view;
    private final ArrayList<Pair<Node, FormatItem>> oldNodeFormats;

    private final ArrayList<Edge> edges;
    private final EdgeArray<EdgeViewBase> edge2view;
    private final ArrayList<Pair<Edge, FormatItem>> oldEdgeFormats;

    /**
     * constructor
     *
	 */
    public FormatChangeCommand(final FormatItem formatItem, Collection<Node> nodes, NodeArray<NodeViewBase> node2view, Collection<Edge> edges, EdgeArray<EdgeViewBase> edge2view) {
        super(formatItem.getName());

        this.formatItem = formatItem.clone();
        this.node2view = node2view;
        this.edge2view = edge2view;

        if (nodes != null && node2view != null && nodes.size() > 0) {
            this.nodes = new ArrayList<>(nodes);
            oldNodeFormats = new ArrayList<>(nodes.size());
            for (Node v : nodes) {
                FormatItem oldItem = FormatItem.createFromSelection(Collections.singletonList(v), node2view, null, null);
                oldNodeFormats.add(new Pair<>(v, oldItem));
            }
        } else {
            this.nodes = null;
            oldNodeFormats = null;
        }
        if (edges != null && edge2view != null && edges.size() > 0) {
            this.edges = new ArrayList<>(edges);
            oldEdgeFormats = new ArrayList<>(edges.size());
            for (Edge e : edges) {
                FormatItem oldItem = FormatItem.createFromSelection(null, null, Collections.singletonList(e), edge2view);
                oldEdgeFormats.add(new Pair<>(e, oldItem));
            }
        } else {
            this.edges = null;
            oldEdgeFormats = null;
        }
    }

    @Override
    public void undo() {
        if (nodes != null) {
            for (Pair<Node, FormatItem> pair : oldNodeFormats) {
                final Node v = pair.getFirst();
                final FormatItem oldItem = pair.getSecond();
                oldItem.apply(Collections.singletonList(v), node2view, null, null);
            }
        }
        if (edges != null) {
            for (Pair<Edge, FormatItem> pair : oldEdgeFormats) {
                final Edge v = pair.getFirst();
                final FormatItem oldItem = pair.getSecond();
                oldItem.apply(null, null, Collections.singletonList(v), edge2view);
            }
        }
    }

    @Override
    public void redo() {
        formatItem.apply(nodes, node2view, edges, edge2view);
    }

    @Override
    public boolean isUndoable() {
        return (oldNodeFormats != null && oldNodeFormats.size() > 0) || (oldEdgeFormats != null && oldEdgeFormats.size() > 0);
    }

    @Override
    public boolean isRedoable() {
        return (oldNodeFormats != null && oldNodeFormats.size() > 0) || (oldEdgeFormats != null && oldEdgeFormats.size() > 0);
    }
}
