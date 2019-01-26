package splitstree5.core.algorithms.trees2splits;

import jloda.phylo.PhyloTree;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.SplitsUtilities;
import splitstree5.utils.TreesUtilities;

import java.util.BitSet;

/**
 * Obtains splits from a selected tree
 * <p>
 * Created on 20.01.2017, original version: 2005
 *
 * @author Daniel Huson
 */
public class TreeSelector extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private int optionWhich = 1; // which tree is selected?

    public TreeSelector() {
        setName("TreeSelector");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock trees, SplitsBlock splits) throws Exception {
        progress.setTasks("Tree selector", "Init.");

        if (optionWhich < 1)
            optionWhich = 1;
        if (optionWhich > trees.getNTrees())
            optionWhich = trees.getNTrees();

        if (trees.getNTrees() == 0)
            return;

        final PhyloTree tree = trees.getTrees().get(optionWhich - 1);

        if (tree.getNumberOfNodes() == 0)
            return;

        progress.setTasks("TreeSelector", "Extracting splits");
        progress.incrementProgress();

        final BitSet taxaInTree = TreesUtilities.computeSplits(null, tree, splits.getSplits());

        splits.setPartial(taxaInTree.cardinality() < taxaBlock.getNtax());
        splits.setCompatibility(Compatibility.compatible);
        splits.setCycle(SplitsUtilities.computeCycle(taxaBlock.size(), splits.getSplits()));

        SplitsUtilities.verifySplits(splits.getSplits(), taxaBlock);
        progress.close();
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return 1 <= optionWhich && optionWhich <= parent.getTrees().size() && !parent.isPartial();
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
