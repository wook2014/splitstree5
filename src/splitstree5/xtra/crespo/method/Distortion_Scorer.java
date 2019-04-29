/*
 *  Distortion_Scorer.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.xtra.crespo.method;

import splitstree5.xtra.crespo.util.MyNode;
import splitstree5.xtra.crespo.util.MyTree;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;


public class Distortion_Scorer {

    public int run(MyTree t, BitSet A, BitSet B, ArrayList<String> taxaOrdering) {

        // computeScoreNodeConflictingSet(t,  A,  B, t.getTaxa());

        ArrayList<MyNode> nodes = t.getNodes();
        HashMap<MyNode, Integer> scoreA = new HashMap<MyNode, Integer>();
        HashMap<MyNode, Integer> scoreB = new HashMap<MyNode, Integer>();

        // calculating set of sub leaves
        HashMap<MyNode, ArrayList<MyNode>> nodeToSubLeaves = new HashMap<MyNode, ArrayList<MyNode>>();
        setUpLeafListRec(t.getRoot(), nodeToSubLeaves);

        // checking overlap of splits with leaf-set
        BitSet leafSet = new BitSet(taxaOrdering.size());
        for (MyNode l : nodeToSubLeaves.get(t.getRoot()))
            leafSet.set(taxaOrdering.indexOf(l.getId()));
        BitSet b = (BitSet) leafSet.clone();
        b.and(A);
        if (b.cardinality() <= 1)
            return 0;
        b = (BitSet) leafSet.clone();
        b.and(B);
        if (b.cardinality() <= 1)
            return 0;

        // initializing A- & B-scores for each node
        for (MyNode v : nodes) {

            // looking into leaf-sets
            boolean hasA = false, hasB = false;
            ArrayList<MyNode> subLeaves = nodeToSubLeaves.get(v);
            for (MyNode l : subLeaves) {
                int index = taxaOrdering.indexOf(l.getId());
                if (A.get(index))
                    hasA = true;
                if (B.get(index))
                    hasB = true;
            }

            // setting A- & B-values
            int aValue = 0, bValue = 0;
            if (hasA && !hasB)
                bValue = Integer.MAX_VALUE;
            else if (!hasA && hasB)
                aValue = Integer.MAX_VALUE;
            // else if (hasA && hasB)
            // aValue = bValue = 1;
            scoreA.put(v, aValue);
            scoreB.put(v, bValue);

        }

        // computing distortion score
        computeScoresRec(t.getRoot(), scoreA, scoreB);
        int rootScoreA = scoreA.get(t.getRoot());
        int rootScoreB = scoreB.get(t.getRoot());
        int distortion = Math.min(rootScoreA, rootScoreB) - 1;

        return distortion;

    }

    private void computeScoresRec(MyNode v, HashMap<MyNode, Integer> scoreA, HashMap<MyNode, Integer> scoreB) {

        boolean isABeatenByB = false, isBBeatenByA = false;
        int countA = 0, countB = 0;

        for (MyNode w : v.getChildren()) {
            computeScoresRec(w, scoreA, scoreB);
            if (scoreA.get(w) < scoreB.get(w)) {
                isBBeatenByA = true;
                countB += scoreA.get(w);
            } else
                countB += scoreB.get(w);
            if (scoreB.get(w) < scoreA.get(w)) {
                isABeatenByB = true;
                countA += scoreB.get(w);
            } else
                countA += scoreA.get(w);
        }
        if (scoreA.get(v) < scoreB.get(v)) {
            isBBeatenByA = true;
            countB += scoreA.get(v);
        } else
            countB += scoreB.get(v);
        if (scoreB.get(v) < scoreA.get(v)) {
            isABeatenByB = true;
            countA += scoreB.get(v);
        } else
            countA += scoreA.get(v);

        if (isBBeatenByA)
            countB++;
        if (isABeatenByB)
            countA++;

        scoreB.put(v, countB);
        scoreA.put(v, countA);
        // System.out.println(v.getId() + " :" + scoreA.get(v) + "/" + scoreB.get(v));

    }

    private void setUpLeafListRec(MyNode v, HashMap<MyNode, ArrayList<MyNode>> nodeToSubLeaves) {
        ArrayList<MyNode> leaves = new ArrayList<MyNode>();
        for (MyNode w : v.getChildren()) {
            if (w.isLeaf()) {
                ArrayList<MyNode> singleSet = new ArrayList<MyNode>();
                singleSet.add(w);
                nodeToSubLeaves.put(w, singleSet);
                leaves.add(w);
            } else {
                setUpLeafListRec(w, nodeToSubLeaves);
                leaves.addAll(nodeToSubLeaves.get(w));
            }
        }
        nodeToSubLeaves.put(v, leaves);
    }


    private void computeScoreNodeConflictingSet(MyTree t, BitSet A, BitSet B, ArrayList<String> taxaOrdering) {


        HashMap<MyNode, BitSet> nodeCluster = new HashMap<MyNode, BitSet>();
        HashMap<MyNode, BitSet> leftConflictingSet = new HashMap<MyNode, BitSet>();
        HashMap<MyNode, BitSet> rightConflictingSet = new HashMap<MyNode, BitSet>();
        BitSet cluster;


        BitSet Ac = (BitSet) A.clone();
        BitSet Bc = (BitSet) B.clone();
        BitSet temp;
        // try wtith t.getLCA


        for (MyNode node : t.getNodes()) {

            Ac = (BitSet) A.clone();
            Bc = (BitSet) B.clone();
            cluster = t.getCluster(node, t.getTaxa()); //indexes of the Bit sets of the splits does not correspond to indexes in the taxa
            nodeCluster.put(node, cluster);
            if (cluster.intersects(A)) {
                Ac.intersects(cluster);
                if (cluster.cardinality() < A.cardinality() && Ac.equals(cluster))
                    leftConflictingSet.put(node, difference(A, cluster));
                else if (cluster.cardinality() > A.cardinality())
                    leftConflictingSet.put(node, difference(cluster, A));
            } else
                leftConflictingSet.put(node, new BitSet(t.getTaxa().size()));
            if (cluster.intersects(B)) {
                Bc.intersects(cluster);
                if (cluster.cardinality() < B.cardinality() && Bc.equals(cluster))
                    rightConflictingSet.put(node, difference(B, cluster));
                else if (cluster.cardinality() > B.cardinality())
                    rightConflictingSet.put(node, difference(cluster, B));
            } else
                rightConflictingSet.put(node, new BitSet(t.getTaxa().size()));


        }


    }

    private BitSet difference(BitSet left, BitSet right) {

        BitSet result = (BitSet) left.clone();

        result.andNot(right);
        return result;
    }
}