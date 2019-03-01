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

package splitstree5.core.algorithms.characters2distances.old_nucleotide;

import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.nucleotideModels.F81model;

import java.util.Arrays;
import java.util.List;

/**
 * @deprecated
 */
public class F81_old extends DNAdistance implements IFromChararacters, IToDistances {
    private double B;

    @Override
    public String getCitation() {
        return "Felsenstein 1981; Felsenstein J (1981). Evolutionary trees from DNA sequences: a maximum likelihood approach. Journal of Molecular Evolution. 17 (6): 368–376.";
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("Gamma", "PropInvariableSites", "UseML_Distances", "SetParameters");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        progress.setTasks("F81 Distance", "computing...");
        F81model model = new F81model(this.getNormedBaseFreq());
        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(getOptionGamma());

        //System.out.println("A is: " + baseFreq[0]);
        double[] freqs = getNormedBaseFreq();

        double piA = freqs[0],
                piC = freqs[1],
                piG = freqs[2],
                piT = freqs[3];

        B = 1.0 - ((piA * piA) + (piC * piC) + (piG * piG) + (piT * piT));

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
    }

    /**
     * return the exact distance
     *
     * @param F
     * @return
     * @throws SaturatedDistancesException
     */
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double D = 1 - (F[0][0] + F[1][1] + F[2][2] + F[3][3]);
        return -B * Minv(1 - D / B);
    }
}
