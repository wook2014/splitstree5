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
public class AntiConsensusSplits extends Algorithm<TreesBlock, SplitsBlock> implements IFromTrees, IToSplits {
    private float optionIncompatibilitiesPercent = 10; // percent of non-trivial incompatibilities that a split should have to be considered for the anti-consensus

    private ConsensusNetwork.EdgeWeights optionEdgeWeights = ConsensusNetwork.EdgeWeights.Mean;

    private int optionSourceTree = 0;

    public final static String DESCRIPTION = "Computes the anti-consensus splits of trees";

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
        // 0. compute all splits:
        final SplitsBlock allSplits = new SplitsBlock();
        {
            progress.setSubtask("Determining all splits");
            final ConsensusNetwork consensusNetwork = new ConsensusNetwork();
            consensusNetwork.setOptionThresholdPercent(0);
            consensusNetwork.setOptionEdgeWeights(optionEdgeWeights);
            consensusNetwork.compute(progress, taxaBlock, treesBlock, allSplits);
            System.err.println("All splits: " + allSplits.size());
        }

        // 1. compute the majority consensus splits and tree
        final SplitsBlock consensusSplits = new SplitsBlock();
        {
            progress.setSubtask("Determining consensus tree splits");
            final ConsensusTreeSplits consensusTreeSplits = new ConsensusTreeSplits();
            consensusTreeSplits.setOptionConsensus(ConsensusTreeSplits.Consensus.Majority); // todo: implement and use loose consensus
            consensusTreeSplits.setOptionEdgeWeights(optionEdgeWeights);
            consensusTreeSplits.compute(progress, taxaBlock, treesBlock, consensusSplits);
            System.err.println("Consensus tree splits: " + consensusSplits.size());
        }

        final PhyloTree consensusTree;
        {
            final TreesBlock trees = new TreesBlock();
            progress.setSubtask("Determining consensus tree");
            final GreedyTree greedyTree = new GreedyTree();
            greedyTree.compute(progress, taxaBlock, consensusSplits, trees);
            consensusTree = trees.getTree(1);
        }

        final int minNumberOfIncompatibilitiesRequired;
        {
            int numberOfNonTrivialConsensusTreeSplits = 0;
            for (ASplit split : consensusSplits.getSplits()) {
                if (split.size() > 1)
                    numberOfNonTrivialConsensusTreeSplits++;
            }
            minNumberOfIncompatibilitiesRequired = (int) (numberOfNonTrivialConsensusTreeSplits / 100.0 * getOptionIncompatibilitiesPercent());
        }

        // 2. compute the result:
        final SplitsBlock otherSplits = new SplitsBlock();
        otherSplits.getSplits().setAll(allSplits.getSplits());
        otherSplits.getSplits().removeAll(consensusSplits.getSplits());
        System.err.println("Difference: " + otherSplits.size());

        splitsBlock.getSplits().setAll(consensusSplits.getSplits()); // add all consensus splits
        final SplitsBlock addSplits = new SplitsBlock();
        for (ASplit split : otherSplits.getSplits()) {
            final int numberOfIncompatible = Compatibility.computeIncompatible(split, consensusSplits.getSplits());
            if (numberOfIncompatible >= minNumberOfIncompatibilitiesRequired) {
                final int distortion = Distortion.computeDistortionForSplit(consensusTree, split.getA(), split.getB());
                if (distortion == 1) {
                    addSplits.getSplits().add(split);
                }
            }
        }
        System.err.println("Incompatible splits added: " + addSplits.size());

        if (addSplits.size() > 0) {
            progress.setSubtask("Mapping back to trees:");

            final Map<Integer, Pair<ASplit, BitSet>> id2splitAndTreeIds = new TreeMap<>();
            mapSplitsToTrees(progress, taxaBlock, addSplits, treesBlock, id2splitAndTreeIds);

            if (getOptionSourceTree() > 0) {
                final ArrayList<Integer> toDelete = new ArrayList<>();
                for (Integer splitId : id2splitAndTreeIds.keySet()) {
                    final Pair<ASplit, BitSet> pair = id2splitAndTreeIds.get(splitId);
                    if (!pair.getSecond().get(getOptionSourceTree())) {
                        toDelete.add(splitId);
                        addSplits.getSplits().remove(pair.getFirst());
                    }
                }
                if (toDelete.size() > 0)
                    id2splitAndTreeIds.keySet().removeAll(toDelete);
            }

            for (Integer splitId : id2splitAndTreeIds.keySet()) {
                System.err.print("Split " + splitId + " from tree(s): ");
                final BitSet bits = id2splitAndTreeIds.get(splitId).getSecond();
                if (bits != null) {
                    boolean first = true;
                    for (Integer t : BitSetUtils.members(bits)) {
                        if (first)
                            first = false;
                        else
                            System.err.print(", ");
                        System.err.print("[" + t + "] " + treesBlock.getTree(t).getName());
                    }
                }
                System.err.println();

            }
        }

        splitsBlock.getSplits().addAll(addSplits.getSplits());
    }

    /**
     * map given splits to trees that contain them
     *
     * @param progress
     * @param taxaBlock
     * @param splitsBlock
     * @param treesBlock
     * @param splitId2SplitAndTreeIds
     * @throws CanceledException
     */
    public static void mapSplitsToTrees(ProgressListener progress, final TaxaBlock taxaBlock, SplitsBlock splitsBlock, TreesBlock treesBlock, Map<Integer, Pair<ASplit, BitSet>> splitId2SplitAndTreeIds) throws CanceledException {
        // compute added split to tree map:
        final ExecutorService executor = ProgramExecutorService.getInstance();

        if (treesBlock.getNTrees() == 1) System.err.println("Consensus network: only one tree specified");

        progress.setMaximum(treesBlock.size());
        progress.setProgress(0);

        {
            final int mask = 511;
            final Object[] sync = new Object[mask + 1];
            for (int i = 0; i < sync.length; i++)
                sync[i] = new Object();

            final int numberOfThreads = Math.min(treesBlock.size(), 8);
            final CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
            final Single<CanceledException> exception = new Single<>();

            for (int i = 1; i <= numberOfThreads; i++) {
                final int threadNumber = i;
                executor.execute(() -> {
                    try {
                        for (int treeId = threadNumber; treeId < treesBlock.size(); treeId += numberOfThreads) {
                            final PhyloTree tree = treesBlock.getTree(treeId);
                            final List<ASplit> splits = new ArrayList<>();
                            TreesUtilities.computeSplits(taxaBlock.getTaxaSet(), tree, splits);
                            for (int splitId = 1; splitId <= splitsBlock.size(); splitId++) {
                                final ASplit split = splitsBlock.get(splitId);
                                if (splits.contains(split)) {
                                    synchronized (sync[splitId & mask]) {
                                        Pair<ASplit, BitSet> pair = splitId2SplitAndTreeIds.get(splitId);
                                        if (pair == null) {
                                            pair = new Pair<>(split, new BitSet());
                                            splitId2SplitAndTreeIds.put(splitId, pair);
                                        }
                                        pair.getSecond().set(treeId);
                                    }
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
    }


    public float getOptionIncompatibilitiesPercent() {
        return optionIncompatibilitiesPercent;
    }

    public void setOptionIncompatibilitiesPercent(float optionIncompatibilitiesPercent) {
        this.optionIncompatibilitiesPercent = optionIncompatibilitiesPercent;
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


