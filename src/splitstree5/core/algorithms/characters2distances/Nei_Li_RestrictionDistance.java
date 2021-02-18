/*
 * Nei_Li_RestrictionDistance.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.FixUndefinedDistances;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromCharacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;

/**
 * Implements the NeiLi (1979) distance for restriction site data.
 *
 * @author David Bryant, 2008?
 */

public class Nei_Li_RestrictionDistance extends Algorithm<CharactersBlock, DistancesBlock> implements IFromCharacters, IToDistances {

    private final DoubleProperty optionRestrictionSiteLength = new SimpleDoubleProperty(6.0);

    @Override
    public String getCitation() {
        return "Nei and Li 1979;M Nei and W H Li. Mathematical model for studying genetic variation in terms of restriction endonucleases, PNAS 79(1):5269-5273, 1979.";
    }

    @Override
    public String getToolTip(String optionName) {
        if (optionName.equals("RestrictionSiteLength"))
            return "Expected length of restriction site (4-8 bp)";
        else
            return optionName;
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock) throws Exception {
        final int ntax = taxaBlock.getNtax();
        distancesBlock.setNtax(ntax);

        progress.setTasks("Nei Li (1979) Restriction Site Distance", "Init.");
        progress.setMaximum(ntax);

        double maxDist = 0.0;
        int numUndefined = 0;

        for (int s = 1; s <= ntax; s++) {
            for (int t = s + 1; t <= ntax; t++) {

                PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, s, t);
                final double[][] F = seqPair.getF();
                double dist = -1.0;
                if (F == null)
                    numUndefined++;
                else {
                    final double ns = F[1][0] + F[1][1];
                    final double nt = F[0][1] + F[1][1];
                    final double nst = F[1][1];

                    if (nst == 0) {
                        dist = -1;
                        numUndefined++;
                    } else {
                        final double s_hat = 2.0 * nst / (ns + nt);
                        final double a = (4.0 * Math.pow(s_hat, 1.0 / (2 * getOptionRestrictionSiteLength())) - 1.0) / 3.0;
                        if (a <= 0.0) {
                            dist = -1;
                            numUndefined++;
                        } else
                            dist = -1.5 * Math.log(a);
                    }
                }
                distancesBlock.set(s, t, dist);
                distancesBlock.set(t, s, dist);
                if (dist > maxDist)
                    maxDist = dist;

            }
            progress.incrementProgress();
        }
        if (numUndefined > 0)
            FixUndefinedDistances.apply(ntax, maxDist, distancesBlock);

        progress.close();
    }

    @Override
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock c) {
        return c.getDataType().equals(CharactersType.Standard);
    }

    public double getOptionRestrictionSiteLength() {
        return this.optionRestrictionSiteLength.getValue();
    }

    public DoubleProperty optionRestrictionSiteLengthProperty() {
        return this.optionRestrictionSiteLength;
    }

    public void setOptionRestrictionSiteLength(double restrictionSiteLength) {
        this.optionRestrictionSiteLength.setValue(restrictionSiteLength);
    }
}
