package splitstree5.core.algorithms.trees2splits;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.trees2splits.utils.ConfidenceNetwork;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.utils.SplitMatrix;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

public class BalancedConfidenceNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {

    private double level = .95;
    public final static String DESCRIPTION = "Computes a confidence network using Beran's algorithm. cf Huson and Bryant (2006)";

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock)
            throws Exception {

        progressListener.setMaximum(100);

        SplitMatrix M = new SplitMatrix(treesBlock, taxaBlock);
        M.print();

        splitsBlock.copy(ConfidenceNetwork.getConfidenceNetwork(M, getOptionLevel(), taxaBlock.getNtax(), progressListener));
    }

    public String getDescription() {
        return BalancedConfidenceNetwork.DESCRIPTION;
    }
    public double getOptionLevel() {
        return level;
    }
    public void setOptionLevel(double level) {
        this.level = level;
    }
}
