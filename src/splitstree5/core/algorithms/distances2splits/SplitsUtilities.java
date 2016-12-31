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

package splitstree5.core.algorithms.distances2splits;

import com.sun.istack.internal.Nullable;
import jloda.util.Basic;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.misc.ASplit;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

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
     * Determines whether a given splits system is (strongly) compatible
     *
     * @param splits the splits object
     * @return true, if the given splits are (strongly) compatible
     */
    static public boolean isCompatible(ArrayList<ASplit> splits) {
        for (int i = 0; i < splits.size(); i++)
            for (int j = i + 1; j < splits.size(); j++)
                if (!SplitsUtilities.areCompatible(splits.get(i), splits.get(j)))
                    return false;
        return true;
    }

    /**
     * determines whether two splits on the same taxa set are compatible
     *
     * @param split1
     * @param split2
     * @return true, if split1 and split2 are compatible
     */
    public static boolean areCompatible(ASplit split1, ASplit split2) {
        final BitSet A1 = split1.getA();
        final BitSet B1 = split1.getComplement();
        final BitSet A2 = split2.getA();
        final BitSet B2 = split2.getComplement();

        return !A1.intersects(A2) || !A1.intersects(B2) || !B1.intersects(A2) || !B1.intersects(B2);
    }


    /**
     * Determines whether a given splits system is weakly compatible
     *
     * @param splits the splits object
     * @return true, if the given splits are weakly compatible
     */
    static public boolean isWeaklyCompatible(ArrayList<ASplit> splits) {
        for (int i = 0; i < splits.size(); i++) {
            for (int j = i + 1; j < splits.size(); j++) {
                for (int k = j + 1; k < splits.size(); k++) {
                    if (!areWeaklyCompatible(splits.get(i), splits.get(j), splits.get(k)))
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * determines whether three splits on the same taxa set are weakly compatible
     *
     * @param split1
     * @param split2
     * @return true, if all three are weakly compatible
     */
    public static boolean areWeaklyCompatible(ASplit split1, ASplit split2, ASplit split3) {
        BitSet A1 = split1.getA();
        BitSet B1 = split1.getComplement();
        BitSet A2 = split2.getA();
        BitSet B2 = split2.getComplement();
        BitSet A3 = split3.getA();
        BitSet B3 = split3.getComplement();

        return !((intersects(A1, A2, A3)
                && intersects(A1, B2, B3)
                && intersects(B1, A2, B3)
                && intersects(B1, B2, A3))
                ||
                (intersects(B1, B2, B3)
                        && intersects(B1, A2, A3)
                        && intersects(A1, B2, A3)
                        && intersects(A1, A2, B3)));
    }


    /**
     * do the three  bitsets intersect?
     *
     * @param a
     * @param b
     * @param c
     * @return true, if non-empty   intersection
     */
    private static boolean intersects(BitSet a, BitSet b, BitSet c) {
        for (int i = a.nextSetBit(1); i >= 0; i = a.nextSetBit(i + 1))
            if (b.get(i) && c.get(i))
                return true;
        return false;
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
     * Determines whether a given splits system is cyclic
     *
     * @param ntax
     * @param splits the splits object
     * @return true, if the given splits are cyclic
     */
    public static boolean isCyclic(int ntax, List<ASplit> splits, @Nullable int[] cycle) {
        if (cycle == null)
            cycle = computeCycle(ntax, splits);

        int[] inverse = new int[ntax + 1];
        for (int t = 1; t <= ntax; t++)
            inverse[cycle[t]] = t;
        for (ASplit split : splits) {
            final BitSet A;
            if (split.isContainedInA(cycle[1]))     // avoid wraparound
                A = split.getComplement();
            else
                A = split.getA();

            int minA = ntax;
            int maxA = 1;
            for (int t = 1; t <= ntax; t++) {
                if (split.isContainedInA(t)) {
                    if (inverse[t] < minA)
                        minA = inverse[t];
                    if (inverse[t] > maxA)
                        maxA = inverse[t];
                }
            }
            if ((maxA - minA + 1) != A.cardinality()) {
                return false;
            }
        }
        return true;
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
}
