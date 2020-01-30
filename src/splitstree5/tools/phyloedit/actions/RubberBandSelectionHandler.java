/*
 *  RubberBandSelectionHandler.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.tools.phyloedit.actions;

import javafx.geometry.Bounds;
import jloda.fx.control.ItemSelectionModel;
import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloGraph;
import splitstree5.gui.utils.RubberBandSelection;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * creates a rubber-band selection handler
 */
public class RubberBandSelectionHandler {
    /**
     * handles a rubber band selection
     */
    public static RubberBandSelection.Handler create(PhyloGraph graph, ItemSelectionModel<Node> nodeSelectionModel, ItemSelectionModel<Edge> edgeSelectionModel, Function<Node, javafx.scene.Node> node2node, Function<Edge, javafx.scene.Node> edge2node) {
        final Set<Node> previouslySelectedNodes = new HashSet<>(nodeSelectionModel.getSelectedItems());
        final Set<Edge> previouslySelectedEdges = new HashSet<>(edgeSelectionModel.getSelectedItems());


        return (rectangle, extendSelection, ignored) -> {
            if (!extendSelection) {
                nodeSelectionModel.clearSelection();
                edgeSelectionModel.clearSelection();
            }

            final NodeSet nodesToDeselect = new NodeSet(graph);
            final NodeSet nodesToSelect = new NodeSet(graph);

            for (Node v : graph.nodes()) {
                final javafx.scene.Node nodeView = node2node.apply(v);
                {
                    final Bounds bounds = nodeView.localToScene(nodeView.getBoundsInLocal());

                    if (rectangle.contains(bounds.getMinX(), bounds.getMinY()) && rectangle.contains(bounds.getMaxX(), bounds.getMaxY())) {
                        if (previouslySelectedNodes.contains(v))
                            nodesToDeselect.add(v);
                        else
                            nodesToSelect.add(v);
                    }
                }
            }
            nodesToDeselect.forEach(nodeSelectionModel::clearSelection);
            nodesToSelect.forEach(nodeSelectionModel::select);


            final EdgeSet edgesToDeselect = new EdgeSet(graph);
            final EdgeSet edgesToSelect = new EdgeSet(graph);

            for (Edge e : graph.edges()) {
                if (nodesToSelect.contains(e.getSource()) && nodesToSelect.contains(e.getTarget())) {
                    if (previouslySelectedEdges.contains(e))
                        edgesToDeselect.add(e);
                    else
                        edgesToSelect.add(e);
                }
            }
            edgesToDeselect.forEach(edgeSelectionModel::clearSelection);
            edgesToSelect.forEach(edgeSelectionModel::select);
        };
    }
}
