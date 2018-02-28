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

package splitstree5.utils;

import com.sun.istack.internal.Nullable;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NotOwnerException;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;

import java.util.BitSet;
import java.util.Collection;

/**
 * some computations on trees
 *
 * @author huson Date: 29-Feb-2004
 * Daria Evseeva,23.01.2017.
 */
public class TreesUtilities {
    /**
     * gets all taxa in tree, if node to taxa mapping has been set
     *
     * @param tree
     * @return all taxa in tree
     */
    public static BitSet getTaxa(PhyloTree tree) {
        BitSet taxa = new BitSet();
        for (Node v : tree.nodes()) {
            if (tree.getTaxa(v) != null) {
                for (Integer t : tree.getTaxa(v)) {
                    taxa.set(t);
                }
            }
        }
        return taxa;
    }

    /**
     * Converts a PhyloTree in a trees block to splits, given taxa.
     * <p/>
     * This code was extracted from TreeSelector.java
     *
     * @param trees
     * @param which
     * @param taxa
     * @return splits
     */
    public static SplitsBlock convertTreeToSplits(TreesBlock trees, int which, TaxaBlock taxa) {
        return convertTreeToSplits(trees, which, taxa, false);
    }

    /**
     * Converts a PhyloTree in a trees block to splits, given taxa.
     * <p/>
     * This code was extracted from TreeSelector.java
     *
     * @param trees
     * @param which
     * @param taxa
     * @param skipNegativeSplitIds don't convert edges with negative split ids
     * @return splits
     */
    public static SplitsBlock convertTreeToSplits(TreesBlock trees, int which, TaxaBlock taxa, boolean skipNegativeSplitIds) {

        SplitsBlock splits = new SplitsBlock();
        PhyloTree tree = trees.getTrees().get(which);

        // choose an arbitrary labeled root
        Node root = null;
        for (Node v : tree.nodes()) {
            BitSet taxaSet = new BitSet();
            taxaSet.set(v.getId());
            //if (tree.getNode2Taxa(v).cardinality() > 0
            if (taxaSet.cardinality() > 0 && v.getDegree() == 1) {
                root = v;
                break; //todo
            }
        }
        System.out.println(root);

        if (root == null) // empty tree?
            return splits;

        tree2splitsRec(root, null, trees, which, taxa, splits, skipNegativeSplitIds);


        try {
            SplitsUtilities.verifySplits(splits.getSplits(), taxa);
        } catch (SplitsException ex) {
            //splits = null;
        }

        // false!!!
        System.out.println("TU splits");
        for (int i = 0; i < splits.getSplits().size(); i++) {
            System.out.println(splits.getSplits().get(i).getA());
        }

        return splits;
    }

    /**
     * recursively extract split froms tree
     *
     * @param v
     * @param e
     * @param trees
     * @param which
     * @param taxa
     * @param splits
     * @return
     * @throws NotOwnerException
     */
    private static BitSet tree2splitsRec(Node v, Edge e, TreesBlock trees, int which,
                                         TaxaBlock taxa, SplitsBlock splits, boolean skipNegativeSplitIds) throws NotOwnerException {
        PhyloTree tree = trees.getTrees().get(which);
        //todo
        //BitSet e_taxa = trees.getTaxaForLabel(taxa, tree.getLabel(v));
        BitSet e_taxa = new BitSet();
        //e_taxa.set(tree.getId(v));
        if (taxa.indexOf(tree.getLabel(v)) != -1) //e_taxa.set(0);
            e_taxa.set(taxa.indexOf(tree.getLabel(v)));

        System.out.println("e taxa   " + e_taxa); // right!!!

        for (Edge f : v.adjacentEdges()) {
            if (f != e) {
                BitSet f_taxa = tree2splitsRec(tree.getOpposite(v, f), f, trees, which, taxa, splits, skipNegativeSplitIds);
                /*if (tree.getConfidence(f) != 1)
                    splits.getFormat().setConfidences(true);*/

                if (!skipNegativeSplitIds) {
                    //splits.getSplitsSet().add(f_taxa, (float) tree.getWeight(f), (float) tree.getConfidence(f));
                    ASplit split = new ASplit(f_taxa, taxa.getNtax(), tree.getWeight(f), tree.getConfidence(f));
                    splits.getSplits().add(split);
                }
                for (int t = 0; t < f_taxa.length(); t++) {
                    if (f_taxa.get(t))
                        e_taxa.set(t);
                }
            }
        }
        return e_taxa;
    }

    /**
     * determines whether every pair of taxa occur together in some tree
     *
     * @param taxa
     * @param trees
     * @return returns true, if every pair of taxa occur together in some  tree
     */
    static public boolean hasAllPairs(TaxaBlock taxa, TreesBlock trees) {
        int numPairs = (taxa.getNtax() * (taxa.getNtax() - 1)) / 2;

        BitSet seen = new BitSet();

        for (int which = 1; which <= trees.getNTrees(); which++) {
            BitSet support = //trees.getSupport(taxa, which).getBits();
                    //---
                    new BitSet();
            PhyloTree tree = trees.getTrees().get(which);
            for (String v : tree.nodeLabels()) {
                support.set(taxa.indexOf(v)); //todo test???
            }
            //---
            for (int i = support.nextSetBit(1); i > 0; i = support.nextSetBit(i + 1)) {
                for (int j = support.nextSetBit(i + 1); j > 0; j = support.nextSetBit(j + 1)) {
                    seen.set(i + taxa.getNtax() * j, true);
                    if (seen.cardinality() == numPairs)
                        return true; // seen all possible pairs
                }
            }
        }
        return false;
    }


    /**
     * are there any labeled internal nodes and are all such labels numbers?
     *
     * @param tree
     * @return true, if some internal nodes labeled by numbers
     */
    public static boolean hasNumbersOnInternalNodes(PhyloTree tree) {
        boolean hasNumbersOnInternalNodes = false;
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getOutDegree() != 0 && v.getInDegree() != 0) {
                String label = tree.getLabel(v);
                if (label != null) {
                    if (Basic.isDouble(label))
                        hasNumbersOnInternalNodes = true;
                    else
                        return false;
                }
            }
        }
        return hasNumbersOnInternalNodes;
    }

    /**
     * reinterpret an numerical label of an internal node as the confidence associated with the incoming edge
     *
     * @param tree
     */
    public static void changeNumbersOnInternalNodesToEdgeConfidencies(PhyloTree tree) {
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            if (v.getOutDegree() != 0 && v.getInDegree() == 1) {
                String label = tree.getLabel(v);
                if (label != null) {
                    if (Basic.isDouble(label)) {
                        tree.setConfidence(v.getFirstInEdge(), Basic.parseDouble(label));
                        tree.setLabel(v, null);
                    }
                }
            }
        }
    }

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
        if (taxaInTree == null)
            taxaInTree = getTaxa(tree);

        Node root = null;
        if (tree.getRoot() != null)
            root = tree.getRoot();
        else {
            // choose an arbitrary leaf
            for (Node v : tree.nodes()) {
                if (tree.hasTaxa(v) && v.getDegree() == 1) {
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
        final BitSet vAndBelowTaxa = asBitSet(tree.getTaxa(v));

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
                if (root != null && (f.getSource() == root || f.getTarget() == root) && root.getDegree() == 2 && !tree.hasTaxa(v)) {
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

    public static BitSet asBitSet(Iterable<Integer> integers) {
        final BitSet bitSet = new BitSet();
        for (Integer i : integers) {
            bitSet.set(i);
        }
        return bitSet;
    }
}
