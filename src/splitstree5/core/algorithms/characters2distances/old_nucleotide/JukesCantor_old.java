/*
 * JukesCantor_old.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.characters2distances.old_nucleotide;

import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromCharacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.nucleotideModels.JCmodel;

import java.util.Arrays;
import java.util.List;

/**
 * Computes the Jukes Cantor distance for a set of characters
 * <p>
 * Created on 12-Jun-2004
 *
 * @author DJB
 * @deprecated
 */

public class JukesCantor_old extends DNAdistance implements IFromCharacters, IToDistances {
    @Override
    public String getCitation() {
        return "Jukes and Cantor 1969; Jukes TH & Cantor CR (1969). Evolution of Protein Molecules. New York: Academic Press. pp. 21–132";
    }

    @Override
    public List<String> listOptions() {
        return Arrays.asList("PropInvariableSites", "SetParameters", "UseML_Distances");
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progress.setTasks("Jukes-Cantor Distance", "Init.");
        progress.setMaximum(taxaBlock.getNtax());

        final JCmodel model = new JCmodel();
        model.setPropInvariableSites(getOptionPropInvariableSites());
        //model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        double D = 1 - (F[0][0] + F[1][1] + F[2][2] + F[3][3]);
        double B = 0.75;
        return -B * Minv(1 - D / B);
    }
}
