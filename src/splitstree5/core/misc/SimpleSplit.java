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

package splitstree5.core.misc;

import java.util.BitSet;

/**
 * simple split implementation
 * Created by huson on 12/9/16.
 */
public final class SimpleSplit implements ISplit {
    private final BitSet A;
    private final int ntax;
    private float weight;

    public SimpleSplit(BitSet A, int ntax, float weight) {
        this.ntax = ntax;
        this.A = new BitSet();
        this.A.or(A);
        this.weight = weight;

    }

    @Override
    public int ntax() {
        return ntax;
    }

    @Override
    public int size() {
        return Math.min(A.cardinality(), ntax - A.cardinality());
    }

    public BitSet getA() {
        return A;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public SimpleSplit clone() {
        // todo: test this
        try {
            return (SimpleSplit) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
