/*
 * DashingDistance.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.untested.dashing;

import jloda.kmers.GenomeDistanceType;

/**
 * computes the dashing distance
 * Daniel Huson, 3.2020
 */
public class DashingDistance {
    /**
     * computes the Dashing distance
     *
     * @param a
     * @param b
     * @return mash distance
     */
    public static double compute(DashingSketch a, DashingSketch b, GenomeDistanceType genomeDistanceType) {

        final DashingSketch union = DashingSketch.union(a, b);
        final double jaccardIndex = Math.max(0, (a.getHarmonicMean() + b.getHarmonicMean() - union.getHarmonicMean()) / union.getHarmonicMean());

        if (genomeDistanceType == GenomeDistanceType.Mash) {
            if (jaccardIndex <= 0)
                return 1;
            else
                return Math.max(0f, -1.0 / a.getKmerSize() * Math.log(2.0 * jaccardIndex / (1 + jaccardIndex)));
        } else {
            if (jaccardIndex <= 0)
                return 1;
            else
                return 1 - jaccardIndex;
        }
    }
}
