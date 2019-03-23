/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import jloda.fx.util.NotificationManager;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.util.Collections;
import java.util.List;

/**
 * hamming distances
 *
 * @author Daniel Huson, 2003, 2017
 */
public class HammingDistances extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {
    private BooleanProperty optionNormalize = new SimpleBooleanProperty(true);

    public List<String> listOptions() {
        return Collections.singletonList("Normalize");
    }

    @Override
    public String getCitation() {
        return "Hamming 1950; Hamming, Richard W. Error detecting and error correcting codes. Bell System Technical Journal. 29 (2): 147–160. MR 0035935, 1950.";
    }


    @Override
    public String getToolTip(String optionName) {
        if (optionName.equals("Normalize"))
            return "Normalize distances";
        else
            return optionName;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxa, CharactersBlock characters, DistancesBlock distances) throws Exception {
        progress.setMaximum(taxa.getNtax());

        distances.setNtax(characters.getNtax());

        int numMissing = 0;
        final int ntax = taxa.getNtax();
        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {
                final PairwiseCompare seqPair = new PairwiseCompare(characters, s, t);
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
            progress.incrementProgress();
        }
        if (numMissing > 0)
            NotificationManager.showWarning("Proceed with caution: " + numMissing + " saturated or missing entries in the distance matrix");
    }

    public boolean isOptionNormalize() {
        return optionNormalize.getValue();
    }
    public BooleanProperty optionNormalizeProperty() {
        return optionNormalize;
    }
    public void setOptionNormalize(boolean optionNormalize) {
        this.optionNormalize.setValue(optionNormalize);
    }
}
