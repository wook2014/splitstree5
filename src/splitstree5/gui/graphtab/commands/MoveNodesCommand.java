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

package splitstree5.gui.graphtab.commands;

import javafx.geometry.Point2D;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import splitstree5.gui.graphtab.base.EdgeView2D;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.undo.UndoableRedoableCommand;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * move nodes
 * Daniel Huson, 2.2018
 */
public class MoveNodesCommand extends UndoableRedoableCommand {
    private final NodeArray<NodeViewBase> node2view;
    private final EdgeArray<EdgeViewBase> edge2view;
    private final Collection<Node> nodes;
    private final double deltaX;
    private final double deltaY;

    /**
     * constructor
     *
     * @param node2view
     * @param edge2view
     * @param nodes
     * @param deltaX
     * @param deltaY
     */
    public MoveNodesCommand(NodeArray<NodeViewBase> node2view, EdgeArray<EdgeViewBase> edge2view, Collection<Node> nodes, double deltaX, double deltaY) {
        super("Move nodes");
        this.node2view = node2view;
        this.edge2view = edge2view;
        this.nodes = new HashSet<>(nodes);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    @Override
    public void undo() {
        final Set<Edge> edges2Update = new HashSet<>();
        for (Node v : nodes) {
            ((NodeView2D) node2view.get(v)).translateCoordinates(-deltaX, -deltaY);
            for (Edge e : v.adjacentEdges()) {
                edges2Update.add(e);
            }
        }
        for (Edge e : edges2Update) {
            final Point2D src = ((NodeView2D) node2view.get(e.getSource())).getLocation();
            final Point2D tar = ((NodeView2D) node2view.get(e.getTarget())).getLocation();
            ((EdgeView2D) edge2view.get(e)).setCoordinates(src, tar);
        }
    }

    @Override
    public void redo() {
        final Set<Edge> edges2Update = new HashSet<>();
        for (Node v : nodes) {
            ((NodeView2D) node2view.get(v)).translateCoordinates(deltaX, deltaY);
            for (Edge e : v.adjacentEdges()) {
                edges2Update.add(e);
            }
        }
        for (Edge e : edges2Update) {
            final Point2D src = ((NodeView2D) node2view.get(e.getSource())).getLocation();
            final Point2D tar = ((NodeView2D) node2view.get(e.getTarget())).getLocation();
            ((EdgeView2D) edge2view.get(e)).setCoordinates(src, tar);
        }
    }
}
