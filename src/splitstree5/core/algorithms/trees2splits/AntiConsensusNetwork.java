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
import jloda.graph.NodeSet;
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
    private double optionMinSpanPercent = 10;

    private int optionMaxDistortion = 1;

    private double optionMinWeight = 0.00001;

    public final static String DESCRIPTION = "Computes the anti-consensus of trees";

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionSinRank", "optionAllSinsUpToRank", "optionMaxDistortion", "optionMinIncompatPercent", "optionMinWeightPercent");
    }

    @Override
    public String getCitation() {
        return "Huson, Steel et al, 2018;D.H. Huson, M.A. Steel and [???]. Anti-consensus: manuscript in preparation";
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
                final int distortion = Distortion.computeDistortionForSplit(consensusTree, split.getA(), split.getB());
                if (distortion > 0 && distortion <= getOptionMaxDistortion()) {
                    final double incompatiblitySpanPercent = (100.0 * computeIncompatibleWeight(split, consensusSplits, split2incompatibilities.get(split))) / totalWeightNonTrivialSplits;
                    // previous line also computes split2incompatibilities mapping
                    split.setConfidence(incompatiblitySpanPercent);
                    splitsWithDistortion.add(split);
                }
            }
            final Graph coverageGraph = computeCoverageDAG(splitsWithDistortion, split2incompatibilities);

            if (getOptionMaxDistortion() == 1) {
                for (Node u : coverageGraph.nodes()) {
                    if (u.getInDegree() == 0) // is not covered by any other split
                    {
                        final ASplit splitU = (ASplit) u.getInfo();
                        if (splitU.getConfidence() >= getOptionMinSpanPercent()) {
                            final SIN sin = new SIN(t, treesBlock.getTree(t).getName(), splitU.getConfidence(), 1);
                            sin.add(splitU);
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
                            if (sin.getTotalWeight() >= 0.01 * getOptionMinWeight())
                                listOfSins.add(sin);
                        }
                    }
                }
            } else if (getOptionMaxDistortion() > 1) {
                ArrayList<Pair<SIN, Node>> consideredSins = new ArrayList<>();
                for (Node u : coverageGraph.nodes()) {
                    if (u.getInDegree() == 0) // is not covered by any other split
                    {
                        final ASplit splitU = (ASplit) u.getInfo();
                        if (splitU.getConfidence() >= getOptionMinSpanPercent()) {
                            final int distortion = Distortion.computeDistortionForSplit(consensusTree, splitU.getA(), splitU.getB());

                            final SIN sin = new SIN(t, treesBlock.getTree(t).getName(), splitU.getConfidence(), distortion);
                            sin.add(splitU);
                            final Queue<Node> queue = new LinkedList<>();
                            for (Node w : u.children()) {
                                queue.add(w);
                            }
                            while (queue.size() > 0) {
                                Node v = queue.remove();
                                final ASplit splitV = (ASplit) v.getInfo();

                                sin.add(splitV);
                                for (Node w : v.children()) {
                                    queue.add(w);
                                }
                            }
                            if (sin.getTotalWeight() >= 0.01 * getOptionMinWeight()) {
                                ArrayList<Pair<SIN, Node>> toDelete = new ArrayList<>();
                                boolean ok = true;
                                for (Pair<SIN, Node> pair : consideredSins) {
                                    final SIN other = pair.get1();
                                    final Node otherNode = pair.get2();
                                    if (isBetter(sin, u, other, otherNode))
                                        toDelete.add(pair);
                                    else if (isBetter(other, otherNode, sin, u))
                                        ok = false;
                                }
                                consideredSins.removeAll(toDelete);
                                if (ok)
                                    consideredSins.add(new Pair<>(sin, u));
                            }
                        }
                    }
                }
                for (Pair<SIN, Node> pair : consideredSins) {
                    listOfSins.add(pair.get1());
                }
            } else throw new IllegalArgumentException("max distortion must be at least 1");


            progress.setProgress(t);
        }

        listOfSins.sort(sinsComparator());
        if (getOptionSinRank() >= listOfSins.size())
            setOptionSinRank(listOfSins.size());

        for (int i = 0; i < listOfSins.size(); i++) {
            final SIN sins = listOfSins.get(i);
            sins.setRank(i + 1);
            System.out.println(sins);
            if (showTrees) {
                System.err.println(reportTree(taxaBlock, consensusSplits, sins.getSplits()) + ";");
            }
        }


        splitsBlock.getSplits().addAll(consensusSplits.getSplits());

        if (listOfSins.size() == 0) {
            NotificationManager.showInformation("No SINs found");
        } else if (!isOptionAllSinsUpToRank()) {
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
     * is given sin better than other? That is, does sin have a bigger score and overlap with other
     *
     * @param sin
     * @param other
     * @return true, if better score and overlaps with other
     */
    private boolean isBetter(SIN sin, Node u, SIN other, Node otherNode) {
        return (sin.getSpanPercent() > other.getSpanPercent() || (sin.getSpanPercent() == other.getSpanPercent() && sin.getTotalWeight() > other.getTotalWeight()))
                && SetUtils.intersection(allBelow(u), allBelow(otherNode)).iterator().hasNext();
    }


    /**
     * get all below
     *
     * @param v
     * @return all below
     */
    private NodeSet allBelow(Node v) {
        final NodeSet result = new NodeSet(v.getOwner());
        final Queue<Node> queue = new LinkedList<>();
        queue.add(v);
        while (queue.size() > 0) {
            v = queue.poll();
            result.add(v);
            for (Node w : v.children()) {
                if (!result.contains(w)) // not necessary in a DAG, but let's play it safe here...
                    queue.add(w);
            }
        }
        return result;
    }


    /**
     * computes the coverage graph: split s covers t, if the incompatibilities associated with t are contained in those for s
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


    public boolean isOptionAllSinsUpToRank() {
        return optionAllSinsUpToRank;
    }

    public void setOptionAllSinsUpToRank(boolean optionAllSinsUpToRank) {
        this.optionAllSinsUpToRank = optionAllSinsUpToRank;
    }

    public String getShortDescriptionAllSinsUpToRank() {
        return "Show all SINs up to selected rank";
    }

    public double getOptionMinSpanPercent() {
        return optionMinSpanPercent;
    }

    public void setOptionMinSpanPercent(double optionMinSpanPercent) {
        this.optionMinSpanPercent = Math.max(0, Math.min(100, optionMinSpanPercent));
    }

    public String getShortDescriptionMinSpanPercent() {
        return "Set the minimum amount of the consensus tree that an incompatible split must span";
    }

    public int getOptionMaxDistortion() {
        return optionMaxDistortion;
    }

    public void setOptionMaxDistortion(int optionMaxDistortion) {
        this.optionMaxDistortion = Math.max(1, optionMaxDistortion);
    }

    public String getShortDescriptionMaxDistortion() {
        return "Set the max-distortion. Uses the single-event heuristic, when set to 1, else the multi-event heuristic\n(see the paper for details)";
    }

    public double getOptionMinWeight() {
        return optionMinWeight;
    }

    public void setOptionMinWeight(double optionMinWeight) {
        this.optionMinWeight = optionMinWeight;
    }

    public String getShortDescriptionMinWeight() {
        return "Set the minimum weight for a SIN to be reported";
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
    }

    public static Comparator<SIN> sinsComparator() {
        return (a, b) -> {
            if (a.getSpanPercent() > b.getSpanPercent())
                return -1;
            else if (a.getSpanPercent() < b.getSpanPercent())
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
        private final int distortion;


        private final ArrayList<ASplit> splits = new ArrayList<>();
        private double totalWeight = 0;
        private int rank;

        public SIN(int treeId, String treeName, double spanPercent, int distortion) {
            this.treeId = treeId;
            this.treeName = treeName;
            this.spanPercent = spanPercent;
            this.distortion = distortion;
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
            return String.format("SIN rank: %d, incompatib.: %f, weight: %f,  distortion: %d, splits: %d, tree: %d (%s)",
                    getRank(), spanPercent, totalWeight, distortion, splits.size(), treeId, treeName);
        }
    }
}

