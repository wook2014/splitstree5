/*
 * GTR.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances.nucleotide;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IFromCharacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.nucleotideModels.GTRmodel;

import java.util.Arrays;
import java.util.List;

/**
 * Computes the distance matrix from a set of characters using the General Time Revisible model.
 *
 * @author Dave Bryant, 2004
 */

public class GTR extends Nucleotides2DistancesBase implements IFromCharacters, IToDistances {
    @Override
    public String getCitation() {
        return "Tavaré 1986; Tavaré S (1986). Some Probabilistic and Statistical Problems in the Analysis of DNA Sequences. Lectures on Mathematics in the Life Sciences. 17: 57–86";
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("PropInvariableSites", "SetSiteVarParams", "RateMatrix", "UseML_Distances");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        progress.setTasks("GTR Distance", "Computing...");

        GTRmodel model = new GTRmodel(getOptionRateMatrix(), getOptionBaseFrequencies());
        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(DEFAULT_GAMMA);

        model.apply(progress, charactersBlock, distancesBlock, isOptionUseML_Distances());
    }
}
