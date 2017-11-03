package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

public class ConsensusTree extends Algorithm<TreesBlock, TreesBlock> implements IFromTrees, IToTrees {

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock parent, TreesBlock child) throws Exception {
        progressListener.setTasks("Consensus tree", "Init.");
        //progressListener.setMaximum(?);

        // todo needs new algorithms that are not contained in SplitsTree4

        progressListener.close();
    }
}
