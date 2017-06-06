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
     * given a list of trees that has the "All Pairs" properties, returns the average
     * distance between any two taxa
     *
     * @param taxa
     * @param trees
     * @return distance between any two taxa
     */

    // todo class AverageDistances trees->distances
    // todo needs TreeSelector first
    public static DistancesBlock getAveragePairwiseDistances(TaxaBlock taxa, TreesBlock trees) throws Exception {
        DistancesBlock distances = new DistancesBlock();
        distances.setNtax(taxa.getNtax());

        int[][] count = new int[taxa.getNtax() + 1][taxa.getNtax() + 1];
        // number of trees that contain two given taxa

        TreeSelector selector = new TreeSelector();

        for (int which = 1; which <= trees.getNTrees(); which++) {
            TaxaBlock tmpTaxa = (TaxaBlock) taxa.clone();
            selector.setOptionWhich(which);

            ProgressListener pl = new ProgressPercentage();
            SplitsBlock splits = new SplitsBlock();
            selector.compute(pl, tmpTaxa, trees, splits);
            selector.compute(new ProgressPercentage(), tmpTaxa, trees, splits); // modifies tmpTaxa, too!
            for (int a = 1; a <= tmpTaxa.getNtax(); a++)
                for (int b = 1; b <= tmpTaxa.getNtax(); b++) {
                    int i = taxa.indexOf(tmpTaxa.getLabel(a)); // translate numbering
                    int j = taxa.indexOf(tmpTaxa.getLabel(b));
                    count[i][j]++;
                    count[j][i]++;
                }

            for (int s = 0; s < splits.getNsplits(); s++) {
                BitSet A = splits.getSplits().get(s).getA();
                        //splits.get(s).getBits();
                BitSet B = splits.getSplits().get(s).getB();
                        //splits.get(s).getComplement(tmpTaxa.getNtax()).getBits();
                for (int a = A.nextSetBit(1); a > 0; a = A.nextSetBit(a + 1)) {
                    for (int b = B.nextSetBit(1); b > 0; b = B.nextSetBit(b + 1)) {
                        int i = taxa.indexOf(tmpTaxa.getLabel(a)); // translate numbering
                        int j = taxa.indexOf(tmpTaxa.getLabel(b));
                        distances.set(i, j, distances.get(i, j) +  splits.getSplits().get(s).getWeight());//splits.getWeight(s));
                        distances.set(j, i, distances.get(i, j));
                    }
                }
            }
        }
        // divide by count
        for (int i = 1; i <= taxa.getNtax(); i++) {
            for (int j = 1; j <= taxa.getNtax(); j++) {
                if (count[i][j] > 0)
                    distances.set(i, j, distances.get(i, j) / count[i][j]);
                else
                    distances.set(i, j, 100); // shouldn't ever happen!
            }
        }
        return distances;
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
            /*if (trees.getTaxaForLabel(taxa, tree.getLabel(v)).cardinality() > 0
                    && tree.getDegree(v) == 1) {
                root = v;
                break;
            }*/ // todo : getTaxaForLabel
        }
        if (root == null) // empty tree?
            return splits;

        tree2splitsRec(root, null, trees, which, taxa, splits, skipNegativeSplitIds);


        try {
            SplitsUtilities.verifySplits(splits, taxa);
        } catch (SplitsException ex) {
            splits = null;
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

        Iterator edges = tree.getAdjacentEdges(v);
        while (edges.hasNext()) {
            Edge f = (Edge) edges.next();

            if (f != e) {
                BitSet f_taxa = tree2splitsRec(tree.getOpposite(v, f), f, trees, which, taxa, splits, skipNegativeSplitIds);
                /*if (tree.getConfidence(f) != 1)
                    splits.getFormat().setConfidences(true);

                if (!skipNegativeSplitIds || tree.getSplit(f) >= 0)
                    splits.getSplitsSet().add(f_taxa, (float) tree.getWeight(f), (float) tree.getConfidence(f));*/
                for (int t = 1; t <= f_taxa.length(); t++) {
                    if (f_taxa.get(t))
                        e_taxa.set(t);
                }
            }
        }
        return e_taxa;
    }
}
