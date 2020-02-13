/*
 * DistancesBlock.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.core.datablocks;

import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.io.nexus.DistancesNexusFormat;

/**
 * A distances block
 * Daniel Huson, 12/21/16.
 */
public class DistancesBlock extends DataBlock {
    public static final String BLOCK_NAME = "DISTANCES";

    private double[][] distances;
    private double[][] variances;

    /**
     * constructor
     */
    public DistancesBlock() {
        distances = new double[0][0];
        format = new DistancesNexusFormat();
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(DistancesBlock that) {
        distances = that.getDistances();
        variances = that.getVariances();
        format = that.getFormat();
    }

    @Override
    public void clear() {
        super.clear();
        distances = new double[0][0];
        variances = null;
    }

    public void setNtax(int n) {
        distances = new double[n][n];
        variances = null;
        setShortDescription(getInfo());
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
     * sets the value for (s,t) and (t,s), indices 1-based
     *
     * @param s
     * @param t
     * @param value
     */
    public void setBoth(int s, int t, double value) {
        distances[s - 1][t - 1] = distances[t - 1][s - 1] = value;
    }

    /**
     * gets the variances,  indices 1-based
     *
     * @param s
     * @param t
     * @return variances or -1, if not set
     */
    public double getVariance(int s, int t) {
        if (variances != null)
            return variances[s - 1][t - 1];
        else
            return -1;
    }

    /**
     * sets the variances,  indices 1-based
     *
     * @param s
     * @param t
     * @param value
     */
    public void setVariance(int s, int t, double value) {
        synchronized (this) {
            if (variances == null) {
                variances = new double[distances.length][distances.length];
            }
        }
        variances[s - 1][t - 1] = value;
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
    public void set(double[][] distances, double[][] variances) {
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

    @Override
    public String getInfo() {
        return "a " + getNtax() + "x" + getNtax() + " distance matrix";
    }

    @Override
    public String getBlockName() {
        return BLOCK_NAME;
    }
}
