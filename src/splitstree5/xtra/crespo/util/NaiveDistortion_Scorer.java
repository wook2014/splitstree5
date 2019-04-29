/*
 *  NaiveDistortion_Scorer.java Copyright (C) 2019 Daniel H. Huson
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

public class NaiveDistortion_Scorer {

    private int distortion = -1;

    public int run(MyTree t, ArrayList<String> taxaOrdering, BitSet A, BitSet B) {
        for (int k = 0; k < 5; k++) {
            cmpDistortionScoreRec(t, taxaOrdering, A, B, 0, k);
            if (distortion != -1)
                break;
        }
        return distortion;
    }

    private void cmpDistortionScoreRec(MyTree t, ArrayList<String> taxaOrdering, BitSet A, BitSet B, int score, int k) {

        if (checkTree(t, taxaOrdering, A, B))
            distortion = score;
        else if (score < k && distortion == -1) {
            ArrayList<BitSet> clusterSet = t.getAllCluster(taxaOrdering);
            for (int i = 0; i < clusterSet.size(); i++) {
                BitSet b1 = clusterSet.get(i);
                if (b1.cardinality() < taxaOrdering.size()) {
                    for (int j = 0; j < clusterSet.size(); j++) {
                        BitSet b2 = clusterSet.get(j);
                        if (!containmentCheck(b1, b2)) {
                            MyTree tCopy = new MyNewickParser().run(t.toNewickString());
                            MyNode v1 = tCopy.getNode(b1, taxaOrdering);
                            MyNode v2 = tCopy.getNode(b2, taxaOrdering);
                            if (!v1.equals(tCopy.getRoot())) {
                                if (tCopy.pruneAndRegraft(v1, v2, taxaOrdering)) {
                                    cmpDistortionScoreRec(tCopy, taxaOrdering, A, B, new Integer(score + 1), k);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private boolean containmentCheck(BitSet b1, BitSet b2) {
        BitSet b = (BitSet) b2.clone();
        b.and(b1);
        return b.equals(b2);
    }

    private boolean checkTree(MyTree t, ArrayList<String> taxaOrdering, BitSet A, BitSet B) {
        for (BitSet c : t.getAllCluster(taxaOrdering)) {
            if (c.equals(A) || c.equals(B))
                return true;
        }
        return false;
    }

}