package splitstree5.core.algorithms.trees2splits;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.TreeUtilities;
import splitstree5.utils.nexus.SplitsUtilities;

import java.util.BitSet;

/**
 * Obtains splits from a selected tree
 *
 * Created on 20.01.2017, original version: 2005
 * @author Daniel Huson
 */
public class TreeSelector extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private int optionWhich = 1; // which tree is selected?

    public TreeSelector() {
        setName("TreeSelector");
    }

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock trees, SplitsBlock splits) throws Exception {
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

        progressListener.setTasks("TreeSelector", "Extracting splits");
        progressListener.incrementProgress();

        final BitSet taxaInTree = TreeUtilities.computeSplits(taxaBlock, null, tree, splits.getSplits());

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
        progressListener.close();
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) {
        return 1 <= optionWhich && optionWhich <= parent.getTrees().size();
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
