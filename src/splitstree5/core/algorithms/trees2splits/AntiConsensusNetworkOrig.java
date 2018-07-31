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

import jloda.fx.NotificationManager;
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
public class AntiConsensusNetworkOrig extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private int optionSinRank = 1;
    private boolean optionRequireSameSource = true;
    private boolean optionAllSinsUpToRank = false;
    private int optionMaxDistortion = 1;
    private double optionMinimumSpanPercent = 10;

    public final static String DESCRIPTION = "Computes the anti-consensus of trees";

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionSinRank", "optionRequireSameSource", "optionAllSinsUpToRank", "optionMinimumSpan", "optionMaxDistortion");
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
        boolean showTrees = false;

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
            if (showTrees) {
                System.err.println("Consensus tree:");
                TreesUtilities.changeNumbersOnLeafNodesToLabels(taxaBlock, consensusTree);
                System.err.println(consensusTree.toBracketString(true) + ";");
            }
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
            if (percentageIncompatiblityWeight > getOptionMinimumSpanPercent()) { // must span at least 10%
                final int distortion = Distortion.computeDistortionForSplit(consensusTree, split.getA(), split.getB());
                if (distortion <= getOptionMaxDistortion()) {
                    addSplits.getSplits().add(split);
                    split.setConfidence(percentageIncompatiblityWeight);
                }
            }
        }
        System.err.println(String.format("Total splits with distortion <= %d and span >= %.1f: %d", getOptionMaxDistortion(), getOptionMinimumSpanPercent(), addSplits.size()));

        if (addSplits.size() > 0) {
            progress.setTasks("Anti-consensus", "Mapping back to trees");

            final ArrayList<Pair<Integer, BitSet>> tree2AddedSplits = new ArrayList<>();
            final Map<ASplit, BitSet> addedSplit2Trees = new HashMap<>();

            mapSplitsToTrees(progress, taxaBlock, addSplits, treesBlock, tree2AddedSplits, addedSplit2Trees);

            progress.setTasks("Anti-consensus", "Computing domination DAG");
            final Graph dominationDAG = computeCoverageDAG(addSplits, split2incompatibilities);

            if (isOptionRequireSameSource()) {
                for (Edge e : dominationDAG.edges()) {
                    final ASplit split1 = addSplits.get((Integer) e.getSource().getInfo());
                    final ASplit split2 = addSplits.get((Integer) e.getTarget().getInfo());
                    if (BitSetUtils.intersection(addedSplit2Trees.get(split1), addedSplit2Trees.get(split2)).cardinality() == 0)
                        dominationDAG.deleteEdge(e);
                }
            }

            progress.setTasks("Anti-consensus", "Computing all SIN");
            ArrayList<SIN> listOfSins = new ArrayList<>();
            for (Node u : dominationDAG.nodes()) {
                if (u.getInDegree() == 0) // is a dominator
                {
                    SIN sin = null;

                    final Queue<Node> queue = new LinkedList<>();
                    queue.add(u);
                    while (queue.size() > 0) {
                        Node v = queue.remove();
                        final int splitId = (Integer) v.getInfo();
                        final ASplit split = addSplits.get(splitId);

                        if (sin == null)
                            sin = new SIN(split.getConfidence());

                        sin.add(splitId, split2incompatibilities.get(split), split.getWeight(), addedSplit2Trees.get(split), treesBlock);
                        for (Node w : v.children()) {
                            queue.add(w);
                        }
                    }

                    if (sin != null)
                        listOfSins.add(sin);
                }
            }
            listOfSins.sort(sinsComparator());
            if (getOptionSinRank() >= listOfSins.size())
                setOptionSinRank(listOfSins.size());

            for (int i = 0; i < listOfSins.size(); i++) {
                final SIN sins = listOfSins.get(i);
                sins.setRank(i + 1);
                System.err.println(sins);
                if (showTrees) {
                    System.err.println(reportTree(taxaBlock, consensusSplits, allSplits, sins.getSplits()) + ";");
                }
            }

            if (!isOptionAllSinsUpToRank()) {
                final int i = getOptionSinRank() - 1;
                final SIN sins = listOfSins.get(i);
                NotificationManager.showInformation(sins.toString());
                for (Integer s : BitSetUtils.members(sins.getSplits())) {
                    splitsBlock.getSplits().add(addSplits.get(s));
                }
            } else {
                for (int i = 0; i < getOptionSinRank(); i++) {
                    for (Integer s : BitSetUtils.members(listOfSins.get(i).getSplits())) {
                        splitsBlock.getSplits().add(addSplits.get(s));
                    }
                }
            }
        }
    }


    /**
     * computes the domination graph: split s covers t, if the incompatibilities associated with t are contained in those for s
     *
     * @param splits
     * @param split2incompatibilities
     * @return graph
     */
    private static Graph computeCoverageDAG(SplitsBlock splits, Map<ASplit, BitSet> split2incompatibilities) {
        final Graph graph = new PhyloGraph();

        final Map<ASplit, Node> split2nodeMap = new HashMap<>();

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
                    for (int treeId = threadNumber; treeId <= treesBlock.size(); treeId += numberOfThreads) {
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
                addedSplit2Trees.computeIfAbsent(split, (e) -> new BitSet()).set(pair.get1());
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

    public int getOptionSinRank() {
        return optionSinRank;
    }

    public void setOptionSinRank(int optionSinsRank) {
        this.optionSinRank = Math.max(1, optionSinsRank);
    }

    public String getShortDescriptionSinSplitsRank() {
        return "The rank of the set of strongly incompatible splits to be shown";
    }

    public boolean isOptionRequireSameSource() {
        return optionRequireSameSource;
    }

    public void setOptionRequireSameSource(boolean optionRequireSameSource) {
        this.optionRequireSameSource = optionRequireSameSource;
    }

    public String getShortDescriptionRequireSameSource() {
        return "SINs only contains splits from same source tree";
    }

    public boolean isOptionAllSinsUpToRank() {
        return optionAllSinsUpToRank;
    }

    public void setOptionAllSinsUpToRank(boolean optionAllSinsUpToRank) {
        this.optionAllSinsUpToRank = optionAllSinsUpToRank;
    }

    public String getShortDescriptionAllSinsUpToRank() {
        return "Show all SINs up to selected rank";
    }

    public int getOptionMaxDistortion() {
        return optionMaxDistortion;
    }

    public void setOptionMaxDistortion(int optionMaxDistortion) {
        this.optionMaxDistortion = Math.max(1, optionMaxDistortion);
    }

    public String getShortDescriptionMaxDistortion() {
        return "Consider only splits whose SPR distance to the consensus tree does not exceed this value";
    }

    public double getOptionMinimumSpanPercent() {
        return optionMinimumSpanPercent;
    }

    public void setOptionMinimumSpanPercent(double optionMinimumSpanPercent) {
        this.optionMinimumSpanPercent = Math.max(0, Math.min(100, optionMinimumSpanPercent));
    }

    public String getShortDescriptionMinimumSpanPercent() {
        return "Set the minimum amount of the consensus tree that an incompatible split must span";
    }


    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }

    public static Comparator<SIN> sinsComparator() {
        return (a, b) -> {
            if (a.getSpanPercent() * a.getTotalWeight() > b.getSpanPercent() * b.getTotalWeight())
                return -1;
            else if (a.getSpanPercent() * a.getTotalWeight() < b.getSpanPercent() * b.getTotalWeight())
                return 1;
            else
                return BitSetUtils.compare(a.getSplits(), b.getSplits());
        };
    }

    /**
     * reports the tree associated with a SIN
     *
     * @param taxaBlock
     * @param trivialSplitsSource
     * @param allSplits
     * @param splits
     * @return tree string
     */
    private static String reportTree(TaxaBlock taxaBlock, SplitsBlock trivialSplitsSource, SplitsBlock allSplits, BitSet splits) {
        final SplitsBlock splitsBlock = new SplitsBlock();
        // add all trivial splits:
        for (ASplit split : trivialSplitsSource.getSplits()) {
            if (split.size() == 1)
                splitsBlock.getSplits().add(split);
        }
        // add other splits:
        for (Integer s : BitSetUtils.members(splits)) {
            splitsBlock.getSplits().add(allSplits.get(s));
        }
        // compute tree:
        final TreesBlock trees = new TreesBlock();
        final GreedyTree greedyTree = new GreedyTree();
        try {
            greedyTree.compute(new ProgressSilent(), taxaBlock, splitsBlock, trees);
        } catch (CanceledException e) {
            Basic.caught(e); // can't happen
        }
        PhyloTree tree = trees.getTree(1);
        TreesUtilities.changeNumbersOnLeafNodesToLabels(taxaBlock, tree);
        return tree.toBracketString(true);
    }


    public class SIN { // set of strongly incompatible splits (sins)
        private final BitSet splits = new BitSet();
        private final BitSet incompatibleConsensusSplits = new BitSet();
        private double spanPercent = 0;
        private double totalWeight = 0;
        private final BitSet sourceTrees = new BitSet();
        private int rank;
        private TreesBlock treesBlock;

        public SIN(double spanPercent) {
            this.spanPercent = spanPercent;
        }

        public void add(int splitId, BitSet incompatibleConsensusSplits, double weight, BitSet trees, TreesBlock treesBlock) {
            splits.set(splitId);
            this.incompatibleConsensusSplits.or(incompatibleConsensusSplits);
            this.totalWeight += weight;
            if (BitSetUtils.intersection(sourceTrees, trees).cardinality() == 0)
                sourceTrees.or(trees);
            this.treesBlock = treesBlock;
        }

        public BitSet getSplits() {
            return splits;
        }

        public BitSet getIncompatibleConsensusSplits() {
            return incompatibleConsensusSplits;
        }

        public double getSpanPercent() {
            return spanPercent;
        }

        public double getTotalWeight() {
            return totalWeight;
        }

        public BitSet getSourceTrees() {
            return sourceTrees;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }


        public String toString() {
            final StringBuilder buf = new StringBuilder(String.format("SIN rank: %d splits: %s incompatibility: %f span: %f weight: %f trees:", getRank(), Basic.toString(splits, " "), spanPercent * totalWeight, spanPercent, totalWeight));
            for (Integer t : BitSetUtils.members(sourceTrees)) {
                buf.append(" ").append(t).append(" (").append(treesBlock.getTree(t).getName()).append(")");
            }
            return buf.toString();
        }
    }
}


