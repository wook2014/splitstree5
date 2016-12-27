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


import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

/**
 * distances top taxon filter
 * Created by huson on 12/12/16.
 */
public class DistancesTopFilter extends Algorithm<DistancesBlock, DistancesBlock> {
    private final TaxaBlock modifiedTaxaBlock;

    /**
     * constructor
     */
    public DistancesTopFilter(TaxaBlock modifiedTaxaBlock) {
        this.modifiedTaxaBlock = modifiedTaxaBlock;
        setName("TopFilter");
    }

    public void compute(TaxaBlock originalTaxaBlock, DistancesBlock original, DistancesBlock modified) {
        try {
            if (modified.getNtax() != modifiedTaxaBlock.getNtax())
                modified.setNtax(modifiedTaxaBlock.getNtax());

            for (Taxon a : modifiedTaxaBlock.getTaxa()) {
                final int originalI = originalTaxaBlock.indexOf(a);
                final int modifiedI = modifiedTaxaBlock.indexOf(a);
                for (Taxon b : modifiedTaxaBlock.getTaxa()) {
                    final int originalJ = originalTaxaBlock.indexOf(b);
                    final int modifiedJ = modifiedTaxaBlock.indexOf(b);
                    modified.set(modifiedI, modifiedJ, original.get(originalI, originalJ));
                    //System.err.println(String.format("set (%d,%d)=%f", modifiedI, modifiedJ, original.get(originalI, originalJ)));
                }
            }
        } catch (Exception ex) {
            throw ex;
        }

    }


}
