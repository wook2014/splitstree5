/*
 *  MashDistance.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.dialogs.genome.mash;

public class MashDistance {
    /**
     * computes the mash distance
     *
     * @param a
     * @param b
     * @return mash distance
     */
    public static double compute(MashSketch a, MashSketch b, boolean phylogenetic) {

        final double j = computeJaccardIndex(a, b);
        if (phylogenetic) {
            if (j == 0)
                return 1;
            else
                return Math.max(0f, -1.0 / a.getkSize() * Math.log(2.0 * j / (1 + j)));
        } else
            return j;
    }

    /**
     * computes the mash distance
     *
     * @param a
     * @param b
     * @return mash distance
     */
    public static double compute(MashSketch a, MashSketch b) {
        final double j = computeJaccardIndex(a, b);
        if (j == 0)
            return 1;
        else
            return Math.max(0f, -1.0 / a.getkSize() * Math.log(2.0 * j / (1 + j)));
    }

    /**
     * computes the Jaccard index for two sketches
     *
     * @param sketch1
     * @param sketch2
     * @return Jaccard index
     */
    public static double computeJaccardIndex(MashSketch sketch1, MashSketch sketch2) {
        final int sketchSize = Math.min(sketch1.getSketchSize(), sketch2.getSketchSize());

        final long[] union = new long[sketchSize];
        final long[] values1 = sketch1.getValues();
        final long[] values2 = sketch2.getValues();

        // compute the union:
        {
            int i = 0;
            int j = 0;
            for (int k = 0; k < sketchSize; k++) { // union upto MashSketch size
                if (values1[i] < values2[j]) {
                    union[k] = values1[i];
                    i++;
                } else if (values1[i] > values2[j]) {
                    union[k] = values2[j];
                    j++;
                } else // if (values1[i] == values2[j])
                {
                    union[k] = values1[i];
                    i++;
                    j++;
                }
            }
        }
        // compute intersection size:
        int intersectionSize = 0;
        {
            int i = 0;
            int j = 0;
            int k = 0;
            while (k < union.length) {
                if (values1[i] < union[k]) {
                    i++;
                    if (i == sketchSize)
                        break;
                } else if (values2[j] < union[k]) {
                    j++;
                    if (j == sketchSize)
                        break;
                } else if (values1[i] == union[k] && values2[j] == union[k]) {
                    intersectionSize++;
                    i++;
                    j++;
                    k++;
                } else
                    k++;
            }
        }

        return (double) intersectionSize / (double) union.length;
    }
}
