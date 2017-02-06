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
import javafx.collections.ObservableList;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.SplitsUtilities;
import splitstree5.core.misc.TreeUtilities;
import splitstree5.utils.nexus.SplitsException;

import java.util.*;

/**
 * implements consensus network
 * Created by huson on 12/11/16.
 */
public class ConsensusNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    public enum EdgeWeights {Median, Mean, Count, Sum, None}

    private final SimpleObjectProperty<EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(EdgeWeights.Mean);
    private double optionThreshold = 0.33;

    /**
     * compute the consensus splits
     *
     * @param progressListener
     * @param taxaBlock
     * @param parent
     * @param child
     */
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) throws CanceledException, SplitsException {
        progressListener.setMaximum(100);
        final ObservableList<PhyloTree> trees = parent.getTrees();

        final Map<BitSet, WeightStats> split2weights = new HashMap<>();
        BitSet taxaInTree = taxaBlock.getTaxaSet();

        for (int which = 0; which < trees.size(); which++) {
            final PhyloTree tree = trees.get(which);
            progressListener.setProgress(50 * which / trees.size());
            try {
                final List<ASplit> splits = new ArrayList<>();
                TreeUtilities.computeSplits(taxaBlock, taxaInTree, tree, splits);
                for (ASplit split : splits) {
                    final WeightStats weightStats = split2weights.computeIfAbsent(split.getA(), k -> new WeightStats());
                    weightStats.add((float) split.getWeight());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int count = 0;
        for (BitSet aSet : split2weights.keySet()) {
            final WeightStats weightStats = split2weights.get(aSet);
            final double wgt;
            if (weightStats.getCount() / (double) trees.size() > optionThreshold) {
                switch (getOptionEdgeWeights()) {
                    case Count:
                        wgt = weightStats.getCount();
                        break;
                    case Mean:
                        wgt = weightStats.getMean();
                        break;
                    case Median:
                        wgt = weightStats.getMedian();
                        break;
                    case Sum:
                        wgt = weightStats.getSum();
                        break;
                    default:
                        wgt = 1;
                        break;
                }
                final float confidence = (float) weightStats.getCount() / (float) trees.size();
                child.getSplits().add(new ASplit(aSet, taxaBlock.getNtax(), wgt, confidence));
            }
            progressListener.setProgress(50 + 50 * ((count++) / split2weights.size()));
        }
        splitstree5.utils.nexus.SplitsUtilities.verifySplits(child, taxaBlock);


        child.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), child.getSplits()));
        child.setFit(-1);
        child.setCompatibility(Compatibility.compute(taxaBlock.getNtax(), child.getSplits(), child.getCycle()));
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock child) {
        return parent.size() > 0 && !parent.isPartial();
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("Consensus", "Threshold", "EdgeWeights");
    }

    public EdgeWeights getOptionEdgeWeights() {
        return optionEdgeWeights.get();
    }

    public SimpleObjectProperty<EdgeWeights> optionEdgeWeightsProperty() {
        return optionEdgeWeights;
    }

    public void setOptionEdgeWeights(EdgeWeights optionEdgeWeights) {
        this.optionEdgeWeights.set(optionEdgeWeights);
    }

    public double getOptionThreshold() {
        return optionThreshold;
    }

    public void setOptionThreshold(double optionThreshold) {
        this.optionThreshold = optionThreshold;
    }

    /**
     * a value object contains a set of all weights seen so far and their counts
     */
    private static class WeightStats {
        private ArrayList<Float> weights;
        private int totalCount;
        private double sum;

        /**
         * construct a new values map
         */
        WeightStats() {
            weights = new ArrayList<>();
            totalCount = 0;
            sum = 0;
        }

        /**
         * add the given weight and count
         *
         * @param weight
         */
        void add(float weight) {
            weights.add(weight);
            totalCount++;
            sum += weight;
        }

        /**
         * returns the number of values
         *
         * @return number
         */
        int getCount() {
            return totalCount;
        }

        /**
         * computes the mean values
         *
         * @return mean
         */
        double getMean() {
            return sum / (double) totalCount;
        }

        /**
         * computes the median value
         *
         * @return median
         */
        public double getMedian() {
            Object[] array = weights.toArray();
            Arrays.sort(array);
            return (Float) array[array.length / 2];

        }

        /**
         * returns the sum of weights
         *
         * @return sum
         */
        double getSum() {
            return sum;
        }
    }

}
