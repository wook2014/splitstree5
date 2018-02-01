package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.trees2splits.utils.ConfidenceNetwork;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.utils.SplitMatrix;

/**
 * Implements confidence networks using Beran's algorithm
 * <p>
 * Created on 07.06.2017
 *
 * @author Daniel Huson and David Bryant
 */

public class BalancedConfidenceNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {

    private double level = .95;
    public final static String DESCRIPTION = "Computes a confidence network using Beran's algorithm. cf Huson and Bryant (2006)";

    @Override
    public String getCitation() {
        return "Huson and Bryant 2006; " +
                "Daniel H. Huson and David Bryant. Application of Phylogenetic Networks in Evolutionary Studies. " +
                "Mol. Biol. Evol. 23(2):254–267. 2006";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock) throws Exception {

        progress.setMaximum(100);

        final SplitMatrix M = new SplitMatrix(treesBlock, taxaBlock);
        M.print();

        splitsBlock.copy(ConfidenceNetwork.getConfidenceNetwork(M, getOptionLevel(), taxaBlock.getNtax(), progress));
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

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) {
        return !parent.isPartial();
    }
}
