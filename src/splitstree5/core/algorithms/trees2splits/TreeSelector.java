/*
 *  TreeSelector.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.core.algorithms.trees2splits;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.util.Collections;
import java.util.List;

/**
 * Obtains splits from a selected tree
 * <p>
 * Created on 20.01.2017, original version: 2005
 *
 * @author Daniel Huson
 */
public class TreeSelector extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private final IntegerProperty optionWhich = new SimpleIntegerProperty(1); // which tree is selected?

    @Override
    public List<String> listOptions() {
        return Collections.singletonList("Which");
    }

    @Override
    public String getToolTip(String optionName) {
        if ("Which".equals(optionName)) {
            return "Which tree to use";
        }
        return optionName;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock trees, SplitsBlock splits) throws Exception {
        progress.setTasks("Tree selector", "Init.");

        if (getOptionWhich() < 1)
            setOptionWhich(1);
        if (getOptionWhich() > trees.getNTrees())
            setOptionWhich(trees.getNTrees());

        if (trees.getNTrees() == 0)
            return;

        final PhyloTree tree = trees.getTrees().get(getOptionWhich() - 1);

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
        return 1 <= getOptionWhich() && getOptionWhich() <= parent.getTrees().size() && !parent.isPartial();
    }

    public int getOptionWhich() {
        return optionWhich.get();
    }

    public IntegerProperty optionWhichProperty() {
        return optionWhich;
    }

    public void setOptionWhich(int optionWhich) {
        this.optionWhich.set(optionWhich);
    }

    @Override
    public String getShortDescription() {
        return "which=" + getOptionWhich();
    }
}
