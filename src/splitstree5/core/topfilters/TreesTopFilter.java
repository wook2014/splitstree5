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
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ITree;

import java.util.Map;

/**
 * splits top taxon filter
 * Created by huson on 12/12/16.
 */
public class TreesTopFilter extends ATopFilter<TreesBlock> {
    /**
     * /**
     * constructor
     *
     * @param originalTaxaNode
     * @param modifiedTaxaNode
     * @param parentBlock
     * @param childBlock
     */
    public TreesTopFilter(ADataNode<TaxaBlock> originalTaxaNode, ADataNode<TaxaBlock> modifiedTaxaNode, ADataNode<TreesBlock> parentBlock, ADataNode<TreesBlock> childBlock) {
        super(originalTaxaNode, modifiedTaxaNode, parentBlock, childBlock);

        setAlgorithm(new Algorithm<TreesBlock, TreesBlock>("TopFilter") {
            public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, TreesBlock parentBlock, TreesBlock childBlock) {
                final TaxaBlock originalTaxaBlock = originalTaxaNode.getDataBlock();

                childBlock.getTrees().clear();

                final Map<Integer, Integer> originalIndex2ModifiedIndex = originalTaxaBlock.computeIndexMap(taxaBlock);

                for (ITree tree : parentBlock.getTrees()) {
                    ITree induced = computeInducedTree(tree, originalIndex2ModifiedIndex, taxaBlock.getNtax());
                    if (induced != null)
                        childBlock.getTrees().add(induced);
                }
            }
        });
    }

    /**
     * compute an induced tree
     *
     * @param originalTree
     * @param originalIndex2ModifiedIndex
     * @param inducedNtax
     * @return induced tree or null
     */
    private static ITree computeInducedTree(ITree originalTree, Map<Integer, Integer> originalIndex2ModifiedIndex, int inducedNtax) {
        // todo: implement
        return null;
    }
}
