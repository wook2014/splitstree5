package splitstree5.core.algorithms.trees2splits;

import javafx.beans.property.SimpleObjectProperty;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.algorithms.splits2trees.GreedyTree;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.Arrays;
import java.util.List;

/**
 * computes a consensus tree from a list of trees
 * Daniel Huson, 2.2018
 */
public class ConsensusTree extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees {
    public enum Consensus {Strict, Majority, Greedy}

    private final SimpleObjectProperty<Consensus> optionConsensus = new SimpleObjectProperty<>(Consensus.Majority);
    private final SimpleObjectProperty<ConsensusNetwork.EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(ConsensusNetwork.EdgeWeights.Mean);


    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) throws Exception {
        if (parent.getNTrees() <= 1)
            child.getTrees().addAll(parent.getTrees());
        else {
            final ConsensusNetwork consensusNetwork = new ConsensusNetwork();
            switch (getOptionConsensus()) {
                default:
                case Majority:
                    consensusNetwork.setOptionThreshold(0.5);
                    break;
                case Strict:
                    consensusNetwork.setOptionThreshold(0.99999999);
                    break;
                case Greedy:
                    consensusNetwork.setOptionThreshold(0);
            }
            final SplitsBlock splitsBlock = new SplitsBlock();
            consensusNetwork.compute(progress, taxaBlock, parent, splitsBlock);
            final GreedyTree greedyTree = new GreedyTree();
            greedyTree.compute(progress, taxaBlock, splitsBlock, child);
        }
    }


    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) {
        return !parent.isPartial();
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("consensus", "edgeWeights");
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
