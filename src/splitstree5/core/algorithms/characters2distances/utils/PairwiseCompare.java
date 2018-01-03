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

package splitstree5.core.algorithms.characters2distances.utils;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.models.SubstitutionModel;
import splitstree5.utils.nexus.SplitsException;


/**
 * Computes pairwise distances
 *
 * @author bryant
 */

//ToDo: Add support for CharWeights

public class PairwiseCompare {
    public enum HandleAmbiguous {Ignore, Average, Match}

    private final int numStates;
    private int numNotMissing;
    private int numActive;
    private final double[][] Fcount; /* Stored as doubles, to handle ambiguities and character weights*/

    /**
     * constructor
     *
     * @param characters
     * @param states
     * @param i
     * @param j
     * @param handleAmbig
     * @throws SplitsException
     */
    public PairwiseCompare(final CharactersBlock characters, final String states, final int i, final int j, final HandleAmbiguous handleAmbig) throws SplitsException {
        final char gapchar = characters.getGapCharacter();
        final char missingchar = characters.getMissingCharacter();

        numStates = states.length();

        /* The Fcount matrix has rows and columns for missing and gap states as well */
        Fcount = new double[numStates + 2][numStates + 2];

        int gapindex = numStates;
        int missingindex = numStates + 1;

        numNotMissing = 0;
        numActive = characters.getNchar();

        for (int k = 1; k <= characters.getNchar(); k++) {
            final char ci = characters.get(i, k);
            final char cj = characters.get(j, k);
            final double charWeight = characters.getCharacterWeight(k);

            if (ci != missingchar && ci != gapchar && cj != missingchar && cj != gapchar)
                numNotMissing++;

            //Handle ambiguouos states?
            final boolean ambigi, ambigj;
            if (characters.isHasAmbiguousStates() && (handleAmbig != HandleAmbiguous.Ignore)) {
                ambigi = characters.isAmbiguityCode(i, k);
                ambigj = characters.isAmbiguityCode(j, k);
            } else {
                ambigi = ambigj = false;
            }

            if (ambigi || ambigj) {
                //ToDo: store a map from the ambig codes to the difference to avoid these computations.

                final String si = characters.getNucleotidesForAmbiguityCode(i, k);
                final String sj = characters.getNucleotidesForAmbiguityCode(j, k);

                //Two cases... if they are the same states, then this needs to be distributed
                //down the diagonal of F. Otherwise, average.

                if (si.equalsIgnoreCase(sj)) {
                    double weight = 1.0 / si.length();
                    for (int pos = 0; pos < si.length(); pos++) {
                        int statei = states.indexOf(si.charAt(pos));
                        Fcount[statei][statei] += weight * charWeight;
                    }
                } else if (handleAmbig == HandleAmbiguous.Average) {
                    double weight = 1.0 / (si.length() * sj.length());

                    for (int x = 0; x < si.length(); x++) {
                        for (int y = 0; y < sj.length(); y++) {
                            final int cx = si.charAt(x);
                            final int cy = sj.charAt(y);
                            int statex = states.indexOf(cx);
                            int statey = states.indexOf(cy);
                            if (cx == gapchar) statex = gapindex;
                            if (cx == missingchar) statex = missingindex;
                            if (cy == gapchar) statey = gapindex;
                            if (cy == missingchar) statey = missingindex;
                            if (statex >= 0 && statey >= 0)
                                Fcount[statex][statey] += weight * charWeight;
                            else {
                                if (statex < 0)
                                    throw new SplitsException("Position " + k + " for taxa " + i + " is the invalid character " + cx);
                                else if (statey < 0)
                                    throw new SplitsException("Position " + k + " for taxa " + j + " is the invalid character " + cy);
                            }
                        }
                    }
                }
            } else {
                int statei = states.indexOf(ci);
                int statej = states.indexOf(cj);
                if (ci == gapchar) statei = gapindex;
                if (ci == missingchar) statei = missingindex;
                if (cj == gapchar) statej = gapindex;
                if (cj == missingchar) statej = missingindex;
                if (statei >= 0 && statej >= 0)
                    Fcount[statei][statej] += charWeight;
                else {
                    if (statei < 0)
                        throw new SplitsException("Position " + k + " for taxa " + i + " is the invalid character " + ci);
                    else if (statej < 0)
                        throw new SplitsException("Position " + k + " for taxa " + j + " is the invalid character " + cj);
                }
            }
        }
    }


    /**
     * Number of sites that are not masked
     *
     * @return numActive
     */
    public int getNumActive() {
        return numActive;
    }

    /**
     * Number of active sites with gaps or missing states in one or both sequences
     *
     * @return numActive - numNotMissing
     */
    public int getNumGaps() {
        return getNumActive() - getNumNotMissing();
    }

    /**
     * Number of active sites with valid, non-gap or non-missing states for both seqs.
     * This number also includes the number of sites where one or other
     * is ambiguous.... not completely accurate really.
     *
     * @return numNotMissing
     */
    public int getNumNotMissing() {
        return numNotMissing;
    }

    /**
     * Number of states (the number of valid symbols)
     *
     * @return numStates
     */
    public int getNumStates() {
        return numStates;
    }


    /**
     * Returns matrix containing the number of sites for each kind of transition
     *
     * @return Fcound
     */
    public double[][] getFcount() {
        return Fcount;
    }

    /**
     * Frequency matrix - returns matrix containing the proportion of each kind of site
     *
     * @return proportions. If no valid sites, returns proportion of 1.
     */
    public double[][] getF() {
        double[][] F = new double[getNumStates()][getNumStates()];
        double Fsum = 0.0;
        if (getNumNotMissing() > 0) {
            for (int i = 0; i < getNumStates(); i++)
                for (int j = 0; j < getNumStates(); j++)
                    Fsum += Fcount[i][j];


            for (int i = 0; i < getNumStates(); i++) {
                for (int j = 0; j < getNumStates(); j++) {
                    F[i][j] =
                            Fcount[i][j] / Fsum;
                }
            }
        } else {
            F = null;
            //TODO: This should probably throw an 'undefinedDistance' exception.
            //System.err.println("Missing distance");
        }
        return F;
    }

    public double[][] getExtendedF() {
        double[][] F = new double[getNumStates() + 2][getNumStates() + 2];
        if (getNumActive() > 0) {
            for (int i = 0; i < getNumStates() + 2; i++) {
                for (int j = 0; j < getNumStates() + 2; j++) {
                    F[i][j] =
                            Fcount[i][j] / (double) getNumActive();
                }
            }
        } else {
            F = null;
            //System.err.println("Missing distance");
        }
        return F;
    }

    /**
     * Returns negative log likelihood of a given F matrix and t value
     *
     * @param model
     * @param F
     * @param t
     * @return negative log likelihood [double]
     */
    private double evalL(SubstitutionModel model, double[][] F, double t) {

        int numstates = model.getNstates();
        double logL = 0.0;
        for (int i = 0; i < numstates; i++) {
            for (int j = 0; j < numstates; j++) {
                if (F[i][j] != 0.0)
                    logL += F[i][j] * Math.log(model.getX(i, j, t));
            }
        }
        return -logL;

    }

    private double goldenSection(
            SubstitutionModel model,
            double[][] F,
            double tmin,
            double tmax) {

        double a, b, tau, aa, bb, faa, fbb;
        tau = 2.0 / (1.0 + Math.sqrt(5.0)); //Golden ratio
        double GS_EPSILON = 0.000001;
        int nSteps;

        nSteps = 0;

        a = tmin;
        b = tmax;
        aa = a + (1.0 - tau) * (b - a);
        bb = a + tau * (b - a);
        faa = evalL(model, F, aa);
        fbb = evalL(model, F, bb);

        while ((b - a) > GS_EPSILON) {
            nSteps++;
            // cout<<"["<<a<<","<<aa<<","<<bb<<","<<b<<"] \t \t ("<<faa<<","<<fbb<<")"<<endl;

            if (faa < fbb) {
                b = bb;
                bb = aa;
                fbb = faa;
                aa = a + (1.0 - tau) * (b - a);
                faa = evalL(model, F, aa);
                //System.out.println("faa was the smallest at this iteration :" + faa);
            } else {
                a = aa;
                aa = bb;
                faa = fbb;
                bb = a + tau * (b - a);
                fbb = evalL(model, F, bb);
                //System.out.println("fbb was the smallest at this iteration :" + fbb);
            }
        }

        return b;
    }

    /**
     * Max Likelihood Distance - returns maximum likelihood distance for a given substitution
     * model.
     *
     * @param model Substitution model in use
     * @return distance
     * @throws SaturatedDistancesException distance undefined if saturated (distance more than 10 substitutions per site)
     */
    public double mlDistance(SubstitutionModel model) throws SaturatedDistancesException {
        double t;
        double dist = 0.0;

        //TODO: Replace the golden arc method with Brent's algorithm
        double[][] fullF = getF();
        double[][] F;
        int nstates = model.getNstates();

        F = new double[nstates][nstates];

        double k = 0.0;
        for (int i = 0; i < nstates; i++) {
            for (int j = 0; j < nstates; j++) {
                double Fij = fullF[i][j];
                F[i][j] = Fij;
                k += Fij;
            }
        }
        for (int i = 0; i < nstates; i++) {
            for (int j = 0; j < nstates; j++) {
                F[i][j] /= k; /* Rescale so the entries sum to 1.0 */
            }
        }

        t = goldenSection(model, F, 0.00000001, 2.0);
        if (t == 2.0) {
            t = goldenSection(model, F, 2.0, 10.0);
            if (t == 10.0) {
                throw new SaturatedDistancesException();
            }
        }
        return t * model.getRate();
    }

    public double bulmerVariance(double dist, double b) {

        return (Math.exp(2 * dist / b) * b * (1 - Math.exp(-dist / b)) * (1 - b + b * Math.exp(-dist / b))) / ((double) this.getNumNotMissing());
    }
}