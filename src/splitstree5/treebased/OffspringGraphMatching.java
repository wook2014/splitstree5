/*
 *  OffspringGraphMatching.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.treebased;

import jloda.graph.*;
import jloda.phylo.PhyloTree;

/**
 * computes the offspring graph matching
 * Daniel Huson, 1.2020
 */
public class OffspringGraphMatching {
    /**
     * computes the matching
     *
     * @param tree
     * @return
     */
    public static EdgeSet compute(PhyloTree tree) {
        final Graph graph = new Graph();

        NodeArray<Node> tree2a = new NodeArray<>(tree);
        NodeArray<Node> tree2b = new NodeArray<>(tree);

        for (Node v : tree.nodes()) {
            tree2a.setValue(v, graph.newNode());
            tree2b.setValue(v, graph.newNode());
        }

        for (Edge e : tree.edges()) {
            graph.newEdge(tree2a.get(e.getSource()), tree2b.get(e.getTarget()));
        }

        final NodeSet oneSide = new NodeSet(graph);
        oneSide.addAll(tree2a.values());

        return BipartiteMatching.computeBipartiteMatching(graph, oneSide);
    }
}
