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
import splitstree5.core.models.nucleotideModels.K2Pmodel;

/**
 * @deprecated
 */
public class K2P_old extends DNAdistance implements IFromChararacters, IToDistances {
    @Override
    public String getCitation() {
        return "Kimura 1980; Kimura M (1980). A simple method for estimating evolutionary rates of base substitutions through comparative studies of nucleotide sequences. Journal of Molecular Evolution. 16 (2): 111–120.";
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {

        progress.setTasks("K2P Distance", "Init.");
        progress.setMaximum(taxaBlock.getNtax());

        K2Pmodel model = new K2Pmodel(getOptionTsTvRatio());
        model.setPropInvariableSites(getOptionPropInvariableSites());
        model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double P = F[0][2] + F[1][3] + F[2][0] + F[3][1];
        double Q = F[0][1] + F[0][3] + F[1][0] + F[1][2];
        Q += F[2][1] + F[2][3] + F[3][0] + F[3][2];
        double dist = 0.5 * Minv(1 / (1 - (2 * P) - Q));
        dist += 0.25 * Minv(1 / (1 - (2 * Q)));
        return dist;
    }
}
