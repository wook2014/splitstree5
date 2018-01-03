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

package splitstree5.core.algorithms.splits2trees;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.utils.GreedyCompatible;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;

import java.util.*;

/**
 * greedy tree
 * Daniel Huson, 12.2017
 */
public class GreedyTree extends Algorithm<SplitsBlock, TreesBlock> implements IFromSplits, IToTrees {

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, SplitsBlock splits, TreesBlock trees)
            throws InterruptedException, CanceledException {

        progressListener.setTasks("Greedy Tree", "Extracting compatible splits...");
        final Map<BitSet, Double> cluster2Weight = new HashMap<>();
        for (ASplit split : splits.getSplits()) {
            cluster2Weight.put(split.getPartNotContaining(1), split.getWeight());
        }
        final ArrayList<ASplit> compatible = GreedyCompatible.apply(progressListener, splits.getSplits());

        final BitSet[] clusters = new BitSet[compatible.size()];
        for (int i = 0; i < compatible.size(); i++) {
            clusters[i] = compatible.get(i).getPartNotContaining(1);

        }
        Arrays.sort(clusters, (a, b) -> Integer.compare(b.cardinality(), a.cardinality()));

        compatible.sort(ASplit.comparatorByDecreasingSize());

        final BitSet allTaxa = union(compatible.get(0).getA(), compatible.get(0).getB());

        final PhyloTree tree = new PhyloTree();
        tree.setRoot(tree.newNode());

        final NodeArray<BitSet> node2taxa = new NodeArray<>(tree);
        node2taxa.put(tree.getRoot(), allTaxa);

        for (final BitSet cluster : clusters) {
            Node v = tree.getRoot();
            while (contains(node2taxa.get(v), cluster)) {
                boolean isBelow = false;
                for (Edge e : v.outEdges()) {
                    final Node w = e.getTarget();
                    if (contains(node2taxa.get(w), cluster)) {
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

        tree.setNode2Taxa(tree.getRoot(), 1);
        for (int t = allTaxa.nextSetBit(2); t != -1; t = allTaxa.nextSetBit(t + 1)) {
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
            tree.setNode2Taxa(v, t);
            String label = tree.getLabel(v);
            if (label == null)
                tree.setLabel(v, "" + t);
            else if (!label.startsWith("<"))
                tree.setLabel(v, "<" + label + "," + t + ">");
            else
                tree.setLabel(v, label.substring(0, label.length() - 1) + "," + t + ">");
        }

        trees.getTrees().add(tree);
        progressListener.close();
    }

    /**
     * get the union of some bit sets
     *
     * @param sets
     * @return union
     */
    public static BitSet union(BitSet... sets) {
        final BitSet result = new BitSet();
        for (BitSet a : sets)
            result.or(a);
        return result;
    }

    /**
     * get the intersection of some bit sets
     *
     * @param sets
     * @return union
     */
    public static BitSet intersection(BitSet... sets) {
        final BitSet result = new BitSet();
        boolean first = true;
        for (BitSet b : sets)
            if (first) {
                result.or(b);
                first = false;
            } else
                result.and(b);
        return result;
    }

    /**
     * does set contain set b?
     *
     * @param a
     * @param b
     * @return true if b is contained in a
     */
    public static boolean contains(BitSet a, BitSet b) {
        return intersection(a, b).cardinality() == b.cardinality();
    }
}
