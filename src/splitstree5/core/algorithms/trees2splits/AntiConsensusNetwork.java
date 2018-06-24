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

import jloda.fx.ProgramExecutorService;
import jloda.graph.Edge;
import jloda.graph.Graph;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import jloda.util.*;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.splits2trees.GreedyTree;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.misc.Distortion;
import splitstree5.utils.TreesUtilities;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * implements the anti-consensus method
 *
 * @author Daniel Huson, June 2018
 */
public class AntiConsensusNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private int optionMaxRank = 1;
    private boolean optionRequireSameSource = false;
    private boolean optionMaxRankOnly = false;

    public final static String DESCRIPTION = "Computes the anti-consensus of trees";

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionMaxRank", "optionRequireSameSource", "optionMaxRankOnly");
    }

    @Override
    public String getCitation() {
        return "Huson, Steel et al, 2018;D.H. Huson, M.A. Steel and ???. Anti-consensus: manuscript in preparation";
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
        System.err.println("Computing anti-consensus network...");
        // 0. compute all splits:
        final SplitsBlock allSplits = new SplitsBlock();
        {
            progress.setTasks("Anti-consensus", "Determining all splits");
            final ConsensusNetwork consensusNetwork = new ConsensusNetwork();
            consensusNetwork.setOptionThresholdPercent(0);
            consensusNetwork.setOptionEdgeWeights(ConsensusNetwork.EdgeWeights.TreeSizeWeightedMean);
            consensusNetwork.compute(progress, taxaBlock, treesBlock, allSplits);
            System.err.println("Input splits: " + allSplits.size());
        }

        // 1. compute the majority consensus splits and tree
        final SplitsBlock consensusSplits = new SplitsBlock();
        {
            progress.setTasks("Anti-consensus", "Determining consensus tree splits");
            final ConsensusTreeSplits consensusTreeSplits = new ConsensusTreeSplits();
            consensusTreeSplits.setOptionConsensus(ConsensusTreeSplits.Consensus.Majority); // todo: implement and use loose consensus
            consensusTreeSplits.setOptionEdgeWeights(ConsensusNetwork.EdgeWeights.TreeSizeWeightedMean);
            consensusTreeSplits.compute(progress, taxaBlock, treesBlock, consensusSplits);
            System.err.println("Consensus tree splits: " + consensusSplits.size());
        }

        final PhyloTree consensusTree;
        {
            final TreesBlock trees = new TreesBlock();
            final GreedyTree greedyTree = new GreedyTree();
            greedyTree.compute(progress, taxaBlock, consensusSplits, trees);
            consensusTree = trees.getTree(1);
        }

        double totalWeightNonTrivialSplits = 0;
        {
            for (ASplit split : consensusSplits.getSplits()) {
                if (split.size() > 1)
                    totalWeightNonTrivialSplits += split.getWeight();
            }
        }

        final SplitsBlock otherSplits = new SplitsBlock();
        otherSplits.getSplits().setAll(allSplits.getSplits());
        otherSplits.getSplits().removeAll(consensusSplits.getSplits());
        //System.err.println("Difference: " + otherSplits.size());

        splitsBlock.getSplits().setAll(consensusSplits.getSplits()); // add all consensus splits

        final SplitsBlock addSplits = new SplitsBlock();
        final Map<ASplit, BitSet> split2incompatibilities = new HashMap<>();

        for (ASplit split : otherSplits.getSplits()) {
            split2incompatibilities.put(split, new BitSet());

            final double percentageIncompatiblityWeight = 100.0 * computeIncompatibleWeight(split, consensusSplits, split2incompatibilities.get(split)) / totalWeightNonTrivialSplits;
            if (percentageIncompatiblityWeight > 10) { // must span at least 10%
                final int distortion = Distortion.computeDistortionForSplit(consensusTree, split.getA(), split.getB());
                if (distortion == 1) {
                    addSplits.getSplits().add(split);
                    split.setConfidence(percentageIncompatiblityWeight);
                }
            }
        }
        System.err.println("Total splits with distortion one and sufficient weight: " + addSplits.size());

        if (addSplits.size() > 0) {
            progress.setTasks("Anti-consensus", "Mapping back to trees");

            final ArrayList<Pair<Integer, BitSet>> tree2AddedSplits = new ArrayList<>();
            final Map<ASplit, BitSet> addedSplit2Trees = new HashMap<>();

            mapSplitsToTrees(progress, taxaBlock, addSplits, treesBlock, tree2AddedSplits, addedSplit2Trees);

            System.err.println("Splits: " + addSplits.size());

            progress.setTasks("Anti-consensus", "Computing domination DAG");
            final Map<ASplit, Node> split2nodeInDominationGraph = new HashMap<>();
            final Graph dominationDAG = computeDominationDAG(addSplits, split2incompatibilities, split2nodeInDominationGraph);

            if (isOptionRequireSameSource()) {
                for (Edge e : dominationDAG.edges()) {
                    final ASplit split1 = addSplits.get((Integer) e.getSource().getInfo());
                    final ASplit split2 = addSplits.get((Integer) e.getTarget().getInfo());
                    if (BitSetUtils.intersection(addedSplit2Trees.get(split1), addedSplit2Trees.get(split2)).cardinality() == 0)
                        dominationDAG.deleteEdge(e);
                }
            }

            final ArrayList<Pair<Integer, Double>> weightAndSplitId = new ArrayList<>();
            for (int s = 1; s <= addSplits.getNsplits(); s++) {
                weightAndSplitId.add(new Pair<>(s, addSplits.get(s).getConfidence()));
            }
            weightAndSplitId.sort((o1, o2) -> {
                int comp = -o1.getSecond().compareTo(o2.getSecond());
                if (comp == 0)
                    comp = -o1.getFirst().compareTo(o2.getFirst());
                return comp;
            });

            final Map<ASplit, Integer> split2rank = computeSplit2Rank(addSplits, weightAndSplitId, split2nodeInDominationGraph);

            for (Pair<Integer, Double> pair : weightAndSplitId) {
                final int s = pair.get1();
                final ASplit split = addSplits.get(s);
                final StringBuilder dominatesBuffer = new StringBuilder();
                final StringBuilder dominatorsBuffer = new StringBuilder();
                {
                    final Node v = split2nodeInDominationGraph.get(split);
                    if (v.getInDegree() == 0 && v.getOutDegree() > 0) {
                        for (Node w : v.children()) {
                            if (dominatesBuffer.length() > 0)
                                dominatesBuffer.append(",");
                            dominatesBuffer.append(w.getInfo());
                        }
                    }
                    if (dominatesBuffer.length() == 0)
                        dominatesBuffer.append("-");
                    if (v.getInDegree() > 0) {
                        for (Node w : v.parents()) {
                            if (dominatorsBuffer.length() > 0)
                                dominatorsBuffer.append(",");
                            dominatorsBuffer.append(w.getInfo());
                        }
                    }
                    if (dominatorsBuffer.length() == 0)
                        dominatorsBuffer.append("-");
                }

                System.err.println(String.format("Split %4d  span: %.1f%% weight: %f size: %3d dominates: %s  dominators: %s  source-trees: %s  rank: %d",
                        s, pair.getSecond(), split.getWeight(), split.size(),
                        //Basic.toString(split2incompatibilities.get(addSplits.get(pair.getFirst()))),
                        dominatesBuffer.toString(), dominatorsBuffer.toString(),
                        Basic.toString(addedSplit2Trees.get(split)),
                        split2rank.get(split)));
            }

            for (ASplit split : addSplits.getSplits()) {
                if ((!isOptionMaxRankOnly() && split2rank.get(split) < getOptionMaxRank()) || split2rank.get(split) == getOptionMaxRank())
                    splitsBlock.getSplits().add(split);
            }
        }
    }


    /**
     * computes the domination graph: split s dominates t, if the incompatibilities associated with t are contained in those for s
     *
     * @param splits
     * @param split2incompatibilities
     * @return graph
     */
    private static Graph computeDominationDAG(SplitsBlock splits, Map<ASplit, BitSet> split2incompatibilities, Map<ASplit, Node> split2nodeMap) {
        final Graph graph = new PhyloGraph();

        for (int s = 1; s <= splits.getNsplits(); s++) {
            final Node v = graph.newNode();
            v.setInfo(s);
            split2nodeMap.put(splits.get(s), v);
        }
        for (int s = 1; s <= splits.getNsplits(); s++) {
            Node v = split2nodeMap.get(splits.get(s));
            final BitSet incompatibilitiesS = split2incompatibilities.get(splits.get(s));
            for (int t = 1; t <= splits.getNsplits(); t++) {
                if (s != t) {
                    final BitSet incompatibilitiesT = split2incompatibilities.get(splits.get(t));
                    if (BitSetUtils.contains(incompatibilitiesS, incompatibilitiesT) && incompatibilitiesS.cardinality() > incompatibilitiesT.cardinality())
                        graph.newEdge(v, split2nodeMap.get(splits.get(t)));
                }
            }
        }

        final Set<Edge> toDelete = new HashSet<>();
        for (Edge e : graph.edges()) {
            for (Edge f : e.getSource().outEdges()) {
                if (f != e) {
                    for (Edge g : f.getTarget().outEdges()) {
                        if (g.getTarget() == e.getTarget()) {
                            toDelete.add(e);
                            break;
                        }
                    }
                }
            }
        }
        for (Edge e : toDelete) {
            graph.deleteEdge(e);
        }
        return graph;
    }

    /**
     * computes the split to rank mapping
     *
     * @param weightAndSplitId
     * @param split2node
     * @return split to level mapping
     */
    private Map<ASplit, Integer> computeSplit2Rank(SplitsBlock splitsBlock, ArrayList<Pair<Integer, Double>> weightAndSplitId, Map<ASplit, Node> split2node) {
        final Map<ASplit, Integer> split2rank = new HashMap<>();

        int rank = 0;
        for (Pair<Integer, Double> pair : weightAndSplitId) {
            ASplit split = splitsBlock.get(pair.getFirst());
            Node v = split2node.get(split);
            if (v.getInDegree() == 0) // is not dominated
            {
                rank++;
                final Queue<Node> queue = new LinkedList<>();
                queue.add(v);
                while (queue.size() > 0) {
                    v = queue.remove();
                    split = splitsBlock.get((Integer) v.getInfo());
                    split2rank.put(split, rank);
                    for (Node w : v.children()) {
                        queue.add(w);
                    }
                }
            }
        }

        return split2rank;
    }

    /**
     * map given splits to trees that contain them
     *
     * @param progress
     * @param taxaBlock
     * @param splitsBlock
     * @param treesBlock
     * @param treeAndSplitIds
     * @throws CanceledException
     */
    public static void mapSplitsToTrees(ProgressListener progress, final TaxaBlock taxaBlock, SplitsBlock splitsBlock, TreesBlock treesBlock, ArrayList<Pair<Integer, BitSet>> treeAndSplitIds, Map<ASplit, BitSet> addedSplit2Trees) throws CanceledException {
        // compute added split to tree map:
        final ExecutorService executor = ProgramExecutorService.getInstance();

        if (treesBlock.getNTrees() == 1) System.err.println("Consensus network: only one tree specified");

        progress.setMaximum(treesBlock.size());
        progress.setProgress(0);

        final int numberOfThreads = Math.min(treesBlock.size(), 8);
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        final Single<CanceledException> exception = new Single<>();

        for (int i = 1; i <= numberOfThreads; i++) {
            final int threadNumber = i;
            executor.execute(() -> {
                try {
                    for (int treeId = threadNumber; treeId < treesBlock.size(); treeId += numberOfThreads) {
                        final Pair<Integer, BitSet> pair = new Pair<>(treeId, new BitSet());
                        final PhyloTree tree = treesBlock.getTree(treeId);
                        final List<ASplit> splits = new ArrayList<>();
                        TreesUtilities.computeSplits(taxaBlock.getTaxaSet(), tree, splits);
                        for (int splitId = 1; splitId <= splitsBlock.size(); splitId++) {
                            final ASplit split = splitsBlock.get(splitId);
                            if (splits.contains(split)) {
                                pair.getSecond().set(splitId);
                            }
                        }
                        if (pair.getSecond().cardinality() > 0) {
                            synchronized (treeAndSplitIds) {
                                treeAndSplitIds.add(pair);
                            }
                        }
                        if (threadNumber == 1) {
                            try {
                                progress.setProgress(treeId);
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

        for (Pair<Integer, BitSet> pair : treeAndSplitIds) {
            for (int s : BitSetUtils.members(pair.getSecond())) {
                final ASplit split = splitsBlock.get(s);
                BitSet trees = addedSplit2Trees.get(split);
                if (trees == null) {
                    trees = new BitSet();
                    addedSplit2Trees.put(split, trees);
                }
                trees.set(pair.get1());
            }
        }
    }

    /**
     * determine all splits that are incompatible to split0 and return their total weight
     *
     * @param split0
     * @param splits
     * @param incompatible will contain indices of incompatible splits
     * @return count
     */
    public static double computeIncompatibleWeight(ASplit split0, SplitsBlock splits, BitSet incompatible) {
        incompatible.clear();
        double weight = 0;
        for (int s = 1; s <= splits.getNsplits(); s++) {
            final ASplit other = splits.get(s);
            if (other.size() > 1 && !Compatibility.areCompatible(split0, other)) {
                weight += other.getWeight();
                incompatible.set(s);
            }
        }
        return weight;
    }

    public int getOptionMaxRank() {
        return optionMaxRank;
    }

    public void setOptionMaxRank(int optionMaxRank) {
        this.optionMaxRank = Math.max(1, optionMaxRank);
    }

    public String getShortDescriptionMaxRank() {
        return "Add all splits upto the given rank";
    }

    public boolean isOptionRequireSameSource() {
        return optionRequireSameSource;
    }

    public void setOptionRequireSameSource(boolean optionRequireSameSource) {
        this.optionRequireSameSource = optionRequireSameSource;
    }

    public String getShortDescriptionRequireSameSource() {
        return "Require splits of same rank to come from the same input tree";
    }

    public boolean isOptionMaxRankOnly() {
        return optionMaxRankOnly;
    }

    public void setOptionMaxRankOnly(boolean optionMaxRankOnly) {
        this.optionMaxRankOnly = optionMaxRankOnly;
    }

    public String getShortDescriptionMaxRankOnly() {
        return "Only add splits that have the set max-rank, not those with a better (lower) rank";
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }
}


