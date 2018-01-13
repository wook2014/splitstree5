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

package splitstree5.core.misc;

import com.sun.istack.internal.Nullable;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.utils.TreesUtilities;

import java.util.BitSet;
import java.util.Collection;

/**
 * tree utilities
 * Daniel Huson, 2/6/17.
 */
public class TreeUtilities {
    /**
     * compute all the splits in a tree
     *
     * @param taxaBlock
     * @param tree
     * @param taxaInTree the taxa in the tree, if null, is computed
     * @param splits     the resulting splits are added here
     * @return bit set of taxa found in tree
     */
    public static BitSet computeSplits(final TaxaBlock taxaBlock, @Nullable BitSet taxaInTree, final PhyloTree tree, final Collection<ASplit> splits) {
        TreesUtilities.setNode2taxa(tree, taxaBlock);
        if (taxaInTree == null)
            taxaInTree = TreesUtilities.getTaxa(tree);
        Node root = tree.getRoot();
        if (root == null) {
            // choose an arbitrary labeled root
            for (Node v : tree.nodes()) {
                if (tree.getNode2Taxa(v).size() > 0 && v.getDegree() == 1) {
                    root = v;
                    break;
                }
            }
        }
        if (root != null) // empty tree?
            tree2splitsRec(root, null, tree, taxaInTree, splits);
        return taxaInTree;
    }

    /**
     * recursively extract all splits
     *
     * @param v
     * @param e
     * @param tree
     * @param taxaInTree
     * @param splits
     * @return
     */
    private static BitSet tree2splitsRec(final Node v, final Edge e, final PhyloTree tree, final BitSet taxaInTree, final Collection<ASplit> splits) {
        final BitSet vAndBelowTaxa = asBitSet(tree.getNode2Taxa(v));

        for (Edge f = v.getFirstAdjacentEdge(); f != null; f = v.getNextAdjacentEdge(f)) {
            if (f != e) {
                final Node w = tree.getOpposite(v, f);
                final BitSet wAndBelowTaxa = tree2splitsRec(w, f, tree, taxaInTree, splits);

                // take care at root of tree,
                // if root has degree 2, then root will give rise to only
                //  one split, with weight that equals
                // the sum of the two weights.. make sure we only produce
                // one split by using the edge that has lower id
                boolean ok = true;
                double weight = tree.getWeight(f);
                double confidence = tree.getConfidence(f);
                Node root = tree.getRoot();
                if (root != null && (f.getSource() == root || f.getTarget() == root) && root.getDegree() == 2 && tree.getNode2Taxa(v).size() == 0) {
                    // get the other  edge adjacent to root:
                    final Edge g;
                    if (root.getFirstAdjacentEdge() != f)
                        g = root.getFirstAdjacentEdge();
                    else
                        g = root.getLastAdjacentEdge();
                    if (f.getId() < g.getId()) {
                        weight = tree.getWeight(f) + tree.getWeight(g);
                        confidence = 0.5 * (tree.getConfidence(f) + tree.getConfidence(g));
                    } else
                        ok = false;
                }

                if (ok) {
                    final BitSet B = new BitSet();
                    B.or(taxaInTree);
                    B.andNot(wAndBelowTaxa);
                    final ASplit newSplit = new ASplit(wAndBelowTaxa, B, weight, confidence);
                    newSplit.setConfidence((float) confidence);
                    splits.add(newSplit);
                }
                vAndBelowTaxa.or(wAndBelowTaxa);
            }
        }
        return vAndBelowTaxa;
    }

    public static BitSet asBitSet(Collection<Integer> integers) {
        final BitSet bitSet = new BitSet();
        for (Integer i : integers) {
            bitSet.set(i);
        }
        return bitSet;
    }
}
