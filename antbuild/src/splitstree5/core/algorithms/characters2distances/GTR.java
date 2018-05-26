package splitstree5.core.algorithms.characters2distances;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.characters2distances.utils.SaturatedDistancesException;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.models.GTRmodel;

/**
 * Computes the distance matrix from a set of characters using the General Time Revisible model.
 * <p>
 * Created on 12-Jun-2004
 *
 * @author DJB
 */

public class GTR extends DNAdistance implements IFromChararacters, IToDistances {

    private double[][] QMatrix; //Q Matrix provided by user for ML estimation.
    public final static String DESCRIPTION = "Calculates distances using a General Time Reversible model";

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, CharactersBlock charactersBlock, DistancesBlock distancesBlock)
            throws Exception {

        progress.setTasks("GTR Distance", "Init.");

        GTRmodel model = new GTRmodel(QMatrix, getNormedBaseFreq());
        model.setPinv(getOptionPInvar());
        model.setGamma(getOptionGamma());

        distancesBlock.copy(fillDistanceMatrix(progress, charactersBlock, model));
    }

    @Override
    protected double exactDist(double[][] F) throws SaturatedDistancesException {
        /* Exact distance - pg 456 in Swofford et al.
         * Let Pi denote the diagonal matrix with base frequencies down the
         * diagonal. The standard formula is
         *
         * dist = -trace(Pi log(Pi^{-1} F))
         *
         * The problem is that Pi^{-1}F will probably not be symmetric, so taking the
         * logarithm is difficult. However we can use an alternative formula:
         *
         * dist = -trace(Pi^{1/2} log(Pi^{-1/2} (F'+F)/2 Pi^{-1/2}) Pi^{1/2} )
         *
         * Then we will be taking the log (or inverse MGF) of a symmetric matrix.
         *
         *
         */

        int n = 4;
        double[] sqrtpi = new double[n];
        double[] baseFreq = getNormedBaseFreq();

        for (int i = 0; i < n; i++)
            sqrtpi[i] = Math.sqrt(baseFreq[i]);
        Matrix X = new Matrix(n, n);


        for (int i = 0; i < 4; i++) {
            for (int j = 0; j <= i; j++) {
                double Xij = (F[i][j] + F[j][i]) / (2.0 * sqrtpi[i] * sqrtpi[j]);
                X.set(i, j, Xij);
                if (i != j)
                    X.set(j, i, Xij);
            }
        }

        /* Compute M^{-1}(Q)  */
        EigenvalueDecomposition EX = new EigenvalueDecomposition(X);
        double[] D = EX.getRealEigenvalues();
        double[][] V = (EX.getV().getArrayCopy());
        for (int i = 0; i < 4; i++)
            D[i] = Minv(D[i]);

        /* Now evaluate trace(pi^{1/2} V D V^T pi^{1/2}) */

        double dist = 0.0;
        for (int i = 0; i < 4; i++) {
            double x = 0.0;
            for (int j = 0; j < 4; j++) {
                x += baseFreq[i] * V[i][j] * V[i][j] * D[j];
            }
            dist += -x;
        }

        return dist;
    }

    // GETTER AND SETTER

    /**
     * gets a short description of the algorithm
     *
     * @return a description
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    public GTR() {
        super();
        //Default Q matrix is the Jukes-Cantor matrix
        QMatrix = new double[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == j)
                    QMatrix[i][j] = -3;
                else
                    QMatrix[i][j] = 1;
            }
        }
    }

    /**
     * get the parameter matrix
     *
     * @return double[][]
     */
    public double[][] getOptionQMatrix() {
        return QMatrix;
    }

    /**
     * get the parameter matrix
     *
     * @param QMatrix Sets the rate matrix
     */
    public void setOptionQMatrix(double[][] QMatrix) {
        this.QMatrix = QMatrix;
    }

    /**
     * Sets Q using only the upper triangle of halfQ
     *
     * @param halfQ Rate matrix: only upper triangle used.
     */
    public void setHalfMatrix(double[][] halfQ) {
        QMatrix = new double[4][4];
        double[] baseFreq = getNormedBaseFreq();

        //Copy top half
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < i; j++) {
                QMatrix[j][i] = halfQ[j][i];
                QMatrix[i][j] = halfQ[j][i] * baseFreq[j] / baseFreq[i];
            }
            double rowsum = 0.0;
            for (int j = 0; j < 4; j++) {
                if (i != j)
                    rowsum += QMatrix[i][j];
            }
            QMatrix[i][i] = rowsum;
        }
    }
}
