/*
 * JukesCantor.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.interfaces.IFromCharacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.nucleotideModels.JCmodel;
import splitstree5.core.models.nucleotideModels.NucleotideModel;

import java.util.Arrays;
import java.util.List;

/**
 * implements the Jukes Cantor transformation
 * Daniel Huson, 2.2019
 */
public class JukesCantor extends Nucleotides2DistancesBase implements IFromCharacters, IToDistances {
    @Override
    public String getCitation() {
        return "Jukes and Cantor 1969; Jukes TH & Cantor CR (1969). Evolution of Protein Molecules. New York: Academic Press. pp. 21–132";
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("PropInvariableSites", "SetSiteVarParams", "UseML_Distances");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock parent, DistancesBlock child) throws Exception {

        final NucleotideModel model = new JCmodel();

        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(DEFAULT_GAMMA);

        model.apply(progress, parent, child, isOptionUseML_Distances());
    }
}
