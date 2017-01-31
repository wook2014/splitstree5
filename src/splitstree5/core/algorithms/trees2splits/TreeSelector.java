package splitstree5.core.algorithms.trees2splits;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.nexus.SplitsUtilities;
import splitstree5.utils.nexus.TreesUtilities;

import java.util.BitSet;
import java.util.Collection;

/**
 * Obtains splits from a selected tree
 * Daniel Huson, 2005
 */
public class TreeSelector extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToTrees {
    private int optionWhich = 1; // which tree is selected?

    public TreeSelector() {
        setName("TreeSelector");
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock trees, SplitsBlock splits) throws Exception {
        progressListener.setDebug(true);
        progressListener.setTasks("Tree selector", "Init.");

        if (optionWhich < 1)
            optionWhich = 1;
        if (optionWhich > trees.getNTrees())
            optionWhich = trees.getNTrees();

        if (trees.getNTrees() == 0)
            return;

        final PhyloTree tree = trees.getTrees().get(optionWhich - 1);

        if (tree.getNumberOfNodes() == 0)
            return;

        TreesUtilities.setNode2taxa(tree, taxaBlock);
        Node root = tree.getRoot();
        if (root == null) {
            // choose an arbitrary labeled root
            for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
                if (tree.getNode2Taxa(v).size() > 0 && tree.getDegree(v) == 1) {
                    root = v;
                    break;
                }
            }
        }
        if (root == null) // empty tree?
            return;

        progressListener.setTasks("TreeSelector", "Extracting splits");
        progressListener.incrementProgress();

        final BitSet taxaInTree = TreesUtilities.getTaxa(tree);
        tree2splitsRec(root, null, tree, taxaInTree, splits);

        // normalize cycle:
        if (taxaBlock.getNtax() > 0) {
            Node vFirstTaxon;
            for (vFirstTaxon = tree.getFirstNode(); vFirstTaxon != null; vFirstTaxon = vFirstTaxon.getNext()) {
                String label = tree.getLabel(vFirstTaxon);
                if (label != null && label.equals(taxaBlock.getLabel(1)))
                    break;
            }
            if (vFirstTaxon != null)
                splits.setCycle(tree.getCycle(vFirstTaxon));
        }

        splits.setPartial(taxaInTree.cardinality() < taxaBlock.getNtax());
        splits.setCompatibility(Compatibility.compatible);

        SplitsUtilities.verifySplits(splits, taxaBlock);
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
    private BitSet tree2splitsRec(final Node v, final Edge e, final PhyloTree tree, BitSet taxaInTree, final SplitsBlock splits) {
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
                    splits.getSplits().add(newSplit);
                }
                vAndBelowTaxa.or(wAndBelowTaxa);
            }
        }

        return vAndBelowTaxa;
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) {
        return 1 <= optionWhich && optionWhich <= parent.getTrees().size();
    }

    private static BitSet asBitSet(Collection<Integer> integers) {
        final BitSet bitSet = new BitSet();
        for (Integer i : integers) {
            bitSet.set(i);
        }
        return bitSet;
    }

    public int getOptionWhich() {
        return optionWhich;
    }

    public void setOptionWhich(int which) {
        this.optionWhich = which;
    }

    @Override
    public String getShortDescription() {
        return "which=" + getOptionWhich();
    }
}
