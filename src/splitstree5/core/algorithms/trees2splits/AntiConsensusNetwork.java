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

/**
 * implements the anti-consensus method
 *
 * @author Daniel Huson, July 2018
 */
public class AntiConsensusNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private int optionSinRank = 1;
    private boolean optionAllSinsUpToRank = false;
    private int optionMaxDistortion = 1;
    private double optionMinimumSpanPercent = 10;

    public final static String DESCRIPTION = "Computes the anti-consensus of trees";

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionSinRank", "optionAllSinsUpToRank", "optionMinimumSpan", "optionMaxDistortion");
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
        // 1. compute the majority consensus splits and tree
        final SplitsBlock consensusSplits = new SplitsBlock();
        {
            progress.setTasks("Anti-consensus", "Determining consensus tree splits");
            final ConsensusTreeSplits consensusTreeSplits = new ConsensusTreeSplits();
            consensusTreeSplits.setOptionConsensus(ConsensusTreeSplits.Consensus.Majority); // todo: implement and use loose consensus
            consensusTreeSplits.setOptionEdgeWeights(ConsensusNetwork.EdgeWeights.TreeSizeWeightedMean);
            consensusTreeSplits.compute(progress, taxaBlock, treesBlock, consensusSplits);

            // normalize weights
            {
                double totalWeight = 0;
                for (ASplit split : consensusSplits.getSplits())
                    totalWeight += split.getWeight();
                if (totalWeight > 0) {
                    for (ASplit split : consensusSplits.getSplits())
                        split.setWeight(split.getWeight() / totalWeight);
                }
            }


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

        // consider each tree in turn:
        progress.setTasks("Anti-consensus", "Comparing majority tree with gene trees");
        progress.setMaximum(treesBlock.getNTrees());

        ArrayList<SIN> listOfSins = new ArrayList<>();

        for (int t = 1; t <= treesBlock.getNTrees(); t++) {
            final PhyloTree tree = treesBlock.getTree(t);
            final ArrayList<ASplit> splits = new ArrayList<>(tree.getNumberOfEdges());
            TreesUtilities.computeSplits(taxaBlock.getTaxaSet(), tree, splits);
            // normalize weights:
            {
                double totalWeight = 0;
                for (ASplit split : splits)
                    totalWeight += split.getWeight();
                if (totalWeight > 0) {
                    for (ASplit split : splits)
                        split.setWeight(split.getWeight() / totalWeight);
                }
            }

            final Collection<ASplit> splitsWithDistortion = new ArrayList<>(tree.getNumberOfEdges());
            final Map<ASplit, BitSet> split2incompatibilities = new HashMap<>();

            for (ASplit split : splits) {
                split2incompatibilities.put(split, new BitSet());
                int distortion = Distortion.computeDistortionForSplit(consensusTree, split.getA(), split.getB());
                if (distortion > 0 && distortion <= getOptionMaxDistortion()) {
                    final double incompatiblitySpanPercent = 100.0 * computeIncompatibleWeight(split, consensusSplits, split2incompatibilities.get(split)) / totalWeightNonTrivialSplits;
                    // previous line also computes split2incompatibilities mapping
                    split.setConfidence(incompatiblitySpanPercent);
                    splitsWithDistortion.add(split);
                }
            }
            final Graph coverageGraph = computeCoverageDAG(splitsWithDistortion, split2incompatibilities);

            for (Node u : coverageGraph.nodes()) {
                if (u.getInDegree() == 0) // is not covered by any other split
                {
                    final ASplit coveringSplit = (ASplit) u.getInfo();
                    if (coveringSplit.getConfidence() >= getOptionMinimumSpanPercent()) {
                        final SIN sin = new SIN(t, treesBlock.getTree(t).getName(), coveringSplit.getConfidence());
                        sin.add(coveringSplit);
                        final Queue<Node> queue = new LinkedList<>();
                        for (Node w : u.children()) {
                            queue.add(w);
                        }
                        while (queue.size() > 0) {
                            Node v = queue.remove();
                            final ASplit split = (ASplit) v.getInfo();

                            sin.add(split);
                            for (Node w : v.children()) {
                                queue.add(w);
                            }
                        }
                        listOfSins.add(sin);
                    }
                }
            }
            progress.setProgress(t);
        }

        listOfSins.sort(sinsComparator());
        if (getOptionSinRank() >= listOfSins.size())
            setOptionSinRank(listOfSins.size());

        for (int i = 0; i < listOfSins.size(); i++) {
            final SIN sins = listOfSins.get(i);
            sins.setRank(i + 1);
            System.err.println(sins);
            if (showTrees) {
                System.err.println(reportTree(taxaBlock, consensusSplits, sins.getSplits()) + ";");
            }
        }

        splitsBlock.getSplits().addAll(consensusSplits.getSplits());
        if (!isOptionAllSinsUpToRank()) {
            final int i = getOptionSinRank() - 1;
            final SIN sins = listOfSins.get(i);
            NotificationManager.showInformation(sins.toString());
            splitsBlock.getSplits().addAll(sins.getSplits());
        } else {
            for (int i = 0; i < getOptionSinRank(); i++) {
                final SIN sins = listOfSins.get(i);
                splitsBlock.getSplits().addAll(sins.getSplits());
            }
            NotificationManager.showInformation("Computed anti-consensus using top " + getOptionSinRank() + " SINs");
        }
    }


    /**
     * computes the domination graph: split s covers t, if the incompatibilities associated with t are contained in those for s
     * If s and t have the same set of incompatibilities, then additionally require that s is lexicographically smaller
     *
     * @param splits
     * @param split2incompatibilities
     * @return graph
     */
    private static Graph computeCoverageDAG(Collection<ASplit> splits, Map<ASplit, BitSet> split2incompatibilities) {
        final Graph graph = new PhyloGraph();

        final Map<ASplit, Node> split2nodeMap = new HashMap<>();

        for (ASplit split : splits) {
            final Node v = graph.newNode();
            v.setInfo(split);
            split2nodeMap.put(split, v);
        }
        for (ASplit split1 : splits) {
            final Node v = split2nodeMap.get(split1);
            final BitSet incompatibilities1 = split2incompatibilities.get(split1);
            for (ASplit split2 : splits) {
                if (!split1.equals(split2)) {
                    final BitSet incompatibilities2 = split2incompatibilities.get(split2);
                    if (BitSetUtils.contains(incompatibilities1, incompatibilities2) &&
                            ((incompatibilities1.cardinality() > incompatibilities2.cardinality())
                                    || (incompatibilities1.cardinality() == incompatibilities2.cardinality() && ASplit.compare(split1, split2) == -1)))
                        graph.newEdge(v, split2nodeMap.get(split2));
                }
            }
        }

        // transitive reduction:
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
                return -Integer.compare(a.getSplits().size(), b.getSplits().size());
        };
    }

    /**
     * reports the tree associated with a SIN
     *
     * @param taxaBlock
     * @param trivialSplitsSource
     * @param sinSplits
     * @param sinSplits
     * @return tree string
     */
    private static String reportTree(TaxaBlock taxaBlock, SplitsBlock trivialSplitsSource, Collection<ASplit> sinSplits) {
        final SplitsBlock splitsBlock = new SplitsBlock();
        // add all trivial splits:
        for (ASplit split : trivialSplitsSource.getSplits()) {
            if (split.size() == 1)
                splitsBlock.getSplits().add(split);
        }
        // add other splits:
        splitsBlock.getSplits().addAll(sinSplits);

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

    /**
     * set of strongly incompatible (SIN) splits
     */
    public class SIN {
        private final int treeId;
        private final String treeName;
        private final double spanPercent;


        private final ArrayList<ASplit> splits = new ArrayList<>();
        private double totalWeight = 0;
        private int rank;

        public SIN(int treeId, String treeName, double spanPercent) {
            this.treeId = treeId;
            this.treeName = treeName;
            this.spanPercent = spanPercent;
        }

        public void add(ASplit split) {
            this.splits.add(split);
            this.totalWeight += split.getWeight();
        }

        public Collection<ASplit> getSplits() {
            return splits;
        }

        public double getSpanPercent() {
            return spanPercent;
        }

        public double getTotalWeight() {
            return totalWeight;
        }

        public int getTree() {
            return treeId;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String toString() {
            return String.format("SIN rank: %d, incompatibility: %f, span: %f, weight: %f,  splits: %d, tree: %d (%s)",
                    getRank(), spanPercent * totalWeight, spanPercent, totalWeight, splits.size(), treeId, treeName);
        }
    }
}


