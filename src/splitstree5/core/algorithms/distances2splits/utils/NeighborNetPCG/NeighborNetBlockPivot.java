package splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.ProgressSilent;

import java.util.Arrays;
import java.util.Random;

import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.CircularSplitAlgorithms.*;
import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.VectorUtilities.minus;
import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.VectorUtilities.norm;

public class NeighborNetBlockPivot {


    static public double[] circularBlockPivot(int n, double[] d, ProgressListener progress) throws CanceledException {
        int npairs = n * (n - 1) / 2;
        //boolean[] F = new boolean[npairs+1];
        //Arrays.fill(F,false);
        boolean[] G = new boolean[npairs + 1];
        Arrays.fill(G, true);
        Random rand = new Random();

        double[] z = circularAtx(n, d);
        for (int i = 1; i <= npairs; i++)
            z[i] = -z[i];
        double tol = 1e-10;

        int p=3;
        int iter=1;
        boolean[] infeasible = new boolean[npairs+1];
        Arrays.fill(infeasible,false);
        int ninf = 0;
        int N = npairs+1;
        int maxiter = 100;

        while (iter<2 || ninf > 0) {
            if (iter>=maxiter) {
                System.err.println("WARNING: Max Iterations exceeded in Block Pivot Algorithm");
                break;
            }
            ninf = 0;
            for (int i = 1; i <= npairs; i++) {
                infeasible[i] = z[i] < 0.0;
                if (infeasible[i])
                    ninf++;
            }
            //System.err.println("ninf = "+ninf);

            double pgnorm = projectedGradientNorm(n,d,z,G);
            System.err.println("f(x) = "+functionVal(n,d,z,G));
            
            if (ninf < N) {
                N = ninf;
                p = 3;
                System.out.print("Swapping (1) "+pgnorm);
                //printSet(infeasible);
                System.out.println();
                for (int i = 1; i <= npairs; i++) {
                    //F[i] = F[i] ^ infeasible[i]; //XOR
                    G[i] = G[i] ^ infeasible[i];
                }

            } else {
                if (p > 0) {
                    p--;
                    System.out.print("Swapping (2) "+pgnorm);
                    //printSet(infeasible);
                    System.out.println();

                    for (int i = 1; i <= npairs; i++) {
                       // F[i] = F[i] ^ infeasible[i]; //XOR
                        G[i] = G[i] ^ infeasible[i];
                    }
                } else {
                    int i = randomElement(infeasible,rand);
                    //int i = 1;
                    //while (i < npairs && !infeasible[i])
                        i++;
                   // F[i] = F[i] ^ true;
                    System.err.println("Single swapping "+i+" pgnorm="+pgnorm);
                    G[i] = !G[i];
                }
            }

            z = circularLeastSquares(n, G, d, 100, tol);
            //double znorm = VectorUtilities.norm(z);
            //System.err.println(znorm);

            for (int i = 1; i <= npairs; i++) {
                if (Math.abs(z[i]) < 1e-10)
                    z[i] = 0.0;
            }
            iter++;

            progress.checkForCancel();
        }
        //Do one final refitting with these edges.
        z = circularLeastSquares(n,G,d,100,1e-3*tol);
        for(int i=1;i<=npairs;i++)
            if (G[i])
                z[i]=0.0;

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

        int npairs = n * (n - 1) / 2; //Dimensions of G,d.

        int nG = 0;
        for (int i = 1; i <= npairs; i++) {
            if (G[i])
                nG++;
        }

        if (nG == 0) { //No equality constraints - use straight solve.
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
        return x;
    }

    /**
     * Converts a boolean mask in  a single 1..npairs vector into separate boolean
     * vectors for each block.
     * @param n number of taxa
     * @param G   boolean vector, size n(n-1)/2
     * @return array of boolean arrays, one for each block.
     */
    public static boolean[][] mask2blockmask(int n,boolean[] G) {

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
    public static double[][] vector2blocks(int n, double[] v, boolean[] G) {
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
                vcell[i] = Arrays.copyOfRange(vi, 0, counti+1);

            if (G[index]) {
                countlast++;
                vlast[countlast] = v[index];
            }
            index++;
        }
        if (countlast>0) {
            vcell[n-1] = Arrays.copyOfRange(vlast,0,countlast+1);
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
            if (x[i]!=null) {
                assert x[i].length == y[i].length : "Trying to add block vectors with different row lengths";
                z[i] = new double[x[i].length];
                for (int j = 1; j < x[i].length; j++)
                    z[i][j] = x[i][j] + alpha * y[i][j];
            }
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
            if (x[i]!=null) {
                assert x[i].length == y[i].length : "Trying to add block vectors with different row lengths";
                for (int j = 1; j < x[i].length; j++)
                    xty += x[i][j] * y[i][j];
            }
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
        for(int i=0;i<x.length;i++) {
            if (x[i] != null)
                y[i] = x[i].clone();
        }
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
                v[index] = vcell[n - 1][countlast];
            }
            index++;
        }
        return v;
    }

    static private void printSet(boolean[] S) {
        for(int i=0;i<S.length;i++) {
            if (S[i])
                System.err.print(i+", ");
        }
    }

    static private int randomElement(boolean[] S, Random rand) {
        int n=0;
        for(int i=0;i<S.length;i++) {
            if (S[i])
                n++;
        }
        int k = rand.nextInt(n);
        int m=0;
        for(int i=0;i<S.length;i++) {
            if (S[i]) {
                if (m==k)
                    return i;
                else
                    m++;
            }

        }
        return -1;
    }

    /**
     * Compute the projected gradient at x=z (with z(G)=0) for 0.5 * \|Ax - d\|^2 with x>=0.
     * @param n
     * @param d
     * @param z
     * @param G
     * @return
     */
    private static double projectedGradientNorm(int n,double d[],double[] z, boolean[] G) {
        int npairs = n * (n - 1) / 2;
        double[] x = z.clone();
        for (int i = 1; i <= npairs; i++) {
            if (G[i])
                x[i] = 0.0;
        }

        double[] grad = CircularSplitAlgorithms.circularAtx(n, minus(CircularSplitAlgorithms.circularAx(n, x), d));
        double gtg = 0.0;
        for (int i = 1; i <= npairs; i++) {
            if (G[i] && grad[i] < 0)
                grad[i] = 0;
            else
                gtg = grad[i] * grad[i];
        }
        return Math.sqrt(gtg);
    }

    /**
     * Compute the projected gradient at x=z (with z(G)=0) for 0.5 * \|Ax - d\|^2 with x>=0.
     * @param n
     * @param d
     * @param z
     * @param G
     * @return
     */
    private static double functionVal(int n,double d[],double[] z, boolean[] G) {
        int npairs = n * (n - 1) / 2;
        double[] x = z.clone();
        for (int i = 1; i <= npairs; i++) {
            if (G[i])
                x[i] = 0.0;
        }

        double[] r = minus(CircularSplitAlgorithms.circularAx(n, x), d);
        return norm(r);
    }
    static public void test(int n) throws CanceledException {
//        Random rand = new Random();
//
//        int npairs = n*(n-1)/2;
//        double[] d = new double[npairs+1];
//        for(int i=1;i<=npairs;i++)
//            d[i] = rand.nextDouble();
//
//        double[] y = circularBlockPivot(n,d);

        double[] d = new double[]{0, 20, 56, 66, 63, 36, 32, 32, 16, 17, 18, 18, 19, 12, 12, 13, 13, 16, 17, 1, 61, 69, 61, 41, 34, 33, 24, 24, 28, 28, 31, 25, 30, 30, 30, 31, 32, 21, 41, 57, 60, 63, 66, 62, 62, 61, 62, 64, 59, 58, 61, 59, 59, 60, 57, 61, 61, 65, 69, 66, 66, 65, 66, 67, 67, 68, 66, 67, 67, 68, 65, 59, 58, 72, 59, 61, 62, 61, 65, 62, 66, 64, 64, 64, 64, 62, 16, 41, 30, 29, 30, 31, 33, 29, 31, 28, 31, 32, 33, 35, 16, 26, 26, 30, 28, 40, 26, 25, 24, 23, 26, 25, 24, 26, 27, 27, 27, 38, 27, 26, 27, 26, 25, 26, 29, 3, 4, 4, 8, 8, 14, 12, 13, 15, 16, 15, 3, 3, 7, 10, 14, 12, 13, 15, 16, 16, 2, 8, 11, 13, 14, 14, 16, 17, 17, 8, 11, 15, 14, 13, 15, 16, 17, 11, 14, 13, 13, 15, 16, 18, 7, 6, 6, 10, 11, 11, 7, 7, 12, 11, 13, 4, 8, 10, 12, 4, 5, 12, 1, 15, 16};
        n=20;
        double[] y = circularBlockPivot(n, d, new ProgressSilent());
        int m = 3;

    }
}
