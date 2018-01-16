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

package splitstree5.core.algorithms.views.algorithms;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import jloda.graph.*;
import jloda.phylo.PhyloGraph;
import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.graphtab.base.GeometryUtils;

import java.util.*;

public class BoxOptimizer {

    private static DaylightOptimizer instance;

    private ProgressListener progress;
    private final IntegerProperty optionIterations = new SimpleIntegerProperty(20);
    private final BooleanProperty optionUseWeights = new SimpleBooleanProperty(true);

    public static DaylightOptimizer getInstance() {
        if (instance == null)
            instance = new DaylightOptimizer();
        return instance;
    }

    /**
     * apply the algorithm to build a new graph
     *
     * @param progress
     * @param taxaBlock
     * @param graph
     * @param node2point
     */
    public void apply(ProgressListener progress, TaxaBlock taxaBlock, int numberOfSplits, PhyloGraph graph, NodeArray<Point2D> node2point) throws CanceledException {
        this.progress = progress;
        System.err.println("Running box optimizer");
        Node start = null;
        for (Node v : graph.nodes()) {
            if (graph.getLabel(v) != null && graph.getLabel(v).equals(taxaBlock.get(1).getName())) {
                start = v;
                break;
            }
        }
        if (start != null) {
            // convert all angles to rad:
            for (Edge e : graph.edges()) {
                graph.setAngle(e, GeometryUtils.deg2rad(graph.getAngle(e)));
            }
            try {
                runOptimizeBoxes(start, graph, numberOfSplits, node2point, new HashSet<BitSet>());
            } finally {
                // convert all angles back to deg:
                for (Edge e : graph.edges()) {
                    graph.setAngle(e, GeometryUtils.rad2deg(graph.getAngle(e)));
                }
            }
        }
    }


    /**
     * optimize the boxes of the graph
     *
     * @param graph
     */
    private void runOptimizeBoxes(Node start, PhyloGraph graph, int numberOfSplits, NodeArray<Point2D> node2point, HashSet forbiddenSplits) throws CanceledException {
        //We first build EdgeSplits, where each split is linked with the set containing all its edges
        final HashMap<Integer, List<Edge>> edgeSplits = getEdgeSplits(graph);
        Edge currentEdge;
        List currentEdges;

        int nbIterations = optionIterations.get();


        progress.setTasks("Optimize boxes", "iterating");
        progress.setMaximum(nbIterations * numberOfSplits);
        progress.setProgress(0);
        int counter = 0;

        Set<Integer> SplitsSet = edgeSplits.keySet();
        double totalSize = 0;
        for (Object aSplitsSet : SplitsSet) {
            int CurrentSplit = (Integer) aSplitsSet;
            currentEdges = edgeSplits.get(CurrentSplit);
            totalSize += maximizeArea(currentEdges, graph, graph.getAngle((Edge) currentEdges.get(0)), graph.getAngle((Edge) currentEdges.get(0))).getFirst();
        }

        double originalSize = totalSize;
        double previousSize = totalSize;

        for (int i = 0; (i < nbIterations); i++) {
            int score = (int) (Math.floor(100 * (totalSize - originalSize) / originalSize));
            double miniScore = (Math.floor(10000 * (totalSize - previousSize) / previousSize)) / 100.00;
            if (score > 0) {
                if (miniScore > 0) {
                    if (miniScore < 10) {
                        progress.setSubtask("box optim.: +" + score + "%  (" + (i) + ":+" + miniScore + "%)");
                    } else {
                        progress.setSubtask("box optim.: +" + score + "%  (" + (i) + ":+" + (int) Math.floor(miniScore) + "%)");
                    }
                } else {
                    progress.setSubtask("box optimiz.: +" + score + "%  (" + (i) + ":" + miniScore + "%)");
                }
            } else {
                if (miniScore > 0) {
                    if (miniScore < 10) {
                        progress.setSubtask("box optim.: " + score + "%  (" + (i) + ":+" + miniScore + "%)");
                    } else {
                        progress.setSubtask("box optim.: " + score + "%  (" + (i) + ":+" + (int) Math.floor(miniScore) + "%)");
                    }
                } else {
                    progress.setSubtask("box optim.: " + score + "%  (" + (i) + ":" + miniScore + "%)");
                }
            }
            previousSize = totalSize;

            SplitsSet = edgeSplits.keySet();
            totalSize = 0;

            //Iterator allSplits=SplitsSet.iterator();
            for (Object aSplitsSet : SplitsSet) {
                int CurrentSplit = (Integer) aSplitsSet;
                if (!forbiddenSplits.contains(CurrentSplit)) {
                    //We can move this split as it's is not in the forbidden list.

                    //If the split is improvable, it will have more chances to be improved:
                    counter++;
                    progress.setProgress(counter);
                    currentEdges = (List) edgeSplits.get(CurrentSplit);
                    currentEdge = (Edge) currentEdges.get(0);
                    Iterator CurrentEdgesIt;

                    double oldAngle = graph.getAngle(currentEdge);

                    //Compute the min and max variations of the split considering only the split itself (which must remain planar).
                    final Pair<Double, Double> currentExtremeAngles = trigClockAngles(currentEdges, graph, node2point);

                    //Choose a new angle for the split between those two critical angles :
                    double trigAngle = oldAngle + currentExtremeAngles.getFirst();
                    double clockAngle = oldAngle + currentExtremeAngles.getSecond();
                    //Good System.out.println("\n Split "+CurrentSplit+" Angle : "+oldAngle+" - Edge: "+CurrentEdge);
                    Pair<Double, Double> newTrigClock = createsCollisions(start, graph, trigAngle, clockAngle, oldAngle, currentEdges, oldAngle, node2point);

                    trigAngle = newTrigClock.getFirst();
                    clockAngle = newTrigClock.getSecond();

                    if (currentEdges.size() > 1) {
                        Pair<Double, Double> optimized = maximizeArea(currentEdges, graph, clockAngle, trigAngle);
                        CurrentEdgesIt = currentEdges.iterator();
                        while (CurrentEdgesIt.hasNext()) {
                            currentEdge = (Edge) CurrentEdgesIt.next();
                            graph.setAngle(currentEdge, optimized.getSecond());
                        }
                        totalSize += optimized.getFirst();
                    } else {
                        //The split only has one edge, we do not move it
                    }
                }
            }
        }
    }


    /**
     * returns (Max(splitArea),argMax(splitArea)) where the split is defined by its SplitEdges and the
     * split angle has to be between minAngle and maxAngle
     * <p/>
     * Can be used to get the current area of the split by setting minAngle=maxAngle= the split angle
     *
     * @param SplitEdges list of the Edges of the Split, not necessary all directed in the same sense.
     * @param graph
     * @param minAngle
     * @param maxAngle
     */
    public Pair<Double, Double> maximizeArea(List SplitEdges, PhyloGraph graph, double minAngle, double maxAngle) {
        //We will first express the area of the split as A cos x + B sin x, x being the split angle
        double alpha = 0;
        double beta = 0;
        double area = 0;

        Iterator EdgesIt = SplitEdges.iterator();
        Edge CurrentEdge = (Edge) EdgesIt.next();
        double resultAngle = 0;
        double splitAngle = GeometryUtils.moduloTwoPI(graph.getAngle(CurrentEdge));

        if (Math.abs(graph.getWeight(CurrentEdge)) > 0.0000000000001) {
            while (EdgesIt.hasNext()) {
                Edge ThePreviousEdge = CurrentEdge;
                CurrentEdge = (Edge) EdgesIt.next();
                Node NodeA = ThePreviousEdge.getSource();
                Node NodeB = CurrentEdge.getSource();
                if (!(NodeA.isAdjacent(NodeB))) {
                    NodeB = CurrentEdge.getTarget();
                }

                Edge uncompEdge = NodeA.getCommonEdge(NodeB);
                if (GeometryUtils.moduloTwoPI(graph.getAngle(uncompEdge) - splitAngle) < Math.PI) {
                    alpha = alpha + Math.sin(graph.getAngle(uncompEdge)) * graph.getWeight(uncompEdge);
                    beta = beta - Math.cos(graph.getAngle(uncompEdge)) * graph.getWeight(uncompEdge);
                } else {
                    alpha = alpha + Math.sin(graph.getAngle(uncompEdge) + Math.PI) * graph.getWeight(uncompEdge);
                    beta = beta - Math.cos(graph.getAngle(uncompEdge) + Math.PI) * graph.getWeight(uncompEdge);
                }
            }

            alpha = alpha * graph.getWeight(CurrentEdge);
            beta = beta * graph.getWeight(CurrentEdge);
            //A cos x + B sin x = C cos (x-D)
            double gamma = Math.sqrt(alpha * alpha + beta * beta);
            double delta = Math.atan(beta / alpha);

            if (alpha * Math.cos(Math.atan(beta / alpha)) + beta * Math.sin(Math.atan(beta / alpha)) < 0) {
                delta = delta + Math.PI;
            }
            double shiftedD = minAngle + GeometryUtils.moduloTwoPI(delta - minAngle);
            if (shiftedD > maxAngle) {
                //We affect "almost" maxAngle to the split
                if (gamma * Math.cos(minAngle - delta) > gamma * Math.cos(maxAngle - delta)) {
                    shiftedD = Math.min(minAngle + 0.00001, (minAngle + maxAngle) / 2);
                } else {
                    //We affect "almost" maxAngle to the split
                    shiftedD = Math.max(maxAngle - 0.00001, (minAngle + maxAngle) / 2);
                }
            }
            resultAngle = shiftedD;
            area = gamma * Math.cos(resultAngle - delta);
        }

        if (!(resultAngle == resultAngle)) {
            //The angle found is not a Number : we don't move the split. This is the case where all edges of the split
            // are in the same place so we could choose a more clever angle? Let's leave the old angle right now.
            resultAngle = splitAngle;
            area = 0;
        }

        return new Pair<>(area, resultAngle);
    }


    /**
     * computes the min and the max angles the parallel edges of the split can have,
     * considering only the configuration of the split itself.
     * <p/>
     * needs to be corrected to deal with boxes with DiffAngle=0
     *
     * @param SplitEdges sorted list of the split edges
     * @param graph
     */
    public Pair<Double, Double> trigClockAngles(List<Edge> SplitEdges, PhyloGraph graph, NodeArray<Point2D> node2point) {
        Iterator edgeIt = SplitEdges.iterator();
        Edge currentEdge = (Edge) edgeIt.next();
        double splitAngle = GeometryUtils.moduloTwoPI(graph.getAngle(currentEdge));
        if (splitAngle >= Math.PI) {
            splitAngle -= Math.PI;
        }

        double currentTrig = splitAngle + Math.PI;
        double currentClock = currentTrig;
        Node firstNodeA = currentEdge.getSource();
        boolean onlyOneEdge = false;
        while (edgeIt.hasNext()) {
            Edge ThePreviousEdge = currentEdge;
            currentEdge = (Edge) edgeIt.next();
            Node NodeB = ThePreviousEdge.getSource();
            Node NodeA = currentEdge.getSource();
            if (!(NodeA.isAdjacent(NodeB))) {
                NodeA = currentEdge.getTarget();
            }

            double angleAB = graph.getAngle(NodeA.getCommonEdge(NodeB));
            double diffAngle = GeometryUtils.moduloTwoPI(angleAB - splitAngle);
            if (diffAngle < Math.PI) {
                //AngleAB is candidate for Trig, AngleAB-180 for Clock
                if (!(GeometryUtils.moduloTwoPI(angleAB - currentTrig) < Math.PI)) {
                    currentTrig = angleAB;
                }
                if (!(GeometryUtils.moduloTwoPI(currentClock - angleAB + Math.PI) < Math.PI)) {
                    currentClock = angleAB - Math.PI;
                }
            } else {
                //AngleAB-180 is candidate for Trig, AngleAB for Clock
                if (!(GeometryUtils.moduloTwoPI(angleAB - Math.PI - currentTrig) < Math.PI)) {
                    currentTrig = angleAB - Math.PI;
                }
                if (!(GeometryUtils.moduloTwoPI(currentClock - angleAB) < Math.PI)) {
                    currentClock = angleAB;
                }
            }
            onlyOneEdge = GeometryUtils.squaredDistance(node2point.get(NodeA), node2point.get(firstNodeA)) < 0.0000000000001;
        }
        if (onlyOneEdge) {
            return new Pair<>(2 * Math.PI, -2 * Math.PI);
        } else {
            return new Pair<>(GeometryUtils.moduloTwoPI(currentTrig - splitAngle), -GeometryUtils.moduloTwoPI(splitAngle - currentClock));
        }
    }

    /**
     * returns a HashMap which gives for each split the list of its edges.
     *
     * @param graph
     */
    public HashMap<Integer, List<Edge>> getEdgeSplits(PhyloGraph graph) {
        final HashMap<Integer, List<Edge>> edgeSplits = new HashMap<>();
        Edge CurrentEdge = graph.getFirstEdge();
        List<Edge> currentEdges;

        int currentsplit;
        int i = 0;
        while (i < graph.getNumberOfEdges()) {

            if (i > 0) {
                CurrentEdge = CurrentEdge.getNext();
            }
            currentsplit = graph.getSplit(CurrentEdge);

            if (edgeSplits.containsKey(currentsplit)) {
                currentEdges = edgeSplits.get(currentsplit);
            } else {
                currentEdges = new ArrayList<>();
            }
            currentEdges.add(CurrentEdge);
            edgeSplits.put(graph.getSplit(CurrentEdge), currentEdges);
            i++;
        }

        //Now we have to sort the Edges
        Set<Integer> splitsSet = edgeSplits.keySet();

        for (Integer aSplit : splitsSet) {
            EdgeArray<Edge> a1Edge = new EdgeArray<>(graph);
            EdgeArray<Edge> a2Edge = new EdgeArray<>(graph);
            EdgeArray<Integer> adjNb = new EdgeArray<>(graph);

            currentsplit = aSplit;
            currentEdges = edgeSplits.get(currentsplit);
            Iterator CurrentEdgesIt = currentEdges.iterator();

            //If there is more than one edge in the split, we sort them
            if (currentEdges.size() > 1) {
                //We find, for each edge of the split, its one or two parallel neighbour edge(s) in the split.
                //The 2 edges with 1 parallel neighbour edge is an "extreme" edge of the split.
                while (CurrentEdgesIt.hasNext()) {
                    CurrentEdge = (Edge) CurrentEdgesIt.next();

                    Iterator AdjNodes = (CurrentEdge.getSource()).adjacentNodes().iterator();

                    while ((a2Edge.get(CurrentEdge) == null) && (AdjNodes.hasNext())) {
                        Node AdjNode = (Node) AdjNodes.next();
                        if (AdjNode != CurrentEdge.getTarget()) {

                            for (Edge ParallEdge : AdjNode.adjacentEdges()) {
                                if (graph.getSplit(ParallEdge) == graph.getSplit(CurrentEdge)) {
                                    if (a1Edge.get(CurrentEdge) == null) {
                                        a1Edge.put(CurrentEdge, ParallEdge);
                                        adjNb.put(CurrentEdge, 1);
                                        //System.out.println("First parallel neighbour of "+CurrentEdge+" : "+ParallEdge);
                                    } else {
                                        if (a2Edge.get(CurrentEdge) == null) {
                                            a2Edge.put(CurrentEdge, ParallEdge);
                                            adjNb.put(CurrentEdge, 2);
                                            //System.out.println("Second parallel neighbour of "+CurrentEdge+" : "+ParallEdge);
                                        } else {
                                            //The split is not planar and this algorithm will crash !!!
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //We first detect one of the extreme edges
                //Not necessary CurrentEdges=(List) EdgeSplits.get(CurrentSplit);
                CurrentEdgesIt = currentEdges.iterator();
                CurrentEdge = (Edge) CurrentEdgesIt.next();
                while ((CurrentEdgesIt.hasNext()) && (adjNb.get(CurrentEdge) != 1)) {
                    CurrentEdge = (Edge) CurrentEdgesIt.next();
                    //System.out.println("Current edge "+CurrentEdge+" : "+((Integer) AdjNb.get(CurrentEdge)).intValue());
                }

                //We check if everything is all right:
                if ((adjNb.get(CurrentEdge)) != 1) {
                    System.out.println("(the graph is not planar! Big Problem here!!! " + adjNb.get(CurrentEdge));
                }

                //Then we go through all the edges of the split to identify the 4 extreme nodes,
                //so our stop condition is to reach the other extreme edge
                Edge TheNextEdge = a1Edge.get(CurrentEdge);
                currentEdges = new ArrayList<>();
                while ((adjNb.get(TheNextEdge)) > 1) {
                    currentEdges.add(CurrentEdge);
                    if (a1Edge.get(TheNextEdge) == CurrentEdge) {
                        CurrentEdge = TheNextEdge;
                        TheNextEdge = a2Edge.get(TheNextEdge);
                    } else {
                        CurrentEdge = TheNextEdge;
                        TheNextEdge = a1Edge.get(TheNextEdge);
                    }
                }

                //TheNextEdge is the other extreme edge
                currentEdges.add(CurrentEdge);
                currentEdges.add(TheNextEdge);
                edgeSplits.put(currentsplit, currentEdges);
            } else {
                //There is only one edge in the split: we don't move it
            }
        }
        return edgeSplits;
    }

    /**
     * Knowing the two critical angles after the local optimization, detect the collisions and
     * return the two critical angles after the global optimization.
     * <p/>
     * Works only with splits which have more than 1 edge
     * <p/>
     * The exclusion zone has not been implemented yet (as in practice there is no node inside it)
     *
     * @param clockAngle clockAngle found so far
     * @param trigAngle  trigAngle found so far
     * @param oldAngle
     * @param SplitEdges sorted list of the split edges
     * @param Angle      we want to affect to the split
     */
    public Pair<Double, Double> createsCollisions(Node start, PhyloGraph graph, double trigAngle, double clockAngle, double oldAngle, List SplitEdges, double Angle, NodeArray<Point2D> node2point) {
        double newTrigAngle = trigAngle;
        double newClockAngle = clockAngle;
        boolean clockBlocked = false;
        boolean trigBlocked = false; //to save a little time down there
        //The critical angles we find will be stored in rresults1 and results2
        //We first determine the 4 extreme nodes of the split
        NodeSet visited1 = new NodeSet(graph);
        NodeSet visited2 = new NodeSet(graph);
        double[] rresults1 = new double[2];
        double[] results2 = new double[2];
        rresults1[0] = 2 * Math.PI;
        rresults1[1] = 2 * Math.PI;
        results2[0] = 0;
        results2[1] = 0;
        Iterator ItSplitEdges = SplitEdges.iterator();
        Edge currentEdge = (Edge) ItSplitEdges.next();
        Node zeNode1 = currentEdge.getTarget();
        Node zeNode2 = currentEdge.getSource();
        visited1.add(zeNode2);
        visited2.add(zeNode1);
        Node previousNode1 = zeNode1;
        Node previousNode2 = zeNode2;
        while (ItSplitEdges.hasNext()) {
            Edge TcurrentEdge = (Edge) ItSplitEdges.next();
            Node currentNode1 = TcurrentEdge.getSource();
            Node currentNode2 = TcurrentEdge.getTarget();

            if (currentNode2.isAdjacent(previousNode1)) {
                currentNode1 = currentNode2;
                currentNode2 = TcurrentEdge.getSource();
            }

            visited1.add(currentNode2);
            visited2.add(currentNode1);
            previousNode1 = currentNode1;
            previousNode2 = currentNode2;
        }
        assignCoordinatesToNodes(start, getOptionUseWeights(), graph, node2point); // we need this to detect collisions
        double firstBoxAngle = GeometryUtils.deg2rad(GeometryUtils.basicComputeAngle(node2point.get(zeNode1), node2point.get(zeNode2), node2point.get((previousNode1))));

        //We go through the 2 parts of the graph to find "defender" and "striker" nodes
        if (firstBoxAngle < Math.PI) {
            visitComponentRec2(zeNode1, currentEdge, 0, 2 * Math.PI, 2 * Math.PI, zeNode1, zeNode2, previousNode1, previousNode2, graph, node2point, visited1, rresults1, false);
            visitComponentRec1(zeNode2, currentEdge, 0, 0, zeNode1, zeNode2, previousNode1, previousNode2, graph, node2point, visited2, results2);
        } else {
            visitComponentRec2(previousNode1, currentEdge, 0, 2 * Math.PI, 2 * Math.PI, previousNode1, previousNode2, zeNode1, zeNode2, graph, node2point, visited1, rresults1, false);
            visitComponentRec1(zeNode2, currentEdge, 0, 0, previousNode1, previousNode2, zeNode1, zeNode2, graph, node2point, visited2, results2);
        }

        //We use the information of defender and striker nodes to eventually change the critical angles.
        if (results2[0] > Math.PI + rresults1[0]) {
            newClockAngle = graph.getAngle(currentEdge);
            clockBlocked = true;
        } else {
            if ((oldAngle - clockAngle) > (Math.PI + rresults1[0] - results2[0])) {
                newClockAngle = oldAngle - (rresults1[0] + Math.PI - results2[0]);
            }
        }

        if (results2[1] > Math.PI + rresults1[1]) {
            newTrigAngle = graph.getAngle(currentEdge);
            trigBlocked = true;
        } else {
            if ((trigAngle - oldAngle) > (rresults1[1] + Math.PI - results2[1])) {
                newTrigAngle = oldAngle + Math.PI + rresults1[1] - results2[1];
            }
        }

        //We do the same, looking from the other part of the graph
        visited1 = new NodeSet(graph);
        visited2 = new NodeSet(graph);
        double rresults2[] = new double[2];
        rresults2[0] = 2 * Math.PI;
        rresults2[1] = 2 * Math.PI;
        results2[0] = 0;
        results2[1] = 0;
        ItSplitEdges = SplitEdges.iterator();
        currentEdge = (Edge) ItSplitEdges.next();
        zeNode1 = currentEdge.getTarget();
        zeNode2 = currentEdge.getSource();
        visited1.add(zeNode2);
        visited2.add(zeNode1);
        previousNode1 = zeNode1;
        previousNode2 = zeNode2;
        while (ItSplitEdges.hasNext()) {
            Edge TcurrentEdge = (Edge) ItSplitEdges.next();
            Node currentNode1 = TcurrentEdge.getSource();
            Node currentNode2 = TcurrentEdge.getTarget();

            if (currentNode2.isAdjacent(previousNode1)) {
                currentNode1 = currentNode2;
                currentNode2 = TcurrentEdge.getSource();
            }
            visited1.add(currentNode2);
            visited2.add(currentNode1);
            previousNode1 = currentNode1;
            previousNode2 = currentNode2;
        }

        if (!(clockBlocked && trigBlocked)) {
            if (firstBoxAngle < Math.PI) {
                visitComponentRec2(previousNode2, currentEdge, 0, (2 * Math.PI), (2 * Math.PI), previousNode2, previousNode1, zeNode2, zeNode1, graph, node2point, visited2, rresults2, false);
                visitComponentRec1(previousNode1, currentEdge, 0, 0, previousNode2, previousNode1, zeNode2, zeNode1, graph, node2point, visited1, results2);
            } else {
                visitComponentRec2(zeNode2, currentEdge, 0, 2 * Math.PI, 2 * Math.PI, zeNode2, zeNode1, previousNode2, previousNode1, graph, node2point, visited2, rresults2, false);
                visitComponentRec1(previousNode1, currentEdge, 0, 0, zeNode2, zeNode1, previousNode2, previousNode1, graph, node2point, visited1, results2);
            }

            if (results2[0] > Math.PI + rresults2[0]) {
                newClockAngle = graph.getAngle(currentEdge);

            } else {
                if ((oldAngle - newClockAngle) > (rresults2[0] + Math.PI - results2[0])) {
                    newClockAngle = oldAngle - (Math.PI + rresults2[0] - results2[0]);
                }
            }

            if (results2[1] > Math.PI + rresults2[1]) {
                newTrigAngle = graph.getAngle(currentEdge);
            } else {
                if ((newTrigAngle - oldAngle) > (Math.PI + rresults2[1] - results2[1])) {
                    newTrigAngle = oldAngle + Math.PI + rresults2[1] - results2[1];
                }
            }
        }

        return new Pair<>(newTrigAngle, newClockAngle);
    }

    /**
     * recursively visit the whole subgraph, obtaining the min and max observed angle
     *
     * @param v
     * @param e
     * @param specialNode     1 if v is a neighbour of angle1in, 2 if v neighbour of angle2in else 0
     * @param previousAngle1  previous angle, except when specialNode=1
     * @param previousAngle2  previous angle, except when specialNode=2
     * @param angle1in
     * @param angle1out
     * @param angle2in
     * @param angle2out
     * @param graph
     * @param visited
     * @param foundParameters angle1 angle2
     */
    private void visitComponentRec2(Node v, Edge e, int specialNode, double previousAngle1, double previousAngle2, Node angle1in, Node angle1out, Node angle2in, Node angle2out, PhyloGraph graph, NodeArray<Point2D> node2point, NodeSet visited, double[] foundParameters, boolean dontCompute) {// throws CanceledException {
        double newAngle1 = 2 * Math.PI;
        double newAngle2 = 2 * Math.PI;
        boolean localDontCompute;
        if (!visited.contains(v)) {
            visited.add(v);
            if (!dontCompute) {
                if (v != angle1in) {
                    if ((specialNode == 1) || (specialNode == 3)) {
                        newAngle1 = GeometryUtils.deg2rad(GeometryUtils.basicComputeAngle(node2point.get(angle1in), node2point.get(v), node2point.get(angle1out)));
                    } else {
                        newAngle1 = previousAngle1 + GeometryUtils.deg2rad(GeometryUtils.signedDiffAngle(GeometryUtils.basicComputeAngle(node2point.get(angle1in), node2point.get(v), node2point.get(angle1out)), previousAngle1));
                    }
                    if (newAngle1 < foundParameters[0]) {
                        foundParameters[0] = newAngle1;
                    }
                }

                if (v != angle2in) {
                    if ((specialNode == 2) || (specialNode == 3)) {
                        newAngle2 = GeometryUtils.deg2rad(GeometryUtils.basicComputeAngle(node2point.get(angle2in), node2point.get(angle2out), node2point.get(v)));
                    } else {
                        newAngle2 = previousAngle2 + GeometryUtils.deg2rad(GeometryUtils.signedDiffAngle(GeometryUtils.basicComputeAngle(node2point.get(angle2in), node2point.get(angle2out), node2point.get(v)), previousAngle2));
                    }
                    if (newAngle2 < foundParameters[1]) {
                        foundParameters[1] = newAngle2;
                    }
                }
            }

            for (Edge f : v.adjacentEdges()) {
                if (f != e) {
                    Node w = graph.getOpposite(v, f);
                    localDontCompute = GeometryUtils.squaredDistance(node2point.get(w), node2point.get(v)) <= 0.0000000000001;
                    if (v == angle1in) {
                        if (dontCompute) {
                            visitComponentRec2(w, f, specialNode, previousAngle1, previousAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                        } else {
                            //If the two extreme nodes are together, notice it:
                            if (GeometryUtils.squaredDistance(node2point.get(angle1in), node2point.get(angle2in)) > 0.0000000000001) {
                                visitComponentRec2(w, f, 1, newAngle1, newAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                            } else {
                                visitComponentRec2(w, f, 3, newAngle1, newAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                            }
                        }
                    } else {
                        if (v == angle2in) {
                            if (dontCompute) {
                                visitComponentRec2(w, f, specialNode, previousAngle1, previousAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                            } else {
                                //If the two extreme nodes are together, notice it
                                if (GeometryUtils.squaredDistance(node2point.get(angle1in), node2point.get(angle2in)) > 0.0000000000001) {
                                    visitComponentRec2(w, f, 2, newAngle1, newAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                                } else {
                                    visitComponentRec2(w, f, 3, newAngle1, newAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                                }
                            }
                        } else {
                            if (dontCompute) {
                                visitComponentRec2(w, f, specialNode, previousAngle1, previousAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                            } else {
                                visitComponentRec2(w, f, 0, newAngle1, newAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters, localDontCompute);
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * recursively visit the whole subgraph, obtaining the min and max observed angle
     *
     * @param v
     * @param e
     * @param angle1in
     * @param angle1out
     * @param angle2in
     * @param angle2out
     * @param graph
     * @param visited
     * @param foundParameters xmin ymin xmax ymax angle1 angle2
     */
    private void visitComponentRec1(Node v, Edge e, double previousAngle1, double previousAngle2, Node angle1in, Node angle1out, Node angle2in, Node angle2out, PhyloGraph graph, NodeArray<Point2D> node2point, NodeSet visited, double[] foundParameters) {
        if (!visited.contains(v)) {
            visited.add(v);

            double newAngle1 = previousAngle1 + GeometryUtils.deg2rad(GeometryUtils.signedDiffAngle(GeometryUtils.basicComputeAngle(node2point.get(angle1in), node2point.get(v), node2point.get(angle1out)), previousAngle1 + Math.PI));
            if (newAngle1 > foundParameters[0]) {
                foundParameters[0] = newAngle1;
            }

            double newAngle2 = previousAngle2 + GeometryUtils.deg2rad(GeometryUtils.signedDiffAngle(GeometryUtils.basicComputeAngle(node2point.get(angle2in), node2point.get(angle2out), node2point.get(v)), previousAngle2 + Math.PI));
            if (newAngle2 > foundParameters[1]) {
                foundParameters[1] = newAngle2;
            }

            for (Edge f : v.adjacentEdges()) {
                if (f != e) {
                    Node w = graph.getOpposite(v, f);
                    visitComponentRec1(w, f, newAngle1, newAngle2, angle1in, angle1out, angle2in, angle2out, graph, node2point, visited, foundParameters);
                }
            }
        }
    }

    /**
     * assigns coordinates to nodes
     *
     * @param useWeights
     * @param graph
     */
    private void assignCoordinatesToNodes(Node start, boolean useWeights, PhyloGraph graph, NodeArray<Point2D> node2point) {
        node2point.set(start, new Point2D(0, 0));
        final BitSet splitsInPath = new BitSet();
        final NodeSet nodesVisited = new NodeSet(graph);

        assignCoordinatesToNodesRec(start, splitsInPath, nodesVisited, useWeights, graph, node2point);
    }


    /**
     * recursively assigns coordinates to all nodes
     *
     * @param v
     * @param splitsInPath
     * @param nodesVisited
     * @param useWeights
     */
    private void assignCoordinatesToNodesRec(Node v, BitSet splitsInPath, NodeSet nodesVisited, boolean useWeights, PhyloGraph graph, NodeArray<Point2D> node2point) {
        if (!nodesVisited.contains(v)) {
            //Deleted so that the user can cancel and it doesn't destroy everything: doc.getProgressListener().checkForCancel();
            nodesVisited.add(v);
            for (Edge e : v.adjacentEdges()) {
                int s = graph.getSplit(e);
                if (!splitsInPath.get(s)) {
                    Node w = graph.getOpposite(v, e);
                    Point2D p = GeometryUtils.translateByAngle(node2point.get(v), GeometryUtils.rad2deg(graph.getAngle(e)), useWeights ? graph.getWeight(e) : 1);
                    node2point.set(w, p);
                    splitsInPath.set(s, true);
                    assignCoordinatesToNodesRec(w, splitsInPath, nodesVisited, useWeights, graph, node2point);
                    splitsInPath.set(s, false);
                }
            }
        }
    }

    public int getOptionIterations() {
        return optionIterations.get();
    }

    public IntegerProperty optionIterationsProperty() {
        return optionIterations;
    }

    public void setOptionIterations(int optionIterations) {
        this.optionIterations.set(optionIterations);
    }

    public boolean getOptionUseWeights() {
        return optionUseWeights.get();
    }

    public BooleanProperty optionUseWeightsProperty() {
        return optionUseWeights;
    }

    public void setOptionUseWeights(boolean optionUseWeights) {
        this.optionUseWeights.set(optionUseWeights);
    }
}
