package splitstree5.core.algorithms.characters2distances;

import Jama.Matrix;
import jloda.util.Alert;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.characters2distances.utils.PairwiseCompare;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

public class LogDet extends Algorithm<CharactersBlock, DistancesBlock> implements IFromChararacters, IToDistances {

    private PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates = PairwiseCompare.HandleAmbiguous.Ignore;

    public final static String DESCRIPTION = "Calculates the logdet- distance";
    private boolean fudgeFactor = false;
    private boolean fillZeros = false;
    private double pInvar;

    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        int ntax = charactersBlock.getNtax();
        progressListener.setTasks("logDet distance", "Init.");
        progressListener.setMaximum(ntax);
        distancesBlock.setNtax(ntax);
        int numUndefined = 0;

        for (int t = 1; t <= ntax; t++) {
            for (int s = t + 1; s <= ntax; s++) {
                String states = charactersBlock.getSymbols();
                PairwiseCompare seqPair = new PairwiseCompare(charactersBlock, states, s, t, optionHandleAmbiguousStates);
                double dist = -1.0;

                int r = seqPair.getNumStates();

                double[][] F = seqPair.getF();
                if (F == null) {
                    numUndefined++;
                } else {
                    if (this.fudgeFactor) {
                        /* LDDist 1.2 implements some questionable tricks to avoid singluar matrices. To enable
                   comparisons, I've implemented these here. */
                        double[][] extF = seqPair.getFcount();

                        double[] rowsum = new double[r];
                        double[] colsum = new double[r];
                        double[] rowgaps = new double[r]; //sum of gap and missng cols
                        double[] colgaps = new double[r]; //sum of gap and missing rows
                        for (int i = 0; i < r + 2; i++) {
                            for (int j = 0; j < r + 2; j++) {
                                if (i < r && j < r) {
                                    rowsum[i] += extF[i][j];
                                    colsum[j] += extF[i][j];
                                } else if (i < r && j >= r) {
                                    rowgaps[i] += extF[i][j];
                                } else if (i >= r && j < r) {
                                    colgaps[j] += extF[i][j];
                                }
                            }
                        }

                        /* add fudge factors from sites with gap or missing */
                        for (int i = 0; i < r; i++) {
                            for (int j = 0; j < r; j++) {
                                double fudgei = 0.0, fudgej = 0.0;
                                if (rowsum[i] != 0) fudgei = rowgaps[i] / rowsum[i];
                                if (colsum[j] != 0) fudgej = colgaps[j] / colsum[j];
                                F[i][j] = extF[i][j] * (1.0 + fudgei + fudgej);
                            }
                        }

                        /* Replace zeros with small numbers !?! but only in rows/columns with values present*/
                        double Fsum = 0.0;
                        for (int i = 0; i < r; i++) {
                            if (rowsum[i] == 0) continue;
                            for (int j = 0; j < r; j++) {
                                if (this.fillZeros && colsum[j] != 0 && F[i][j] < 0.5) F[i][j] = 0.5;
                                Fsum += F[i][j];
                            }
                        }
                        /*Normalise */
                        for (int i = 0; i < r; i++)
                            for (int j = 0; j < r; j++)
                                F[i][j] /= Fsum;

                    }

                    /* Determine base frequencies */
                    double[] Pi_x = new double[r];
                    double[] Pi_y = new double[r];
                    double[] Pi = new double[r];
                    for (int i = 0; i < r; i++)
                        Pi_x[i] = Pi_y[i] = Pi[i] = 0.0;

                    for (int i = 0; i < r; i++)
                        for (int j = 0; j < r; j++) {
                            double Fij = F[i][j];
                            Pi_x[i] += Fij;
                            Pi_y[j] += Fij;
                        }


                    for (int i = 0; i < r; i++)
                        Pi[i] = (Pi_x[i] + Pi_y[i]) / 2.0;

                    double logPi = 0.0;
                    for (int i = 0; i < r; i++)
                        if (Pi_x[i] != 0.0 && Pi_y[i] != 0.0)
                            logPi += Math.log(Pi_x[i]) + Math.log(Pi_y[i]);
                    logPi *= 0.5;

                    /* Compute Log Det */

                    /* Incorporate proportion of invariable sites */
                    double pinv = getOptionPInvar();
                    if (pinv > 0.0)
                        for (int i = 0; i < r; i++)
                            F[i][i] -= pinv * Pi[i];

                    Matrix Fmatrix = new Matrix(F);
                    double[] Feigs = Fmatrix.eig().getRealEigenvalues();
                    double x = 0.0;
                    boolean thisIsSaturated = false;
                    for (double Feig : Feigs) {
                        if (Feig <= 0.0)
                            thisIsSaturated = true;
                        else
                            x += Math.log(Feig);
                    }
                    /* now x =  trace(log(F)) = log(det(F)) */
                    if (thisIsSaturated) {
                        numUndefined++;
                        x = -10000000;
                    }

                    double PiSum = 0;
                    for (int i = 0; i < r; i++) {
                        PiSum += Pi[i] * Pi[i];
                    }

                    dist = -(1.0 - PiSum) / (r - 1.0) * (x - logPi);
                }
                distancesBlock.set(s, t, dist);
                distancesBlock.set(t, s, dist);

            }
            //doc.notifySetProgress(t * 100 / taxa.getNtax());
            progressListener.incrementProgress();
        }

        if (numUndefined > 0) {
            new Alert("Warning: there were saturated or missing distances in the matrix. These have been replaced with arbitrary large values - proceed with caution ");
        }

        progressListener.close();
    }



    public boolean isApplicable(TaxaBlock taxa, CharactersBlock characters) {
       //todo
       return true;
    }

    // GETTER AND SETTER

    public PairwiseCompare.HandleAmbiguous getOptionHandleAmbiguousStates() {
        return optionHandleAmbiguousStates;
    }

    public void setOptionHandleAmbiguousStates(PairwiseCompare.HandleAmbiguous optionHandleAmbiguousStates) {
        this.optionHandleAmbiguousStates = optionHandleAmbiguousStates;
    }

    /**
     * gets a short description of the algorithm
     *
     * @return a description
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Gets flag of whether missing entries in the F matrix are imputed using the method
     * that LDDist uses.
     *
     * @return boolean
     */
    public boolean getOptionImpute_Gaps() {
        return fudgeFactor;
    }

    /**
     * Sets flag of whether missing entries in the F matrix are imputed, using the method that LDDist uses.
     *
     * @param val
     */
    public void setOptionImpute_Gaps(boolean val) {
        fudgeFactor = val;
    }

    /**
     * Sets proportion of invariable sites used when computing log det.
     *
     * @return double: proportion being used.
     */
    public double getOptionPInvar() {
        return pInvar;
    }

    /**
     * Set proportion of invariable sites to use for log det.
     *
     * @param pInvar
     */
    public void setOptionPInvar(double pInvar) {
        this.pInvar = pInvar;
    }
}
