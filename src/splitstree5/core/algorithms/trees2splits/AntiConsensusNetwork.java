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
    private float optionIncompatibilityWeightPercent = 10; // percent of non-trivial incompatibilities that a split should have to be considered for the anti-consensus

    private ConsensusNetwork.EdgeWeights optionEdgeWeights = ConsensusNetwork.EdgeWeights.TreeSizeWeightedMean;

    private int optionSourceTree = 0;

    public final static String DESCRIPTION = "Computes the anti-consensus of trees";

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionIncompatibilitiesPercent", "optionEdgeWeights", "optionTree");
    }

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
        System.err.println("Computing anti-consensus network...");
        // 0. compute all splits:
        final SplitsBlock allSplits = new SplitsBlock();
        {
            progress.setTasks("Anti-consensus", "Determining all splits");
            final ConsensusNetwork consensusNetwork = new ConsensusNetwork();
            consensusNetwork.setOptionThresholdPercent(0);
            consensusNetwork.setOptionEdgeWeights(optionEdgeWeights);
            consensusNetwork.compute(progress, taxaBlock, treesBlock, allSplits);
            System.err.println("All splits: " + allSplits.size());
        }

        // 1. compute the majority consensus splits and tree
        final SplitsBlock consensusSplits = new SplitsBlock();
        {
            progress.setTasks("Anti-consensus", "Determining consensus tree splits");
            final ConsensusTreeSplits consensusTreeSplits = new ConsensusTreeSplits();
            consensusTreeSplits.setOptionConsensus(ConsensusTreeSplits.Consensus.Majority); // todo: implement and use loose consensus
            consensusTreeSplits.setOptionEdgeWeights(optionEdgeWeights);
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

        // 2. compute the result:
        final SplitsBlock otherSplits = new SplitsBlock();
        otherSplits.getSplits().setAll(allSplits.getSplits());
        otherSplits.getSplits().removeAll(consensusSplits.getSplits());
        System.err.println("Difference: " + otherSplits.size());

        splitsBlock.getSplits().setAll(consensusSplits.getSplits()); // add all consensus splits
        final SplitsBlock addSplits = new SplitsBlock();
        final Map<ASplit, BitSet> split2incompatibilities = new HashMap<>();
        for (ASplit split : otherSplits.getSplits()) {
            split2incompatibilities.put(split, new BitSet());

            final double percentageIncompatiblityWeight = 100.0 * computeIncompatibleWeight(split, consensusSplits, split2incompatibilities.get(split)) / totalWeightNonTrivialSplits;
            if (percentageIncompatiblityWeight >= getOptionIncompatibilityWeightPercent()) {
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

            mapSplitsToTrees(progress, taxaBlock, addSplits, treesBlock, tree2AddedSplits);

            if (getOptionSourceTree() > 0) {
                System.err.println("Sourcing splits from tree " + getOptionSourceTree() + ":");
                boolean found = false;
                for (Pair<Integer, BitSet> pair : tree2AddedSplits) {
                    if (pair.get1() == getOptionSourceTree()) {
                        final SplitsBlock toKeep = new SplitsBlock();
                        for (Integer s : BitSetUtils.members(pair.get2())) {
                            toKeep.getSplits().add(addSplits.get(s));
                        }
                        addSplits.getSplits().setAll(toKeep.getSplits());
                        pair.get2().clear();
                        pair.get2().set(1, addSplits.getNsplits() + 1);
                        tree2AddedSplits.clear();
                        tree2AddedSplits.add(pair);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tree2AddedSplits.clear();
                    addSplits.clear();
                }
            }
            System.err.println("Splits: " + addSplits.size());

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
            for (Pair<Integer, Double> pair : weightAndSplitId) {
                System.err.println(String.format("Split %d weight: %.1f (incomp-tree-splits %s)", pair.getFirst(), pair.getSecond(), Basic.toString(split2incompatibilities.get(addSplits.get(pair.getFirst())))));
            }

            progress.setTasks("Anti-consensus", "Computing domination DAG");
            final PhyloGraph dominationDAG = computeDominationDAG(addSplits, split2incompatibilities);
            if (dominationDAG.getNumberOfEdges() > 0) {
                for (Node v : dominationDAG.nodes()) {
                    if (v.getInDegree() == 0 && v.getOutDegree() > 0) {
                        System.err.print("Split " + dominationDAG.getLabel(v) + " dominates:");
                        for (Node w : v.children()) {
                            System.err.print(" " + dominationDAG.getLabel(w));
                        }
                        System.err.println();
                    }
                }
            }

            // sort by decreasing number of splits from given tree
            tree2AddedSplits.sort((o1, o2) -> {
                final BitSet splits1 = o1.getSecond();
                final BitSet splits2 = o2.getSecond();

                if (splits1.cardinality() > splits2.cardinality())
                    return -1;
                else if (splits1.cardinality() < splits2.cardinality())
                    return 1;

                if (o1.getFirst() < o2.getFirst())
                    return -1;
                else if (o1.getFirst() > o2.getFirst())
                    return 1;
                else
                    return 0;
            });

            for (Pair<Integer, BitSet> pair : tree2AddedSplits) {
                System.err.println(String.format("tree [%d] %s source of added splits: %s", pair.get1(),
                        treesBlock.getTree(pair.get1()).getName(), Basic.toString(pair.getSecond(), ", ")));

            }
        }

        splitsBlock.getSplits().addAll(addSplits.getSplits());
    }

    /**
     * computes the domination graph: split s dominates t, if the incompatibilities associated with t are contained in those for s
     *
     * @param splits
     * @param split2incompatibilities
     * @return graph
     */
    private PhyloGraph computeDominationDAG(SplitsBlock splits, Map<ASplit, BitSet> split2incompatibilities) {
        final PhyloGraph graph = new PhyloGraph();

        final Node[] split2node = new Node[splits.getNsplits() + 1];
        for (int s = 1; s <= splits.getNsplits(); s++) {
            Node v = graph.newNode();
            split2node[s] = v;
            graph.setLabel(v, "" + s);
        }
        for (int s = 1; s <= splits.getNsplits(); s++) {
            Node v = split2node[s];
            final BitSet incompatibilitiesS = split2incompatibilities.get(splits.get(s));
            for (int t = 1; t <= splits.getNsplits(); t++) {
                if (s != t) {
                    final BitSet incompatibilitiesT = split2incompatibilities.get(splits.get(t));
                    if (BitSetUtils.contains(incompatibilitiesS, incompatibilitiesT) && incompatibilitiesS.cardinality() > incompatibilitiesT.cardinality())
                        graph.newEdge(v, split2node[t]);
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
     * map given splits to trees that contain them
     *
     * @param progress
     * @param taxaBlock
     * @param splitsBlock
     * @param treesBlock
     * @param treeAndSplitIds
     * @throws CanceledException
     */
    public static void mapSplitsToTrees(ProgressListener progress, final TaxaBlock taxaBlock, SplitsBlock splitsBlock, TreesBlock treesBlock, ArrayList<Pair<Integer, BitSet>> treeAndSplitIds) throws CanceledException {
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

    public float getOptionIncompatibilityWeightPercent() {
        return optionIncompatibilityWeightPercent;
    }

    public void setOptionIncompatibilityWeightPercent(float optionIncompatibilityWeightPercent) {
        this.optionIncompatibilityWeightPercent = optionIncompatibilityWeightPercent;
    }

    public ConsensusNetwork.EdgeWeights getOptionEdgeWeights() {
        return optionEdgeWeights;
    }

    public void setOptionEdgeWeights(ConsensusNetwork.EdgeWeights optionEdgeWeights) {
        this.optionEdgeWeights = optionEdgeWeights;
    }

    public int getOptionSourceTree() {
        return optionSourceTree;
    }

    public void setOptionSourceTree(int optionSourceTree) {
        this.optionSourceTree = optionSourceTree;
    }

    public String getShortDescriptionSourceTree() {
        return "The index of the tree from which incompatible splits are considered, 0 means use all trees";
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }
}


