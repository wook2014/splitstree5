/*
 * AverageConsensus.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
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
 * average consensus
 * <p>
 * Created on 07.06.2017
 *
 * @author Tobias Kloepper, Daniel Huson and David Bryant
 */

public class AverageConsensus extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock) throws Exception {

        progress.setMaximum(100);

        DistancesBlock pairwiseDistances = new DistancesBlock();
        AverageDistances averageDistances = new AverageDistances();
        averageDistances.compute(new ProgressPercentage(), taxaBlock, treesBlock, pairwiseDistances);

        //StringWriter sw = new StringWriter();
        //new DistancesNexusOutput().write(sw, taxaBlock, pairwiseDistances, null);
        //dist.write(sw, taxa);
        //System.out.println(sw.toString());

        final NeighborNet nnet = new NeighborNet();
        ProgressListener pl = new ProgressPercentage();
        nnet.compute(pl, taxaBlock, pairwiseDistances, splitsBlock);
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }
}
