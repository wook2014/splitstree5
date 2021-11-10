package splitstree5.core.algorithms.distances2splits.neighbornet.NeighborNetPCG;

import Jama.Matrix;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class CircularSplitAlgorithms {

    /**
     * Computes A*x where A is the matrix for a full circular split system. The indices of the rows and columns
     * of A and x correspond to an ordering of pairs (1,2),(1,3),...,(1,n),(2,3),...,(2,n),...,(n-1,n).
     * In A we have A{(i,j)(k,l)} = 1 if i and j are on opposite sides of the split {k,k+1,...,l-1}|...
     * This algorithm runs in O(n^2) time, which is the number of entries of x.
     * @param n Number of taxa.
     * @param x vector with dimension n(n-1)/2
     * vector d is overwritten by vector A*x with dimension n(n-1)/2.
     */
    static public void circularAx(int n,double[] x , double[] d) {

        Arrays.fill(d,0.0);
        //First compute d[i][i+1] for all i.
        int dindex = 1; //index of (i,i+1)
        for(int i=1;i<=n-1;i++) {
            double d_ij = 0;
            int index = i;
            //Sum over weights of splits (1,i+1), (2,i+1),...(i,i+1)
            for(int k=1;k<=i;k++) {
                d_ij += x[index]; //split (k,i+1)
                index+=(n-k-1);
            }
            //Sum over weights of splits (i+1,i+2), (i+1,i+3),...(i+1,n)
            index = dindex + n-i;
            for(int j=i+2;j<=n;j++) {
                d_ij += x[index]; //split (i+1,j)
                index++;
            }
            d[dindex] = d_ij;
            dindex += (n-i);
        }
        //Now compute d[i][i+2] for all i.
        int index = 2; //pair (1,3)
        for (int i=1;i<=n-2;i++) {
            //d[i ][i+2] = d[i ][i+1] + d[i + 1][i + 2] - 2 * x[i+1][i+2];
            d[index] = d[index-1] + d[index + n-i-1] - 2*x[index + n-i-1];
            index += n-i;
        }

        //Now loop through remaining pairs.
        for(int k=3;k<=n-1;k++) {
            index = k; //Pair (1,k+1)
            for(int i=1;i<=n-k;i++) {
                //pair (i,i+k)
                //d[i][j] = d[i][j - 1] + d[i+1][j] - d[i+1][j-1] - 2.0 * b[i+1][j];
                d[index] = d[index-1] + d[index + n-i-1] - d[index + n-i-2] - 2*x[index + n-i-1];
                index = index + n-i;
            }
        }

    }

    /**
     * Computes A'*x where A is the matrix for a full circular split system. The indices of the rows and columns
     * of A and x correspond to an ordering of pairs (1,2),(1,3),...,(1,n),(2,3),...,(2,n),...,(n-1,n).
     * In A we have A{(i,j)(k,l)} = 1 if i and j are on opposite sides of the split {k,k+1,...,l-1}|...
     * This algorithm runs in O(n^2) time, which is the number of entries of x.
     * @param n Number of taxa.
     * @param x vector with dimension n(n-1)/2 +1
     * @param p, vector assumed to be of size n(n-1)/2. Overwritten by A'x.
     */
    static public void  circularAtx(int n, double[] x, double[] p) {

        Arrays.fill(p,0.0);

        //First compute trivial splits
        int sIndex = 1;
        for(int i=1;i<=n-1;i++) {
            //sIndex is pair (i,i+1)
            int xindex = i-1;  //Index (1,i)
            double total = 0.0;
            for(int j=1;j<=i-1;j++) {
                total+=x[xindex]; //pair (j,i)
                xindex = xindex+n-j-1;
            }
            xindex++;
            for(int j=i+1;j<=n;j++) {
                total += x[xindex]; //pair(i,j)
                xindex++;
            }
            p[sIndex] = total;
            sIndex = xindex;
        }

        sIndex = 2;
        for (int i=1;i<=n-2;i++) {
            //p[i][i+2] = p[i][i+1] + p[i + 1][i + 2] - 2 * x[i][i + 1];
            p[sIndex]=p[sIndex-1] + p[sIndex+n-i-1] - 2*x[sIndex-1];
            sIndex += (n-i);
        }

        for (int k=3;k<=n-1;k++) {
            sIndex = k;
            for (int i=1;i<=n-k;i++) {
                //Index = i(i+k)
                //p[i][j] = p[i][j - 1] + p[i+1][j] - p[i+1][j - 1] - 2.0 * x[i][j-1];
                p[sIndex] = p[sIndex-1] + p[sIndex + n-i-1] - p[sIndex + n-i-2] - 2.0*x[sIndex-1];
                sIndex += (n-i);
            }
        }
    }

    /**
     * Computes A\x where A is the matrix for a full circular split system. The indices of the rows and columns
     * of A and y correspond to an ordering of pairs (1,2),(1,3),...,(1,n),(2,3),...,(2,n),...,(n-1,n).
     * In A we have A{(i,j)(k,l)} = 1 if i and j are on opposite sides of the split {k,k+1,...,l-1}|...
     * This algorithm runs in O(n^2) time, which is the number of entries of x.
     * @param n Number of taxa.
     * @param y vector with dimension n(n-1)/2
     * overwrite vector x=A\y which solves Ax = y. x has dimension n(n-1)/2
     */
    static public void circularSolve(int n, double[] y, double[] x) {

        Arrays.fill(x,0.0);

        int index = 1;
        //x[1,2]= (y[1,2]+y[1,n] - y[2,n])/2
        x[index] = (y[index] + y[n-1] - y[2*n-3])/2.0; //(1,2).
        index++;
        for(int j=3;j<=n-1;j++) {
            //x[1,j] = (y[1,j]+y[j-1,n] - y[1,j-1] - y[j,n])/2
            x[index] = (y[index] + y[(2*n-j)*(j-1)/2] - y[index-1] - y[j*(2*n-j-1)/2])/2.0;
            index++;
        }
        //x[1,n] = (y(1,n) + y(n-1,n) - y(1,n-1))/2
        x[index] = (y[n-1] + y[n*(n-1)/2] - y[n-2])/2.0; //(1,n)
        index++;

        for(int i=2;i<=n-1;i++) {
            //x[i,i+1] = (y[i][i+1] + y[i-1][i] - y[i-1,i+1])/2
            x[index] = (y[index]  - y[index -n + i] + y[index-n+i-1] ) / 2.0;
            index++;
            for(int j=i+2;j<=n;j++) {
                // x[i][j] = ( y[i,j] + y[i-1,j-1] - y[i,j-1] - y[i-1][j])
                x[index] = (y[index] - y[index - 1] + y[index - n + i - 1] - y[index - n + i]) / 2.0;
                index++;
            }
        }
    }

    /**
     * Computes inv(A)' * x  where A is the matrix for a full circular split system. The indices of the rows and columns
     * of A and x correspond to an ordering of pairs (1,2),(1,3),...,(1,n),(2,3),...,(2,n),...,(n-1,n).
     * In A we have A{(i,j)(k,l)} = 1 if i and j are on opposite sides of the split {k,k+1,...,l-1}|...
     * This algorithm runs in O(n^2) time, which is the number of entries of x.
     * @param n Number of taxa.
     * @param x vector with dimension n(n-1)/2
     * overwrites vector y =  inv(A)'*x which has dimension n(n-1)/2
     */
    static public void circularAinvT(int n, double[] x, double[] y) {
        int npairs = n*(n-1)/2;
        Arrays.fill(y,0.0);

        //Suppose B = inv(A). Evaluates B*x column by column:
        // B*x = \sum_{ij} B(:,ij) * x(ij)

        y[1] = 0.5*x[1];
        y[n-1] = 0.5*x[1];
        y[2*n-3] = -0.5*x[1];

        int ij=2;
        for (int j=3;j<=n-1;j++) {
            //pair ij = (1,j)
            //Nonzero in column: (j-1,n), (1,j),-(j,n),-(1,j-1)
            y[(j-1)*(2*n-j)/2] += 0.5*x[ij];
            y[ij] += 0.5*x[ij];
            y[j*(2*n-j-1)/2] += -0.5*x[ij];
            y[ij-1] += - 0.5*x[ij];
            ij++;
        }

        y[n-1]+=0.5*x[ij];
        y[npairs]+=0.5*x[ij];
        y[n-2] += -0.5*x[ij];
        ij++;

        for(int i=2;i<=n-1;i++) {
            //(i,i+1)
            y[ij+i-n-1] +=  0.5 * x[ij];
            y[ij] += 0.5 * x[ij];
            y[ij+i-n] += - 0.5*x[ij];
            ij++;

            for (int j=i+2;j<=n;j++) {
                y[ij+i-n-1] +=0.5*x[ij];
                y[ij] += 0.5*x[ij];
                y[ij+i-n] +=  - 0.5*x[ij];
                y[ij-1] += - 0.5*x[ij];
                ij++;
            }
        }
    }

    /**
     * Constructs 0-1 design matrix for a full collection of circular splits. Note: JAMA matrix index from 0,1,2,...
     * (FOR DEBUGGING)
     * @param n int, ntaxa.
     * @return Matrix
     */
    static public Matrix makeA(int n) {
        int npairs = n*(n-1)/2;
        Matrix A = new Matrix(npairs,npairs);
        int ij=1;
        for(int i=1;i<=n;i++) {
            for (int j=i+1;j<=n;j++) {
                int kl=1;
                for(int k=1;k<=n;k++) {
                    for(int l=k+1;l<=n;l++) {
                        if (i<k && k<=j && j<l)
                            A.set(ij-1,kl-1,1);
                        else if (k<=i && i<l && l<=j)
                            A.set(ij-1,kl-1,1);
                        else
                            A.set(ij-1,kl-1,0);
                        kl++;
                    }
                }
                ij++;
            }
        }
        return A;
    }

    /**
     * Compute the sum of squares of the gradient for non-active indices after calling equality constrained least squares.
     * Gradient g = A'(d-Ax). Returns \sum_{i:!G[i]} g[i]
     * @param n  ntaxa
     * @param d  vector of distances
     * @param G  vector indicating active indices: G[i] true <-> i is active
     * @param z  x[!G] = z[!G]
     * @return    \sum_{i:!G[i]} g[i]^2
     */
    static public double checkCircularLeastsquares(int n, double[] d, boolean[] G, double[] z) {
        int npairs = n*(n-1)/2;
        double[] x = new double[npairs+1];
        double[] g = new double[npairs+1];
        double[] Ax = new double[npairs+1];
        for(int i=1;i<=npairs;i++)
            if (!G[i])
                x[i] = z[i];
        CircularSplitAlgorithms.circularAx(n,x,Ax);
        for(int i=1;i<=npairs;i++)
            Ax[i]-=d[i];
        CircularSplitAlgorithms.circularAtx(n,Ax,g);

        //DEBUG: Check that Ax, Atx are calculated right
//        double[] x1 = new double[npairs+1];
//        Random random = new Random();
//        for(int i=1;i<=npairs;i++) {
//            x1[i] = random.nextDouble();
//        }
//        double[] x2 = new double[npairs+1];
//        double[] x3 = new double[npairs+1];
//        CircularSplitAlgorithms.circularAx(n,x1,x2);
//        CircularSplitAlgorithms.circularSolve(n,x2,x3);
//        double diff = 0.0;
//        for(int i=1;i<=npairs;i++)
//            diff+=(x3[i]-x1[i])*(x3[i]-x1[i]);
//        System.err.println("Check Ax and solve. Error = "+diff);
//DEBUG TO HERE


        double norm2 = 0.0;
        for(int i=1;i<=npairs;i++) {
            if (!G[i])
                norm2 += g[i] * g[i];
        }
        return norm2;
    }
    /**
     * Compute the sum of squares of the gradient for non-active indices after calling equality constrained least squares.
     * Gradient g = A'(d-Ax). Returns \sum_{i:x[i]>0} g[i]^2 + \sum_{i:x[i]=0} min(0,g[i])^2
     * @param n  ntaxa
     * @param d  vector of distances
     * @param x  vector of split weights --- assumed to be non-negative.
     * @return    norm of projected gradient.
     */
    static public double checkNnegLeastsquares(int n, double[] d, double[] x) {
        int npairs = n*(n-1)/2;
        double[] g = new double[npairs+1];
        double[] Ax = new double[npairs+1];
        CircularSplitAlgorithms.circularAx(n,x,Ax);
        for(int i=1;i<=npairs;i++)
            Ax[i]-=d[i];
        CircularSplitAlgorithms.circularAtx(n,Ax,g);

        double dsum = 0.0;
        double xsum = 0.0;
        for (int i=1;i<=npairs;i++) {
            dsum+=d[i];
            xsum+=x[i];
        }
        System.err.println("dsum = "+dsum+"\txsum = "+xsum);




        double norm1 = 0.0, norm2 = 0.0;
        for(int i=1;i<=npairs;i++) {
            if (x[i]>0)
                norm1 += g[i] * g[i];
            else
                norm2 += Math.min(g[i],0.0) * Math.min(g[i],0.0);
        }
        return norm1+norm2;
    }


    static public void main(String[] args) {
        //Test Ax.

        int n=5;
        if (args.length>0)
            n = Integer.parseInt(args[0]);



        System.err.println("Testing Circular Algorithms for n = " + n);
        int npairs = n*(n-1)/2;
        double[] x = new double[npairs+1];
        Random rand = new Random();
        for(int i=1;i<=npairs;i++)
            x[i] = rand.nextDouble();

        Arrays.fill(x,1,npairs+1,1.0);


        double[] y = new double[npairs+1];
        circularAx(n,x,y);

        Matrix xJ = new Matrix(npairs,1);
        for(int i=1;i<=npairs;i++)
            xJ.set(i-1,0,x[i]);
        Matrix A = makeA(n);

        Matrix yJ = A.times(xJ);
        double[] y2 = new double[npairs+1];
        for(int i=1;i<=npairs;i++)
            y2[i] = yJ.get(i-1,0);

        double err = VectorUtilities.dist(y,y2);
        System.err.println("Compare CircularAx, err = "+ err);

        circularAtx(n,x,y);
        yJ = (A.transpose()).times(xJ);
        for(int i=1;i<=npairs;i++)
            y2[i] = yJ.get(i-1,0);
        err = VectorUtilities.dist(y,y2);

//        System.err.print("x = ["+x[1]);
//        for(int i=2;i<=npairs;i++)
//            System.err.print(","+x[i]);
//        System.err.println("]");
//
//        System.err.print("y = ["+y[1]);
//        for(int i=2;i<=npairs;i++)
//            System.err.print(","+y[i]);
//        System.err.println("]");


//        PrintWriter output = new PrintWriter(System.err);
//        A.print(output,3,0);
//        output.flush();

        circularAinvT(n,y,y2);
        err = VectorUtilities.dist(y2,x);
        System.err.println("Compare CircularAinvTx, err = "+ err);

//
//        System.err.println("Compare CircularATx, err = "+ err);
//        circularAinvT(n,x,y);
//        yJ = (A.transpose()).inverse().times(xJ);
//        for(int i=1;i<=npairs;i++)
//            y2[i] = yJ.get(i-1,0);
//         err = VectorUtilities.dist(y,y2);
//        System.err.println("Compare CircularAinvTx, err = "+ err);
//



        circularSolve(n,x,y);
        yJ = (A.inverse()).times(xJ);
        for(int i=1;i<=npairs;i++)
            y2[i] = yJ.get(i-1,0);
        err = VectorUtilities.dist(y,y2);
        System.err.println("Compare CircularSolve, err = "+ err);



    }







}
