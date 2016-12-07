package nexus.distances;

import splitstree4.core.SplitsException;

/**
 * Created by Daria on 10.10.2016.
 */
public class Format {

    private String triangle;
    private boolean labels;
    private boolean diagonal;
    private String varType = "ols";

    /**
     * the Constructor
     */
    public Format() {
        triangle = "both";
        labels = true;
        diagonal = true;
        varType = "ols";
    }

    /**
     * Get the value of triangle
     *
     * @return the value of triangle
     */
    public String getTriangle() {
        return triangle;
    }

    /**
     * Set the value of triangle.
     *
     * @param triangle the value of triangle
     */
    public void setTriangle(String triangle) throws SplitsException {
        if (!triangle.equals("both") && !triangle.equals("lower") && !triangle.equals("upper"))
            throw new SplitsException("Illegal triangle:" + triangle);
        this.triangle = triangle;
    }

    /**
     * Get the value of labels
     *
     * @return the value of labels
     */
    public boolean getLabels() {
        return labels;
    }

    /**
     * Set the value of labels.
     *
     * @param labels the value of labels
     */
    public void setLabels(boolean labels) {
        this.labels = labels;
    }

    /**
     * Get the value of diagonal
     *
     * @return the value of diagonal
     */
    public boolean getDiagonal() {
        return diagonal;
    }

    /**
     * Set the value of diagonal.
     *
     * @param diagonal the value diagonal
     */
    public void setDiagonal(boolean diagonal) {
        this.diagonal = diagonal;
    }

    /**
     * Get the value of varPower
     *
     * @return the value of varPower
     */
    public String getVarType() {
        return varType;
    }

    /**
     * Set the var type
     *
     * @param val
     */
    public void setVarType(String val) {
        this.varType = val;
    }
}
