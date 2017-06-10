package splitstree5.utils.nexus;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.NotOwnerException;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import splitstree5.core.algorithms.trees2splits.TreeSelector;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;

/**
 * some computations on trees
 *
 * @author huson Date: 29-Feb-2004
 *         Created by Daria on 23.01.2017.
 */
public class TreesUtilities {

    // no translate, ioexeption
    /**
     * verify that tree, translation and taxa fit together
     *
     * @param tree
     * @param taxa
     * @param allowAddTaxa if taxa-block is missing taxa in tree, do we allow them to be added to taxa block?
     * @throws IOException
     */
    public static void verifyTree(PhyloTree tree, TaxaBlock taxa, boolean allowAddTaxa) throws IOException {
        final BitSet seen = new BitSet();
        Iterator it = tree.nodeIterator();
        while (it.hasNext()) {
            try {
                String taxonLabel = tree.getLabel((Node) it.next());

                //if (taxa.indexOf(taxonLabel) == -1) {
                if (taxa.getLabels().indexOf(taxonLabel) == -1) {
                    if (allowAddTaxa) {
                        //taxa.add(taxonLabel);
                        Taxon t = new Taxon(taxonLabel);
                        taxa.add(t);
                    } else {
                        //Taxa.show("current taxon block", taxa);
                        throw new IOException("Taxon-label not contained in taxa-block: " + taxonLabel);
                    }
                }
                //seen.set(taxa.indexOf(taxonLabel));
                seen.set(taxa.getLabels().indexOf(taxonLabel));
            } catch (NotOwnerException ex) {
                Basic.caught(ex);
            }
        }
        if (seen.cardinality() != taxa.getNtax())
            throw new IOException("Taxa " + taxa + " and seen <" + seen + "> differ");
    }

    /**
     * sets the node2taxa and taxon2node maps for a tree
     *
     * @param tree
     * @param taxa
     */
    public static void setNode2taxa(PhyloTree tree, TaxaBlock taxa) {
        tree.clearTaxon2Node();
        for (Node v = tree.getFirstNode(); v != null; v = v.getNext()) {
            tree.clearNode2Taxa(v);
            String label = tree.getLabel(v);
            if (label != null) {
                //int id = taxa.indexOf(label);
                int id = taxa.indexOf(label);
                if (id > 0) {
                    tree.setNode2Taxa(v, id);
                    tree.setTaxon2Node(id, v);
                }
            }
        }
    }

    /**
     * gets all taxa in tree, if node to taxa mapping has been set
     *
     * @param tree
     * @return all taxa in tree
     */
    public static BitSet getTaxa(PhyloTree tree) {
        BitSet taxa = new BitSet();
        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            if (tree.getNode2Taxa(v) != null) {
                for (Integer t : tree.getNode2Taxa(v)) {
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
        for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
            BitSet taxaSet = new BitSet();
            taxaSet.set(tree.getId(v));
            //if (tree.getNode2Taxa(v).cardinality() > 0
            if (taxaSet.cardinality() > 0
                    && tree.getDegree(v) == 1) {
                root = v;
                break; //todo
            }
        }
        System.out.println(root);

        if (root == null) // empty tree?
            return splits;

        tree2splitsRec(root, null, trees, which, taxa, splits, skipNegativeSplitIds);


        try {
            SplitsUtilities.verifySplits(splits, taxa);
        } catch (SplitsException ex) {
            //splits = null;
        }

        // false!!!
        System.out.println("TU splits");
        for(int i = 0; i<splits.getSplits().size(); i++){
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

        System.out.println("e taxa   "+e_taxa); // right!!!

        Iterator edges = tree.getAdjacentEdges(v);
        while (edges.hasNext()) {
            Edge f = (Edge) edges.next();

            if (f != e) {
                BitSet f_taxa = tree2splitsRec(tree.getOpposite(v, f), f, trees, which, taxa, splits, skipNegativeSplitIds);
                /*if (tree.getConfidence(f) != 1)
                    splits.getFormat().setConfidences(true);*/

                if (!skipNegativeSplitIds || tree.getSplit(f) >= 0) {
                    //splits.getSplitsSet().add(f_taxa, (float) tree.getWeight(f), (float) tree.getConfidence(f));
                    ASplit split = new ASplit(f_taxa, taxa.getNtax(), (float) tree.getWeight(f), (float) tree.getConfidence(f));
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
            for(String v : tree.getNodeLabels()){
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
}
