/*
 *  Copyright (C) 2016 Daniel H. Huson
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
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.Arrays;
import java.util.List;

/**
 * implements consensus splits
 * corresponds to the "ConsensusTree" algorithm in SplitsTree4
 *
 * Created on 12/11/16.
 * @author huson
 */
public class ConsensusSplits extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    public enum Consensus {Strict, Majority, Loose, Network, All}

    private final SimpleObjectProperty<Consensus> consensusOption = new SimpleObjectProperty<>(Consensus.All);

    /**
     * compute the consensus splits
     *
     * @param progressListener
     * @param taxaBlock
     * @param treesBlock
     * @param splitsBlock
     */
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock) throws CanceledException {
        System.err.println("Not implemented");
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) {
        return parent.size() > 0 && !parent.isPartial();
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("consensus", "threshold", "edgeWeights");
    }

    public Consensus getConsensusOption() {
        return consensusOption.get();
    }

    public SimpleObjectProperty<Consensus> consensusOptionProperty() {
        return consensusOption;
    }

    public void setConsensusOption(Consensus consensusOption) {
        this.consensusOption.set(consensusOption);
    }
}
