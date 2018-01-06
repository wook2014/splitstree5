package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.trees2distances.AverageDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

/**
 * implements consensus networks
 * <p>
 * Created on 07.06.2017
 *
 * @author Tobias Kloepper, Daniel Huson and David Bryant
 */

public class AverageConsensus extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {

    private boolean analyseDistances = false;

    public final static String DESCRIPTION = "Constructs a Neighbor-Net from the average pairwise distances in the trees";


    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock)
            throws Exception {

        progressListener.setMaximum(100);

        DistancesBlock pairwiseDistances = new DistancesBlock();
        AverageDistances averageDistances = new AverageDistances();
        averageDistances.compute(new ProgressPercentage(), taxaBlock, treesBlock, pairwiseDistances);

        //StringWriter sw = new StringWriter();
        //DistancesNexusIO.write(sw, taxaBlock, pairwiseDistances, null);
        //dist.write(sw, taxa);
        //System.out.println(sw.toString());

        NeighborNet nnet = new NeighborNet();
        ProgressListener pl = new ProgressPercentage();
        nnet.compute(pl, taxaBlock, pairwiseDistances, splitsBlock);

    }


    public String getDescription() {
        return DESCRIPTION;
    }

    public boolean getOptionAnalyseDistances() {
        return analyseDistances;
    }

    public void setOptionAnalyseDistances(boolean analyseDistances) {
        this.analyseDistances = analyseDistances;
    }
}
