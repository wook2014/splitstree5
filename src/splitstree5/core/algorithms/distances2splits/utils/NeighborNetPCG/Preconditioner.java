package splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG;

import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.TridiagonalMatrix.multiplyLU;

public class Preconditioner {

    public BlockXMatrix X;

    public TridiagonalMatrix[] L;
    public TridiagonalMatrix[] U;
    public SparseRowMatrix[] Y;
    public SparseRowMatrix[] Z;

    /**
     * Constructs the block conditioner, in O(n^2) time [linear in the number of variables].
     * @param X BlockXMatrix
     * @param bands Number of bands used when computing the Y and Z matrices.
     */
    public Preconditioner(BlockXMatrix X,int bands) {
        this.X = X; //TODO Check that this is the kind of copy/assignment that we want. I think copy by ref is ok here.
        int n=X.n;
        int[] m = X.m;

        L = new TridiagonalMatrix[n];
        U = new TridiagonalMatrix[n];
        if (m[1]>0) {
            TridiagonalMatrix[] LU = X.A[1].trilu();
            L[1] = LU[0];
            U[1] = LU[1];
            Y[1] = X.C[1];
            Z[1] = X.C[1];
        }

        for(int i=2;i<=n-2;i++) {
            if (m[i]>0) {
                if (m[i-1]>0) {
                    TridiagonalMatrix T = TridiagonalMatrix.multiplyLU(L[i-1],U[i-1]);
                    T.preprocessInverse();

                    TridiagonalMatrix T2 = tri_X_Tinv_Y(X.B[i-1],T,X.B[i-1]);
                    TridiagonalMatrix[] LU = T2.trilu();
                    L[i] = LU[0];
                    U[i] = LU[1];

                    SparseRowMatrix S2 = sparse_X_Tinv_Y(Y[i-1],T,X.B[i-1],false,bands);
                    Y[i] = SparseRowMatrix.minus(X.C[i],S2,4);
                    S2 = sparse_X_Tinv_Y(Z[i-1],T,X.B[i-1],true,bands);
                    Z[i] = SparseRowMatrix.minus(X.C[i],S2,4);
                } else {
                    TridiagonalMatrix[] LU = X.A[i].trilu();
                    L[i] = LU[0];
                    U[i] = LU[1];
                }
            }
        }
        if (m[n-1]>0) {
            TridiagonalMatrix YZ = X.A[n-1].clone();
            for(int i=1;i<=n-2;i++) {
                if (m[i]>0) {
                    TridiagonalMatrix T = multiplyLU(L[i],U[i]);
                    T.preprocessInverse();
                    YZ = TridiagonalMatrix.minus(YZ,tri_X_Tinv_Y(Y[i],T,Z[i]));
                }
            }
            TridiagonalMatrix[] LU = YZ.trilu();
            L[n-1] = LU[0];
            U[n-1] = LU[1];

        }
    }



    /**
     * Computes the matrix \tau(X*inv(T)*Y') where X,Y is sparse and T is triangular. Assumes that T.preprocessInverse() has
     * already been called. Takes linear time
     * @param X Sparse matrix
     * @param T Tridiagonal matrix
     * @param Y Sparse matrix
     * @return Tridiagonal matrix formed from central three diagonals of X inv(T) Y'.
     */
    private TridiagonalMatrix tri_X_Tinv_Y(SparseRowMatrix X, TridiagonalMatrix T, SparseRowMatrix Y) {
        int m = X.m;
        TridiagonalMatrix T2 = new TridiagonalMatrix();
        T2.n = m;
        T2.a = new double[m+1];
        T2.b = new double[m];
        T2.c = new double[m];

        T2.a[1] = evalX_Tinv_Y(X,T,Y,1,1,false);
        for(int i=2;i<=m;i++) {
            T2.a[i] = evalX_Tinv_Y(X,T,Y,i,i,false);
            T2.b[i-1] = evalX_Tinv_Y(X,T,Y,i,i-1,false);
            T2.c[i-1] = evalX_Tinv_Y(X,T,Y,i-1,i,false);
        }
        return T2;
    }

    /**
     * Returns the sparse matrix equal to the central 2bands+1 of X * inv(T)*Y'.
     * @param X Sparse matrix
     * @param T Tridiagonal matrix (assumes preprocessInverse() already called)
     * @param Y Sparse matrix
     * @param useTinvTranspose if true then uses inv(T') in place of inv(T)
     * @param bands Number of bands
     * @return Sparse matrix equal to the central 2bands+1 of X * inv(T)*Y'.
     */
    private SparseRowMatrix sparse_X_Tinv_Y(SparseRowMatrix X, TridiagonalMatrix T, SparseRowMatrix Y, boolean useTinvTranspose, int bands) {
        SparseRowMatrix S = new SparseRowMatrix(X.m,Y.m,1+2*bands);
        for(int i=1;i<=S.m;i++) {
            int nnz = 0;
            for (int j = Math.max(i-bands,1);j<=Math.min(i+bands,S.n);j++) {
                nnz++;
                S.ind[i][nnz] = j;
                S.val[i][nnz] = evalX_Tinv_Y(X,T,Y,i,j,useTinvTranspose);
            }
        }
        return S;
    }

    /**
     * Returns element i,j of X * inv(T)*Y'.
     * @param X Sparse matrix
     * @param T Tridiagonal matrix
     * @param Y Sparse matrix
     * @param i integer index
     * @param j integer index
     * @param useTinvTranspose if true, uses T' in place of T.
     * @return entry of matrix X * inv(T)*Y'.
     */
    private double evalX_Tinv_Y(SparseRowMatrix X, TridiagonalMatrix T, SparseRowMatrix Y, int i, int j, boolean useTinvTranspose) {
        double z = 0;
        for(int k1=1;k1<=X.N;k1++) {
            int i1 = X.ind[i][k1];
            if (i1!=0) {
                double x = X.val[i][k1];
                for(int k2=2;k2<=Y.N;k2++) {
                    int i2 = Y.ind[j][k2];
                    if (i2!=0) {
                        double y = Y.ind[j][k2];
                        if (useTinvTranspose)
                            z+= x*T.getTinv(i2,i1)*y;
                        else
                            z+= x*T.getTinv(i1,i2)*y;
                    }
                }
            }
        }
        return z;
    }

    public double[][] solve(double[][] y) {
        int n = X.n;
        double[][] eta = new double[n][];
        double[][] nu = new double[n][];
        int[] m = X.m;

        if (m[1]>0) {
            eta[1] = L[1].solveL(y[1]);
        }
        for(int i=2;i<=n-2;i++) {
            if (m[i]>0) {
                if (m[i-1]>0)
                    eta[i] = L[i].solveL(minus(y[i],X.B[i-1].multiply(U[i-1].solveU(eta[i-1]))));
                else
                    eta[i] = L[i].solveL(y[i]);
            }
        }
        if (m[n-1]>0) {
            double[] v = y[n-1].clone();
            for(int i=1;i<=n-2;i++) {
                if (m[i]>0) {
                    v = minus(v,Y[i].multiply(U[i].solveU(eta[i])));
                }
            }
            eta[n-1] = L[n-1].solveL(v);
        }

        if (m[n-1]>0) {
            nu[n-1] = U[n-1].solveU(eta[n-1]);
        }
        if (m[n-2]>0) {
            if (m[n-1]>0)
                nu[n-2] = U[n-2].solveU(minus(eta[n-2],L[n-2].solveL(Z[n-2].multiplyTranspose(nu[n-1]))));
            else
                nu[n-2] = U[n-2].solveU(eta[n-2]);
        }
        for(int i = n-3;i>=1;i--) {
            if (m[i]>0) {
                double[] v = eta[i].clone();
                if (m[i+1]>0)
                    v = minus(v,L[i].solveL(X.B[i].multiplyTranspose(nu[i+1])));
                if (m[n-1]>0)
                    v = minus(v,L[i].solveL(Z[i].multiplyTranspose(nu[n-1])));
                nu[i] = U[i].solveU(v);
            }
        }
        return nu;
    }

    /**
     * Subtract one vector from another
     * @param x array
     * @param y array of the same length as x
     * @return array with x-y.
     */
    private double[] minus(double[] x, double[] y) {
        assert x.length == y.length : "Computing difference between vectors of different sizes";
        double[] z = new double[x.length];
        for(int i=0;i<x.length;i++)
            z[i] = x[i] - y[i];
        return z;
    }
}
