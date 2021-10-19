/*
 * GreedyTree.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.splits2trees;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import jloda.util.BitSetUtils;
import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.utils.GreedyCompatible;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.utils.PhyloGraphUtils;
import splitstree5.utils.RerootingUtils;

import java.util.*;

/**
 * greedy tree
 * Daniel Huson, 12.2017
 */
public class GreedyTree extends Algorithm<SplitsBlock, TreesBlock> implements IFromSplits, IToTrees {

    @Override
    public String getCitation() {
        return "Huson et al 2012;D.H. Huson, R. Rupp and C. Scornavacca, Phylogenetic Networks, Cambridge, 2012.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, SplitsBlock splits, TreesBlock trees) throws CanceledException {

        progress.setTasks("Greedy Tree", "Extracting compatible splits...");
        final Map<BitSet, Double> cluster2Weight = new HashMap<>();
        for (ASplit split : splits.getSplits()) {
            cluster2Weight.put(split.getPartNotContaining(1), split.getWeight());
        }

        final BitSet[] clusters;
        {
            final ArrayList<ASplit> compatibleSplits = GreedyCompatible.apply(progress, splits.getSplits());
            clusters = new BitSet[compatibleSplits.size()];
            for (int i = 0; i < compatibleSplits.size(); i++) {
                clusters[i] = compatibleSplits.get(i).getPartNotContaining(1);
            }
        }
        Arrays.sort(clusters, (a, b) -> Integer.compare(b.cardinality(), a.cardinality()));

        final BitSet allTaxa = taxaBlock.getTaxaSet();

        final PhyloTree tree = new PhyloTree();
        tree.setRoot(tree.newNode());

        final NodeArray<BitSet> node2taxa = new NodeArray<>(tree);
        node2taxa.put(tree.getRoot(), allTaxa);

        // create tree:
        for (final BitSet cluster : clusters) {
            Node v = tree.getRoot();
            while (BitSetUtils.contains(node2taxa.get(v), cluster)) {
                boolean isBelow = false;
                for (Edge e : v.outEdges()) {
                    final Node w = e.getTarget();
                    if (BitSetUtils.contains(node2taxa.get(w), cluster)) {
                        v = w;
                        isBelow = true;
                        break;
                    }
                }
                if (!isBelow)
                    break;
            }
            final Node u = tree.newNode();
            final Edge f = tree.newEdge(v, u);
            tree.setWeight(f, cluster2Weight.get(cluster));
            node2taxa.put(u, cluster);
        }

        // add all labels:

        for (int t : BitSetUtils.members(allTaxa)) {
            Node v = tree.getRoot();
            while (node2taxa.get(v).get(t)) {
                boolean isBelow = false;
                for (Edge e : v.outEdges()) {
                    final Node w = e.getTarget();
                    if (node2taxa.get(w).get(t)) {
                        v = w;
                        isBelow = true;
                        break;
                    }
                }
                if (!isBelow)
                    break;
            }
            tree.addTaxon(v, t);
        }
        PhyloGraphUtils.addLabels(taxaBlock, tree);

        // todo: ask about internal node labels
        RerootingUtils.rerootByMidpoint(false, tree);

        trees.getTrees().add(tree);
        progress.close();
    }

}
