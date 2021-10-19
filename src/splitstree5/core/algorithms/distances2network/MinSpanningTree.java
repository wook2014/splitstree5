/*
 * MinSpanningTree.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.distances2network;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.progress.ProgressListener;
import jloda.util.Triplet;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToNetwork;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * computes a minimum spanning network
 * Daniel Huson, 2018
 */
public class MinSpanningTree extends Algorithm<DistancesBlock, NetworkBlock> implements IFromDistances, IToNetwork {
    @Override
    public String getCitation() {
        return "Kruskal 1956;  Kruskal, J. B. (1956). On the shortest spanning subtree of a graph and the traveling salesman problem. Proceedings of the American Mathematical Society. 7: 48–50";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock parent, NetworkBlock child) throws Exception {
        final PhyloGraph graph = child.getGraph();
        final int ntax = taxaBlock.getNtax();

        SortedSet<Triplet<Double, Integer, Integer>> distancesAndTaxa = new TreeSet<>();

        for (int a = 1; a <= taxaBlock.getNtax(); a++) {
            for (int b = a + 1; b <= taxaBlock.getNtax(); b++) {
                distancesAndTaxa.add(new Triplet<>(parent.get(a, b), a, b));
            }
        }

        final Node[] node = new Node[ntax + 1];
        final int[] component = new int[ntax + 1];

        for (int t = 1; t <= ntax; t++) {
            final Node v = graph.newNode(t);
            node[t] = v;
            component[t] = t;
            graph.addTaxon(v, t);
            graph.setLabel(v, taxaBlock.get(t).getDisplayLabelOrName());
        }

        progress.setMaximum(distancesAndTaxa.size());

        for (Triplet<Double, Integer, Integer> triplet : distancesAndTaxa) {
            progress.incrementProgress();

            final int a = triplet.getSecond();
            final int b = triplet.getThird();
            if (component[a] != component[b]) {
                final Edge e = graph.newEdge(node[a], node[b]);
                graph.setWeight(e, parent.get(a, b));
                final int oldComponent = component[a];
                final int newComponent = component[b];
                for (int k = 1; k <= ntax; k++) {
                    if (component[k] == oldComponent)
                        component[k] = newComponent;
                }
            }
        }
    }
}