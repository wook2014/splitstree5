/*
 * ConsensusTreeSplits.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.core.algorithms.trees2splits;

import javafx.beans.property.SimpleObjectProperty;
import jloda.util.BitSetUtils;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.SplitsUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * implements consensus tree splits
 * <p>
 * Daniel Huson, 2.2018
 */
public class ConsensusTreeSplits extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    public enum Consensus {Strict, Majority, Greedy} // todo: add loose?

    private final SimpleObjectProperty<Consensus> optionConsensus = new SimpleObjectProperty<>(Consensus.Majority);
    private final SimpleObjectProperty<ConsensusNetwork.EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(ConsensusNetwork.EdgeWeights.TreeSizeWeightedMean);

    @Override
    public List<String> listOptions() {
        return Arrays.asList("Consensus", "EdgeWeights");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "Consensus":
                return "Select consensus method";
            case "EdgeWeights":
                return "Determine how to calculate edge weights in resulting network";
            default:
                return optionName;
        }
    }

    /**
     * compute the consensus splits
     *
     * @param progress
     * @param taxaBlock
     * @param parent
     * @param child
     */
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) throws Exception {
        final ConsensusNetwork consensusNetwork = new ConsensusNetwork();
        switch (getOptionConsensus()) {
            default:
            case Majority:
                consensusNetwork.setOptionThresholdPercent(50);
                break;
            case Strict:
                consensusNetwork.setOptionThresholdPercent(99.999999); // todo: implement without use of splits
                break;
            case Greedy:
                consensusNetwork.setOptionThresholdPercent(0);
        }
        final SplitsBlock consensusSplits = new SplitsBlock();
        consensusNetwork.setOptionEdgeWeights(getOptionEdgeWeights());
        consensusNetwork.compute(progress, taxaBlock, parent, consensusSplits);

        if (getOptionConsensus().equals(Consensus.Greedy)) {
            final ArrayList<ASplit> list = new ArrayList<>(consensusSplits.getSplits());
            list.sort((s1, s2) -> {
                if (s1.getWeight() > s2.getWeight())
                    return -1;
                else if (s1.getWeight() < s2.getWeight())
                    return 1;
                else
                    return BitSetUtils.compare(s1.getA(), s2.getA());
            });
            for (ASplit split : list) {
                if (Compatibility.isCompatible(split, child.getSplits()))
                    child.getSplits().add(split);
            }
        } else
            child.getSplits().setAll(consensusSplits.getSplits());

        child.setCompatibility(Compatibility.compatible);
        child.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), child.getSplits()));
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }

    public Consensus getOptionConsensus() {
        return optionConsensus.get();
    }

    public SimpleObjectProperty<Consensus> optionConsensusProperty() {
        return optionConsensus;
    }

    public void setOptionConsensus(Consensus optionConsensus) {
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
