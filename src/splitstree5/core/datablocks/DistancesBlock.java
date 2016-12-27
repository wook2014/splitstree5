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

/**
 * A distances block
 * Created by huson on 12/21/16.
 */
public class DistancesBlock extends ADataBlock {
    private double[][] data;

    public DistancesBlock() {
        data = new double[0][0];
    }

    public DistancesBlock(String name) {
        this();
        setName(name);
    }

    public void setNtax(int n) {
        data = new double[n][n];
    }

    /**
     * gets the value for i and j
     *
     * @param i in range 1-nTax
     * @param j in range 1-nTax
     * @return value
     */
    public double get(int i, int j) {
        return data[i - 1][j - 1];
    }

    /**
     * sets the value
     *
     * @param i     in range 1-nTax
     * @param j     in range 1-nTax
     * @param value
     */
    public void set(int i, int j, double value) {
        data[i - 1][j - 1] = value;
    }

    public int getNtax() {
        return data.length;
    }

    /**
     * sets the value data[row][col]=data[col][row]=value
     *
     * @param i
     * @param j
     * @param value
     */
    public void setBoth(int i, int j, double value) {
        data[i - 1][j - 1] = data[j - 1][i - 1] = value;
    }

    /**
     * set values, change dimensions if necessary
     *
     * @param values
     */
    public void set(double[][] values) {
        if (data.length != values.length)
            data = new double[values.length][values.length];

        for (int i = 0; i < values.length; i++) {
            System.arraycopy(values[i], 0, data[i], 0, values.length);
        }
    }
}
