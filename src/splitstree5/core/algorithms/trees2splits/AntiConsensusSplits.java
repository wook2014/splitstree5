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
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.trees2splits.utils.PartialSplit;
import splitstree5.core.algorithms.trees2trees.ConsensusTree;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.Distortion;
import splitstree5.utils.SplitsException;
import splitstree5.utils.SplitsUtilities;
import splitstree5.utils.TreesUtilities;
import jloda.graph.Graph;

import java.util.stream.Collectors;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import java.util.*;
import java.util.stream.Stream;

import splitstree5.core.algorithms.splits2trees.GreedyTree;

/**
 * implements the anti-consensus method
 *
 * @author Fabian Crespo && Daniel Huson, March 2018
 */
public class AntiConsensusSplits extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    public enum EdgeWeights {Median, Mean, Count, Sum, None}

    private int optionMaxDistortionScore = 1;
    private double optionMinimumSplitWeight = 0.5;//10000 *0.5
    private double percentofSplitsWithLowDistortion = 0.1;
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
    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, TreesBlock treesBlock, SplitsBlock splitsBlock) throws Exception {


        final ObservableList<PhyloTree> trees = treesBlock.getTrees();
        final Map<BitSet, Pair<BitSet, WeightStats>> splitsAndWeights = new HashMap<>();
        final BitSet taxaInTree = taxaBlock.getTaxaSet();

        List<ASplit> treeSplits = new ArrayList<ASplit>();
        ArrayList<ASplit> treeSplits2 = new ArrayList<ASplit>();
        int count;

        SplitsBlock splits = new SplitsBlock();
        //super.compute(progress, taxaBlock, treesBlock, splits);

        progress.setSubtask("Processing trees");
        progress.setMaximum(splits.getNsplits());


        if (treesBlock.getNTrees() == 1)
            System.err.println("Anti-consensus: only one tree specified");


        ////incompatibility graph
        //Graph graph = buildIncompatibilityGraph(splits.getSplits());
        //Graph g2=buildIncompatibilityGraph(findMajorityRuleLowDistortionSplits(splits, treesBlock, 0.5));
        //for (Node v : graph.nodes()) {
        //   float compatibility = getConflictScore(v);

        // }

        ///////////////////////
        progress.setSubtask("Processing splits");
        progress.setMaximum(splits.getNsplits() * treesBlock.getNTrees());
        progress.setProgress(0);

        int totalScore;
        System.err.println("Filtering splits:");

        /*double[] scores = new double[treesBlock.getNTrees()];
        for (int s = 1; s <= splits.getNsplits(); s++) {
             totalScore = 0;
             BitSet A = splits.get(s).getA();
             BitSet B = splits.get(s).getB();
             count=0;

          for (int t = 1; t <= treesBlock.getNTrees(); t++) {
                treeSplits.clear();
                treeSplits2.clear();
                TreesUtilities.computeSplits(null, treesBlock.getTree(t), treeSplits);
                try {
                    SplitsUtilities.verifySplits(treeSplits, taxaBlock);
                } catch (SplitsException e) {
                    e.printStackTrace();
                }



                if (treeSplits.contains(splits.get(s))) count++;

                final BitSet treeTaxa = taxaBlock.getTaxaSet();
                final BitSet treeTaxaAndA = (BitSet) (treeTaxa.clone());
                treeTaxaAndA.and(A);
                final BitSet treeTaxaAndB = (BitSet) (treeTaxa.clone());
                treeTaxaAndB.and(B);

                if (treeTaxaAndA.cardinality() > 1 && treeTaxaAndB.cardinality() > 1) {
                    try {
                        PhyloTree tree = treesBlock.getTree(t);
                        totalScore += Distortion.computeDistortionForSplit(tree, A, B);
                    } catch (IOException ex) {
                        Basic.caught(ex);
                    }
                }
                progress.incrementProgress();
            }
            scores[s]= (double)count/treesBlock.getNTrees();
            if (totalScore <= getOptionMaxDistortionScore()) {
                final ASplit aSplit = splits.get(s);
                splitsBlock.getSplits().add(new ASplit(aSplit.getA(), aSplit.getB(), aSplit.getWeight()));
            }
        }
*/
        Object[] pSplitsOfTrees = new Object[treesBlock.getNTrees() + 1];

        BitSet[] supportSet = new BitSet[treesBlock.getNTrees() + 1];
        Set<PartialSplit> allPSplits = new HashSet<>();
        HashSet<ASplit> set = new HashSet<ASplit>();
        Collection<ASplit> setCopy = new HashSet<ASplit>();
        Collection<ASplit> tempSet;

        for (int t = 1; t <= treesBlock.getNTrees(); t++) {
            treeSplits.clear();
            pSplitsOfTrees[t] = new ArrayList<ASplit>();
            supportSet[t] = new BitSet();
            //super.computePartialSplits(taxaBlock, treesBlock,t,  pSplitsOfTrees[t], supportSet[t]);
            TreesUtilities.computeSplits(taxaBlock.getTaxaSet(), treesBlock.getTree(t), treeSplits);
            pSplitsOfTrees[t] = new ArrayList<ASplit>(treeSplits);
            ;
            if (!set.isEmpty()) {

                setCopy = (HashSet<ASplit>) set.clone();
                setCopy.retainAll(treeSplits);
                tempSet = treeSplits;
                //tempSet.removeAll(setCopy);
                for (ASplit a : setCopy) {
                    tempSet.remove(a);
                }
                set.addAll(tempSet);

            } else {
                set.addAll(treeSplits);

            }


            try {
                SplitsUtilities.verifySplits(treeSplits, taxaBlock);
            } catch (SplitsException e) {
                e.printStackTrace();
            }


        }

        splits.getSplits().addAll(set);

        count = 0;
        Map<ASplit, Double> splitScores = new HashMap<ASplit, Double>();
        ArrayList<ASplit> temp;
        ASplit as;


        for (int s = 0; s < splits.getSplits().size(); s++) {
            as = (ASplit) splits.getSplits().get(s);
            count = 0;
            for (int t = 1; t <= treesBlock.getNTrees(); t++) {
                temp = (ArrayList<ASplit>) pSplitsOfTrees[t];
                if (temp.contains(as)) count++;


            }
            splitScores.put(as, (double) count / treesBlock.getNTrees());
        }

        Map<Object, Double> majoritySplits =
                splitScores.entrySet()
                        .stream()
                        .filter(p -> p.getValue() > getOptionMinimumSplitWeight())
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        Set<ASplit> majoritySplitsList = (Set) majoritySplits.keySet();
        ArrayList<ASplit> majoritySplitsList2 = new ArrayList<ASplit>();
        majoritySplitsList2.addAll(majoritySplitsList);
        splitsBlock.getSplits().addAll(majoritySplitsList2);

        Map<Object, Double> complementSplits =
                splitScores.entrySet()
                        .stream()
                        .filter(p -> p.getValue() <= getOptionMinimumSplitWeight())
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

       /* Stream<Map.Entry<Object, Double>> sortedComplementSplits =
                complementSplits.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));*/

        Map<Object, Double> sortedComplementSplits =
                complementSplits.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        // compute a tree from majority splits

        GreedyTree greedyTree = new GreedyTree();
        TreesBlock majorityTreeBlock = new TreesBlock();
        greedyTree.compute(progress, taxaBlock, splits, majorityTreeBlock);

        Iterator it = sortedComplementSplits.keySet().iterator();
        Iterator it1 = majoritySplits.keySet().iterator();
        count = 0;
        double[] incompatibilityScores = new double[treesBlock.getNTrees()];
        int s = 0;
        ASplit asplit;
        int numerOfSplitswithLowDistortion=(int) Math.floor(percentofSplitsWithLowDistortion * complementSplits.keySet().size());

        Map<Object, Double> topComplementSplits =
                complementSplits.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(numerOfSplitswithLowDistortion)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        while (it.hasNext()) {
            totalScore = 0;
            asplit = (ASplit) it.next();
            BitSet A = asplit.getA();
            BitSet B = asplit.getB();
            for (int t = 1; t <= majorityTreeBlock.getNTrees(); t++) {
                final BitSet treeTaxa = taxaBlock.getTaxaSet();
                final BitSet treeTaxaAndA = (BitSet) (treeTaxa.clone());
                treeTaxaAndA.and(A);
                final BitSet treeTaxaAndB = (BitSet) (treeTaxa.clone());
                treeTaxaAndB.and(B);

                if (treeTaxaAndA.cardinality() > 1 && treeTaxaAndB.cardinality() > 1) {
                    try {
                        PhyloTree tree = majorityTreeBlock.getTree(t);
                        totalScore += Distortion.computeDistortionForSplit(tree, A, B);
                    } catch (IOException ex) {
                        Basic.caught(ex);
                    }
                }

            }
            if (totalScore > 0 && totalScore <= getOptionMaxDistortionScore() && s<= numerOfSplitswithLowDistortion) {
                s++;
                final ASplit aSplit = asplit;
                splitsBlock.getSplits().add(new ASplit(aSplit.getA(), aSplit.getB(), aSplit.getWeight()));
            }
            it.remove();
        }


    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return parent.size() > 0 && !parent.isPartial();
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("Consensus", "Threshold", "EdgeWeights");
    }


    public void setOptionEdgeWeights(EdgeWeights optionEdgeWeights) {
        this.optionEdgeWeights.set(optionEdgeWeights);
    }

    public double getOptionThreshold() {
        return optionThreshold.get();
    }

    public int getOptionMaxDistortionScore() {
        return optionMaxDistortionScore;
    }

    public double getOptionMinimumSplitWeight() {
        return optionMinimumSplitWeight;
    }
    //optionMinimumSplitWeight

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

    private Graph buildIncompatibilityGraph(List<ASplit> splits) {
        final Graph graph = new Graph();

        final Node[] split2node = new Node[splits.size()];

        for (int s = 0; s < splits.size(); s++) {
            final Pair<Integer, Integer> pair = new Pair<>(s, (int) (10000 * splits.get(s).getWeight()));
            split2node[s] = graph.newNode(pair);
        }
        for (int s = 0; s < splits.size(); s++) {

            for (int t = s + 1; t < splits.size(); t++)
                if (!Compatibility.areCompatible(splits.get(s), splits.get(t))) {
                    graph.newEdge(split2node[s], split2node[t]);
                }
        }
        return graph;
    }

    /**
     * gets the node will the lowest compatibility score
     *
     * @param graph
     * @return worst node
     */
    private Node getWorstNode(Graph graph) {
        float worstCompatibility = 0;
        Node worstNode = null;
        for (Node v : graph.nodes()) {
            float compatibility = getCompatibilityScore(v);
            if (worstNode == null || compatibility < worstCompatibility) {
                worstNode = v;
                worstCompatibility = compatibility;
            }
        }
        return worstNode;
    }

    /**
     * gets the compatibility score of a node.
     * This is the weight of the splits minus the weight of all contradicting splits
     *
     * @param v
     * @return compatibility score
     */
    private int getCompatibilityScore(Node v) {
        int score = ((Pair<Integer, Integer>) v.getInfo()).getSecond();
        for (Node w : v.adjacentNodes()) {
            score -= ((Pair<Integer, Integer>) w.getInfo()).getSecond();
        }
        return score;
    }

    /**
     * gets the incompatibility score of a node.
     * This is the sum of the  weights of the adyacent nodes to a given node
     *
     * @param v
     * @return get  conflict score
     */
    public int getConflictScore(Node v) {
        int score = 0;
        for (Node w : v.adjacentNodes()) {
            score += ((Pair<Integer, Integer>) w.getInfo()).getSecond();
        }
        return score;
    }

    /**
     * gets the  nodes in decreasing order of conflict score.
     *
     * @param aset
     */
    private void sortByDecreasingConflictScore(NodeSet aset) {

        Node[] nodeArray = aset.toArray();

        Arrays.sort(nodeArray, (v1, v2) -> this.getConflictScore(v2) - (this.getConflictScore(v1)));
    }

    /**
     * gets the  p percent of the  least conflicting nodes nodes
     *
     * @param aset
     * @return get a sorted array  of nodes in decreasing conflict score order
     */
    private Node[] getPercentageLeastConflictingNodes(NodeSet aset, double p) throws Exception {
        if (p < 0.0 || p > 1.0) throw new IllegalArgumentException();
        Node[] nodeArray = aset.toArray();

        Arrays.sort(nodeArray, (v1, v2) -> this.getConflictScore(v2) - (this.getConflictScore(v1)));
        double d = p * ((double) nodeArray.length);
        int numberOfNodes = (int) Math.floor(d);
        return Arrays.copyOf(nodeArray, numberOfNodes);

    }

    /**
     * gets the  p percent of the  least conflicting nodes nodes
     *
     * @param splits
     * @return returns all the  splits with a weight  greater than  the value percent
     */
    private List<ASplit> findMajorityRuleSplits(List<ASplit> splits, double percent) throws Exception {
        if (percent < 0.0 || percent > 1.0) throw new IllegalArgumentException();
        List<ASplit> splits2 = new ArrayList<>();

        for (int s = 0; s < splits.size(); s++) {
            if (splits.get(s).getWeight() >= percent) {
                splits2.add(splits.get(s));

            }

        }
        return splits2;
    }

    /**
     * gets the  p percent of the  least conflicting nodes nodes
     *
     * @param splits
     * @return returns all the  splits with a weight  greater than  the value percent
     */
    private List<ASplit> findMajorityRuleLowDistortionSplits(SplitsBlock splits, TreesBlock trees, double percent) {

        List<ASplit> splits2 = new ArrayList<>();
        int totalScore;
        int distortion;
        double[] splitScores = computeSplitScores(splits, trees);


        final BitSet[] tree2taxa = new BitSet[trees.getNTrees() + 1];
        for (int t = 1; t <= trees.getNTrees(); t++) {
            tree2taxa[t] = TreesUtilities.getTaxa(trees.getTree(t));

        }

        for (int s = 0; s < splits.size(); s++) {
            BitSet A = splits.get(s).getA();
            BitSet B = splits.get(s).getB();
            totalScore = 0;
            for (int t = 1; t <= trees.getNTrees(); t++) {

                final BitSet treeTaxa = tree2taxa[t];
                final BitSet treeTaxaAndA = (BitSet) (treeTaxa.clone());
                treeTaxaAndA.and(A);
                final BitSet treeTaxaAndB = (BitSet) (treeTaxa.clone());
                treeTaxaAndB.and(B);

                if (treeTaxaAndA.cardinality() > 1 && treeTaxaAndB.cardinality() > 1) {
                    try {
                        PhyloTree tree = trees.getTree(t);
                        distortion = Distortion.computeDistortionForSplit(tree, A, B);
                        totalScore += distortion;
                    } catch (IOException ex) {
                        Basic.caught(ex);
                    }
                }


            }
            if (totalScore < getOptionMaxDistortionScore() && splitScores[s] >= percent) {

                splits2.add(splits.get(s));
            }


        }

        return splits2;
    }

    private Graph buildIncompatibilityGraphwithMajorityRuleandLowDistortion(SplitsBlock splits, TreesBlock trees, double percent) {

        return buildIncompatibilityGraph(findMajorityRuleLowDistortionSplits(splits, trees, percent));
    }

    private double[] computeSplitScores(SplitsBlock splitsBlock, TreesBlock treesBlock) {
        double[] scores = new double[splitsBlock.getNsplits()];
        List<ASplit> treeSplits = new ArrayList<>();

        int count;

        for (int s = 1; s <= splitsBlock.getNsplits(); s++) {

            BitSet A = splitsBlock.get(s).getA();
            BitSet B = splitsBlock.get(s).getB();
            count = 0;
            for (int t = 1; t <= treesBlock.getNTrees(); t++) {
                treeSplits.clear();
                TreesUtilities.computeSplits(null, treesBlock.getTree(t), treeSplits);
                if (treeSplits.contains(splitsBlock.get(s))) {
                    count++;
                }


            }
            scores[s] = (double) count / treesBlock.getNTrees();
        }
        return scores;
    }


    private static class WeightStats1 {
        private ArrayList<Float> weights;
        private int totalCount;
        private double sum;

        /**
         * construct a new values map
         */
        WeightStats1() {
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


