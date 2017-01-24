package splitstree5.core.algorithms.trees2splits;


import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.NotOwnerException;
import jloda.util.ProgressListener;
import splitstree4.core.TaxaSet;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.nexus.TreesUtilities;
import splitstree5.utils.nexus.SplitsUtilities;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;

import java.util.Iterator;

public class TreeSelector extends Algorithm<TreesBlock, SplitsBlock> {

    //todo: classes, imported form splitstree4 : TaxaSet, TreesUtilities

    private int which = 1; // which tree is to be converted?
    private TreesBlock trees;

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock trees, SplitsBlock splits) throws Exception {
        //todo check if applicable
        this.trees = trees;
        progressListener.setDebug(true);
        progressListener.setTasks("Tree selector", "Init.");
        //progressListener.setMaximum(?);
        apply(progressListener, taxaBlock, splits);
        progressListener.close();
    }

    public void apply(ProgressListener progressListener, TaxaBlock taxa, SplitsBlock splits) throws Exception{
        if (which < 0)
            which = 1;
        if (which > trees.getNTrees())
            which = trees.getNTrees();
        setOptionWhich(which);

        if (trees.getNTrees() == 0)
            return;
        //return new Splits(taxa.getNtax());

        PhyloTree tree = trees.getTrees().get(which);

        //todo: for partial trees
        //taxa.hideAdditionalTaxa(null);
        //TaxaSet taxaInTree = trees.getTaxaInTree(taxa, which);
        //if (trees.getPartial() || !taxaInTree.equals(taxa.getTaxaSet())) // need to adjust the taxa set!
        //{
          //  taxa.hideAdditionalTaxa(taxaInTree.getComplement(taxa.getNtax()));
        //}

        if (tree.getNumberOfNodes() == 0)
            return;


        Node root = tree.getRoot();
        if (root == null) {
            // choose an arbitrary labeled root
            for (Node v = tree.getFirstNode(); v != null; v = tree.getNextNode(v)) {
                if (trees.getTaxaForLabel(taxa, tree.getLabel(v)).cardinality() > 0
                        && tree.getDegree(v) == 1) {
                    root = v;
                    break;
                }
            }
        }
        if (root == null) // empty tree?
            return;

        TreesUtilities.verifyTree(trees.getTrees().get(which), trees.getTranslate(), taxa, true);
        TreesUtilities.setNode2taxa(trees.getTrees().get(which), taxa);

        //if (doc != null)
            //doc.notifyTasks("TreeSelector", "Extracting splits");
        progressListener.setTasks("TreeSelector", "Extracting splits");
        progressListener.incrementProgress();

        //todo
        //tree2splitsRec(root, null, trees, taxa, splits);

        //splits.getProperties().setCompatibility(Splits.Properties.COMPATIBLE);
        splits.setCompatibility(Compatibility.compatible);

        //if (doc != null)
            //doc.notifyTasks("TreeSelector", "Computing cycle");
        progressListener.setTasks("TreeSelector", "Computing cycle");
        progressListener.incrementProgress();

        //if (doc != null && doc.isValidByName(Assumptions.NAME)
          //      && doc.getAssumptions().getLayoutStrategy() == Assumptions.RECOMPUTE) {
            if (taxa.getNtax() > 0) {
                Node vFirstTaxon;
                for (vFirstTaxon = trees.getTrees().get(which).getFirstNode(); vFirstTaxon != null; vFirstTaxon = vFirstTaxon.getNext()) {
                    String label = trees.getTrees().get(which).getLabel(vFirstTaxon);
                    if (label != null && label.equals(taxa.getLabel(1)))
                        break;
                }
                if (vFirstTaxon != null)
                    splits.setCycle(trees.getTrees().get(which).getCycle(vFirstTaxon));
            }
        //} else {
            // if in stabilize, use NNet later to compute cycle
        //}
        SplitsUtilities.verifySplits(splits, taxa);
    }

    // recursively compute the splits:

    private TaxaSet tree2splitsRec(Node v, Edge e, TreesBlock trees,
                                   TaxaBlock taxa, SplitsBlock splits) throws NotOwnerException {
        PhyloTree tree = trees.getTrees().get(which);
        TaxaSet e_taxa = trees.getTaxaForLabel(taxa, tree.getLabel(v));

        Iterator edges = tree.getAdjacentEdges(v);
        while (edges.hasNext()) {
            Edge f = (Edge) edges.next();

            if (f != e) {
                TaxaSet f_taxa = tree2splitsRec(tree.getOpposite(v, f), f, trees,
                        taxa, splits);

                // take care at root of tree,
                // if root has degree 2, then root will give rise to only
                //  one split, with weight that equals
                // the sum of the two weights.. make sure we only produce
                // one split by using the edge that has lower id
                boolean ok = true;
                double weight = tree.getWeight(f);
                double confidence = tree.getConfidence(f);
                Node root = tree.getRoot();
                if (root != null && (f.getSource() == root || f.getTarget() == root) &&
                        root.getDegree() == 2 && trees.getTaxaForLabel(taxa, tree.getLabel(root)).cardinality() == 0) {
                    // get the other  edge adjacent to root:
                    Edge g;
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
                    //todo
                    //if (confidence != 1)
                        //splits.getFormat().setConfidences(true);

                    //splits.getSplitsSet().add(f_taxa, (float) weight, (float) confidence);
                    ASplit newSplit = new ASplit(f_taxa.getBits(), taxa.getNtax (), (float) weight);
                    newSplit.setConfidence((float) confidence);
                    splits.getSplits().add(newSplit);
                }
                e_taxa.set(f_taxa);
            }
        }
        return e_taxa;
    }

    public int getOptionWhich() {
        return which;
    }
    public void setOptionWhich(int which) {
        this.which = which;
    }
}
