/*
 * NeighborNetSplitWeightOptimizer.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.distances2splits.neighbornet;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.distances2splits.neighbornet.NeighborNetPCG.NeighborNetBlockPivot;
import splitstree5.core.misc.ASplit;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Given a circular ordering and a distance matrix,
 * computes the unconstrained or constrained least square weighted splits
 * <p>
 * For all vectors, the canonical ordering of pairs is (0,1),(0,2),...,(0,n-1),(1,2),(1,3),...,(1,n-1), ...,(n-1,n)
 * <p>
 * (i,j) ->  (2n - i -3)i/2 + j-1  .
 * <p>
 * Increase i -> increase index by n-i-2
 * Decrease i -> decrease index by n-i-1.
 * <p>
 * <p>
 * x[i][j] is the split {i+1,i+2,...,j} | -------
 * <p>
 * David Bryant and Daniel Huson, 2005
 */
public class NeighborNetSplits {
    public enum LeastSquares {ols, fm1, fm2, estimated}

    public enum Regularization {nnls, lasso, normlasso, internallasso}

    static final double CG_EPSILON = 0.0001;     /* Epsilon constant for the conjugate gradient algorithm */

    /**
     * Compute optimal weight squares under least squares for Splits compatible with a circular ordering.
     * This version carries out adaptive L1 regularisation, controlled by the parameter lambdaFraction.
     * That is, it minimizes  0.5*||Ax - d||^2_2  +  \lambda ||x||_1
     *
     * @param runPCG
     * @param nTax           number of taxa
     * @param cycle          taxon cycle, 1-based
     * @param distances      pairwise distances, 0-based
     * @param cutoff         min split weight
     * @param leastSquares   least square mode
     * @param regularization use regularization
     * @param lambdaFrac     the lambda fraction
     * @param progress       progress listener
     * @return weighted splits
     * @throws CanceledException
     */
    static public ArrayList<ASplit> compute(boolean runPCG, int nTax, int[] cycle, double[][] distances, double[][] variances, double cutoff, LeastSquares leastSquares, Regularization regularization, double lambdaFrac, ProgressListener progress) throws CanceledException {
        //Handle n=1,2 separately.
        if (nTax == 1) {
            return new ArrayList<>();
        }
        if (nTax == 2) {
            final ArrayList<ASplit> splits = new ArrayList<>();
            float d_ij = (float) distances[cycle[1] - 1][cycle[2] - 1];
            if (d_ij > 0.0) {
                final BitSet A = new BitSet();
                A.set(cycle[1]);
                splits.add(new ASplit(A, 2, d_ij));
            }
            return splits;
        }
        final int nPairs = (nTax * (nTax - 1)) / 2;

        if (runPCG) {
            //Set up the distance vector.

            final double[] d = new double[nPairs + 1];
            {
                int index = 1;
                for (int i = 1; i <= nTax; i++) {
                    for (int j = i + 1; j <= nTax; j++) {
                        d[index++] = distances[cycle[i] - 1][cycle[j] - 1];
                    }
                }
            }

            System.err.print("d=[");
            for (int i = 1; i <= nPairs; i++) {
                System.out.print(d[i] + " ");
            }
            System.err.println("];");

            NeighborNetBlockPivot.BlockPivotParams params = new NeighborNetBlockPivot.BlockPivotParams();
            double[] x = NeighborNetBlockPivot.circularBlockPivot(nTax, d, progress, params);
            final ArrayList<ASplit> splits = new ArrayList<>();

            int index = 1;
            for (int i = 1; i <= nTax; i++) {
                final BitSet A = new BitSet();
                for (int j = i + 1; j <= nTax; j++) {
                    A.set(cycle[j - 1]);
                    if (x[index] > cutoff)
                        splits.add(new ASplit(A, nTax, (float) (x[index])));
                    index++;
                }
            }
            return splits;
        }

        /* Re-order taxa so that the ordering is 0,1,2,...,n-1 */
        final double[] d = setupD(nTax, nPairs, distances, cycle);
        final double[] v = setupV(nTax, nPairs, distances, variances, leastSquares, cycle);
        final double[] x = new double[nPairs];

        /* Initialize the weight matrix */
        final double[] W = new double[nPairs];
        for (int k = 0; k < nPairs; k++) {
            if (v[k] == 0.0)
                W[k] = 10E10;
            else
                W[k] = 1.0 / v[k];
        }
        /* Find the constrained optimal values for x */

        runActiveConjugate(nTax, nPairs, d, W, x, regularization, lambdaFrac);

        /* Construct the splits with the appropriate weights */
        final ArrayList<ASplit> splits = new ArrayList<>();

        int index = 0;
        for (int i = 1; i <= nTax; i++) {
            final BitSet A = new BitSet();
            for (int j = i + 1; j <= nTax; j++) {
                A.set(cycle[j]);
                if (x[index] > cutoff)
                    splits.add(new ASplit(A, nTax, (float) (x[index])));
                index++;
            }
        }
        return splits;
    }

    /**
     * setup working distance so that ordering is trivial.
     * Note the the code assumes that taxa are labeled 0..nTax-1 and
     * we do the transition here. It is undone when extracting the splits
     */
    static private double[] setupD(int nTax, int nPairs, double[][] distances, int[] cycle) {
        final double[] d = new double[nPairs];
        int index = 0;
        for (int i = 1; i <= nTax; i++)
            for (int j = i + 1; j <= nTax; j++)
                d[index++] = distances[cycle[i] - 1][cycle[j] - 1];
        return d;
    }

    static private double[] setupV(int nTax, int nPairs, double[][] distances, double[][] variances, LeastSquares leastSquares, int[] cycle) {
        final double[] v = new double[nPairs];

        int index = 0;
        for (int i = 1; i <= nTax; i++)
            for (int j = i + 1; j <= nTax; j++) {
                double dij = distances[cycle[i] - 1][cycle[j] - 1];
                switch (leastSquares) {
                    default:
                    case ols:
                        v[index] = 1.0;
                        break;
                    case fm1:
                        v[index] = dij;
                        break;
                    case fm2:
                        v[index] = dij * dij;
                        break;
                    case estimated:
                        v[index] = (variances != null ? variances[cycle[i] - 1][cycle[j] - 1] : 1.0);
                        break;
                }
                index++;
            }
        return v;
    }

    /**
     * Compute the branch lengths for unconstrained least squares using
     * the formula of Chepoi and Fichet (this takes O(N^2) time only!).
      */
    static private void runUnconstrainedLS(int n, double[] d, double[] x) {
        int index = 0;

        for (int i = 0; i <= n - 3; i++) {
            x[index] = (d[index] + d[index + (n - i - 2) + 1] - d[index + 1]) / 2.0;
            index++;
            for (int j = i + 2; j <= n - 2; j++) {
                x[index] = (d[index] + d[index + (n - i - 2) + 1] - d[index + 1] - d[index + (n - i - 2)]) / 2.0;
                index++;
            }
            if (i == 0)
                x[index] = (d[0] + d[n - 2] - d[2 * n - 4]) / 2.0;
            else
                x[index] = (d[index] + d[i] - d[i - 1] - d[index + (n - i - 2)]) / 2.0;
            index++;
        }
        x[index] = (d[index] + d[n - 2] - d[n - 3]) / 2.0;
    }

    /**
     * Returns the array indices for the smallest propKept proportion of negative values in x.
     * In the case of ties, priority is given to the earliest entries.
     * Size of resulting array will be propKept * (number of negative entries) rounded up.
     */
    static private int[] worstIndices(double[] x, double propKept) {
        //System.err.println(Basic.toString(x, " "));
        if (propKept == 0)
            return null;

        final int n = x.length;

        int numNeg = 0;
        for (double aX1 : x)
            if (aX1 < 0.0)
                numNeg++;

        if (numNeg == 0)
            return null;

        //Make a copy of negative values in x.
        final double[] xCopy = new double[numNeg];
        int j = 0;
        for (double aX : x)
            if (aX < 0.0)
                xCopy[j++] = aX;

        //Sort the copy
        Arrays.sort(xCopy);

        //Find the cut-off value. All values greater than this should
        //be returned, as well as some (or perhaps all) of the values
        //equals to this.
        final int nkept = (int) Math.ceil(propKept * numNeg);  //Ranges from 1 to n
        final double cutoff = xCopy[nkept - 1];

        //we now fill the result vector. Values < cutoff are filled
        //in from the front. Values == cutoff are filled in the back.
        //Values filled in from the back can be overwritten by values
        //filled in from the front, but not vice versa.
        final int[] result = new int[nkept];
        int front = 0;
        int back = nkept - 1;

        for (int i = 0; i < n; i++) {
            if (x[i] < cutoff)
                result[front++] = i; //Definitely in the top entries.
            else if (x[i] == cutoff) {
                if (back >= front)
                    result[back--] = i;
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    static private void printVec(String msg, double[] x) {
        int n = x.length;
        DecimalFormat fmt = new DecimalFormat("#0.00000");

        System.out.print(msg + "\t");
        for (double aX : x) System.out.print(" " + fmt.format(aX));
        System.out.println();
    }

    /**
     * Uses an active set method with the conjugate gradient algorithm to find x that minimises
     * <p>
     * 0.5 * (Ax - d)'W(Ax-d) + \lambda 1'x   s.t. x \geq 0
     * <p>
     * Here, A is the design matrix for the set of cyclic splits with ordering 0,1,2,...,n-1
     * d is the distance vector, with pairs in order (0,1),(0,2),...,(0,n-1),(1,2),(1,3),...,(1,n-1), ...,(n-1,n)
     * W is a vector of variances for d, with pairs in same order as d.
     * x is a vector of split weights, with pairs in same order as d. The split (i,j), for i<j, is {i,i+1,...,j-1}| rest
     * lambda is the regularisation parameter, given by lambda = max_i (A'Wd)_i   * ( 1 - lambdaFraction)
     * <p>
     * Note that lambdaFraction = 1 => lambda = 0, and lambdaFraction = 0 => x = 0.
     */
    static private void runActiveConjugate(int nTax, int nPairs, double[] d, double[] W, double[] x, Regularization regularization, double lambdaFrac) {

        if (W.length != nPairs || x.length != nPairs)
            throw new IllegalArgumentException("Vectors d,W,x have different dimensions");

        /* First evaluate the unconstrained optima. If this is feasible then we don't have to do anything more! */
        NeighborNetSplits.runUnconstrainedLS(nTax, d, x);
        {
            boolean all_positive = true;
            for (int k = 0; k < nPairs; k++) {
                if (x[k] < 0.0) {
                    all_positive = false;
                    break;
                }
            }

            if (all_positive) /* If the unconstrained optimum is feasible then it is also the constrained optimum */
                return;
        }

        /* Allocate memory for the "utility" vectors */
        final double[] r = new double[nPairs];
        final double[] u = new double[nPairs];
        final double[] p = new double[nPairs];
        final double[] y = new double[nPairs];
        final double[] old_x = new double[nPairs];
        Arrays.fill(old_x, 1.0);

        /* Initialise active - originally no variables are active (held to 0.0) */
        final boolean[] active = new boolean[nPairs];
        final boolean[] fixedActive = new boolean[nPairs];
        // Arrays.fill(active, false); // not necessary
        // Arrays.fill(fixedActive, false); // not necessary

        /* Allocate and compute AtWd */
        final double[] AtWd = new double[nPairs];
        for (int k = 0; k < nPairs; k++)
            y[k] = W[k] * d[k];
        NeighborNetSplits.calculateAtx(nTax, y, AtWd);

        /* Compute lambda parameter */
        final boolean computeRegularised = (regularization != Regularization.nnls);

        if (computeRegularised) {
            double maxAtWd = 0.0;
            for (double val : AtWd)
                if (val > maxAtWd)
                    maxAtWd = val;
            final double lambda = maxAtWd * (1.0 - lambdaFrac);

            /* Replace AtWd with AtWd = lambda. This has same effect as regularisation term */
            for (int k = 0; k < nPairs; k++)
                AtWd[k] -= lambda;
        }

        boolean first_pass = true; //This is the first time through the loops.
        while (true) {
            while (true) /* Inner loop: find the next feasible optimum */ {
                if (first_pass)
                    first_pass = false; /* The first time through we use the unconstrained branch lengths */
                else
                    NeighborNetSplits.circularConjugateGrads(nTax, nPairs, r, u, p, y, W, AtWd, active, x);

                {
                    /* Typically, a large number of edges are negative, so on the first
                                                pass of the algorithm we add the worst 60% to the active set */
                    final int[] entriesToContract = worstIndices(x, 0.6);
                    if (entriesToContract != null) {
                        for (int index : entriesToContract) {
                            x[index] = 0.0;
                            active[index] = true;
                        }
                        NeighborNetSplits.circularConjugateGrads(nTax, nPairs, r, u, p, y, W, AtWd, active, x); /* Re-optimise, so that the current x is always optimal */
                    }
                }

                int min_i = -1;
                double min_xi = -1.0;
                for (int i = 0; i < nPairs; i++) {
                    if (x[i] < 0.0) {
                        double xi = (old_x[i]) / (old_x[i] - x[i]);
                        if ((min_i == -1) || (xi < min_xi)) {
                            min_i = i;
                            min_xi = xi;
                        }
                    }
                }

                if (min_i == -1) /* This is a feasible solution - go to the next stage to check if its also optimal */
                    break;
                else {/* There are still negative edges. We move to the feasible point that is closest to
                                            x on the line from x to old_x */

                    for (int i = 0; i < nPairs; i++) /* Move to the last feasible solution on the path from old_x to x */ {
                        if (!active[i])
                            old_x[i] += min_xi * (x[i] - old_x[i]);
                    }
                    active[min_i] = true; /* Add the first constraint met to the active set */
                    x[min_i] = 0.0; /* This fixes problems with round-off errors */
                }
            }

            /* Find i,j that minimizes the gradient over all i,j in the active set. Note that grad = (AtWAb-AtWd)  */
            calculateAb(nTax, x, y);
            for (int i = 0; i < nPairs; i++) {
                y[i] *= W[i];
            }
            calculateAtx(nTax, y, r); /* r = AtWAx */

            /* We check to see that we are at a constrained minimum.... that is that the gradient is positive for
             * all i,j in the active set.
             */
            int min_i = -1;
            double min_grad = 1.0;
            for (int i = 0; i < nPairs; i++) {
                r[i] -= AtWd[i];
                //r[i] *= 2.0;
                if (active[i] && !fixedActive[i]) {
                    double grad_ij = r[i];
                    if ((min_i == -1) || (grad_ij < min_grad)) {
                        min_i = i;

                        min_grad = grad_ij;
                    }
                }
            }

            if ((min_i == -1) || (min_grad > -0.0001)) {
//            	if (computeRegularised) {
//            		/* Return to the main loop, without the regularisation term, and fixing active all variables currently active */
//            		for (int k = 0; k < nPairs; k++)
//                        y[k] = W[k] * d[k];
//            		CircularSplitWeights.calculateAtx(nTax, y, AtWd);
//            		for(int k=0; k<nPairs; k++)
//            			fixedActive[k] = active[k];
//            		computeRegularised = false;         		
//            	}
//            	else
                return; /* We have arrived at the constrained optimum */
            } else
                active[min_i] = false;

        }
    }

    /* Compute the row sum in d. */

    static private double rowSum(int n, double[] d, int k) {
        double r = 0;
        int index = 0;

        if (k > 0) {
            index = k - 1;     //The index for (0,k)

            //First sum the pairs (i,k) for i<k
            for (int i = 0; i < k; i++) {
                r += d[index];
                index += (n - i - 2);
            }
            index++;
        }
        //we now have index = (k,k+1)
        //Now sum the pairs (k,j) for k<j
        for (int j = k + 1; j < n; j++)
            r += d[index++];

        return r;
    }


    /**
     * Computes p = A^Td, where A is the topological matrix for the
     * splits with circular ordering 0,1,2,....,nTax-1
     */
    static private void calculateAtx(int n, double[] d, double[] p) {
        //First the trivial splits
        {
            int index = 0;
            for (int i = 0; i < n - 1; i++) {
                p[index] = rowSum(n, d, i + 1);
                index += (n - i - 1);
            }
        }

        //Now the splits separating out two.
        {
            int index = 1;
            for (int i = 0; i < n - 2; i++) {
                //index = (i,i+2)

                //p[i][i+2] = p[i][i+1] + p[i + 1][i + 2] - 2 * d[i + 1][i + 2];
                p[index] = p[index - 1] + p[index + (n - i - 2)] - 2 * d[index + (n - i - 2)];
                index += (n - i - 2) + 1;
            }
        }

        //Now the remaining splits
        for (int k = 3; k < n; k++) {
            int index = k - 1;
            for (int i = 0; i < n - k; i++) {
                //index = (i,i+k)

                // p[i][j] = p[i][j - 1] + p[i+1][j] - p[i+1][j - 1] - 2.0 * d[i+1][j];
                p[index] = p[index - 1] + p[index + n - i - 2] - p[index + n - i - 3] - 2.0 * d[index + n - i - 2];
                index += (n - i - 2) + 1;
            }
        }
    }

    /**
     * Computes d = Ab, where A is the topological matrix for the
     * splits with circular ordering 0,1,2,....,nTax-1
     */
    static private void calculateAb(int n, double[] b, double[] d) {
        //First the pairs distance one apart.
        {
            int dIndex = 0;
            for (int i = 0; i < n - 1; i++) {
                double d_ij = 0.0;
                //Sum over splits (k,i) 0<=k<i.
                int index = i - 1;  //(0,i)
                for (int k = 0; k < i; k++) {
                    d_ij += b[index];  //(k,i)
                    index += (n - k - 2);
                }
                index++;
                //index = (i,i+1)
                for (int k = i + 1; k < n; k++)  //sum over splits (i,k)  i+1<=k<=n-1
                    d_ij += b[index++];

                d[dIndex] = d_ij;
                dIndex += (n - i - 2) + 1;
            }
        }

        //Distance two apart.
        {
            int index = 1; //(0,2)
            for (int i = 0; i < n - 2; i++) {
//            d[i ][i+2] = d[i ][i+1] + d[i + 1][i + 2] - 2 * b[i][i+1];

                d[index] = d[index - 1] + d[index + (n - i - 2)] - 2 * b[index - 1];
                index += 1 + (n - i - 2);
            }
        }

        for (int k = 3; k < n; k++) {
            int index = k - 1;
            for (int i = 0; i < n - k; i++) {
                //int j = i + k;
                //d[i][j] = d[i][j - 1] + d[i+1][j] - d[i+1][j - 1] - 2.0 * b[i][j - 1];
                d[index] = d[index - 1] + d[index + (n - i - 2)] - d[index + (n - i - 2) - 1] - 2.0 * b[index - 1];
                index += 1 + (n - i - 2);
            }
        }
    }


    /**
     * Computes sum of squares of the lower triangle of the matrix x
     *
     * @param x the matrix
     * @return sum of squares of the lower triangle
     */
    static private double norm(double[] x) {
        double ss = 0.0;
        for (double aX : x) {
            ss += aX* aX;
        }
        return ss;
    }

    /**
     * Conjugate gradient algorithm solving A^tWA x = b (where b = AtWd)
     * such that all x[i][j] for which active[i][j] = true are set to zero.
     * We assume that x[i][j] is zero for all active i,j, and use the given
     * values for x as our starting vector.
     *
     * @param nTax   the number of taxa
     * @param nPairs dimension of b and x
     * @param r      scratch matrix
     * @param u      scratch matrix
     * @param p      scratch matrix
     * @param y      scratch matrix
     * @param W      the W matrix
     * @param b      the b matrix
     * @param active the active constraints
     * @param x      the x matrix
     */
    static private void circularConjugateGrads(int nTax, int nPairs, double[] r, double[] u, double[] p, double[] y, double[] W, double[] b, boolean[] active, double[] x) {
        final int kmax = nTax * (nTax - 1) / 2;
        /* Maximum number of iterations of the cg algorithm (probably too many) */

        calculateAb(nTax, x, y);

        for (int k = 0; k < nPairs; k++) {
            y[k] = W[k] * y[k];
        }
        calculateAtx(nTax, y, r); /*r = AtWAx */

        for (int k = 0; k < nPairs; k++) {
            if (!active[k])
                r[k] = b[k] - r[k];
            else
                r[k] = 0.0;
        }

        double rho = norm(r);
        double rho_old = 0;

        double e_0 = CG_EPSILON * Math.sqrt(norm(b));
        int k = 0;

        while ((k < kmax) && (rho > e_0 * e_0)) {
            k = k + 1;

            if (k == 1) {
                System.arraycopy(r, 0, p, 0, nPairs);
            } else {
                double beta = rho / rho_old;
                //System.out.println("bbeta = " + beta);
                for (int i = 0; i < nPairs; i++)
                    p[i] = r[i] + beta * p[i];
            }

            calculateAb(nTax, p, y);
            for (int i = 0; i < nPairs; i++)
                y[i] *= W[i];

            calculateAtx(nTax, y, u); /*u = AtWAp */
            for (int i = 0; i < nPairs; i++)
                if (active[i])
                    u[i] = 0.0;

            double alpha = 0.0;
            for (int i = 0; i < nPairs; i++)
                alpha += p[i] * u[i];

            alpha = rho / alpha;

            /* Update x and the residual, r */
            for (int i = 0; i < nPairs; i++) {
                x[i] += alpha * p[i];
                r[i] -= alpha * u[i];
            }
            rho_old = rho;
            rho = norm(r);
        }
    }
}

