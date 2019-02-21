package splitstree5.core.algorithms.distances2network;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToNetwork;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * principle coordinate analysis
 * Daniel Huson, David Bryant and Daria Evseeva, 2.2008/9.2018
 */

public class PCoA2D extends Algorithm<DistancesBlock, NetworkBlock> implements IFromDistances, IToNetwork {

    public final static String DESCRIPTION = "Performs Principle Coordinates Analysis (Gower, J.C. (1966))";
    private Matrix distanceMatrix;
    private double totalSquaredDistance;
    private int rank;
    private int numberOfPositiveEigenValues;
    private double[] eigenValues;
    private Map<String, double[]> name2vector = new HashMap<>();
    private double[][] vectors;
    private boolean done = false;

    private IntegerProperty optionFirstCoordinate = new SimpleIntegerProperty(1);
    private IntegerProperty optionSecondCoordinate = new SimpleIntegerProperty(2);

    public List<String> listOptions() {
        return Arrays.asList("FirstCoordinate", "SecondCoordinate");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "FirstCoordinate":
                return "Choose principal component for the x Axis";
            case "SecondCoordinate":
                return "Choose principal component for the y Axis";
            default:
                return null;
        }
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock distancesBlock, NetworkBlock networkBlock) throws Exception {
        progress.setMaximum(6);
        progress.setProgress(0);

        rank = taxaBlock.getNtax();
        distanceMatrix = new Matrix(rank, rank);
        double sum = 0;
        for (int i = 0; i < rank; i++) {
            for (int j = 0; j < rank; j++) {
                if (i == j)
                    distanceMatrix.set(i, j, 0);
                else {
                    double d = distancesBlock.get(i + 1, j + 1);
                    distanceMatrix.set(i, j, d);
                    sum += d * d;
                }
            }
        }
        totalSquaredDistance = 2 * sum;
        vectors = new double[rank][];

        progress.incrementProgress();

        final Matrix centered = computeDoubleCenteringOfSquaredMatrix(distanceMatrix);

        final EigenvalueDecomposition eigenValueDecomposition = centered.eig();
        final Matrix eigenVectors = eigenValueDecomposition.getV();

        numberOfPositiveEigenValues = 0;
        Matrix positiveEigenValues = eigenValueDecomposition.getD();
        for (int i = 0; i < rank; i++) {
            if (positiveEigenValues.get(i, i) > 0)
                numberOfPositiveEigenValues++;
            else
                positiveEigenValues.set(i, i, 0);
        }

        progress.incrementProgress();

        // multiple eigenvectors by sqrt of eigenvalues
        Matrix scaledEigenVectors = (Matrix) eigenVectors.clone();
        for (int i = 0; i < rank; i++) {
            for (int j = 0; j < rank; j++) {
                double v = scaledEigenVectors.get(i, j);
                v = v * Math.sqrt(positiveEigenValues.get(j, j));
                scaledEigenVectors.set(i, j, v);
            }
        }

        progress.incrementProgress();

        final int[] indices = sortValues(positiveEigenValues);

        eigenValues = new double[numberOfPositiveEigenValues];
        for (int j = 0; j < numberOfPositiveEigenValues; j++) {
            eigenValues[j] = positiveEigenValues.get(indices[j], indices[j]);
        }
        System.err.println("Positive eigenvalues:");
        System.err.println(Basic.toString("%.6f", eigenValues, ", "));

        progress.incrementProgress();

        for (int i = 0; i < rank; i++) {
            String name = taxaBlock.getLabel(i + 1);
            double[] vector = new double[numberOfPositiveEigenValues];
            name2vector.put(name, vector);
            vectors[i] = vector;
            for (int j = 0; j < numberOfPositiveEigenValues; j++) {
                vector[j] = scaledEigenVectors.get(i, indices[j]);
            }
        }
        done = true;

        setOptionFirstCoordinate(Math.min(numberOfPositiveEigenValues, getOptionFirstCoordinate()));
        setOptionSecondCoordinate(Math.min(numberOfPositiveEigenValues, getOptionSecondCoordinate()));

        progress.incrementProgress();

        final PhyloGraph graph = networkBlock.getGraph();
        System.err.println(String.format("Stress: %.6f", getStress(getOptionFirstCoordinate() - 1, getOptionSecondCoordinate() - 1)));
        for (int t = 1; t <= taxaBlock.getNtax(); t++) {
            String name = taxaBlock.getLabel(t);
            double[] coordinates = getProjection(getOptionFirstCoordinate() - 1, getOptionSecondCoordinate() - 1, name);
            Node v = graph.newNode();
            graph.setLabel(v, name);
            NetworkBlock.NodeData nodeData = networkBlock.getNodeData(v);
            nodeData.put("x", 100 * coordinates[0]+"");
            nodeData.put("y", 100 * coordinates[1]+"");
            nodeData.put("w", "3");
            nodeData.put("h", "3");

            // todo width/height = 3 ?
        }

        progress.incrementProgress();

    }

    /**
     * Gets a short description of the algorithm
     *
     * @return description
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * get coordinates for given name
     *
     * @param name
     * @return coordinates
     */
    public double[] getCoordinates(String name) {
        return name2vector.get(name);
    }

    /**
     * get i-th and j-th coordinates for given name
     *
     * @param i
     * @param j
     * @param name
     * @return (i, j)
     */
    public double[] getProjection(int i, int j, String name) {
        double[] vector = name2vector.get(name);
        return new double[]{vector[i], vector[j]};
    }

    /**
     * given i-th, j-th and k-th coordinates for given name
     *
     * @param i
     * @param j
     * @param k
     * @param name
     * @return (i, j, k)
     */
    public double[] getProjection(int i, int j, int k, String name) {
        double[] vector = name2vector.get(name);
        return new double[]{vector[i], vector[j], vector[k]};
    }

    /**
     * get rank
     *
     * @return rank
     */
    public int getRank() {
        return rank;
    }


    /**
     * compute centered inner product matrix
     *
     * @param matrix
     * @return new matrix
     */
    private Matrix computeDoubleCenteringOfSquaredMatrix(Matrix matrix) {
        int size = matrix.getColumnDimension();
        Matrix result = new Matrix(matrix.getColumnDimension(), matrix.getRowDimension());
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double v1 = 0;
                for (int k = 0; k < size; k++) {
                    v1 += matrix.get(k, j) * matrix.get(k, j) / size;
                }
                double v2 = 0;
                for (int k = 0; k < size; k++) {
                    v2 += matrix.get(i, k) * matrix.get(i, k) / size;
                }
                double v3 = 0;
                for (int k = 0; k < size; k++) {
                    for (int l = 0; l < size; l++) {
                        v3 += matrix.get(k, l) * matrix.get(k, l) / (size * size);
                    }
                }
                double v4 = matrix.get(i, j);
                result.set(i, j, 0.5 * (v1 + v2 - v3 - (v4 * v4)));
            }
        }
        return result;
    }

    /**
     * sort indices by values
     *
     * @param m
     * @return sorted indices
     *         todo: replace by proper sorting
     */
    private int[] sortValues(Matrix m) {
        double[] v = new double[m.getColumnDimension()];
        int[] index = new int[v.length];
        for (int i = 0; i < v.length; i++) {
            v[i] = m.get(i, i);
            index[i] = i;
        }

        for (int i = 0; i < v.length; i++) {
            for (int j = i + 1; j < v.length; j++) {
                if (Math.abs(v[i]) < Math.abs(v[j])) {
                    double tmpValue = v[j];
                    v[j] = v[i];
                    v[i] = tmpValue;
                    int tmpIndex = index[j];
                    index[j] = index[i];
                    index[i] = tmpIndex;
                }
            }
        }

        return index;
    }

    public boolean isDone() {
        return done;
    }

    public double[] getEigenValues() {
        return eigenValues;
    }

    public double getStress(int i, int j) {
        return getStress(new int[]{i, j});
    }

    public double getStress(int i, int j, int k) {
        return getStress(new int[]{i, j, k});
    }

    public double getStress(int[] indices) {
        double squaredSum = 0;
        for (int a = 0; a < rank; a++) {
            for (int b = 0; b < rank; b++) {
                if (a != b) {
                    double d = 0;
                    for (int z : indices) {
                        d += (vectors[a][z] - vectors[b][z]) * (vectors[a][z] - vectors[b][z]);
                    }
                    d = Math.sqrt(d);
                    squaredSum += (d - distanceMatrix.get(a, b)) * (d - distanceMatrix.get(a, b));
                }
            }
        }
        return Math.sqrt(squaredSum / totalSquaredDistance);
    }

    public int getOptionFirstCoordinate() {
        return optionFirstCoordinate.getValue();
    }
    public IntegerProperty optionFirstCoordinateProperty() {
        return optionFirstCoordinate;
    }

    public void setOptionFirstCoordinate(int optionFirstCoordinate) {
        if (optionFirstCoordinate > 0)
            this.optionFirstCoordinate.setValue(optionFirstCoordinate);
    }

    public int getOptionSecondCoordinate() {
        return optionSecondCoordinate.getValue();
    }
    public IntegerProperty optionSecondCoordinateProperty() {
        return optionSecondCoordinate;
    }

    public void setOptionSecondCoordinate(int optionSecondCoordinate) {
        if (optionSecondCoordinate > 0)
            this.optionSecondCoordinate.setValue(optionSecondCoordinate);
    }


}
