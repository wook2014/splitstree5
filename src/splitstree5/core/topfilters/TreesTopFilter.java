/*
 * TreesTopFilter.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.phylo.PhyloTree;
import jloda.util.*;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.utils.TreesUtilities;

/**
 * tree top taxon filter
 * Daniel Huson, 12/12/16.
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
    public TreesTopFilter(DataNode<TaxaBlock> originalTaxaNode, DataNode<TaxaBlock> modifiedTaxaNode, DataNode<TreesBlock> parentBlock, DataNode<TreesBlock> childBlock) {
        super(originalTaxaNode.getDataBlock(), modifiedTaxaNode, parentBlock, childBlock);

        setAlgorithm(new Algorithm<TreesBlock, TreesBlock>("TopFilter") {
            public void compute(ProgressListener progress, TaxaBlock modifiedTaxaBlock, TreesBlock parent, TreesBlock child) throws CanceledException {
                {
                    setShortDescription("Trees top filter");
                }
                final TaxaBlock originalTaxa = originalTaxaNode.getDataBlock();

                if (originalTaxa.getTaxa().equals(modifiedTaxaBlock.getTaxa())) {
                    child.copy(parent);
                    setShortDescription("using all " + modifiedTaxaBlock.size() + " taxa");
                } else {
                    final int[] oldTaxonId2NewTaxonId = new int[originalTaxa.getNtax() + 1];
                    for (int t = 1; t <= originalTaxa.getNtax(); t++) {
                        oldTaxonId2NewTaxonId[t] = modifiedTaxaBlock.indexOf(originalTaxa.get(t).getName());
                    }

                    progress.setMaximum(parent.getNTrees());

                    for (PhyloTree tree : parent.getTrees()) {
                        // PhyloTree inducedTree = computeInducedTree(tree, modifiedTaxaBlock.getLabels());
                        final PhyloTree inducedTree = TreesUtilities.computeInducedTree(oldTaxonId2NewTaxonId, tree);
                        if (inducedTree != null) {
                            child.getTrees().add(inducedTree);
                            if (false && !BitSetUtils.contains(modifiedTaxaBlock.getTaxaSet(), TreesUtilities.getTaxa(inducedTree))) {
								System.err.println("taxa:" + StringUtils.toString(modifiedTaxaBlock.getTaxaSet()));
								System.err.println("tree:" + StringUtils.toString(TreesUtilities.getTaxa(inducedTree)));
								throw new RuntimeException("Induce tree failed");
							}
                        }
                        progress.incrementProgress();
                    }

                    setShortDescription("using " + modifiedTaxaBlock.size() + " of " + getOriginalTaxaBlock().size() + " taxa");
                }
                child.setPartial(parent.isPartial());
                child.setRooted(parent.isRooted());
            }
        });
    }

}
