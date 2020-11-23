package splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG;

import java.util.Arrays;

import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.CircularSplitAlgorithms.*;
import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.VectorUtilities.minus;

public class NeighborNetBlockPivot {


    static public double[] circularBlockPivot(int n, double[] d) {
        int npairs = n*(n-1)/2;
        //boolean[] F = new boolean[npairs+1];
        //Arrays.fill(F,false);
        boolean[] G = new boolean[npairs+1];
        Arrays.fill(G,true);

        double[] z = circularAtx(n,d);
        for(int i=1;i<=npairs;i++)
            z[i]=-z[i];
        double tol = 1e-10;

        int p=3;
        int iter=1;
        boolean[] infeasible = new boolean[npairs+1];
        int ninf = 0;
        int N = npairs+1;

        while (iter<2 || ninf > 0) {

            ninf = 0;
            for (int i = 1; i <= npairs; i++) {
                infeasible[i] = z[i] < 0.0;
                if (infeasible[i])
                    ninf++;
            }

            if (ninf < N) {
                N = ninf;
                p = 3;
                for (int i = 1; i <= npairs; i++) {
                    //F[i] = F[i] ^ infeasible[i]; //XOR
                    G[i] = G[i] ^ infeasible[i];
                }

            } else {
                if (p > 0) {
                    p--;
                    for (int i = 1; i <= npairs; i++) {
                       // F[i] = F[i] ^ infeasible[i]; //XOR
                        G[i] = G[i] ^ infeasible[i];
                    }
                } else {
                    int i = 1;
                    while (i < npairs && !infeasible[i])
                        i++;
                   // F[i] = F[i] ^ true;
                    G[i] = !G[i];
                }
            }
            z = circularLeastSquares(n,G,d,100,tol);

            for(int i=1;i<=npairs;i++) {
                if (Math.abs(z[i])<1e-10)
                    z[i]=0.0;
            }
            iter++;
        }

        return z;
    }

    /**
     * Finds x that minimizes ||Ax - d|| such that x_i = 0 for all i \in G.
     * Here A is the circular split weight matrix.
     * This implementation solves the dual problem by means of a preconditioned conjugate gradient
     * algorithm, with a 'bespoke' preconditioner.
     *
     * @param n Number of taxa
     * @param G  Mask: indicating variables to c
     * @param d Vector of distances
     * @param maxiter Maximum number of iterations for conjugate gradient.
     * @param tol  Target tolerance: algorithm halts when ||Ax-d|| <= tol.
     * @return vector
     */
    private static double[] circularLeastSquares(int n, boolean[] G, double[] d, int maxiter, double tol) {

        int npairs = n*(n-1)/2; //Dimensions of G,d.

        int nG = 0;
        for(int i=1;i<=npairs;i++) {
            if (G[i])
                nG++;
        }

        if (nG==0) { //No equality constraints - use straight solve.
            return circularSolve(n,d);
        }

        boolean[][] gcell = mask2blockmask(n,G);
        BlockXMatrix X = new BlockXMatrix(n,gcell);
        double[] unconstrained = circularSolve(n,d);
        double[][] b = vector2blocks(n,unconstrained,G);
        Preconditioner M = new Preconditioner(X,4);

        double[] nuvec = new double[npairs+1];
        double[][] nu = vector2blocks(n,nuvec,G);

        double[][] r = blockvectorAdd(b,-1,X.multiply(nu));
        double[][] z = M.solve(r);

        double[][] p = blockclone(z);

        double rnorm,alpha,beta,rtz;

        for(int j=1;j<=maxiter;j++) {
            rnorm = Math.sqrt(blockvectorDot(r,r));
            if (rnorm<tol)
                break;

            double[][] xp = X.multiply(p);
            alpha = blockvectorDot(r,z)/blockvectorDot(p,xp);

            nu = blockvectorAdd(nu,alpha,p);
            rtz = blockvectorDot(r,z);
            r = blockvectorAdd(r,-alpha,X.multiply(p));
            z = M.solve(r);
            beta = blockvectorDot(r,z)/rtz;
            p = blockvectorAdd(z,beta,p);
        }

        nuvec = blocks2vector(n,nu,G);
        double[] x = minus(unconstrained,circularSolve(n,circularAinvT(n,nuvec)));
        double[] y = circularAtx(n,minus(circularAx(n,x),d));
        for(int i = 1;i<=npairs;i++) {
            if (G[i])
                x[i] = y[i];
        }
        return y;
    }

    /**
     * Converts a boolean mask in  a single 1..npairs vector into separate boolean
     * vectors for each block.
     * @param n number of taxa
     * @param G   boolean vector, size n(n-1)/2
     * @return array of boolean arrays, one for each block.
     */
    private static boolean[][] mask2blockmask(int n,boolean[] G) {

        //Allocate arrays, initialising as false.
        boolean[][] gcell = new boolean[n][];
        for(int i=1;i<=n-2;i++) {
            gcell[i] = new boolean[n-i];
            Arrays.fill(gcell[i],false);
        }
        gcell[n-1] = new boolean[n];
        Arrays.fill(gcell[n-1],false);

        //Transfer mask values into blco format.
        int index = 1;
        for(int i=1;i<=n-1;i++) {
            for(int j=i+1;j<=n-1;j++) {
                gcell[i][j-i] = G[index];
                index++;
            }
            gcell[n-1][i]=G[index];
            index++;
        }
        return gcell;
    }

    /**
     * Converts a vector single 1..npairs arrays into separate
     * vectors for each block.
     * @param n number of taxa
     * @param v double vector, size n(n-1)/2
     * @param G   boolean vector, size n(n-1)/2, indicating blocks
     * @return array of double arrays, one for each block.
     */
    private static double[][] vector2blocks(int n, double[] v, boolean[] G) {
        //TODO: Make this simpler using a map from pairs to blocks.

        double[][] vcell = new double[n][];

        int countlast = 0; //Number of elements in block n-1
        double[] vlast = new double[n]; //Elements in block n-1

        int index=1;
        double[] vi = new double[n];
        for(int i=1;i<=n-1;i++) {
            Arrays.fill(vi, 0.0);
            int counti = 0;

            for (int j = i + 1; j <= n - 1; j++) {
                if (G[index]) {
                    counti++;
                    vi[counti] = v[index];
                }
                index++;
            }
            if (counti > 0)
                vcell[i] = Arrays.copyOfRange(vi, 0, counti);

            if (G[index]) {
                countlast++;
                vlast[countlast] = v[index];
            }
            index++;
        }
        if (countlast>0) {
            vcell[n-1] = Arrays.copyOfRange(vlast,0,countlast);
        }
        return vcell;
    }

    /**
     * Takes two block vectors x,y with the same sizes, and computes x + alpha*y
     * @param x block vector
     * @param alpha  double
     * @param y block vector
     * @return block vector
     */
    private static double[][] blockvectorAdd(double[][] x, double alpha, double[][] y) {
        assert x.length==y.length:"Trying to add block vectors with different lengths";
        double[][] z = new double[x.length][];

        for (int i=1;i<x.length;i++) {
            assert x[i].length==y[i].length:"Trying to add block vectors with different row lengths";
            z[i] = new double[x[i].length];
            for(int j=1;j<x[i].length;j++)
                z[i][j] = x[i][j] + alpha*y[i][j];
        }
        return z;
    }

    /**
     * Computes dot product of two block vectors
     * @param x block vector
     * @param y block vector
     * @return block vector
     */
    private static double blockvectorDot(double[][] x, double[][] y) {
        assert x.length==y.length:"Trying to compute dot product of block vectors with different lengths";
        double xty = 0.0;

        for (int i=1;i<x.length;i++) {
            assert x[i].length==y[i].length:"Trying to add block vectors with different row lengths";
            for(int j=1;j<x[i].length;j++)
                xty += x[i][j]*y[i][j];
        }
        return xty;
    }

    /**
     * Clone a block array.
     * @param x double[][]
     * @return clone of x
     */
    private static double[][] blockclone(double[][] x) {
        double[][] y = new double[x.length][];
        for(int i=0;i<x.length;i++)
            y[i] = x[i].clone();
        return y;
    }

    /**
     * Converts a vector stored as blocks into a single vector.
     * @param n number of taxa
     * @param vcell vector of blocks
     * @param G boolean vector indicating which rows are kept
     * @return vector
     */
    public static double[] blocks2vector(int n, double[][] vcell, boolean[] G) {
        double[] v = new double[n*(n-1)/2+1];
        int countlast = 0;
        int index=1;
        int counti;

        for(int i=1;i<=n-1;i++) {
            counti = 0;
            for(int j=(i+1);j<=n-1;j++) {
                if (G[index]) {
                    counti++;
                    v[index] = vcell[i][counti];
                }
                index++;
            }
            if (G[index]) {
                countlast++;
                v[index] = vcell[n-1][countlast];
            }
            index++;
        }
        return v;
    }
}
