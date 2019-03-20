package splitstree5.core.algorithms.trees2splits;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.trees2splits.utils.ConfidenceNetwork;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.utils.SplitMatrix;

import java.util.Collections;
import java.util.List;

/**
 * Implements confidence networks using Beran's algorithm
 * <p>
 * Created on 07.06.2017
 *
 * @author Daniel Huson and David Bryant
 */

public class BalancedConfidenceNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {

    private final DoubleProperty optionLevel = new SimpleDoubleProperty(.95);

    @Override
    public String getCitation() {
        return "Huson and Bryant 2006; " +
                "Daniel H. Huson and David Bryant. Application of Phylogenetic Networks in Evolutionary Studies. " +
                "Mol. Biol. Evol. 23(2):254–267. 2006";
    }

    @Override
    public List<String> listOptions() {
        return Collections.singletonList("Level");
    }

    @Override
    public String getToolTip(String optionName) {
        if ("Level".equals(optionName)) {
            return "Set the level";
        }
        return optionName;
    }


    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock) throws Exception {

        progress.setMaximum(100);

        final SplitMatrix M = new SplitMatrix(treesBlock, taxaBlock);
        M.print();

        splitsBlock.copy(ConfidenceNetwork.getConfidenceNetwork(M, getOptionLevel(), taxaBlock.getNtax(), progress));
    }

    public double getOptionLevel() {
        return optionLevel.get();
    }

    public DoubleProperty optionLevelProperty() {
        return optionLevel;
    }

    public void setOptionLevel(double optionLevel) {
        this.optionLevel.set(optionLevel);
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }
}
