package splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG;

import Jama.Matrix;

import java.util.Arrays;

import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.CircularSplitAlgorithms.makeA;
import static splitstree5.core.algorithms.distances2splits.utils.NeighborNetPCG.VectorUtilities.add;

public class BlockXMatrix {
    public int n;  //Number of taxa = one more than the number of blocks
    public int[] m; //Array of block sizes.
    public TridiagonalMatrix[] A; //Diagonal blocks: tridiagonal
    public SparseRowMatrix[] B; //Off-diagonal blocks: sparse
    public SparseRowMatrix[] C; //Blocks in the last row/column.
    public boolean hasCorners; //Flag indicating wether the top right and bottom left entries of A[n-1] are present.

    /**
     * BlockXMatrix
     * <p>
     * Construct a full X matrix, with all rows and columns. This is a block matrix, specified in the pdf.
     *
     * @param n number of taxa.
     */
    public BlockXMatrix(int n) {
        this.n = n;

        //Construct array of block sizes
        m = new int[n];
        for (int i = 1; i <= n - 2; i++) {
            m[i] = n - i - 1;
        }
        m[n - 1] = n - 1;

        //Create the diagonal blocks
        A = new TridiagonalMatrix[n];
        for (int i = 1; i <= n - 1; i++) {
            A[i] = new TridiagonalMatrix();
            A[i].a = new double[m[i] + 1];
            A[i].a[1] = 0.75;
            if (m[i] > 1)
                Arrays.fill(A[i].a, 2, m[i], 1.0);
            if (i == n - 1)
                A[i].a[n - 1] = 0.75;
            A[i].b = new double[m[i]];
            Arrays.fill(A[i].b, 1, m[i] - 1, -0.5);
            A[i].c = new double[m[i]];
            Arrays.fill(A[i].c, 1, m[i] - 1, -0.5);
        }

        B = new SparseRowMatrix[n - 2];
        for (int i = 1; i <= n - 3; i++) {
            B[i] = new SparseRowMatrix(m[i + 1], m[i], 3);
            for (int j = 1; j <= m[i + 1] - 1; j++) {
                B[i].ind[j][1] = j;
                B[i].ind[j][2] = j + 1;
                B[i].ind[j][3] = j + 2;
                B[i].val[j][1] = 0.25;
                B[i].val[j][2] = -0.5;
                B[i].val[j][3] = 0.25;
            }
            B[i].ind[m[i + 1]][1] = m[i + 1];
            B[i].ind[m[i + 1]][2] = m[i + 1] + 1;
            B[i].val[m[i + 1]][1] = 0.25;
            B[i].val[m[i + 1]][2] = -0.5;
        }

        C = new SparseRowMatrix[n - 1];
        //First block is a combination of a tridiagonal matrix and some extra bits
        C[1] = new SparseRowMatrix(m[n - 1], m[1], 3);
        C[1].ind[1][1] = 1;
        C[1].ind[1][2] = n - 2;
        C[1].ind[1][3] = 0;
        C[1].ind[2][1] = 1;
        C[1].ind[2][2] = 2;
        C[1].ind[2][3] = n - 2;
        C[1].val[1][1] = 0.25;
        C[1].val[1][2] = -0.5;
        C[1].val[1][3] = 0.0;
        C[1].val[2][1] = -0.5;
        C[1].val[2][2] = 0.25;
        C[1].val[2][3] = 0.25;
        for (int j = 3; j <= m[n - 1] - 1; j++) {
            C[1].ind[j][1] = j - 2;
            C[1].ind[j][2] = j - 1;
            C[1].ind[j][3] = j;
            C[1].val[j][1] = 0.25;
            C[1].val[j][2] = -0.5;
            C[1].val[j][3] = 0.25;
        }
        C[1].ind[m[n - 1]][1] = m[n - 1] - 2;
        C[1].ind[m[n - 1]][2] = m[n - 1] - 1;
        C[1].val[m[n - 1]][1] = 0.25;
        C[1].val[m[n - 1]][2] = -0.5;

        for (int i = 2; i <= n - 2; i++) {
            C[i] = new SparseRowMatrix(m[n - 1], m[i], 1);
            C[i].ind[i - 1][1] = m[i];
            C[i].val[i - 1][1] = 0.25;
            C[i].ind[i][1] = m[i];
            C[i].val[i][1] = -0.5;
            C[i].ind[i + 1][1] = m[i];
            C[i].val[i][1] = 0.25;
        }
    }

    /**
     * Construct a block X matrix, restricted to rows/columns specified in gcell
     *
     * @param n     number of taxa
     * @param gcell (n-1) dimensional array of boolean arrays, indicating which rows/cols in each block to keep.
     */
    public BlockXMatrix(int n, boolean[][] gcell) {
        BlockXMatrix X = new BlockXMatrix(n);
        this.n = n;
        A = new TridiagonalMatrix[n];
        B = new SparseRowMatrix[n - 2];
        C = new SparseRowMatrix[n - 1];
        m = new int[n];

        for (int i = 1; i <= n - 1; i++) {
            A[i] = X.A[i].submatrix(gcell[i]);
            m[i] = A[i].n;
            if (i < n - 2)
                B[i] = X.B[i].submatrix(gcell[i + 1], gcell[i]);
            if (i < n - 1)
                C[i] = X.C[i].submatrix(gcell[n - 1], gcell[i]);
        }
        hasCorners = gcell[n - 1][1] && gcell[n - 1][m[n - 1]];
    }

    public double[][] multiply(double[][] x) {
        double[][] y = new double[n][];
        for (int i = 1; i <= n - 2; i++) {
            if (m[i] > 0) {
                double[] yi = A[i].multiply(x[i]);
                if (i > 1 && m[i - 1] > 0)
                    yi = add(yi, B[i - 1].multiplyTranspose(x[i - 1])); //TODO do this without reallocating yi
                if (i < n - 2 && m[i + 1] > 0)
                    yi = add(yi, B[i].multiplyTranspose(x[i + 1]));
                if (m[n - 1] > 0)
                    yi = add(yi, C[i].multiplyTranspose(x[n - 1]));
                y[i] = yi;
            }
        }

        if (m[n - 1] > 0) {
            double[] ylast = A[n - 1].multiply(x[n - 1]);
            if (hasCorners) {
                ylast[1] += 0.25 * x[n - 1][m[n - 1]];
                ylast[m[n - 1]] += 0.25 * x[n - 1][1];
            }
            for (int i = 1; i <= n - 2; i++) {
                if (m[i] > 0)
                    ylast = add(ylast, C[i].multiply(x[i]));
            }
            y[n - 1] = ylast;
        }
        return y;
    }

    /**
     * Constructs a JAMA matrix version of the matrix X. Note this matrix is indexed 0,1,2...
     * @param n Number of taxa
     * @param G boolean array, indexed 1,2,....,npairs
     * @return Matrix
     */
    private Matrix makeX(int n, boolean[] G) {
        int npairs = n * (n - 1) / 2;

        //First construct the full npairsxnpairs matrix
        Matrix A = makeA(n);
        Matrix B = A.inverse();
        //Set all entries <0.2 in inv(A) to 0.
        for (int i = 0; i < npairs; i++) {
            for (int j = 0; j < npairs; j++) {
                if (B.get(i, j) < 0.2)
                    B.set(i, j, 0);
            }
        }
        Matrix XX = B.transpose().times(B);

        //Restrict entries to those in G and move the last block to the end.
        int[] lastBlock = new int[n - 1]; //Indices of rows to move to last block
        int[] otherBlocks = new int[(n - 1) * (n - 2) / 2];
        int lastk = 0;
        int otherk = 0;

        int ij = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = i + 1; j <= n; j++) {
                if (G[ij + 1]) {
                    if (j == n) {
                        lastBlock[lastk] = ij;
                        lastk++;
                    } else {
                        otherBlocks[otherk] = ij;
                        otherk++;
                    }
                }
                ij = ij + 1;
            }
        }
        lastBlock = Arrays.copyOfRange(lastBlock,0,lastk);
        otherBlocks = Arrays.copyOfRange(otherBlocks,0,otherk);


        //XX = [XX(~toMove,~toMove) XX(~toMove,toMove);XX(toMove,~toMove) XX(toMove,toMove)];
        Matrix X = new Matrix(npairs, npairs);
        int m = (n - 1) * (n - 2) / 2;
        X.setMatrix(0, m - 1, 0, m - 1, XX.getMatrix(otherBlocks, otherBlocks));
        X.setMatrix(0, m - 1, m, npairs - 1, XX.getMatrix(otherBlocks, lastBlock));
        X.setMatrix(m, npairs - 1, 0, m - 1, XX.getMatrix(lastBlock, otherBlocks));
        X.setMatrix(m, npairs - 1, m, npairs - 1, XX.getMatrix(lastBlock, lastBlock));

        return X;

    }



}
