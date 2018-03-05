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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.utils.SplitsException;
import splitstree5.utils.SplitsUtilities;
import splitstree5.utils.TreesUtilities;

import java.util.*;

/**
 * implements the anti-consensus method
 *
 * @author Fabian Crespo && Daniel Huson, March 2018
 */
public class AntiConsensusSplits extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    public enum EdgeWeights {Median, Mean, Count, Sum, None}

    private final SimpleObjectProperty<EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(EdgeWeights.Mean);
    private DoubleProperty optionThreshold = new SimpleDoubleProperty(0.5);

    private final Object sync = new Object();

    public final static String DESCRIPTION = "Computes the anti-consensus splits of trees";

    @Override
    public String getCitation() {
        return null;
    }

    /**
     * compute the consensus splits
     *
     * @param progress
     * @param taxaBlock
     * @param treesBlock
     * @param splitsBlock
     */
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock) throws CanceledException, SplitsException {

        final ObservableList<PhyloTree> trees = treesBlock.getTrees();
        final Map<BitSet, Pair<BitSet, WeightStats>> splitsAndWeights = new HashMap<>();
        final BitSet taxaInTree = taxaBlock.getTaxaSet();

        if (treesBlock.getNTrees() == 1)
            System.err.println("Anti-consensus: only one tree specified");

        progress.setMaximum(100);
        progress.setProgress(0);

        {
            for (int which = 0; which < trees.size(); which++) {
                final PhyloTree tree = trees.get(which);
                final List<ASplit> splits = new ArrayList<>();
                TreesUtilities.computeSplits(taxaInTree, tree, splits);
                try {
                    SplitsUtilities.verifySplits(splits, taxaBlock);
                } catch (SplitsException e) {
                    e.printStackTrace();
                }

                for (ASplit split : splits) {
                    synchronized (sync) {
                        final Pair<BitSet, WeightStats> pair = splitsAndWeights.computeIfAbsent(split.getPartContaining(1), k -> new Pair<>(k, new WeightStats()));
                        pair.getSecond().add((float) split.getWeight());
                    }
                }
                progress.setProgress((long) (which * 80.0 / trees.size()));
            }
        }

        {
            final double threshold = (optionThreshold.getValue() < 1 ? optionThreshold.getValue() : 0.999999);

            final ArrayList<Pair<BitSet, WeightStats>> array = new ArrayList<>(splitsAndWeights.values());

            for (Pair<BitSet, WeightStats> pair : array) {
                final BitSet side = pair.getFirst();
                final WeightStats weightStats = pair.getSecond();
                final double wgt;
                if (weightStats.getCount() / (double) trees.size() > threshold) {
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
                    synchronized (sync) {
                        splitsBlock.getSplits().add(new ASplit(side, taxaBlock.getNtax(), wgt, confidence));
                    }
                }
            }

        }

        SplitsUtilities.verifySplits(splitsBlock.getSplits(), taxaBlock);

        splitsBlock.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), splitsBlock.getSplits()));
        splitsBlock.setFit(-1);
        splitsBlock.setCompatibility(Compatibility.compute(taxaBlock.getNtax(), splitsBlock.getSplits(), splitsBlock.getCycle()));
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
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
        return optionThreshold.get();
    }

    public DoubleProperty optionThresholdProperty() {
        return optionThreshold;
    }

    public void setOptionThreshold(double optionThreshold) {
        this.optionThreshold.set(optionThreshold);
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
