/*
 * SplitsTopFilter.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.core.topfilters;

import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;
import splitstree5.core.workflow.DataNode;

import java.util.BitSet;
import java.util.Map;

/**
 * splits top taxon filter
 * Daniel Huson, 12/12/16.
 */
public class SplitsTopFilter extends ATopFilter<SplitsBlock> {
    /**
     * /**
     * constructor
     *
     * @param originalTaxaNode
     * @param modifiedTaxaNode
     * @param parentNode
     * @param childNode
     */
    public SplitsTopFilter(DataNode<TaxaBlock> originalTaxaNode, DataNode<TaxaBlock> modifiedTaxaNode, DataNode<SplitsBlock> parentNode, DataNode<SplitsBlock> childNode) {
        super(originalTaxaNode.getDataBlock(), modifiedTaxaNode, parentNode, childNode);

        setAlgorithm(new Algorithm<>("TopFilter") {
            public void compute(ProgressListener progress, TaxaBlock modifiedTaxaBlock, SplitsBlock parent, SplitsBlock child) throws CanceledException {
                {
                    setShortDescription("Splits top filter");
                }
                if (originalTaxaNode.getDataBlock().getTaxa().equals(modifiedTaxaBlock.getTaxa())) {
                    child.copy(parent);
                    child.setCycle(parent.getCycle());
                    child.setCompatibility(parent.getCompatibility());
                    setShortDescription("using all " + modifiedTaxaBlock.size() + " taxa");
                } else {
                    progress.setMaximum(parent.getNsplits());
                    final Map<Integer, Integer> originalIndex2ModifiedIndex = getOriginalTaxaBlock().computeIndexMap(modifiedTaxaBlock);
                    for (ASplit split : parent.getSplits()) {
                        ASplit induced = computeInducedSplit(split, originalIndex2ModifiedIndex, modifiedTaxaBlock.getNtax());
                        if (induced != null)
                            child.getSplits().add(induced);
                        progress.incrementProgress();
                    }
                    child.setCycle(computeInducedCycle(parent.getCycle(), originalIndex2ModifiedIndex, modifiedTaxaBlock.getNtax()));
                    child.setCompatibility(Compatibility.compute(modifiedTaxaBlock.getNtax(), child.getSplits(), child.getCycle()));
                    setShortDescription("using " + modifiedTaxaBlock.size() + " of " + getOriginalTaxaBlock().size() + " taxa");
                }
                child.setFit(parent.getFit());
                child.setThreshold(parent.getThreshold());
                child.setPartial(parent.isPartial());
            }
        });
    }

    /**
     * compute an induced split
     *
     * @param originalSplit
     * @param originalIndex2ModifiedIndex
     * @param inducedNtax
     * @return induced split or null
     */
    private static ASplit computeInducedSplit(ASplit originalSplit, Map<Integer, Integer> originalIndex2ModifiedIndex, int inducedNtax) {
        final BitSet originalA = originalSplit.getA();

        final BitSet inducedA = new BitSet();
        for (int t = originalA.nextSetBit(0); t != -1; t = originalA.nextSetBit(t + 1)) {
            if (originalIndex2ModifiedIndex.containsKey(t))
                inducedA.set(originalIndex2ModifiedIndex.get(t));
        }
        if (inducedA.cardinality() < inducedNtax) {
            return new ASplit(inducedA, inducedNtax, originalSplit.getWeight());
        } else
            return null;
    }

    private static int[] computeInducedCycle(int[] originalCycle, Map<Integer, Integer> originalIndex2ModifiedIndex, int inducedNtax) {
        final int[] cycle = new int[inducedNtax + 1];

        int i = 1;
        for (int originalI : originalCycle) {
            if (originalIndex2ModifiedIndex.containsKey(originalI)) {
                cycle[i++] = originalIndex2ModifiedIndex.get(originalI);
            }
        }
        return cycle;
    }
}
