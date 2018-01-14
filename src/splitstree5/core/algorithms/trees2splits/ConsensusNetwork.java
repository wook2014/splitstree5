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
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.utils.SplitsException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * implements consensus network
 * Created on 12/11/16.
 *
 * @author Tobias Kloepper, Daniel Huson and David Bryant
 */
public class ConsensusNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private boolean debug = false;

    public enum EdgeWeights {Median, Mean, Count, Sum, None}

    private final SimpleObjectProperty<EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(EdgeWeights.Mean);
    private DoubleProperty optionThreshold = new SimpleDoubleProperty(0.5);

    public final static String DESCRIPTION = "Computes the consensus splits of trees (Holland and Moulton 2003)";

    @Override
    public String getCitation() {
        return "ConsensusNetwork; Holland and Moulton 2003; " +
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
        final Map<BitSet, WeightStats> split2weights = new HashMap<>();
        final BitSet taxaInTree = taxaBlock.getTaxaSet();

        if (treesBlock.getNTrees() == 1) System.err.println("Consensus network: only one Tree specified");

        progress.setMaximum(100);
        progress.setProgress(0);

        for (int which = 0; which < trees.size(); which++) {
            final PhyloTree tree = trees.get(which);
            final List<ASplit> splits = new ArrayList<>();
            TreeUtilities.computeSplits(taxaBlock, taxaInTree, tree, splits);
                /*SplitsBlock tmp = new SplitsBlock();
                TreesBlock tmpT= new TreesBlock();
                tmpT.getTrees().add(tree);
                trans = new TreeSelector();
                trans.compute(progressListener, taxaBlock, treesBlock, tmp);*/

            for (ASplit split : splits) {
                System.err.println(split.getA());
                final WeightStats weightStats = split2weights.computeIfAbsent(split.getA(), k -> new WeightStats());
                weightStats.add((float) split.getWeight());
            }
            progress.setProgress((long) (which * 50.0 / trees.size()));
        }

        final double threshold = (optionThreshold.getValue() < 1 ? optionThreshold.getValue() : 0.999999);

        int count = 0;
        for (BitSet aSet : split2weights.keySet()) {
            final WeightStats weightStats = split2weights.get(aSet);
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
                splitsBlock.getSplits().add(new ASplit(aSet, taxaBlock.getNtax(), wgt, confidence));
            }
            progress.setProgress(50 + 50 * ((count++) / split2weights.size()));
        }
        splitstree5.utils.SplitsUtilities.verifySplits(splitsBlock, taxaBlock);

        splitsBlock.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), splitsBlock.getSplits()));
        splitsBlock.setFit(-1);
        splitsBlock.setCompatibility(Compatibility.compute(taxaBlock.getNtax(), splitsBlock.getSplits(), splitsBlock.getCycle()));

        if (debug) {
            System.err.println("DEBUG 2");
            final StringWriter w1 = new StringWriter();
            w1.write("#nexus\n");
            try {
                SplitsNexusIO.write(w1, taxaBlock, splitsBlock, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.err.println(w1.toString());
        }
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent, SplitsBlock splitsBlock) {
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
