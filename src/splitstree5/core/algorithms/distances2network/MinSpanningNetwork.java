/*
 * MinSpanningNetwork.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.distances2network;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.Pair;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IToNetwork;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.util.*;

/**
 * computes a minimum spanning network
 */
public class MinSpanningNetwork extends Algorithm<DistancesBlock, NetworkBlock> implements IFromDistances, IToNetwork {
    @Override
    public String getCitation() {
        return "Excoffier & Smouse 1994; Excoffier L, Smouse PE. Using allele frequencies and geographic subdivision to reconstruct gene trees within a species: molecular variance parsimony (1994) Genetics.136(1):343-59.";
    }

    private final DoubleProperty optionEpsilon = new SimpleDoubleProperty(0);
    private final BooleanProperty optionMinSpanningTree = new SimpleBooleanProperty(false);

    public List<String> listOptions() {
        return Arrays.asList("Epsilon", "MinSpanningTree");
    }

    @Override
    public String getToolTip(String optionName) {
        switch (optionName) {
            case "Epsilon":
                return "weighted genetic distance measure. Low: MedianJoining, High: full median network";
            case "MinSpanningTree":
                return "calculate MinSpanningTree ";
            default:
                return optionName;
        }
    }

    /**
     * Determine whether given method can be applied to given data.
     *
     * @param taxa  the taxa
     * @param chars the characters matrix
     * @return true, if method applies to given data
     */
    public boolean isApplicable(TaxaBlock taxa, CharactersBlock chars) {
        return taxa != null && chars != null && chars.getNcolors() < 8; // not too  many different states
    }

    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DistancesBlock parent, NetworkBlock child) throws Exception {
        final int ntax = taxaBlock.getNtax();

        final PhyloGraph graph = child.getGraph();

        final Map<Double, List<Pair<Integer, Integer>>> distancesToTaxonPairs = new TreeMap<>();
        for (int a = 1; a <= taxaBlock.getNtax(); a++) {
            for (int b = a + 1; b <= taxaBlock.getNtax(); b++) {
                distancesToTaxonPairs.computeIfAbsent(parent.get(a, b), k -> new ArrayList<>()).add((new Pair<>(a, b)));
            }
        }

        final Node[] node = new Node[ntax + 1];
        final int[] component = new int[ntax + 1];

        for (int t = 1; t <= ntax; t++) {
            final Node v = graph.newNode(t);
            node[t] = v;
            graph.addTaxon(v, t);
            graph.setLabel(v, taxaBlock.get(t).getDisplayLabelOrName());
            component[t] = t;
        }

        int numComponentsMSN = ntax;


        double maxValue = Double.POSITIVE_INFINITY;
        // all sets of edges in ascending order of lengths

        progress.setMaximum(distancesToTaxonPairs.size());

        for (Double value : distancesToTaxonPairs.keySet()) {
            progress.incrementProgress();

            double threshold = value;
            if (threshold > maxValue)
                break;

            boolean mustUpdateMSNComponents = false;

            // create edges:
            for (Pair<Integer, Integer> pair : distancesToTaxonPairs.get(value)) {
                final int a = pair.getFirst();
                final int b = pair.getSecond();

                if (!optionMinSpanningTree.getValue() || component[a] != component[b]) {
                    Edge e = graph.newEdge(node[a], node[b]);
                    graph.setWeight(e, parent.get(a, b));

                    if (!optionMinSpanningTree.getValue())
                        mustUpdateMSNComponents = true;
                    else { // need to update immediately for tree
                        numComponentsMSN--;
                        int oldComponent = component[a];
                        int newComponent = component[b];
                        for (int k = 1; k <= ntax; k++)
                            if (component[k] == oldComponent)
                                component[k] = newComponent;
                    }
                }
            }

            // update MSN components
            if (mustUpdateMSNComponents) {
                for (Pair<Integer, Integer> pair : distancesToTaxonPairs.get(value)) {
                    final int a = pair.getFirst();
                    final int b = pair.getSecond();

                    if (component[a] != component[b]) {
                        numComponentsMSN--;
                        int oldComponent = component[a];
                        int newComponent = component[b];
                        for (int k = 1; k <= ntax; k++)
                            if (component[k] == oldComponent)
                                component[k] = newComponent;
                    }
                }
            }
            if (numComponentsMSN == 1) {
                if (optionMinSpanningTree.getValue())
                    return; // tree
                if (maxValue == Double.POSITIVE_INFINITY)
                    maxValue = threshold + getOptionEpsilon(); // once network is connected, add all edges upto threshold+epsilon
            }
        }
    }

    public double getOptionEpsilon() {
        return optionEpsilon.getValue();
    }

    public DoubleProperty optionEpsilonProperty() {
        return optionEpsilon;
    }

    public void setOptionEpsilon(double optionEpsilon) {
        this.optionEpsilon.setValue(optionEpsilon);
    }

    public boolean isOptionMinSpanningTree() {
        return optionMinSpanningTree.getValue();
    }

    public BooleanProperty optionMinSpanningTreeProperty() {
        return optionMinSpanningTree;
    }

    public void setOptionMinSpanningTree(boolean optionMinSpanningTree) {
        this.optionMinSpanningTree.setValue(optionMinSpanningTree);
    }
}