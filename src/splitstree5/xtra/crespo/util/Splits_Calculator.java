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

package splitstree5.xtra.crespo.util;

import java.util.ArrayList;
import java.util.BitSet;

public class Splits_Calculator {

    public static ArrayList<BitSet[]> run(MyTree t, ArrayList<String> taxaOrdering) {

        ArrayList<BitSet[]> splits = new ArrayList<BitSet[]>();
        computeSplitsRec(t.getRoot(), splits, taxaOrdering);
        return splits;

    }

    private static BitSet computeSplitsRec(MyNode v, ArrayList<BitSet[]> splits, ArrayList<String> taxaOrdering) {
        BitSet b = new BitSet(taxaOrdering.size());
        if (v.isLeaf())
            b.set(taxaOrdering.indexOf(v.getId()));
        else {
            for (MyNode w : v.getChildren())
                b.or(computeSplitsRec(w, splits, taxaOrdering));
        }
        BitSet[] split = createSplit(b, taxaOrdering);
        splits.add(split);
        return b;
    }

    private static BitSet[] createSplit(BitSet b1, ArrayList<String> taxaOrdering) {
        BitSet b2 = new BitSet(taxaOrdering.size());
        b2.set(0, taxaOrdering.size());
        b2.xor(b1);
        BitSet[] split = {b1, b2};
        return split;
    }

}