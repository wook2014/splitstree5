/*
 * SplitsUtilities.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.utils;

import jloda.util.*;
import splitstree5.core.algorithms.distances2splits.neighbornet.NeighborNetCycle;
import splitstree5.core.algorithms.distances2trees.NeighborJoining;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.core.misc.Compatibility;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * utilities for splits
 * Daniel Huson, 2005, 2016
 * Daria Evseeva,23.01.2017.
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
                return NeighborNetCycle.computeNeighborNetCycle(ntax, splitsToDistances(ntax, splits));
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
        final int posPrev = (posOf1 == 1 ? cycle.length - 1 : posOf1 - 1);
        final int posNext = (posOf1 == cycle.length - 1 ? 1 : posOf1 + 1);
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
        Arrays.sort(array, (a, b) -> {
            if (a.getWeight() > b.getWeight())
                return -1;
            else if (a.getWeight() < b.getWeight())
                return 1;
            return 0;
        });
        return new ArrayList<>(Arrays.asList(array)); // this construction ensures that the resulting list can grow
    }

    /**
     * is split circular with respect to the given cycle?
     *
     * @param taxa
     * @param cycle uses indices 1 to number-of-taxa
     * @param split
     * @return true if circular
     */
    public static boolean isCircular(TaxaBlock taxa, int[] cycle, ASplit split) {
        final BitSet part = (!split.getA().get(cycle[1]) ? split.getA() : split.getB()); // choose part that doesn't go around the horn
        int prev = 0;
        for (int t = 1; t <= taxa.getNtax(); t++) {
            if (part.get(cycle[t])) {
                if (prev != 0 && t != prev + 1)
                    return false;
                prev = t;
            }
        }
        return true;
    }

    /**
     * verify that all splits are proper and are contained in the taxon set
     *
     * @param splits
     * @param taxa
     * @throws SplitsException
     */
    public static void verifySplits(Collection<ASplit> splits, TaxaBlock taxa) throws SplitsException {
        final Set<BitSet> seen = new HashSet<>();

        for (ASplit split : splits) {
            final BitSet aSet = split.getA();
            if (seen.contains(aSet))
                throw new SplitsException("Split " + aSet + " occurs multiple times");
            if (aSet.cardinality() == 0)
                throw new SplitsException("Split " + aSet + " not proper, size is 0");
            if (aSet.cardinality() == taxa.getNtax())
                throw new SplitsException("Split " + aSet + " not proper, size is ntax");
            if (aSet.nextSetBit(0) == 0 || aSet.nextSetBit(taxa.getNtax() + 1) != -1)
                throw new SplitsException("Split " + aSet + " not contained in taxa set <" + taxa.getTaxaSet() + ">");
            seen.add(aSet);
        }
    }

    /**
     * Determines the fit of a splits system, ie how well it
     * represents a given distance matrix, in percent. Computes two different values.
     * //ToDo: Fix variances.
     * // todo no lsfit?
     *
     * @param forceRecalculation always recompute the fit, even if there is a valid value stored.
     * @param splits             the splits
     * @param dist               the distances
     */
    static public void computeFits(boolean forceRecalculation, SplitsBlock splits, DistancesBlock dist, ProgressListener pl) {
        float dsum = 0;
        float ssum = 0;
        float dsumSquare = 0;
        float ssumSquare = 0;
        float netsumSquare = 0;

        int ntax = dist.getNtax();

        if (splits == null || dist == null)
            return;

        if (!forceRecalculation && splits.getFit() >= 0)
            return; //No need to recalculate.

        splits.setFit(-1);
        //splits.getProperties().setLSFit(-1); //A fit of -1 means that we don't have a valid value.

        pl.setSubtask("Recomputing fit");

        double[][] sdist = new double[ntax + 1][ntax + 1];

        for (int i = 1; i <= ntax; i++) {
            sdist[i][i] = 0;
            for (int j = i + 1; j <= ntax; j++) {
                float dij = 0;
                for (int s = 1; s <= splits.getNsplits(); s++) {
                    BitSet split = splits.getSplits().get(s - 1).getA();
                    if (split.get(i) != split.get(j))
                        dij += splits.getSplits().get(s - 1).getWeight();
                }
                sdist[i][j] = sdist[j][i] = dij;
            }
        }
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++) {
                double sij = sdist[i][j];
                double dij = dist.get(i, j);
                double x = Math.abs(sij - dij);
                ssum += x;
                ssumSquare += x * x;
                dsum += dij;
                dsumSquare += dij * dij;
                netsumSquare += sij * sij;
            }
        }
        float fit = 100 * (1 - ssum / dsum);
        fit = Math.max(fit, 0);
        splits.setFit(fit);

        double lsfit = 100.0 * (1.0 - ssumSquare / dsumSquare);


        lsfit = Math.max(lsfit, 0.0);
        //splits.getProperties().setLSFit(lsfit);

        double stress = Math.sqrt(ssumSquare / netsumSquare);

        System.err.println("\nRecomputed fit:\n\tfit = " + fit + "\n\tLS fit =" + lsfit + "\n\tstress =" + stress + "\n");
    }

    public static void rotateCycle(int[] cycle, int first) {
        final int[] tmp = new int[2 * cycle.length - 1];
        System.arraycopy(cycle, 0, tmp, 0, cycle.length);
        System.arraycopy(cycle, 1, tmp, cycle.length, cycle.length - 1);
        for (int i = 1; i < tmp.length; i++) {
            if (tmp[i] == first) {
                for (int j = 1; j < cycle.length; j++) {
                    cycle[j] = tmp[i++];
                }
                return;
            }
        }
    }

    public static int getTighestSplit(BitSet taxa, SplitsBlock splitsBlock) {
        int best = 0;
        int bestSideCardinality = Integer.MAX_VALUE;
        for (int s = 1; s <= splitsBlock.getNsplits(); s++) {
            final ASplit split = splitsBlock.get(s);
            if (BitSetUtils.contains(split.getA(), taxa) && split.getA().cardinality() < bestSideCardinality) {
                best = s;
                bestSideCardinality = split.getA().cardinality();
            }
            if (BitSetUtils.contains(split.getB(), taxa) && (split.getB().cardinality() < bestSideCardinality)) {
                best = s;
                bestSideCardinality = split.getB().cardinality();
            }
        }
        return best;
    }

    /**
     * computes the midpoint root location
     *
     * @param alt         use alternative side
     * @param ntaxa       number of taxa
     * @param outgroup    outgroup taxa or empty, if performing simple midpoint rooting
     * @param cycle
     * @param splitsBlock
     * @param useWeights  use split weights or otherwise give all splits weight 1
     * @param progress
     * @return rooting split and both distances
     * @throws CanceledException
     */
    public static Triplet<Integer, Double, Double> computeRootLocation(boolean alt, int ntaxa, Set<Integer> outgroup, int[] cycle, SplitsBlock splitsBlock, boolean useWeights, ProgressListener progress) throws CanceledException {
        progress.setSubtask("Computing root location");

        final double[][] distances = new double[ntaxa + 1][ntaxa + 1];
        for (ASplit split : splitsBlock.getSplits()) {
            for (int a : BitSetUtils.members(split.getA())) {
                for (int b : BitSetUtils.members(split.getB()))
                    distances[a][b] += useWeights ? split.getWeight() : 1;
            }
        }
        final Set<Integer> setA = new TreeSet<>();
        final Set<Integer> setB = new TreeSet<>();
        if (outgroup.size() > 0) {
            setA.addAll(outgroup);
            setB.addAll(IntStream.rangeClosed(1, ntaxa).filter(i -> !outgroup.contains(i)).boxed().collect(Collectors.toList()));
        } else {
            setA.addAll(IntStream.rangeClosed(1, ntaxa).boxed().collect(Collectors.toList()));
            setB.addAll(setA);
        }

        double maxDistance = 0;
        final Pair<Integer, Integer> furthestPair = new Pair<>(0, 0);

        for (int a : setA) {
            for (int b : setB) {
                if (b != a && distances[a][b] > maxDistance) {
                    maxDistance = distances[a][b];
                    furthestPair.set(a, b);
                }
            }
        }

        final Map<ASplit, Integer> split2id = new HashMap<>();

        final ArrayList<ASplit> splits = new ArrayList<>();
        for (int s = 1; s <= splitsBlock.getNsplits(); s++) {
            final ASplit split = splitsBlock.get(s);
            if (split.separates(furthestPair.getFirst(), furthestPair.getSecond())) {
                splits.add(split);
                split2id.put(split, s);
            }
        }

        final BitSet interval = computeInterval(ntaxa, furthestPair.getFirst(), furthestPair.getSecond(), cycle, alt);

        splits.sort((s1, s2) -> {
            final BitSet a1 = s1.getPartContaining(furthestPair.getFirst());
            final BitSet a2 = s2.getPartContaining(furthestPair.getFirst());
            final int size1 = BitSetUtils.intersection(a1, interval).cardinality();
            final int size2 = BitSetUtils.intersection(a2, interval).cardinality();

            if (size1 < size2)
                return -1;
            else if (size1 > size2)
                return 1;
            else
                return Integer.compare(a1.cardinality(), a2.cardinality());
        });

        double sum = 0;
        for (ASplit split : splits) {
            final double weight = (useWeights ? split.getWeight() : 1);
            final double delta = (sum + weight - 0.5 * maxDistance);
            if (delta > 0) {
                return new Triplet<>(split2id.get(split), delta, weight - delta);
                //return new Triplet<>(split2id.get(split), weight - delta, delta);
            }
            sum += weight;
        }
        return new Triplet<>(1, 0.0, useWeights ? splitsBlock.get(1).getWeight() : 1);
    }

    private static BitSet computeInterval(int ntaxa, int a, int b, int[] cycle, boolean alt) {
        final BitSet set = new BitSet();

        if (cycle.length > 0) {
            if (alt) {
                boolean in = false;
                int i = cycle.length - 1;
                while (true) {
                    if (cycle[i] == a) {
                        set.set(a);
                        in = true;
                    }
                    if (in && cycle[i] == b) {
                        break;
                    }
                    if (i == 1)
                        i = cycle.length - 1;
                    else
                        i--;
                }
            } else {
                boolean in = false;
                int i = 1;
                while (true) {
                    if (cycle[i] == a) {
                        set.set(a);
                        in = true;
                    }
                    if (in && cycle[i] == b) {
                        break;
                    }
                    if (i >= cycle.length - 1)
                        i = 1;
                    else
                        i++;
                }
            }
        }
        return set;
    }

    public static boolean computeSplitsForLessThan4Taxa(TaxaBlock taxaBlock, DistancesBlock distancesBlock, SplitsBlock splitsBlock) throws CanceledException {
        if (taxaBlock.getNtax() < 4) {
            final TreesBlock treesBlock = new TreesBlock();
            new NeighborJoining().compute(new ProgressSilent(), taxaBlock, distancesBlock, treesBlock);
            splitsBlock.clear();
            TreesUtilities.computeSplits(taxaBlock.getTaxaSet(), treesBlock.getTree(1), splitsBlock.getSplits());
            splitsBlock.setCompatibility(Compatibility.compatible);
            splitsBlock.setCycle(SplitsUtilities.computeCycle(taxaBlock.getNtax(), splitsBlock.getSplits()));
            splitsBlock.setFit(100);
            return true;
        }
        return false;
    }
}
