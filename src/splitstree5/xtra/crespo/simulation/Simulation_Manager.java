/*
 *  Simulation_Manager.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.xtra.crespo.simulation;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.io.imports.utils.SimpleNewickParser;
import splitstree5.xtra.crespo.util.MyNewickParser;
import splitstree5.xtra.crespo.util.MyNode;
import splitstree5.xtra.crespo.util.MyTree;
import splitstree5.xtra.crespo.util.RandomBinaryTree_Generator;

import java.io.IOException;
import java.util.*;

public class Simulation_Manager {

    private final static char[] SIGMA = {'A', 'G', 'T', 'C'};
    private double maxPE = 0., minPE = Double.POSITIVE_INFINITY, avgPE = 0, peCounter = 0;

    public Object[] run(int numberOfTaxa, int maxBranchLength, int seqLength, int numOfTrees, int numOfSPRTrees, int numOfSPRs, double u) throws IOException {

        // creating random tree
        System.out.println(">Step 1 - Initializing Jukes Cantor Model");
        Object[] res = RandomBinaryTree_Generator.run(numberOfTaxa);
        MyTree speciesTree = (MyTree) res[0];
        ArrayList<String> taxaOrdering = (ArrayList<String>) res[1];
        speciesTree.assingBranchLength(maxBranchLength, numberOfTaxa);
        String startSequence = createRandomSequence(seqLength);
        System.out.println(speciesTree.toNewickString());
        System.out.println("Avg-EdgeLength: " + speciesTree.getAvgEdgeLength());

        // computing sets of evolved sequences
        System.out.println(">Step 2 - Evolving sequences along the model tree");
        ArrayList<HashMap<MyNode, String>> setOfEvolvedSeqs = evolveSequence(speciesTree, startSequence, numOfTrees, u);
        System.out.println("MaxPE: " + maxPE + ", minPE: " + minPE + " AvgPE: " + avgPE);
        System.out.println(setOfEvolvedSeqs.size() + "x" + setOfEvolvedSeqs.get(0).size() + " sequences generated.");

        // inferring trees based on evolved sequences
        System.out.println(">Step 3 - Inferring trees from evolved sequences");
        ArrayList<MyTree> geneTrees = new RAxML_TreeGenerator().run(setOfEvolvedSeqs);
        System.out.println(geneTrees.size() + " trees inferred.");
        for (MyTree t : geneTrees)
            System.out.println(t.toNewickString());

        // applying rSPR-moves
        System.out.println(">Step 4 - Applying rSPR moves");
        ArrayList<BitSet[]> sprMoves = new SPR_Performer().run(speciesTree, geneTrees, numOfSPRTrees, numOfSPRs, taxaOrdering);
        for (Object[] spr : sprMoves)
            System.out.println("SPR-Clusters: " + spr[0] + " " + spr[1]);
        System.out.println(numOfSPRs + "x" + numOfSPRTrees + " tree(s) modified.");

        Map<String, Integer> taxName2Id = new HashMap<>(); // starts at 1
        Set<String> taxonNamesFound = new TreeSet<>();


        for (String name : taxaOrdering) {
            taxonNamesFound.add(name);
            taxName2Id.put(name, taxonNamesFound.size()); //
        }

        // reporting output
        System.out.println(">Output ");
        ArrayList<PhyloTree> phyloTrees = new ArrayList<PhyloTree>();
        for (MyTree t : geneTrees) {
            System.out.println(t.toNewickString());
            PhyloTree pt = converToPhyloTree(t);

            for (Node v : pt.nodes()) {
                final String label = pt.getLabel(v);
                if (label != null && label.length() > 0) {
                    if (taxonNamesFound.contains(label)) {
                        pt.addTaxon(v, taxName2Id.get(label));
                    }
                }
            }

            pt.setRoot(pt.leaves().iterator().next());
            phyloTrees.add(pt);

        }

        Object[] result = {converToPhyloTree(speciesTree), phyloTrees, taxaOrdering, geneTrees};
        return result;

    }

    public PhyloTree converToPhyloTree(MyTree myTree) {

        PhyloTree result = null;
        try {
            result = new SimpleNewickParser().parse(myTree.toNewickString());

        } catch (Exception ex) {

            ex.printStackTrace();

        }

        return result;

    }

    public MyTree converToMyTree(PhyloTree phyloTree) {


        return new MyNewickParser().run(phyloTree.toBracketString());


    }

    private ArrayList<HashMap<MyNode, String>> evolveSequence(MyTree modelTree, String startSequence, int numOfTrees, double u) {
        ArrayList<HashMap<MyNode, String>> setOfevolvedSeqs = new ArrayList<HashMap<MyNode, String>>();
        for (int i = 0; i < numOfTrees; i++) {
            HashMap<MyNode, String> evolvedSeqs = new HashMap<MyNode, String>();
            evolveSeqRec(modelTree.getRoot(), startSequence, evolvedSeqs, u);
            setOfevolvedSeqs.add(evolvedSeqs);
        }
        avgPE /= peCounter;

        return setOfevolvedSeqs;
    }

    private void evolveSeqRec(MyNode v, String sequence, HashMap<MyNode, String> evolvedSeqs, double u) {
        if (v.isLeaf())
            evolvedSeqs.put(v, sequence);
        else {
            for (MyNode c : v.getChildren()) {
                StringBuilder evolvedSeq = new StringBuilder();
                for (int i = 0; i < sequence.length(); i++) {
                    double t = c.getBranchLength();
                    double p = 1. - Math.pow(Math.E, (-4. / 3.) * u * t);
                    if (Math.random() < p)
                        evolvedSeq.append(randomChar());
                    else
                        evolvedSeq.append(sequence.charAt(i));
                    maxPE = p > maxPE ? p : maxPE;
                    minPE = p < minPE ? p : minPE;
                    avgPE += p;
                    peCounter++;
                }
                evolveSeqRec(c, evolvedSeq.toString(), evolvedSeqs, u);
            }
        }
    }

    private String createRandomSequence(int seqLength) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < seqLength; i++)
            s.append(randomChar());
        return s.toString();
    }

    private char randomChar() {
        double d = Math.random() * (SIGMA.length - 1);
        int index = (int) Math.round(d);
        return SIGMA[index];
    }

}
