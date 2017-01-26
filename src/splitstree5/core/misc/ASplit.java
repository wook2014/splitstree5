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
    private final BitSet B;
    private double weight;
    private double confidence;
    private String label;

    /**
     * constructor
     *
     * @param A
     * @param ntax
     */
    public ASplit(BitSet A, int ntax) {
        this(A, ntax, 1, 1);
    }

    /**
     * constructor
     *
     * @param A
     * @param ntax
     * @param weight
     */
    public ASplit(BitSet A, int ntax, double weight) {
        this(A, ntax, weight, 1);
    }

    /**
     * constructor
     *
     * @param A
     * @param ntax
     * @param weight
     */
    public ASplit(BitSet A, int ntax, double weight, double confidence) {
        this.A = new BitSet();
        this.B = new BitSet();
        if (A.get(1)) {
            this.A.or(A);
            this.B.or(A);
            this.B.flip(1, ntax + 1);
        } else {
            this.A.or(A);
            this.A.flip(1, ntax + 1);
            this.B.or(A);
        }
        this.weight = weight;
        this.confidence = confidence;
    }

    /**
     * constructor
     *
     * @param A
     * @param B
     */
    public ASplit(BitSet A, BitSet B) {
        this(A, B, 1, 1);
    }

    /**
     * constructor
     *
     * @param A
     * @param B
     * @param weight
     */
    public ASplit(BitSet A, BitSet B, double weight) {
        this(A, B, weight, 1);
    }

    /**
     * constructor
     *
     * @param A
     * @param B
     * @param weight
     */
    public ASplit(BitSet A, BitSet B, double weight, double confidence) {
        this.A = new BitSet();
        this.B = new BitSet();
        if (A.get(1)) {
            this.A.or(A);
            this.B.or(B);
        } else {
            this.A.or(B);
            this.B.or(A);
        }
        this.weight = weight;
        this.confidence = confidence;
    }

    public int ntax() {
        return A.cardinality() + B.cardinality();
    }

    public int size() {
        return Math.min(A.cardinality(), B.cardinality());
    }

    /**
     * get part A
     *
     * @return A
     */
    public BitSet getA() {
        return A;
    }

    /**
     * get part B
     *
     * @return B
     */
    public BitSet getB() {
        return B;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * is taxon t contained in part A?
     *
     * @param t number from 1 to ntax
     * @return true, if contained
     */
    public boolean isContainedInA(int t) {
        return A.get(t);
    }

    /**
     * is taxon t contained in part B?
     *
     * @param t number from 1 to ntax
     * @return true, if contained
     */
    public boolean isContainedInB(int t) {
        return B.get(t);
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
     * is this equal to the given split in terms of A and B
     *
     * @param obj
     * @return true, if obj is instance of ASplit and has the sets A and B
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASplit) {
            final ASplit that = (ASplit) obj;
            return this.A.equals(that.A) && this.B.equals(that.B);
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
