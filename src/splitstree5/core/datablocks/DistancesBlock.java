/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.core.datablocks;

import com.sun.istack.internal.NotNull;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToDistances;

/**
 * A distances block
 * Created by huson on 12/21/16.
 */
public class DistancesBlock extends ADataBlock {
    private double[][] distances;
    private double[][] variances;

    /**
     * constructor
     */
    public DistancesBlock() {
        distances = new double[0][0];
    }

    /**
     * named constructor
     *
     * @param name
     */
    public DistancesBlock(String name) {
        this();
        setName(name);
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(DistancesBlock that) {
        distances = that.getDistances();
        variances = that.getVariances();
    }

    @Override
    public void clear() {
        distances = new double[0][0];
        variances = null;
        setShortDescription("");
    }

    public void setNtax(int n) {
        distances = new double[n][n];

    }

    @Override
    public int size() {
        return distances.length;
    }

    /**
     * gets the value for i and j
     *
     * @param i in range 1..nTax
     * @param j in range 1..nTax
     * @return value
     */
    public double get(int i, int j) {
        return distances[i - 1][j - 1];
    }

    /**
     * sets the value
     *
     * @param i     in range 1-nTax
     * @param j     in range 1-nTax
     * @param value
     */
    public void set(int i, int j, double value) {
        distances[i - 1][j - 1] = value;
    }

    public int getNtax() {
        return size();
    }

    /**
     * sets the value distances[row][col]=distances[col][row]=value
     *
     * @param i     in range 1..nTax
     * @param j     in range 1..nTax
     * @param value
     */
    public void setBoth(int i, int j, double value) {
        distances[i - 1][j - 1] = distances[j - 1][i - 1] = value;
    }

    /**
     * gets the variances
     *
     * @param i
     * @param j
     * @return variances or -1, if not set
     */
    public double getVariance(int i, int j) {
        if (variances != null)
            return variances[i - 1][j - 1];
        else
            return -1;
    }

    /**
     * sets the variances
     *
     * @param i
     * @param j
     * @param value
     */
    public void setVariance(int i, int j, double value) {
        synchronized (this) {
            if (variances == null) {
                variances = new double[distances.length][distances.length];
            }
        }
        variances[i][j] = value;
    }

    public void clearVariances() {
        variances = null;
    }

    public boolean isVariances() {
        return variances != null;
    }

    /**
     * set distances, change dimensions if necessary. If dimensions are changed, delete variances
     *
     * @param distances
     */
    public void set(double[][] distances) {
        if (this.distances.length != distances.length) {
            this.distances = new double[distances.length][distances.length];
            variances = null;
        }

        for (int i = 0; i < distances.length; i++) {
            System.arraycopy(distances[i], 0, this.distances[i], 0, distances.length);
        }
    }

    /**
     * set values, change dimensions if necessary
     *
     * @param distances
     * @param variances
     */
    public void set(@NotNull double[][] distances, @NotNull double[][] variances) {
        if (this.distances == null || this.distances.length != distances.length)
            this.distances = new double[distances.length][distances.length];

        if (this.variances == null || this.variances.length != variances.length)
            this.variances = new double[variances.length][variances.length];

        for (int i = 0; i < distances.length; i++) {
            System.arraycopy(distances[i], 0, this.distances[i], 0, distances.length);
            System.arraycopy(variances[i], 0, this.variances[i], 0, distances.length);
        }
    }

    public double[][] getDistances() {
        return distances;
    }

    public double[][] getVariances() {
        return variances;
    }

    @Override
    public Class getFromInterface() {
        return IFromDistances.class;
    }

    @Override
    public Class getToInterface() {
        return IToDistances.class;
    }
}
