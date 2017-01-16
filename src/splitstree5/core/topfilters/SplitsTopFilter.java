/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.core.topfilters;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;

import java.util.BitSet;
import java.util.Map;

/**
 * splits top taxon filter
 * Created by huson on 12/12/16.
 */
public class SplitsTopFilter extends ATopFilter<SplitsBlock> {
    /**
     * /**
     * constructor
     *
     * @param originalTaxaNode
     * @param modifiedTaxaNode
     * @param parent
     * @param child
     */
    public SplitsTopFilter(ADataNode<TaxaBlock> originalTaxaNode, ADataNode<TaxaBlock> modifiedTaxaNode, ADataNode<SplitsBlock> parent, ADataNode<SplitsBlock> child) {
        super(originalTaxaNode.getDataBlock(), modifiedTaxaNode, parent, child);

        setAlgorithm(new Algorithm<SplitsBlock, SplitsBlock>("TopFilter") {
            public void compute(ProgressListener progressListener, TaxaBlock modifiedTaxaBlock, SplitsBlock original, SplitsBlock modified) {
                modified.getSplits().clear();

                final Map<Integer, Integer> originalIndex2ModifiedIndex = getOriginalTaxaBlock().computeIndexMap(modifiedTaxaBlock);

                for (ASplit split : original.getSplits()) {
                    ASplit induced = computeInducedSplit(split, originalIndex2ModifiedIndex, modifiedTaxaBlock.getNtax());
                    if (induced != null)
                        modified.getSplits().add(induced);
                }
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
}
