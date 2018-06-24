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
import jloda.fx.ProgramExecutorService;
import jloda.phylo.PhyloTree;
import jloda.util.*;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * implements consensus network
 *
 * @author Daniel Huson, 1/2017
 */
public class ConsensusNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    public enum EdgeWeights {Mean, TreeSizeWeightedMean, Median, Count, Sum, None}

    private final SimpleObjectProperty<EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(EdgeWeights.Mean);
    private DoubleProperty optionThresholdPercent = new SimpleDoubleProperty(30.0);

    private final Object sync = new Object();

    public final static String DESCRIPTION = "Computes the consensus splits of trees (Holland and Moulton 2003)";

    @Override
    public String getCitation() {
        return "Holland and Moulton 2003; " +
                "B. Holland and V. Moulton. Consensus networks:  A method for visualizing incompatibilities in  collections  of  trees.   " +
                "In  G.  Benson  and  R.  Page,  editors, Proceedings  of  “Workshop  on Algorithms in Bioinformatics”, " +
                "volume 2812 of LNBI, pages 165–176. Springer, 2003.";
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


        final ExecutorService executor = ProgramExecutorService.getInstance();

        if (treesBlock.getNTrees() == 1) System.err.println("Consensus network: only one tree specified");

        progress.setMaximum(100);
        progress.setProgress(0);

        {
            final int numberOfThreads = Math.min(trees.size(), 8);
            final CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
            final Single<CanceledException> exception = new Single<>();

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadNumber = i;
                executor.execute(() -> {
                    try {
                        for (int which = threadNumber; which < trees.size(); which += numberOfThreads) {
                            final PhyloTree tree = trees.get(which);
                            final double factor;
                            if (getOptionEdgeWeights() == EdgeWeights.TreeSizeWeightedMean)
                                factor = 1.0 / TreesUtilities.computeTotalWeight(tree);
                            else
                                factor = 1;

                            //System.err.println("Tree "+which+": "+factor);

                            final List<ASplit> splits = new ArrayList<>();
                            TreesUtilities.computeSplits(taxaInTree, tree, splits);
                            try {
                                SplitsUtilities.verifySplits(splits, taxaBlock);
                            } catch (SplitsException ex) {
                                Basic.caught(ex);
                            }

                            for (ASplit split : splits) {
                                synchronized (sync) {
                                    final Pair<BitSet, WeightStats> pair = splitsAndWeights.computeIfAbsent(split.getPartContaining(1), k -> new Pair<>(k, new WeightStats()));
                                    pair.getSecond().add((float) (factor * split.getWeight()), which);
                                }
                            }
                            if (threadNumber == 0) {
                                try {
                                    progress.setProgress((long) (which * 80.0 / trees.size()));
                                } catch (CanceledException ex) {
                                    while (countDownLatch.getCount() > 0)
                                        countDownLatch.countDown(); // flush
                                    exception.set(ex);
                                    return;
                                }
                            }
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                if (exception.get() == null) // must have been canceled
                    exception.set(new CanceledException());
            }
            if (exception.get() != null) {
                throw exception.get();
            }
        }

        {
            final int numberOfThreads = Math.min(splitsAndWeights.size(), 8);
            final CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
            final Single<CanceledException> exception = new Single<>();

            final double threshold = (optionThresholdPercent.getValue() < 100 ? optionThresholdPercent.getValue() / 100.0 : 0.999999);

            final ArrayList<Pair<BitSet, WeightStats>> array = new ArrayList<>(splitsAndWeights.values());

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadNumber = i;
                executor.execute(() -> {
                    try {
                        for (int which = threadNumber; which < array.size(); which += numberOfThreads) {
                            final BitSet side = array.get(which).getFirst();
                            final WeightStats weightStats = array.get(which).getSecond();
                            final double wgt;
                            if (weightStats.getCount() / (double) trees.size() > threshold) {
                                switch (getOptionEdgeWeights()) {
                                    case Count:
                                        wgt = weightStats.getCount();
                                        break;
                                    case TreeSizeWeightedMean: // values have all already been divided by total tree length, just need mean here...
                                        wgt = weightStats.getMean();
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
                            if (threadNumber == 0) {
                                try {
                                    progress.setProgress(80 + 20 * (which / array.size()));
                                } catch (CanceledException ex) {
                                    while (countDownLatch.getCount() > 0)
                                        countDownLatch.countDown(); // flush
                                    exception.set(ex);
                                    return;
                                }
                            }
                        }

                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                // Basic.caught(e);
                if (exception.get() == null) // must have been canceled
                    exception.set(new CanceledException());
            }
            if (exception.get() != null) {
                throw exception.get();
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

    public double getOptionThresholdPercent() {
        return optionThresholdPercent.get();
    }

    public DoubleProperty optionThresholdPercentProperty() {
        return optionThresholdPercent;
    }

    public void setOptionThresholdPercent(double optionThresholdPercent) {
        this.optionThresholdPercent.set(optionThresholdPercent);
    }

    /**
     * a value object contains a set of all weights seen so far and their counts
     */
    private static class WeightStats {
        private ArrayList<Float> weights;
        private int totalCount;
        private double sum;
        private int treeIndex;

        /**
         * construct a new values map
         */
        WeightStats() {
            weights = new ArrayList<>();
            totalCount = 0;
            sum = 0;
            treeIndex = -1;
        }

        /**
         * add the given weight and count
         *
         * @param weight
         */
        void add(float weight, int treeIndex) {
            weights.add(weight);
            totalCount++;
            sum += weight;
            this.treeIndex = treeIndex;
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
