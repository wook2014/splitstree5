/*
 *  Copyright (C) 2018 Daniel H. Huson
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

import jloda.util.Basic;

import java.util.List;

/**
 * Distances format
 * Daniel Huson, 12/22/16.
 */
public class DistancesNexusFormat implements INexusFormat {
    public enum Triangle {Upper, Lower, Both}

    private Triangle optionTriangle;
    private boolean optionLabels;
    private boolean optionDiagonal;
    private boolean optionVariancesIO = false;
    private String varType = "ols";

    /**
     * the Constructor
     */
    public DistancesNexusFormat() {
        optionTriangle = Triangle.Both;
        optionLabels = true;
        optionDiagonal = true;
        varType = "ols";
    }

    /**
     * Get the value of triangle
     *
     * @return the value of triangle
     */
    public Triangle getOptionTriangle() {
        return optionTriangle;
    }

    /**
     * Set the value of triangle.
     *
     * @param triangleLabel the label of triangle
     */
    public void setOptionTriangleByLabel(String triangleLabel) {
        this.optionTriangle = Basic.valueOfIgnoreCase(Triangle.class, triangleLabel);
    }

    public void setOptionTriangle(Triangle triangle) {
        this.optionTriangle = triangle;
    }

    /**
     * Get the value of labels
     *
     * @return the value of labels
     */
    public boolean isOptionLabels() {
        return optionLabels;
    }

    /**
     * Set the value of labels.
     *
     * @param optionLabels the value of labels
     */
    public void setOptionLabels(boolean optionLabels) {
        this.optionLabels = optionLabels;
    }

    /**
     * Get the value of diagonal
     *
     * @return the value of diagonal
     */
    public boolean isOptionDiagonal() {
        return optionDiagonal;
    }

    /**
     * Set the value of diagonal.
     *
     * @param optionDiagonal the value diagonal
     */
    public void setOptionDiagonal(boolean optionDiagonal) {
        this.optionDiagonal = optionDiagonal;
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
    public boolean isOptionVariancesIO() {
        return optionVariancesIO;
    }

    public void setOptionVariancesIO(boolean optionVariancesIO) {
        this.optionVariancesIO = optionVariancesIO;
    }


    @Override
    public List<String> listOptions() {
        return null;
    }

}