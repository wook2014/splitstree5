/*
 * Copyright (C) 2015 Daniel H. Huson and David J. Bryant
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

package splitstree5.core.analysis;


import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.CharactersBlock;

import java.util.Arrays;
import java.util.Random;

/**
 * Estimates the proportion of invariant sites using capture-recapture
 */
public class CaptureRecapture {
    public static String DESCRIPTION = "Estimation of invariant sites using capture-recapture method (Lockhart, Huson, Steel, 2000)";
    int optionTaxaCutoff = 20; // Cut off before we switch to sampling

    /**
     * gets a description of the method
     *
     * @return description
     */
    public String getDescription() {
        return DESCRIPTION;
    }


    /**
     * Chooses a random (small) subset of size elements in [1...n]
     *
     * @param size
     * @param n
     * @param random
     * @return array of size with numbers from [1...n]
     */
    private static int[] randomSubset(int size, int n, Random random) {
        final int[] s = new int[size];
        for (int i = 0; i < size; i++) {
            int x = random.nextInt(n - i) + 1; //random integer from 1 to n-i
            for (int j = 0; j < i; j++) {      //Make sure that its unique
                if (x >= s[j])
                    x++;
            }
            s[i] = x;
        }
        Arrays.sort(s);
        return s;
    }

    /**
     * Checks to see that, for site m, the taxa in q are not missing, gaps, etc.
     *
     * @param block
     * @param q     array of taxa ids
     * @param m     site
     * @return true iff all not missing, not gaps, and site not masked
     */
    private static boolean goodSite(CharactersBlock block, int[] q, int m) {
        for (int aQ : q) {
            char ch = block.get(aQ, m);
            if (ch == block.getMissingCharacter())
                return false;
            if (ch == block.getGapCharacter())
                return false;
        }
        return true;
    }

    /**
     * Computes v statistic (Steel etal) for the quartet q
     *
     * @param q
     * @param block
     * @return v score
     */

    private static double vscore(int[] q, CharactersBlock block) {

        int nsites = block.getNchar();
        int ngood = 0; //Number of sites without gaps in all four

        int f_ij_kl, f_ik_jl, f_il_jk;
        int f_ij, f_ik, f_il, f_jk, f_jl, f_kl;
        f_ij_kl = f_ik_jl = f_il_jk = 0;
        f_ij = f_ik = f_il = f_jk = f_jl = f_kl = 0;

        int nconst = 0;

        final char[] s = new char[4];

        for (int m = 1; m <= nsites; m++) {
            if (!goodSite(block, q, m))
                continue;
            ngood++;

            for (int a = 0; a < 4; a++)
                s[a] = block.get(q[a], m);


            if (s[0] != s[1])
                f_ij++;
            if (s[0] != s[2])
                f_ik++;
            if (s[0] != s[3])
                f_il++;
            if (s[1] != s[2])
                f_jk++;
            if (s[1] != s[3])
                f_jl++;
            if (s[2] != s[3])
                f_kl++;
            if ((s[0] != s[1]) && (s[2] != s[3]))
                f_ij_kl++;
            if ((s[0] != s[2]) && (s[1] != s[3]))
                f_ik_jl++;
            if ((s[0] != s[3]) && (s[1] != s[2]))
                f_il_jk++;
            if (s[0] == s[1] && s[0] == s[2] && s[0] == s[3])
                nconst++;
        }

        if (ngood == 0)
            return 100.0;   //Returns an impossible amount - says choose another.

        double v = 1.0 - (double) nconst / ngood;
        if (f_ij_kl > 0)
            v = Math.max(v, (double) f_ij * f_kl / f_ij_kl / ngood);
        if (f_ik_jl > 0)
            v = Math.max(v, (double) f_ik * f_jl / f_ik_jl / ngood);
        if (f_il_jk > 0)
            v = Math.max(v, (double) f_il * f_jk / f_il_jk / ngood);

        v = Math.min(v, 1.0);
        //System.err.println(q+"\tv = "+ v);
        return v;
    }


    /**
     * Computes the proportion of Invariance sites using Steel et al.'s method
     *
     * @param chars
     * @return proportion assumed invariant
     */
    public double estimatePropInvariableSites(ProgressListener progress, CharactersBlock chars) throws CanceledException {
        final int nchar = chars.getNtax();
        final int maxsample = (nchar * (nchar - 1) * (nchar - 2) * (nchar - 3)) / 24;

        double vsum = 0.0;
        int count = 0;

        if (nchar > optionTaxaCutoff) {
            //Sampling          - we do a minimum of 1000, and stop once |sd| is less than 0.05 |mean|
            progress.setMaximum(2000);
            progress.setProgress(0);

            final Random random = new Random();
            int[] q = new int[4];
            double sum2 = 0.0;
            boolean done = false;
            int iter = 0;

            while (!done) {
                iter++;
                q = randomSubset(4, nchar, random);
                double v = vscore(q, chars);
                if (v > 1.0)
                    continue; //Invalid quartet.
                vsum += v;
                sum2 += v * v;
                count++;
                if (count > 1000) {
                    //Evaluate how good the stdev is.
                    double mean = vsum / count;
                    double var = sum2 / count - mean * mean;
                    double sd = Math.sqrt(var);
                    if (Math.abs(sd / mean) < 0.05)
                        done = true;
                    // System.err.println("Mean = " + mean + " sd = " + sd);
                }
                if (iter > maxsample) {
                    done = true; //Safety check to prevent infinite loop
                }
                progress.incrementProgress();
            }
        } else { //Exact count
            progress.setMaximum(nchar);
            progress.setProgress(0);
            for (int i = 1; i <= nchar; i++) {
                for (int j = i + 1; j <= nchar; j++) {
                    for (int k = j + 1; k <= nchar; k++) {
                        for (int l = k + 1; l <= nchar; l++) {
                            int[] q = new int[4];
                            q[0] = i;
                            q[1] = j;
                            q[2] = k;
                            q[3] = l;
                            vsum += vscore(q, chars);
                            count++;
                        }
                    }
                }
                progress.incrementProgress();
            }
        }

        return vsum / count;

    }

    public int getOptionTaxaCutoff() {
        return optionTaxaCutoff;
    }

    public void setOptionTaxaCutoff(int optionTaxaCutoff) {
        this.optionTaxaCutoff = optionTaxaCutoff;
    }

}
