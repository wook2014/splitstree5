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

package splitstree5.core.algorithms.characters2distances.nucleotide;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.F81model;

import java.util.Arrays;
import java.util.List;

/**
 * Implements the Felsenstein 1981 DNA distance model
 * Dave Bryant, 2004
 */

public class F81 extends Nucleotides2DistancesBase implements IFromChararacters, IToDistances {
    @Override
    public String getCitation() {
        return "Felsenstein 1981; Felsenstein J (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of Molecular Evolution. 17 (6): 368–376.";
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("optionBaseFrequencies", "SetBaseFrequencies", "PropInvariableSites", "SetSiteVarParams", "UseML_Distances");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        progress.setTasks("Felsenstein 1981 distance", "Computing...");

        final F81model model = new F81model(getOptionBaseFrequencies());

        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(DEFAULT_GAMMA);

        model.apply(progress, charactersBlock, distancesBlock, isOptionUseML_Distances());
    }
}
