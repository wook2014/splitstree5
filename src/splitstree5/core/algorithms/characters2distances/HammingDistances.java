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

package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.dialog.Alert;

/**
 * hamming distances
 *
 * @author Daniel Huson, 2003, 2017
 */
public class HammingDistances extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {
    private PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates = PairwiseCompare.HandleAmbiguous.Ignore;
    private boolean optionNormalize = true;

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxa, CharactersBlock characters, DistancesBlock distances) throws Exception {
        progressListener.setMaximum(taxa.getNtax());

        distances.setNtax(characters.getNtax());

        int numMissing = 0;
        final int ntax = taxa.getNtax();
        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                final PairwiseCompare seqPair = new PairwiseCompare(characters, characters.getSymbols(), s, t, optionHandleAmbiguousStates);
                double p = 1.0;

                final double[][] F = seqPair.getF();

                if (F == null) {
                    numMissing++;
                } else {
                    for (int x = 0; x < seqPair.getNumStates(); x++) {
                        p = p - F[x][x];
                    }

                    if (!isOptionNormalize())
                        p = Math.round(p * seqPair.getNumNotMissing());
                }
                distances.set(s, t, p);
                distances.set(t, s, p);
            }
            progressListener.incrementProgress();
        }
        if (numMissing > 0)
            new Alert("Warning: " + numMissing + " saturated or missing entries in the distance matrix - proceed with caution ");

    }

    public PairwiseCompare.HandleAmbiguous getOptionHandleAmbiguousStates() {
        return optionHandleAmbiguousStates;
    }

    public void setOptionHandleAmbiguousStates(PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates) {
        this.optionHandleAmbiguousStates = optionHandleAmbiguousStates;
    }

    public boolean isOptionNormalize() {
        return optionNormalize;
    }

    public void setOptionNormalize(boolean optionNormalize) {
        this.optionNormalize = optionNormalize;
    }
}
