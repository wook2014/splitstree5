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
public final class ASplit {
    private final BitSet A;
    private final int ntax;
    private float weight;
    private float confidence;
    private String label;

    /**
     * constructor
     *
     * @param A
     * @param ntax
     * @param weight
     */
    public ASplit(BitSet A, int ntax, float weight) {
        this.ntax = ntax;
        this.A = new BitSet();
        this.A.or(A);
        if (!this.A.get(1))
            this.A.flip(1, ntax + 1); // always want A to be the set containing 1...
        this.weight = weight;
    }

    public int ntax() {
        return ntax;
    }

    public int size() {
        return Math.min(A.cardinality(), ntax - A.cardinality());
    }

    public BitSet getA() {
        return A;
    }

    /**
     * get complement
     *
     * @return complement
     */
    public BitSet getComplement() {
        BitSet result = (BitSet) A.clone();
        result.flip(1, ntax + 1);
        return result;
    }

    /**
     * get complement
     *
     * @param ntax
     * @return complement
     */
    public BitSet getComplement(int ntax) {
        BitSet result = new BitSet();
        for (int t = A.nextClearBit(1); t != -1 && t <= ntax; t = A.nextClearBit(t + 1))
            result.set(t);
        return result;
    }

    /**
     * does the set A contain the given taxon
     *
     * @param t in range 1 to ntax
     * @return true, if contained
     */
    public boolean isContainedInA(int t) {
        return A.get(t + 1);
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (int t = A.nextSetBit(0); t != -1; t = A.nextSetBit(t + 1)) {
            if (first)
                first = false;
            else
                buf.append(" ");
            buf.append(t);
        }
        return buf.toString();
    }

    /**
     * is this equal to the given split in terms of ntax, set A and weight?
     *
     * @param obj
     * @return true, if obj is instance of SimpleSplit and has the same ntax, set A and weight
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASplit) {
            final ASplit that = (ASplit) obj;
            return this.ntax == that.ntax && this.A.equals(that.A) && this.weight == that.weight;
        } else
            return false;
    }

    public ASplit clone() {
        // todo: test this
        try {
            return (ASplit) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
