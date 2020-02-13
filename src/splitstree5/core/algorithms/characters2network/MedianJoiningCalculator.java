/*
 * MedianJoiningCalculator.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2network;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloGraph;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;

import java.util.*;

public class MedianJoiningCalculator extends QuasiMedianBase {
    private int optionEpsilon = 0;

    /**
     * runs the median joining algorithm
     *
     * @param inputSequences
     * @param weights
     * @return median joining network
     */
    public void computeGraph(ProgressListener progressListener, Set<String> inputSequences, double[] weights, PhyloGraph graph) throws CanceledException {
        System.err.println("Computing the median joining network for epsilon=" + getOptionEpsilon());
        Set<String> outputSequences = new HashSet<>();
        computeMedianJoiningMainLoop(progressListener, inputSequences, weights, getOptionEpsilon(), outputSequences);

        final EdgeSet feasibleLinks = new EdgeSet(graph);
        boolean changed;
        do {
            graph.clear();
            feasibleLinks.clear();
            computeMinimumSpanningNetwork(outputSequences, weights, 0, graph, feasibleLinks);
            for (Edge e : graph.edges()) {
                if (!feasibleLinks.contains(e))
                    graph.deleteEdge(e);
            }
            changed = removeObsoleteNodes(graph, inputSequences, outputSequences, feasibleLinks);
            progressListener.incrementProgress();
        }
        while (changed);
    }

    /**
     * Main loop of the median joining algorithm
     *
     * @param input
     * @param epsilon
     * @return sequences present in the median joining network
     */
    private void computeMedianJoiningMainLoop(ProgressListener progress, Set<String> input, double[] weights, int epsilon, Set<String> outputSequences) throws CanceledException {
        outputSequences.addAll(input);

        boolean changed = true;
        while (changed) {
            System.err.println("Median joining: " + outputSequences.size() + " sequences");
            progress.incrementProgress();
            changed = false;

            final PhyloGraph graph = new PhyloGraph();
            final EdgeSet feasibleLinks = new EdgeSet(graph);

            computeMinimumSpanningNetwork(outputSequences, weights, epsilon, graph, feasibleLinks);

            if (removeObsoleteNodes(graph, input, outputSequences, feasibleLinks)) {
                changed = true;   // sequences have been changed, recompute graph
            } else {
                // determine min connection cost:
                double minConnectionCost = Double.MAX_VALUE;

                for (Node u = graph.getFirstNode(); u != null; u = u.getNext()) {
                    String seqU = (String) u.getInfo();
                    for (Edge e = u.getFirstAdjacentEdge(); e != null; e = u.getNextAdjacentEdge(e)) {
                        Node v = e.getOpposite(u);
                        String seqV = (String) v.getInfo();
                        for (Edge f = u.getNextAdjacentEdge(e); f != null; f = u.getNextAdjacentEdge(f)) {
                            Node w = f.getOpposite(u);
                            String seqW = (String) w.getInfo();
                            String[] qm = computeQuasiMedian(seqU, seqV, seqW);
                            for (String aQm : qm) {
                                if (!outputSequences.contains(aQm)) {
                                    double cost = computeConnectionCost(seqU, seqV, seqW, aQm, weights);
                                    if (cost < minConnectionCost)
                                        minConnectionCost = cost;
                                }
                            }
                        }
                        progress.checkForCancel();
                    }
                }
                for (Edge e : feasibleLinks) {
                    final Node u = e.getSource();
                    final Node v = e.getTarget();
                    final String seqU = (String) u.getInfo();
                    final String seqV = (String) v.getInfo();
                    for (Edge f : feasibleLinks.successors(e)) {
                        Node w;
                        if (f.getSource() == u || f.getSource() == v)
                            w = f.getTarget();
                        else if (f.getTarget() == u || f.getTarget() == v)
                            w = f.getSource();
                        else
                            continue;
                        String seqW = (String) w.getInfo();
                        String[] qm = computeQuasiMedian(seqU, seqV, seqW);
                        for (String aQm : qm) {
                            if (!outputSequences.contains(aQm)) {
                                double cost = computeConnectionCost(seqU, seqV, seqW, aQm, weights);
                                if (cost <= minConnectionCost + epsilon) {
                                    outputSequences.add(aQm);
                                    changed = true;
                                }
                            }
                        }
                    }
                    progress.checkForCancel();
                }
            }
        }
    }

    /**
     * computes the minimum spanning network upto a tolerance of epsilon
     *
     * @param sequences
     * @param weights
     * @param epsilon
     * @param graph
     * @param feasibleLinks
     */
    private void computeMinimumSpanningNetwork(Set<String> sequences, double[] weights, int epsilon, PhyloGraph graph, EdgeSet feasibleLinks) {
        String[] array = (String[]) sequences.toArray(new String[sequences.size()]);
        // compute a distance matrix between all sequences:
        double[][] matrix = new double[array.length][array.length];

        SortedMap<Double, List<Pair<Integer, Integer>>> value2pairs = new TreeMap<>();

        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                matrix[i][j] = computeDistance(array[i], array[j], weights);
                Double value = matrix[i][j];
                final List<Pair<Integer, Integer>> pairs = value2pairs.computeIfAbsent(value, k -> new LinkedList<>());
                pairs.add(new Pair<>(i, j));
            }
        }

        Node[] nodes = new Node[array.length];
        int[] componentsOfMSN = new int[array.length];
        int[] componentsOfThresholdGraph = new int[array.length];

        for (int i = 0; i < array.length; i++) {
            nodes[i] = graph.newNode(array[i]);
            graph.setLabel(nodes[i], array[i]);
            componentsOfMSN[i] = i;
            componentsOfThresholdGraph[i] = i;
        }

        int numComponentsMSN = array.length;

        // TODO: This implementation of the minimum spanning network is wrong, add only edges between different connected components

        double maxValue = Double.MAX_VALUE;
        // all sets of edges in ascending order of lengths
        for (Object o : value2pairs.keySet()) {
            Double value = (Double) o;
            double threshold = value;
            if (threshold > maxValue)
                break;
            List<Pair<Integer, Integer>> ijPairs = value2pairs.get(value);

            // update threshold graph components:
            for (int i = 0; i < array.length; i++) {
                for (int j = i + 1; j < array.length; j++) {
                    if (componentsOfThresholdGraph[i] != componentsOfThresholdGraph[j] && matrix[i][j] < threshold - epsilon) {
                        int oldComponent = componentsOfThresholdGraph[i];
                        int newComponent = componentsOfThresholdGraph[j];
                        for (int k = 0; k < array.length; k++) {
                            if (componentsOfThresholdGraph[k] == oldComponent)
                                componentsOfThresholdGraph[k] = newComponent;
                        }
                    }
                }
            }

            // determine new edges for minimum spanning network and determine feasible links
            List<Pair<Integer, Integer>> newPairs = new LinkedList<>();
            for (Pair<Integer, Integer> ijPair : ijPairs) {
                int i = ijPair.getFirst();
                int j = ijPair.getSecond();

                Edge e = graph.newEdge(nodes[i], nodes[j]);
                graph.setWeight(e, matrix[i][j]);

                if (feasibleLinks != null && componentsOfThresholdGraph[i] != componentsOfThresholdGraph[j]) {
                    feasibleLinks.add(e);
                    if (false)
                        System.err.println("ERROR nodes are connected: " + i + ", " + j);
                }
                newPairs.add(new Pair<>(i, j));
            }

            // update MSN components
            for (Pair<Integer, Integer> pair : newPairs) {
                int i = pair.getFirst();
                int j = pair.getSecond();
                if (componentsOfMSN[i] != componentsOfMSN[j]) {
                    numComponentsMSN--;
                    int oldComponent = componentsOfMSN[i];
                    int newComponent = componentsOfMSN[j];
                    for (int k = 0; k < array.length; k++)
                        if (componentsOfMSN[k] == oldComponent)
                            componentsOfMSN[k] = newComponent;
                }
            }
            if (numComponentsMSN == 1 && maxValue == Double.MAX_VALUE)
                maxValue = threshold + epsilon; // once network is connected, add all edges upto threshold+epsilon
        }
    }

    /**
     * determine whether v and target are connected by a chain of edges all of weight-threshold. Use for debugging
     *
     * @param graph
     * @param v
     * @param target
     * @param visited
     * @param threshold
     * @return true, if connected
     */
    private boolean areConnected(PhyloGraph graph, Node v, Node target, NodeSet visited, double threshold) {
        if (v == target)
            return true;

        if (!visited.contains(v)) {
            visited.add(v);

            for (Edge e = v.getFirstAdjacentEdge(); e != null; e = v.getNextAdjacentEdge(e)) {
                if (graph.getWeight(e) < threshold) {
                    Node w = e.getOpposite(v);
                    if (areConnected(graph, w, target, visited, threshold))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * iteratively removes all nodes that are connected to only two other and are not part of the original input
     *
     * @param graph
     * @param input
     * @param sequences
     * @return true, if anything was removed
     */
    private boolean removeObsoleteNodes(PhyloGraph graph, Set input, Set sequences, EdgeSet feasibleLinks) {
        int removed = 0;
        boolean changed = true;
        while (changed) {
            changed = false;
            List toDelete = new LinkedList();

            for (Node v = graph.getFirstNode(); v != null; v = v.getNext()) {
                String seqV = (String) v.getInfo();
                if (!input.contains(seqV)) {
                    int count = 0;
                    for (Edge e = v.getFirstAdjacentEdge(); count <= 2 && e != null; e = v.getNextAdjacentEdge(e)) {
                        if (feasibleLinks.contains(e))
                            count++;
                    }
                    if (count <= 2)
                        toDelete.add(v);
                }
            }
            if (toDelete.size() > 0) {
                changed = true;
                removed += toDelete.size();
                for (Object aToDelete : toDelete) {
                    Node v = (Node) aToDelete;
                    sequences.remove(v.getInfo());
                    graph.deleteNode(v);
                }
            }
        }
        return removed > 0;
    }


    /**
     * compute the cost of connecting seqM to the other three sequences
     *
     * @param seqU
     * @param seqV
     * @param seqW
     * @param seqM
     * @return cost
     */
    private double computeConnectionCost(String seqU, String seqV, String seqW, String seqM, double[] weights) {
        return computeDistance(seqU, seqM, weights) + computeDistance(seqV, seqM, weights) + computeDistance(seqW, seqM, weights);
    }

    /**
     * compute weighted distance between two sequences
     *
     * @param seqA
     * @param seqB
     * @return distance
     */
    private double computeDistance(String seqA, String seqB, double[] weights) {
        double cost = 0;
        for (int i = 0; i < seqA.length(); i++) {
            if (seqA.charAt(i) != seqB.charAt(i))
                if (weights != null)
                    cost += weights[i];
                else
                    cost++;
        }
        return cost;
    }


    /**
     * computes the quasi median for three sequences
     *
     * @param seqA
     * @param seqB
     * @param seqC
     * @return quasi median
     */
    private String[] computeQuasiMedian(String seqA, String seqB, String seqC) {
        StringBuilder buf = new StringBuilder();
        boolean hasStar = false;
        for (int i = 0; i < seqA.length(); i++) {
            if (seqA.charAt(i) == seqB.charAt(i) || seqA.charAt(i) == seqC.charAt(i))
                buf.append(seqA.charAt(i));
            else if (seqB.charAt(i) == seqC.charAt(i))
                buf.append(seqB.charAt(i));
            else {
                buf.append("*");
                hasStar = true;
            }
        }
        if (!hasStar)
            return new String[]{buf.toString()};

        Set median = new HashSet();
        Stack stack = new Stack();
        stack.add(buf.toString());
        while (!stack.empty()) {
            String seq = (String) stack.pop();
            int pos = seq.indexOf('*');
            int pos2 = seq.indexOf('*', pos + 1);
            String first = seq.substring(0, pos) + seqA.charAt(pos) + seq.substring(pos + 1);
            String second = seq.substring(0, pos) + seqB.charAt(pos) + seq.substring(pos + 1);
            String third = seq.substring(0, pos) + seqC.charAt(pos) + seq.substring(pos + 1);
            if (pos2 == -1) {
                median.add(first);
                median.add(second);
                median.add(third);
            } else {
                stack.add(first);
                stack.add(second);
                stack.add(third);
            }
        }
        return (String[]) median.toArray(new String[median.size()]);
    }

    public int getOptionEpsilon() {
        return optionEpsilon;
    }

    public void setOptionEpsilon(int optionEpsilon) {
        this.optionEpsilon = optionEpsilon;
    }
}