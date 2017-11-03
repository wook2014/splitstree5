/*
 *  Copyright (C) 2017 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package splitstree5.io.nexus;

import java.io.IOException;

/**
 * Distances format
 * Created by huson on 12/22/16.
 */
public class DistancesNexusFormat {
    private String triangle;
    private boolean labels;
    private boolean diagonal;
    private boolean VariancesIO = true;
    private String varType = "ols";

    /**
     * the Constructor
     */
    public DistancesNexusFormat() {
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
    public void setTriangle(String triangle) throws IOException {
        if (!triangle.equals("both") && !triangle.equals("lower") && !triangle.equals("upper"))
            throw new IOException("Illegal triangle:" + triangle);
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

    /**
     * in and output variances, if they have been defined
     *
     * @return true, if want defined variances to in and output
     */
    public boolean isVariancesIO() {
        return VariancesIO;
    }

    public void setVariancesIO(boolean variancesIO) {
        this.VariancesIO = variancesIO;
    }
}
