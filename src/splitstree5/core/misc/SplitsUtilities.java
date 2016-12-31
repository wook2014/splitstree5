/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.core.misc;

import jloda.util.Basic;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.datablocks.DistancesBlock;

import java.io.PrintStream;
import java.util.*;

/**
 * utilities for splits
 * Created by huson on 12/30/16.
 */
public class SplitsUtilities {
    /**
     * computes the least squares fit
     *
     * @param distancesBlock
     * @param splits
     * @return squares fit
     */
    public static float computeLeastSquaresFit(DistancesBlock distancesBlock, List<ASplit> splits) {
        final int ntax = distancesBlock.getNtax();
        final double[][] sdist = new double[ntax + 1][ntax + 1];

        double dsumSquare = 0;
        double ssumSquare = 0;
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                double dij = 0.0;
                for (ASplit split : splits) {
                    if (split.isContainedInA(i) != split.isContainedInA(j))
                        dij += split.getWeight();
                }
                sdist[i][j] = sdist[j][i] = dij;
            }
        }
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                double sij = sdist[i][j];
                double dij = distancesBlock.get(i, j);
                double x = Math.abs(sij - dij);
                ssumSquare += x * x;
                dsumSquare += dij * dij;
            }
        }

        return (float) (dsumSquare > 0 ? (100.0 * (1.0 - ssumSquare / dsumSquare)) : 0);
    }


    /**
     * Computes a cycle for the given splits system
     *
     * @param ntax   number of taxa
     * @param splits the splits
     */
    static public int[] computeCycle(int ntax, List<ASplit> splits) {
        try {
            final PrintStream pso = jloda.util.Basic.hideSystemOut();
            final PrintStream pse = jloda.util.Basic.hideSystemErr();
            try {
                return NeighborNet.computeNeighborNetOrdering(ntax, splitsToDistances(ntax, splits));
            } finally {
                jloda.util.Basic.restoreSystemErr(pse);
                jloda.util.Basic.restoreSystemOut(pso);
            }
        } catch (Exception ex) {
            Basic.caught(ex);
            final int[] order = new int[ntax + 1];
            for (int t = 1; t <= ntax; t++) {
                order[t] = t;
            }
            return order;
        }
    }

    /**
     * Given splits, returns the matrix split distances, as the number of splits separating each pair of taxa
     *
     * @param ntax
     * @param splits
     * @return distance matrix indexed 0 .. nTax-1 and 0 .. nTax-1
     */
    public static double[][] splitsToDistances(int ntax, List<ASplit> splits) {
        final double[][] dist = new double[ntax][ntax];

        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                for (ASplit split : splits) {
                    BitSet A = split.getA();

                    if (A.get(i) != A.get(j)) {
                        dist[i - 1][j - 1]++;
                        dist[j - 1][i - 1]++;
                    }
                }
            }
        }
        return dist;
    }

    /**
     * normalize cycle so that it is lexicographically smallest
     *
     * @param cycle
     * @return normalized cycle
     */
    public static int[] normalizeCycle(int[] cycle) {
        int posOf1 = -1;
        for (int i = 1; i < cycle.length; i++) {
            if (cycle[i] == 1) {
                posOf1 = i;
                break;
            }
        }
        int posPrev = (posOf1 == 1 ? cycle.length - 1 : posOf1 - 1);
        int posNext = (posOf1 == cycle.length - 1 ? 0 : posOf1 + 1);
        if (cycle[posPrev] > cycle[posNext]) { // has correct orientation, ensure that taxon 1 is at first position
            if (posOf1 != 1) {
                int[] tmp = new int[cycle.length];
                int i = posOf1;
                for (int j = 1; j < tmp.length; j++) {
                    tmp[j] = cycle[i];
                    if (++i == cycle.length)
                        i = 1;
                }
                return tmp;
            } else
                return cycle;
        } else // change orientation, as well
        {
            int[] tmp = new int[cycle.length];
            int i = posOf1;
            for (int j = 1; j < tmp.length; j++) {
                tmp[j] = cycle[i];
                if (--i == 0)
                    i = cycle.length - 1;
            }
            return tmp;
        }
    }

    /**
     * sort splits by decreasing weight
     *
     * @param splits
     */
    public static ArrayList<ASplit> sortByDecreasingWeight(List<ASplit> splits) {
        final ASplit[] array = splits.toArray(new ASplit[splits.size()]);
        Arrays.sort(array, new Comparator<ASplit>() {
            @Override
            public int compare(ASplit a, ASplit b) {
                if (a.getWeight() > b.getWeight())
                    return -1;
                else if (a.getWeight() < b.getWeight())
                    return 1;
                return 0;
            }
        });
        return new ArrayList<>(Arrays.asList(array)); // this construction ensures that the resulting list can grow
    }
}
