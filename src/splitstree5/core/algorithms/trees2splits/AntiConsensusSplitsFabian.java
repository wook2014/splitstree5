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
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.splits2trees.GreedyTree;
import splitstree5.core.algorithms.trees2splits.utils.PartialSplit;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.Distortion;
import splitstree5.io.imports.utils.SimpleNewickParser;
import splitstree5.utils.SplitsException;
import splitstree5.utils.SplitsUtilities;
import splitstree5.utils.TreesUtilities;
import splitstree5.xtra.crespo.method.Distortion_Scorer;
import splitstree5.xtra.crespo.simulation.Simulation_Manager;
import splitstree5.xtra.crespo.util.MyNewickParser;
import splitstree5.xtra.crespo.util.MyTree;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * implements the anti-consensus method
 *
 * @author Fabian Crespo && Daniel Huson, March 2018
 */
public class AntiConsensusSplitsFabian extends Algorithm<TreesBlock, SplitsBlock> { // implements IFromTrees, IToSplits {
    public enum EdgeWeights {Median, Mean, Count, Sum, None}

    private int optionMaxDistortionScore = 1;
    private double optionMinimumSplitWeight = 0.5;//10000 *0.5
    private double percentofSplitsWithLowDistortion = 0.2;
    private final SimpleObjectProperty<EdgeWeights> optionEdgeWeights = new SimpleObjectProperty<>(EdgeWeights.Mean);
    private DoubleProperty optionThreshold = new SimpleDoubleProperty(0.5);

    private final Object sync = new Object();

    public final static String DESCRIPTION = "Computes the anti-consensus splits of trees";

    private static ProgressListener progress = null;
    private static SplitsBlock splitsBlock = null;
    private static TreesBlock treesBlock = null;
    private static TaxaBlock taxaBlock = null;
    private static TreesBlock majorityTreeBlock = null;

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


        //for tests
     /*   if (AntiConsensusNetwork.progress == null)
            AntiConsensusNetwork.progress = progress;

        if (AntiConsensusNetwork.splitsBlock == null)
            AntiConsensusNetwork.splitsBlock = splitsBlock;

        if (AntiConsensusNetwork.treesBlock == null)
            AntiConsensusNetwork.treesBlock = treesBlock;

        if (AntiConsensusNetwork.taxaBlock == null)
            AntiConsensusNetwork.taxaBlock = taxaBlock;*/

        final ObservableList<PhyloTree> trees = treesBlock.getTrees();
        final Map<BitSet, Pair<BitSet, WeightStats>> splitsAndWeights = new HashMap<>();
        final BitSet taxaInTree = taxaBlock.getTaxaSet();

        List<ASplit> treeSplits = new ArrayList<ASplit>();
        ArrayList<ASplit> treeSplits2 = new ArrayList<ASplit>();


        SplitsBlock splits = new SplitsBlock();
        //super.compute(progress, taxaBlock, treesBlock, splits);

        progress.setSubtask("Processing trees");
        progress.setMaximum(splits.getNsplits());


        if (treesBlock.getNTrees() == 1)
            System.err.println("Anti-consensus: only one tree specified");

        progress.setSubtask("Processing splits");
        progress.setMaximum(splits.getNsplits() * treesBlock.getNTrees());
        progress.setProgress(0);

        int totalScore;
        System.err.println("Filtering splits:");

        Map<ASplit, Double> splitScores = new HashMap<ASplit, Double>();
        computeSplits(progress, treesBlock, taxaBlock, splitsBlock, splitScores);
        TreesBlock majorityTreeBlock = new TreesBlock();
        GreedyTree greedyTree = new GreedyTree();
        try {
            greedyTree.compute(progress, taxaBlock, splitsBlock, majorityTreeBlock);
        } catch (CanceledException e) {
            e.printStackTrace();
        }
        ArrayList<String> taxaNames = new ArrayList<String>();

        Node firstLeaf = majorityTreeBlock.getTrees().get(0).leaves().iterator().next();
        majorityTreeBlock.getTrees().get(0).setRoot(firstLeaf.getFirstInEdge().getOpposite(firstLeaf));
        MyTree majorityMyTree = converToMyTree(majorityTreeBlock.getTrees().get(0));

        scoreSplitsbyDistortionRespectToTreesBlock(majorityTreeBlock, splitsBlock, taxaBlock, splitScores);
        //for tests
        //main(new String[]{"arg1", "arg2", "arg3"});


    }

    public void main(String[] args) throws IOException {
        Simulation_Manager simulationManager = new Simulation_Manager();
        int numberOfTaxa = 20;
        Object[] objects = simulationManager.run(numberOfTaxa, 50, 500, 10, 1, 1, 0.015);
        ArrayList<PhyloTree> phyloTrees = (ArrayList<PhyloTree>) objects[1];
        ArrayList<MyTree> myTrees = (ArrayList<MyTree>) objects[3];
        ArrayList<String> taxaNames = (ArrayList<String>) objects[2];
        Distortion_Scorer distortionScorer = new Distortion_Scorer();
        AntiConsensusSplitsFabian.treesBlock.clear();

        AntiConsensusSplitsFabian.splitsBlock.clear();
        AntiConsensusSplitsFabian.taxaBlock.clear();

        AntiConsensusSplitsFabian.taxaBlock.addTaxaByNames(taxaNames);
        AntiConsensusSplitsFabian.treesBlock.getTrees().addAll(phyloTrees);
        Map<ASplit, Double> splitScores = new HashMap<ASplit, Double>();

        (new AntiConsensusSplitsFabian()).computeSplits(AntiConsensusSplitsFabian.progress, AntiConsensusSplitsFabian.treesBlock, AntiConsensusSplitsFabian.taxaBlock, AntiConsensusSplitsFabian.splitsBlock, splitScores);

        Map<Object, Double> complementSplits =
                splitScores.entrySet()
                        .stream()
                        .filter(p -> p.getValue() <= 0.5)
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));


        Map<Object, Double> sortedComplementSplits =
                complementSplits.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        GreedyTree greedyTree = new GreedyTree();
        TreesBlock majorityTreeBlock = new TreesBlock();
        try {
            greedyTree.compute(progress, taxaBlock, splitsBlock, majorityTreeBlock);
        } catch (CanceledException e) {
            e.printStackTrace();
        }


        Node firstLeaf = majorityTreeBlock.getTrees().get(0).leaves().iterator().next();
        majorityTreeBlock.getTrees().get(0).setRoot(firstLeaf.getFirstInEdge().getOpposite(firstLeaf));
        MyTree majorityMyTree = converToMyTree(majorityTreeBlock.getTrees().get(0));

        scoreSplitsbyDistortionRespectToTreesBlock(majorityTreeBlock, splitsBlock, taxaBlock, splitScores);
        int distortionScore;
        PhyloTree pt;
        final Iterator it = sortedComplementSplits.keySet().iterator();
        ASplit asplit;
        pt = (new Simulation_Manager()).converToPhyloTree(majorityMyTree);
        while (it.hasNext()) {
            asplit = (ASplit) it.next();
            BitSet A = asplit.getA();
            BitSet B = asplit.getB();

            distortionScore = distortionScorer.run(majorityMyTree, A, B, majorityMyTree.getTaxa());
            it.remove();
        }
    }

    public MyTree converToMyTree(PhyloTree phyloTree) {
        return new MyNewickParser().run(phyloTree.toBracketString());
    }

    public PhyloTree converToPhyloTree(MyTree myTree) {

        PhyloTree result = null;
        try {
            result = new SimpleNewickParser().parse(myTree.toNewickString());

        } catch (Exception ex) {

            ex.printStackTrace();

        }

        return result;

    }

    private void computeSplits(ProgressListener progress, TreesBlock treesBlock, TaxaBlock taxaBlock, SplitsBlock splitsBlock, Map<ASplit, Double> splitScores) {


        SplitsBlock splits = new SplitsBlock();
        Object[] pSplitsOfTrees = new Object[treesBlock.getNTrees() + 1];

        ArrayList<ASplit> treeSplits = new ArrayList<ASplit>();

        BitSet[] supportSet = new BitSet[treesBlock.getNTrees() + 1];
        Set<PartialSplit> allPSplits = new HashSet<>();
        HashSet<ASplit> set = new HashSet<ASplit>();
        Collection<ASplit> setCopy = new HashSet<ASplit>();
        Collection<ASplit> tempSet;

        for (int t = 1; t <= treesBlock.getNTrees(); t++) {
            treeSplits.clear();
            pSplitsOfTrees[t] = new ArrayList<ASplit>();
            supportSet[t] = new BitSet();

            TreesUtilities.computeSplits(taxaBlock.getTaxaSet(), treesBlock.getTree(t), treeSplits);
            pSplitsOfTrees[t] = new ArrayList<ASplit>(treeSplits);

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

        int count = 0;
        //Map<ASplit, Double> splitScores = new HashMap<ASplit, Double>();
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

 /*   GreedyTree greedyTree = new GreedyTree();
    TreesBlock majorityTreeBlock = new TreesBlock();
    try {
        greedyTree.compute(progress, taxaBlock, splitsBlock, majorityTreeBlock);
    } catch (CanceledException e) {
        e.printStackTrace();
    }
*/

    }

    public void scoreSplitsbyDistortionRespectToTreesBlock(TreesBlock majorityTreeBlock, SplitsBlock splitsBlock, TaxaBlock taxaBlock, Map<ASplit, Double> splitScores) {

        Map<Object, Double> majoritySplits =
                splitScores.entrySet()
                        .stream()
                        .filter(p -> p.getValue() > getOptionMinimumSplitWeight())
                        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        Set<ASplit> majoritySplitsList = (Set) majoritySplits.keySet();
        ArrayList<ASplit> majoritySplitsList2 = new ArrayList<ASplit>();
        majoritySplitsList2.addAll(majoritySplitsList);
        //splitsBlock.getSplits().addAll(majoritySplitsList2);

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

        Iterator it = sortedComplementSplits.keySet().iterator();
        Iterator it1 = majoritySplits.keySet().iterator();
        int count = 0;
        double[] incompatibilityScores = new double[sortedComplementSplits.keySet().size()];
        int s = 0;
        ASplit asplit;
        int numerOfSplitswithLowDistortion = (int) Math.floor(percentofSplitsWithLowDistortion * complementSplits.keySet().size());

        Map<Object, Double> topComplementSplits =
                complementSplits.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(numerOfSplitswithLowDistortion)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        int index = 0;
        int totalScore;
        Distortion_Scorer distortionScorer = new Distortion_Scorer();
        while (it.hasNext()) {
            totalScore = 0;
            asplit = (ASplit) it.next();
            BitSet A = asplit.getA();
            BitSet B = asplit.getB();
            for (int t = 1; t <= majorityTreeBlock.getNTrees(); t++) {//only one tree: 50 % majority tree
                final BitSet treeTaxa = taxaBlock.getTaxaSet();
                final BitSet treeTaxaAndA = (BitSet) (treeTaxa.clone());
                treeTaxaAndA.and(A);
                final BitSet treeTaxaAndB = (BitSet) (treeTaxa.clone());
                treeTaxaAndB.and(B);

                if (treeTaxaAndA.cardinality() > 1 && treeTaxaAndB.cardinality() > 1) {

                    PhyloTree tree = majorityTreeBlock.getTree(t);
                    MyTree myTree = converToMyTree(tree);
                    //totalScore += Distortion.computeDistortionForSplit(tree, A, B);
                    totalScore += distortionScorer.run(myTree, A, B, myTree.getTaxa());

                }

            }
            incompatibilityScores[index] = totalScore;
            index++;
            if (totalScore > 0 && totalScore <= getOptionMaxDistortionScore() && s <= numerOfSplitswithLowDistortion) {
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


