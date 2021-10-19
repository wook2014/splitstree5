/*
 * DistancesTopFilter.java Copyright (C) 2021. Daniel H. Huson
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
import splitstree5.core.datablocks.GenomesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.DataNode;

import java.util.HashMap;
import java.util.Map;

/**
 * genomes top taxon filter
 * Daniel Huson, 3.2020
 */
public class GenomesTopFilter extends ATopFilter<GenomesBlock> {
    /**
     * constructor
     *
     * @param originalTaxaNode
     * @param modifiedTaxaNode
     * @param parent
     * @param child
     */
    public GenomesTopFilter(DataNode<TaxaBlock> originalTaxaNode, DataNode<TaxaBlock> modifiedTaxaNode, DataNode<GenomesBlock> parent, DataNode<GenomesBlock> child) {
        super(originalTaxaNode.getDataBlock(), modifiedTaxaNode, parent, child);

        setAlgorithm(new Algorithm<>("TopFilter") {
            {
                setShortDescription("Genomes top filter");
            }

            public void compute(ProgressListener progress, TaxaBlock modifiedTaxaBlock, GenomesBlock original, GenomesBlock modified) throws CanceledException {
                if (originalTaxaNode.getDataBlock().getTaxa().equals(modifiedTaxaBlock.getTaxa())) {
                    child.getDataBlock().copy(parent.getDataBlock());
                    setShortDescription("using all " + modifiedTaxaBlock.size() + " taxa");
                } else {
                    progress.setMaximum(modifiedTaxaBlock.getNtax());
                    progress.setProgress(0);
                    child.getDataBlock().clear();

                    final Map<String, Integer> name2index = new HashMap<>();
                    for (int t = 1; t <= getOriginalTaxaBlock().getNtax(); t++)
                        name2index.put(getOriginalTaxaBlock().getLabel(t), t);

                    for (int t = 1; t <= modifiedTaxaBlock.getNtax(); t++) {
                        child.getDataBlock().getGenomes().add(original.getGenome(name2index.get(modifiedTaxaBlock.get(t).getName())));
                        progress.incrementProgress();
                    }
                    setShortDescription("using " + modifiedTaxaBlock.size() + " of " + getOriginalTaxaBlock().size() + " taxa");
                }
            }
        });
    }
}
