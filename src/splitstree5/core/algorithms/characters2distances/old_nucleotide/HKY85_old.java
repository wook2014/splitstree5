/*
 * HKY85_old.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances.old_nucleotide;

import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromCharacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.nucleotideModels.HKY85model;

/**
 * @deprecated
 */
public class HKY85_old extends DNAdistance implements IFromCharacters, IToDistances {
    @Override
    public String getCitation() {
        return "Hasegawa, Kishino, Yano 1985; " +
                "Hasegawa M, Kishino H, Yano T. \"Dating of human-ape splitting by a molecular clock of mitochondrial DNA\". " +
                "Journal of Molecular Evolution. 22 (2): 160–174. PMID 3934395. doi:10.1007/BF02101694, 1985.";
    }

    public HKY85_old() {
        super();
        setOptionUseML_Distances(true);
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progress.setTasks("HKY85 Distance", "Init.");

        HKY85model model = new HKY85model(getNormedBaseFreq(), getOptionTsTvRatio());
        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(getOptionGamma());

        setOptionUseML_Distances(true);
        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        return 0.0;//We will never get here!
    }

}
