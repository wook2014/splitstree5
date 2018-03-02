/*
 *  Copyright (C) 2018 Daniel H. Huson
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

import javafx.beans.property.SimpleObjectProperty;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.trees2trees.ConsensusTree;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.utils.SplitsUtilities;
import splitstree5.utils.TreesUtilities;

import java.util.Arrays;
import java.util.List;

/**
 * implements consensus tree splits
 *
 * Daniel Huson, 2.2018
 */
public class ConsensusTreeSplits extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private final SimpleObjectProperty<ConsensusTree.Consensus> optionConsensus = new SimpleObjectProperty<>(ConsensusTree.Consensus.Majority);
    private final SimpleObjectProperty<ConsensusNetwork.EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(ConsensusNetwork.EdgeWeights.Mean);


    /**
     * compute the consensus splits
     *
     * @param progress
     * @param taxaBlock
     * @param parent
     * @param child
     */
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) throws Exception {
        final ConsensusTree consensusTree = new ConsensusTree();
        consensusTree.setOptionConsensus(getOptionConsensus());
        consensusTree.setOptionEdgeWeights(getOptionEdgeWeights());

        final TreesBlock trees = new TreesBlock();
        consensusTree.compute(progress, taxaBlock, parent, trees);

        TreesUtilities.computeSplits(null, trees.getTrees().get(0), child.getSplits());
        child.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), child.getSplits()));
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("consensus", "edgeWeights");
    }

    public ConsensusTree.Consensus getOptionConsensus() {
        return optionConsensus.get();
    }

    public SimpleObjectProperty<ConsensusTree.Consensus> optionConsensusProperty() {
        return optionConsensus;
    }

    public void setOptionConsensus(ConsensusTree.Consensus optionConsensus) {
        this.optionConsensus.set(optionConsensus);
    }

    public ConsensusNetwork.EdgeWeights getOptionEdgeWeights() {
        return optionEdgeWeights.get();
    }

    public SimpleObjectProperty<ConsensusNetwork.EdgeWeights> optionEdgeWeightsProperty() {
        return optionEdgeWeights;
    }

    public void setOptionEdgeWeights(ConsensusNetwork.EdgeWeights optionEdgeWeights) {
        this.optionEdgeWeights.set(optionEdgeWeights);
    }
}
