/*
 *  AntiConsensusNetwork.java Copyright (C) 2019 Daniel H. Huson
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

import javafx.beans.property.*;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.window.NotificationManager;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * implements the anti-consensus method
 *
 * @author Daniel Huson, July 2018
 */
public class AntiConsensusNetwork extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    public enum Reference {MajorityConsensus, FirstInputTree, LastInputTree}

    private final ObjectProperty<Reference> optionReferenceTree = new SimpleObjectProperty<>(Reference.MajorityConsensus);

    private final IntegerProperty optionSinRank = new SimpleIntegerProperty(1);
    private final BooleanProperty optionAllSinsUpToRank = new SimpleBooleanProperty(false);
    private final DoubleProperty optionMinSpanPercent = new SimpleDoubleProperty(1);

    private final IntegerProperty optionMaxDistortion = new SimpleIntegerProperty(1);

    private final BooleanProperty optionRequireSingleSPR = new SimpleBooleanProperty(false);

    private final DoubleProperty optionMinWeight = new SimpleDoubleProperty(0.00001);

    private final BooleanProperty optionOnePerTree = new SimpleBooleanProperty(false);

    @Override
    public String getCitation() {
        return "Huson et al. 2019;D.H. Huson, B. Albrecht, P. Lockhart and M.A. Steel. Anti-consensus: manuscript in preparation";
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("SinRank", "AllSinsUpToRank", "MaxDistortion", "RequireSingleSPR", "MinSpanPercent", "MinWeight", "ReferenceTree", "MultipleSINsPerTree");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "SinSplitsRank":
                return "The rank of the set of strongly incompatible splits to be shown";
            case "SinsUpToRank":
                return "Show all SINs up to selected rank";
            case "MinSpanPercent":
                return "Set the minimum amount of the consensus tree that an incompatible split must span";
            case "MaxDistortion":
                return "Set the max-distortion. Uses the single-event heuristic, when set to 1, else the multi-event heuristic\n(see the paper for details)";
            case "MinWeight":
                return "Set the minimum weight for a SIN to be reported";
            case "ReferenceTree":
                return "By default, uses the majority consensus as the reference 'species' tree. Alternatively, the first or last input tree can be used";
            case "RequireSingleSPR":
                return "For distortion=1, require that all members of the SIN are reconciled using the same SPR";
        }
        return optionName;
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

        // 1. compute the references splits and tree
        final SplitsBlock referenceSplits = new SplitsBlock();

        final int firstTreeToUse;
        final int lastTreeToUse;

        switch (getOptionReferenceTree()) {
            default:
            case MajorityConsensus: {
                progress.setTasks("Anti-consensus", "Determining majority consensus splits");
                final ConsensusTreeSplits consensusTreeSplits = new ConsensusTreeSplits();
                consensusTreeSplits.setOptionConsensus(ConsensusTreeSplits.Consensus.Majority); // todo: implement and use loose consensus
                consensusTreeSplits.setOptionEdgeWeights(ConsensusNetwork.EdgeWeights.TreeSizeWeightedMean);
                consensusTreeSplits.compute(progress, taxaBlock, treesBlock, referenceSplits);
                firstTreeToUse = 1;
                lastTreeToUse = treesBlock.getNTrees();
                break;
            }
            case FirstInputTree: {
                progress.setTasks("Anti-consensus", "Extracting splits from first tree");
                final TreeSelectorSplits treeSelector = new TreeSelectorSplits();
                treeSelector.setOptionWhich(1);
                treeSelector.compute(progress, taxaBlock, treesBlock, referenceSplits);
                firstTreeToUse = 2;
                lastTreeToUse = treesBlock.getNTrees();
                break;
            }
            case LastInputTree: {
                progress.setTasks("Anti-consensus", "Extracting splits from last tree");
                final TreeSelectorSplits treeSelector = new TreeSelectorSplits();
                treeSelector.setOptionWhich(treesBlock.getNTrees());
                treeSelector.compute(progress, taxaBlock, treesBlock, referenceSplits);
                firstTreeToUse = 1;
                lastTreeToUse = treesBlock.getNTrees() - 1;
                break;
            }
        }

        // normalize weights
        {
            double totalWeight = 0;
            for (ASplit split : referenceSplits.getSplits())
                totalWeight += split.getWeight();
            if (totalWeight > 0) {
                for (ASplit split : referenceSplits.getSplits())
                    split.setWeight(split.getWeight() / totalWeight);
            }
        }

        System.err.println("Reference tree splits: " + referenceSplits.size());

        final PhyloTree referenceTree;
        {
            final TreesBlock trees = new TreesBlock();
            final GreedyTree greedyTree = new GreedyTree();
            greedyTree.compute(progress, taxaBlock, referenceSplits, trees);
            referenceTree = trees.getTree(1);
            if (showTrees) {
                System.err.println("Consensus tree:");
                TreesUtilities.changeNumbersOnLeafNodesToLabels(taxaBlock, referenceTree);
                System.err.println(referenceTree.toBracketString(true) + ";");
            }
        }

        // 2. consider each tree in turn:
        progress.setTasks("Anti-consensus", "Comparing majority tree with gene trees");
        progress.setMaximum(lastTreeToUse - firstTreeToUse + 1);
        progress.setProgress(0);
        final ArrayList<SIN> listOfSins = new ArrayList<>();

        final ExecutorService executor = ProgramExecutorService.getInstance();
        final int numberOfThreads = Math.max(1, Basic.min(lastTreeToUse - firstTreeToUse + 1,
                ProgramExecutorService.getNumberOfCoresToUse(), Runtime.getRuntime().availableProcessors()));
        final CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        final Single<Exception> exception = new Single<>();

        for (int thread = 0; thread < numberOfThreads; thread++) {
            final int offset = thread;

            executor.submit(() -> {
                try {
                    for (int t = firstTreeToUse + offset; t <= lastTreeToUse; t += numberOfThreads) {
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
                        final Map<BitSet, BitSet> splitSet2Incompatibilities = new HashMap<>();

                        for (ASplit split : splits) {
                            final int distortion = Distortion.computeDistortionForSplit(referenceTree, split.getA(), split.getB());

                            final BitSet incompatibilities = new BitSet();
                            final double incompatiblitySpan = 100 * computeTotalWeightOfIncompatibleReferenceSplits(split, referenceSplits, incompatibilities);


                            if (distortion > 0 && distortion <= getOptionMaxDistortion()) {
                                split.setConfidence(incompatiblitySpan);
                                splitsWithDistortion.add(split);
                                splitSet2Incompatibilities.put(split.getSmallerPart(), incompatibilities);
                            }
                        }

                        final Graph coverageGraph = computeCoverageDAG(splitsWithDistortion, splitSet2Incompatibilities);

                        if (getOptionMaxDistortion() == 1) {
                            for (Node u : coverageGraph.nodes()) {
                                if (u.getInDegree() == 0) // is not covered by any other split
                                {
                                    final ASplit splitU = (ASplit) u.getInfo();
                                    final int countIncompatibilities = computeNumberOfIncompatibleReferenceSplits(splitU, referenceSplits);

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

                                            if (!isOptionRequireSingleSPR() || computeNumberOfIncompatibleReferenceSplits(split, referenceSplits) == countIncompatibilities)
                                                sin.add(split);
                                            for (Node w : v.children()) {
                                                queue.add(w);
                                            }
                                        }
                                        if (sin.getTotalWeight() >= 0.01 * getOptionMinWeight()) {
                                            synchronized (listOfSins) {
                                                listOfSins.add(sin);
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (getOptionMaxDistortion() > 1) {
                            final ArrayList<Pair<SIN, Node>> consideredSins = new ArrayList<>();
                            for (Node u : coverageGraph.nodes()) {
                                if (u.getInDegree() == 0) // is not covered by any other split
                                {
                                    final ASplit splitU = (ASplit) u.getInfo();
                                    if (splitU.getConfidence() >= getOptionMinSpanPercent()) {
                                        final int distortion = Distortion.computeDistortionForSplit(referenceTree, splitU.getA(), splitU.getB());

                                        final SIN sin = new SIN(t, treesBlock.getTree(t).getName(), splitU.getConfidence(), distortion);
                                        sin.add(splitU);
                                        final Queue<Node> queue = new LinkedList<>();
                                        for (Node w : u.children()) {
                                            queue.add(w);
                                        }
                                        while (queue.size() > 0) {
                                            final Node v = queue.remove();
                                            final ASplit splitV = (ASplit) v.getInfo();

                                            sin.add(splitV);
                                            for (Node w : v.children()) {
                                                queue.add(w);
                                            }
                                        }
                                        if (sin.getTotalWeight() >= 0.01 * getOptionMinWeight()) {
                                            final ArrayList<Pair<SIN, Node>> toDelete = new ArrayList<>();
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
                            synchronized (listOfSins) {
                                for (Pair<SIN, Node> pair : consideredSins) {
                                    listOfSins.add(pair.get1());
                                }
                            }
                        } else throw new IllegalArgumentException("max distortion must be at least 1");
                        progress.incrementProgress();

                        if (exception.get() != null)
                            return;
                    }
                } catch (Exception ex) {
                    exception.set(ex);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            if (exception.get() == null)
                exception.set(new CanceledException());
        }
        if (exception.get() != null)
            throw exception.get();

        // 3. sort SINs by decreasing span:

        listOfSins.sort(sinsComparator());

        if (getOptionSinRank() >= listOfSins.size())
            setOptionSinRank(listOfSins.size());

        if (isOptionOnePerTree()) {
            final BitSet trees = new BitSet();
            final ArrayList<SIN> toKeep = new ArrayList<>();
            for (final SIN sin : listOfSins) {
                if (!trees.get(sin.getTree())) {
                    trees.set(sin.getTree());
                    toKeep.add(sin);
                }
            }
            listOfSins.clear();
            listOfSins.addAll(toKeep);
        }

        for (int i = 0; i < Math.min(100, listOfSins.size()); i++) {
            final SIN sin = listOfSins.get(i);
            sin.setRank(i + 1);
            System.out.println(sin);
            if (showTrees) {
                System.err.println(reportTree(taxaBlock, referenceSplits, sin.getSplits()) + ";");
            }
        }
        if (listOfSins.size() > 100)
            System.out.println("(" + (listOfSins.size() - 100) + " more)");

        splitsBlock.getSplits().addAll(referenceSplits.getSplits());
        for (int s = 1; s <= splitsBlock.getNsplits(); s++) {
            splitsBlock.getSplitLabels().put(s, "BOLD");
        }

        if (listOfSins.size() == 0) {
            NotificationManager.showInformation("No SINs found");
        } else if (!isOptionAllSinsUpToRank()) {
            final int i = getOptionSinRank() - 1;
            final SIN sins = listOfSins.get(i);
            NotificationManager.showInformation(sins.toString());
            splitsBlock.getSplits().addAll(sins.getSplits());
        } else {
            final Map<BitSet, ASplit> splitSets2Splits = new HashMap<>();
            for (int i = 0; i < getOptionSinRank(); i++) {
                final SIN sins = listOfSins.get(i);
                for (ASplit split : sins.getSplits()) {
                    final ASplit other = splitSets2Splits.get(split.getSmallerPart());
                    if (other == null || other.getWeight() < split.getWeight()) {
                        splitSets2Splits.put(split.getSmallerPart(), split);
                    }
                }
            }
            splitsBlock.getSplits().addAll(splitSets2Splits.values());
            NotificationManager.showInformation("Computed anti-consensus using top " + getOptionSinRank() + " SINs");
        }
    }

    /**
     * is given a sin better than other? That is, does sin have a bigger score and overlap with other
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
    private static Graph computeCoverageDAG(Collection<ASplit> splits, Map<BitSet, BitSet> split2incompatibilities) {
        final Graph graph = new PhyloGraph();
        final Map<BitSet, Node> split2nodeMap = new HashMap<>();

        for (ASplit split : splits) {
            final Node v = graph.newNode();
            v.setInfo(split);
            split2nodeMap.put(split.getSmallerPart(), v);
        }
        for (ASplit split1 : splits) {
            final Node v = split2nodeMap.get(split1.getSmallerPart());
            final BitSet incompatibilities1 = split2incompatibilities.get(split1.getSmallerPart());
            for (ASplit split2 : splits) {
                if (!split1.equals(split2)) {
                    final BitSet incompatibilities2 = split2incompatibilities.get(split2.getSmallerPart());
                    if (BitSetUtils.contains(incompatibilities1, incompatibilities2) &&
                            ((incompatibilities1.cardinality() > incompatibilities2.cardinality())
                                    || (incompatibilities1.cardinality() == incompatibilities2.cardinality() && ASplit.compare(split1, split2) == -1)))
                        graph.newEdge(v, split2nodeMap.get(split2.getSmallerPart()));
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
     * count then number of incompatible splis
     *
     * @param split
     * @param referenceSplits
     * @return count
     */
    public static int computeNumberOfIncompatibleReferenceSplits(ASplit split, SplitsBlock referenceSplits) {
        int count = 0;
        for (int s = 1; s <= referenceSplits.getNsplits(); s++) {
            final ASplit other = referenceSplits.get(s);
            if (other.size() > 1 && !Compatibility.areCompatible(split, other)) {
                count++;
            }
        }
        return count;
    }

    /**
     * determine all splits that are incompatible to the given split and return their total weight
     *
     * @param split
     * @param referenceSplits
     * @param incompatible will contain indices of incompatible splits
     * @return count
     */
    public static double computeTotalWeightOfIncompatibleReferenceSplits(ASplit split, SplitsBlock referenceSplits, BitSet incompatible) {
        incompatible.clear();
        double weight = 0;
        for (int s = 1; s <= referenceSplits.getNsplits(); s++) {
            final ASplit other = referenceSplits.get(s);
            if (other.size() > 1 && !Compatibility.areCompatible(split, other)) {
                weight += other.getWeight();
                incompatible.set(s);
            }
        }
        return weight;
    }

    public Reference getOptionReferenceTree() {
        return optionReferenceTree.get();
    }

    public ObjectProperty<Reference> optionReferenceTreeProperty() {
        return optionReferenceTree;
    }

    public void setOptionReferenceTree(Reference optionReferenceTree) {
        this.optionReferenceTree.set(optionReferenceTree);
    }

    public int getOptionSinRank() {
        return optionSinRank.get();
    }

    public IntegerProperty optionSinRankProperty() {
        return optionSinRank;
    }

    public void setOptionSinRank(int optionSinRank) {
        this.optionSinRank.set(optionSinRank);
    }

    public boolean isOptionAllSinsUpToRank() {
        return optionAllSinsUpToRank.get();
    }

    public BooleanProperty optionAllSinsUpToRankProperty() {
        return optionAllSinsUpToRank;
    }

    public void setOptionAllSinsUpToRank(boolean optionAllSinsUpToRank) {
        this.optionAllSinsUpToRank.set(optionAllSinsUpToRank);
    }

    public double getOptionMinSpanPercent() {
        return optionMinSpanPercent.get();
    }

    public DoubleProperty optionMinSpanPercentProperty() {
        return optionMinSpanPercent;
    }

    public void setOptionMinSpanPercent(double optionMinSpanPercent) {
        this.optionMinSpanPercent.set(optionMinSpanPercent);
    }

    public int getOptionMaxDistortion() {
        return optionMaxDistortion.get();
    }

    public IntegerProperty optionMaxDistortionProperty() {
        return optionMaxDistortion;
    }

    public void setOptionMaxDistortion(int optionMaxDistortion) {
        this.optionMaxDistortion.set(optionMaxDistortion);
    }

    public boolean isOptionRequireSingleSPR() {
        return optionRequireSingleSPR.get();
    }

    public BooleanProperty optionRequireSingleSPRProperty() {
        return optionRequireSingleSPR;
    }

    public void setOptionRequireSingleSPR(boolean optionRequireSingleSPR) {
        this.optionRequireSingleSPR.set(optionRequireSingleSPR);
    }

    public double getOptionMinWeight() {
        return optionMinWeight.get();
    }

    public DoubleProperty optionMinWeightProperty() {
        return optionMinWeight;
    }

    public void setOptionMinWeight(double optionMinWeight) {
        this.optionMinWeight.set(optionMinWeight);
    }

    public boolean isOptionOnePerTree() {
        return optionOnePerTree.get();
    }

    public BooleanProperty optionOnePerTreeProperty() {
        return optionOnePerTree;
    }

    public void setOptionOnePerTree(boolean optionOnePerTree) {
        this.optionOnePerTree.set(optionOnePerTree);
    }

    @Override
    public boolean isApplicable(TaxaBlock taxaBlock, TreesBlock parent) {
        return !parent.isPartial();
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

    public static Comparator<SIN> sinsComparator() {
        return (a, b) -> {
            if (a.getSpanPercent() > b.getSpanPercent())
                return -1;
            else if (a.getSpanPercent() < b.getSpanPercent())
                return 1;
            else if (a.getTotalWeight() > b.getTotalWeight())
                return -1;
            else if (a.getTotalWeight() < b.getTotalWeight())
                return 1;
            else {
                final Iterator<ASplit> aIt = a.getSplits().iterator();
                final Iterator<ASplit> bIt = b.getSplits().iterator();

                while (aIt.hasNext() && bIt.hasNext()) {
                    final int value = aIt.next().compareTo(bIt.next());
                    if (value != 0)
                        return value;
                }
                if (aIt.hasNext())
                    return -1;
                else if (bIt.hasNext())
                    return 1;
                else
                    return 0;
            }
        };
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
            return String.format("SIN rank: %d, span: %.1f%%, weight: %.4f,  distortion: %d, splits: %d, tree: %d (%s)",
                    getRank(), spanPercent, totalWeight, distortion, splits.size(), treeId, treeName);
        }
    }
}


