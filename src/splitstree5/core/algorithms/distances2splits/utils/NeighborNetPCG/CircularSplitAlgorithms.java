package splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG;

public class CircularSplitAlgorithms {

    /**
     * Computes A*x where A is the matrix for a full circular split system. The indices of the rows and columns
     * of A and x correspond to an ordering of pairs (1,2),(1,3),...,(1,n),(2,3),...,(2,n),...,(n-1,n).
     * In A we have A{(i,j)(k,l)} = 1 if i and j are on opposite sides of the split {k,k+1,...,l-1}|...
     * This algorithm runs in O(n^2) time, which is the number of entries of x.
     * @param n Number of taxa.
     * @param x vector with dimension n(n-1)/2
     * @return vector A*x with dimension n(n-1)/2.
     */
    static public double[] circularAx(int n,double[] x ) {
        int npairs = n*(n-1)/2;
        double[] d = new double[npairs+1];

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
        return d;

    }

    /**
     * Computes A'*x where A is the matrix for a full circular split system. The indices of the rows and columns
     * of A and x correspond to an ordering of pairs (1,2),(1,3),...,(1,n),(2,3),...,(2,n),...,(n-1,n).
     * In A we have A{(i,j)(k,l)} = 1 if i and j are on opposite sides of the split {k,k+1,...,l-1}|...
     * This algorithm runs in O(n^2) time, which is the number of entries of x.
     * @param n Number of taxa.
     * @param x vector with dimension n(n-1)/2
     * @return vector A'*x with dimension n(n-1)/2.
     */
    static public double[] circularAtx(int n, double[] x) {
        int npairs = n*(n-1)/2;
        double[] p = new double[npairs+1];

        //First compute trivial splits
        int sIndex = 1;
        for(int i=1;i<=n-1;i++) {
            //sIndex is pair (i,i+1)
            int index = i-1;
            double p_sIndex = 0.0;
            for(int j=1;j<=i-1;j++) {
                p_sIndex+=x[index]; //pair (j,i)
                index = index+n-j-1;
            }
            index++;
            for(int j=i+1;j<=n;j++) {
                p_sIndex += x[index]; //pair(i,j)
                index++;
            }
            p[sIndex] = p_sIndex;
            sIndex = index;
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
        return p;
    }

    /**
     * Computes A\x where A is the matrix for a full circular split system. The indices of the rows and columns
     * of A and y correspond to an ordering of pairs (1,2),(1,3),...,(1,n),(2,3),...,(2,n),...,(n-1,n).
     * In A we have A{(i,j)(k,l)} = 1 if i and j are on opposite sides of the split {k,k+1,...,l-1}|...
     * This algorithm runs in O(n^2) time, which is the number of entries of x.
     * @param n Number of taxa.
     * @param y vector with dimension n(n-1)/2
     * @return vector x=A\y which solves Ax = y. x has dimension n(n-1)/2
     */
    static public double[] circularSolve(int n, double[] y) {
        int npairs = n*(n-1)/2;
        double[] x = new double[npairs+1];

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
        return x;
    }
}
