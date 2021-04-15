/*
 * ASplit.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.misc;

import jloda.util.BitSetUtils;

import java.util.BitSet;

/**
 * simple split implementation
 * Daniel Huson, 12/9/16.
 */
public final class ASplit implements Comparable<ASplit> {
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
        if (A.cardinality() == 0 || B.cardinality() == 0)
            System.err.println("Internal error: A.cardinality()=" + A.cardinality() + ", B.cardinality()=" + B.cardinality());
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

    public static int compare(ASplit a, ASplit b) {
        int com = BitSetUtils.compare(a.getA(), b.getA());
        if (com == 0)
            com = BitSetUtils.compare(a.getB(), b.getB());
        return com;
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

    /**
     * gets the split part that contains the given taxon, or A, if none contains it
     *
     * @param t
     * @return split part containing taxon t
     */
    public BitSet getPartContaining(int t) {
        if (B.get(t))
            return B;
        else
            return A;
    }

    /**
     * returns A, if A doesn't contain t, else B
     *
     * @param t
     * @return set not containing t
     */
    public BitSet getPartNotContaining(int t) {
        if (!A.get(t))
            return A;
        else
            return B;
    }

    /**
     * gets the smaller part. In the case of a tie, return the set that contains 1
     *
     * @return smaller part
     */
    public BitSet getSmallerPart() {
        if (A.cardinality() < B.cardinality())
            return A;
        else if (A.cardinality() > B.cardinality())
            return B;
        else if (A.get(1))
            return A;
        else
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
     * is this equals to the given split in terms of A and B
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
        ASplit result = new ASplit(this.getA(), this.ntax());
        result.setWeight(this.getWeight());
        result.setConfidence(this.getConfidence());
        result.setLabel(this.label);
        return result;
    }

    public int compareTo(ASplit other) {
        int result = BitSetUtils.compare(getPartContaining(1), other.getPartContaining(1));
        if (result == 0)
            result = BitSetUtils.compare(getPartNotContaining(1), other.getPartNotContaining(1));
        return result;

    }

    public boolean isTrivial() {
        return getSmallerPart().cardinality() == 1;
    }

    public boolean separates(int a, int b) {
        return getPartContaining(a) != getPartContaining(b);
    }
}
