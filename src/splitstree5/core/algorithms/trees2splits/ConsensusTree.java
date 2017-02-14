package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

public class ConsensusTree extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToTrees {

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) throws Exception {
        progressListener.setTasks("Consensus tree", "Init.");
        //progressListener.setMaximum(?);

        progressListener.close();
    }
}
