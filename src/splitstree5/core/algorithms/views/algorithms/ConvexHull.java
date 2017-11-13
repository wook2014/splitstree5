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

import javafx.geometry.Point2D;
import jloda.graph.Edge;
import jloda.graph.EdgeIntegerArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.view.GeometryUtils;

import java.util.*;

/**
 * applies the convex hull algorithm to build a split network from splits
 * Daniel Huson, 11.2017
 */
public class ConvexHull {
    /**
     * apply the algorithm to build a new graph
     *
     * @param progress
     * @param useWeights
     * @param taxa
     * @param splits
     * @param phyloGraph
     * @param node2point
     */
    public static void apply(ProgressListener progress, boolean useWeights, TaxaBlock taxa, SplitsBlock splits, PhyloGraph phyloGraph, NodeArray<Point2D> node2point) throws CanceledException {
        phyloGraph.clear();
        apply(progress, useWeights, taxa, splits, phyloGraph, node2point, new BitSet());
    }

    /**
     * assume that some splits have already been processed and applies convex hull algorithm to remaining splits
     *
     * @param progress
     * @param useWeights
     * @param taxa
     * @param splits
     * @param graph
     * @param node2point
     * @param usedSplits
     */
    public static void apply(ProgressListener progress, boolean useWeights, TaxaBlock taxa, SplitsBlock splits, PhyloGraph graph, NodeArray<Point2D> node2point, BitSet usedSplits) throws CanceledException {

        if (usedSplits.cardinality() == splits.getNsplits())
            return; // all nodes have been processed
        System.err.println("Running convex hull algorithm");

        progress.setTasks("Convex Hull", null);
        progress.setMaximum(splits.getNsplits());    //initialize maximum progress
        progress.setProgress(-1);        //set progress to 0

        if (graph.getNumberOfNodes() == 0) {
            Node startNode = graph.newNode();
            graph.setTaxon2Node(1, startNode);
            //graph.setLabel(startNode, taxa.getLabel(1));
            graph.setNode2Taxa(startNode, 1);

            for (int i = 2; i <= taxa.getNtax(); i++) {
                graph.setTaxon2Node(i, startNode);
                //graph.setLabel(startNode, (graph.getLabel(startNode)+", "+taxa.getLabel(i)));
                graph.setNode2Taxa(startNode, i);
            }
        }

        final int[] order = getOrderToProcessSplitsIn(splits, usedSplits);

        //process one split at a time
        progress.setMaximum(order.length);    //initialize maximum progress
        try {
            for (int z = 0; z < order.length; z++) {

                progress.setProgress(z);

                BitSet currentSplitPartA = splits.getA(order[z] - 1);

                //is 0, if the node is member of convex hull for the "0"-side of the current split,
                //is 1, if the node is member of convex hull for the "1"-side of the current split,
                //is 2, if the node is member of both hulls
                NodeArray<Integer> hulls = new NodeArray<>(graph);

                //here all found "critical" nodes are stored
                final ArrayList<Node> intersectionNodes = new ArrayList<>();

                final BitSet splits1 = new BitSet();
                final BitSet splits0 = new BitSet();

                //find splits, where taxa of side "0" of current split are divided
                for (int i = 1; i <= splits.getNsplits(); i++) {
                    if (!usedSplits.get(i)) continue;    //only splits already used must be regarded

                    if (splits.intersect2(order[z] - 1, false, i - 1, true).cardinality() != 0 &&
                            splits.intersect2(order[z] - 1, false, i - 1, false).cardinality() != 0)
                        splits0.set(i);
                    progress.checkForCancel();
                }

                //find splits, where taxa of side "1" of current split are divided
                for (int i = 1; i <= splits.getNsplits(); i++) {
                    progress.checkForCancel();

                    if (!usedSplits.get(i)) continue;    //only splits already used must be regarded

                    if (splits.intersect2(order[z] - 1, true, i - 1, true).cardinality() != 0 &&
                            splits.intersect2(order[z] - 1, true, i - 1, false).cardinality() != 0)
                        splits1.set(i);
                }

                //find startNodes

                Node start0 = null;
                Node start1 = null;

                for (int i = 1; i <= taxa.getNtax(); i++) {
                    if (!currentSplitPartA.get(i)) {
                        start0 = graph.getTaxon2Node(i);
                    } else {
                        start1 = graph.getTaxon2Node(i);
                    }
                    if (start0 != null && start1 != null) break;
                }

                hulls.set(start0, 0);

                if (start0 == start1) {
                    hulls.set(start1, 2);
                    intersectionNodes.add(start1);
                } else
                    hulls.set(start1, 1);

                //construct the remainder of convex hull for split-side "0" by traversing all allowed (and reachable) edges (i.e. all edges in splits0)

                EdgeIntegerArray visited = new EdgeIntegerArray(graph, 0);

                convexHullPath(graph, start0, visited, hulls, splits0, intersectionNodes, 0);

                //construct the remainder of convex hull for split-side "1" by traversing all allowed (and reachable) edges (i.e. all edges in splits0)

                visited = new EdgeIntegerArray(graph, 0);

                convexHullPath(graph, start1, visited, hulls, splits1, intersectionNodes, 1);

                //first duplicate the intersection nodes, set an edge between each node and its duplicate and label new edges and nodes
                for (Object intersectionNode1 : intersectionNodes) {

                    Node v = (Node) intersectionNode1;
                    Node v1 = graph.newNode();

                    Edge e = graph.newEdge(v1, v);
                    graph.setSplit(e, order[z]);
                    graph.setWeight(e, splits.getWeight(order[z] - 1));
                    graph.setLabel(e, "" + order[z]);

                    final List<Integer> aTaxa = graph.getNode2Taxa(v);

                    graph.clearNode2Taxa(v);

                    for (Integer taxon : aTaxa) {
                        if (currentSplitPartA.get(taxon)) {
                            graph.setTaxon2Node(taxon, v1);
                            graph.setNode2Taxa(v1, taxon);
                        } else {
                            graph.setNode2Taxa(v, taxon);
                        }
                    }

                    //graph.setLabel(v, vlab);
                    //graph.setLabel(v1, v1lab);
                }

                //connect edges accordingly
                for (Node v : intersectionNodes) {
                    progress.checkForCancel();
                    //find duplicated node of v (and their edge)
                    Node v1 = null;
                    Edge toV1 = null;

                    for (toV1 = v.getFirstAdjacentEdge(); toV1 != null; toV1 = v.getNextAdjacentEdge(toV1)) {
                        if (graph.getSplit(toV1) == order[z]) {
                            v1 = graph.getOpposite(v, toV1);
                            break;
                        }
                    }

                    //visit all edges of v and move or add edges
                    for (Edge consider : v.adjacentEdges()) {
                        progress.checkForCancel();

                        if (consider == toV1) continue;

                        Node w = graph.getOpposite(v, consider);

                        if (hulls.getValue(w) == -1) {
                        } else if (hulls.getValue(w) == 1) {        //node belongs to other side
                            Edge considerDup = graph.newEdge(v1, w);
                            graph.setLabel(considerDup, "" + graph.getSplit(consider));
                            graph.setSplit(considerDup, graph.getSplit(consider));
                            graph.setWeight(considerDup, graph.getWeight(consider));
                            graph.setAngle(considerDup, graph.getAngle(consider));
                            graph.deleteEdge(consider);
                        } else if (hulls.getValue(w) == 2) {  //node is in intersection
                            Node w1 = null;

                            for (Edge toW1 : w.adjacentEdges()) {
                                progress.checkForCancel();
                                if (graph.getSplit(toW1) == order[z]) {
                                    w1 = graph.getOpposite(w, toW1);
                                    break;
                                }
                            }

                            if (v1 != null && v1.getCommonEdge(w1) == null) {
                                final Edge considerDup = graph.newEdge(v1, w1);
                                graph.setLabel(considerDup, "" + graph.getSplit(consider));

                                graph.setWeight(considerDup, graph.getWeight(consider));
                                graph.setSplit(considerDup, graph.getSplit(consider));
                            }
                        }
                    }
                }
                //add split to usedSplits
                usedSplits.set(order[z], true);
            }
        } catch (CanceledException e) {
            progress.setUserCancelled(false);
        }

        progress.setProgress(-1);
        for (Node n : graph.nodes()) {

            graph.setLabel(n, null);
            List list = graph.getNode2Taxa(n);
            if (list.size() != 0) {
                String label = taxa.getLabel((Integer) list.get(0));
                for (int i = 1; i < list.size(); i++) {
                    int taxon = (Integer) list.get(i);
                    label += (", " + taxa.getLabel(taxon));
                }
                graph.setLabel(n, label);
            }
        }

        int[] cyclicOrdering = splits.getCycle();

        for (int i = 1; i < cyclicOrdering.length; i++) {
            graph.setTaxon2Cycle(cyclicOrdering[i], i);
        }

        final NodeArray coords = embed(graph, cyclicOrdering, useWeights, true);

        int maxNumberOfTaxaOnNode = 0;
        for (Node v = graph.getFirstNode(); v != null; v = graph.getNextNode(v)) {
            node2point.put(v, (Point2D) coords.get(v));
            if (graph.getNode2Taxa(v) != null && graph.getNode2Taxa(v).size() > maxNumberOfTaxaOnNode)
                maxNumberOfTaxaOnNode = graph.getNode2Taxa(v).size();
        }

        BitSet seen = new BitSet();
        for (Edge e = graph.getFirstEdge(); e != null; e = e.getNext()) {
            int s = graph.getSplit(e);
            if (s > 0 && !seen.get(s)) {
                seen.set(s);
                graph.setLabel(e, "" + s);
            } else
                graph.setLabel(e, null);
        }
    }//end apply

    /**
     * convex hull path
     *
     * @param g
     * @param start
     * @param visited
     * @param hulls
     * @param allowedSplits
     * @param intersectionNodes
     * @param side
     */
    private static void convexHullPath(PhyloGraph g, Node start, EdgeIntegerArray visited, NodeArray<Integer> hulls, BitSet allowedSplits, ArrayList<Node> intersectionNodes, int side) {
        final Stack<Node> todo = new Stack<>();
        todo.push(start);

        while (todo.size() > 0) {
            final Node v = todo.pop();

            for (Edge f : v.adjacentEdges()) {
                final Node w = g.getOpposite(v, f);

                if (visited.getValue(f) == 0 && allowedSplits.get(g.getSplit(f))) {
                    //if(hulls.getValue(m)==side) continue;
                    visited.set(f, 1);

                    if (hulls.get(w) == null) {
                        hulls.set(w, side);
                        todo.push(w);
                    } else if (hulls.getValue(w) == Math.abs(side - 1)) {
                        hulls.set(w, 2);
                        intersectionNodes.add(w);
                        todo.push(w);
                    }
                } else
                    visited.set(f, 1);
            }
        }
    }

    /**
     * computes a good order in which to process the splits.
     * Currently orders splits by increasing size
     *
     * @param splits
     * @param usedSplits
     * @return order
     */
    private static int[] getOrderToProcessSplitsIn(SplitsBlock splits, BitSet usedSplits) {
        final SortedSet<Integer> set = new TreeSet<>();
        for (int s = 1; s <= splits.getNsplits(); s++) {
            if (!usedSplits.get(s)) {
                Integer pair = 10000 * splits.get(s - 1).size() + s;
                set.add(pair);
            }
        }

        final int[] order = new int[set.size()];
        int i = 0;
        for (Integer value : set) {
            int size = value / 10000;
            int id = value - size * 10000;
            // System.err.println("pair "+id+" size "+size);
            order[i++] = id;
        }
        return order;
    }

    /**
     * Embeds the graph using the given cyclic ordering.
     *
     * @param ordering   the cyclic ordering.
     * @param useWeights scale edges by their weights?
     * @param noise      alter split-angles randomly by a small amount to prevent occlusion of edges.
     * @return node array of coordinates
     */
    public static NodeArray embed(PhyloGraph graph, int[] ordering, boolean useWeights, boolean noise) {
        int ntax = ordering.length - 1;

        Node[] ordering_n = new Node[ntax];

        for (int i = 1; i <= ntax; i++) {
            ordering_n[graph.getTaxon2Cycle(i) - 1] = graph.getTaxon2Node(i);
        }

        // get splits
        HashMap<Integer, ArrayList<Node>> splits = getSplits(graph, ordering_n);
        for (Integer key : splits.keySet()) sortSplit(ordering_n, splits.get(key));

        /* get unit-vectors in split-direction */
        HashMap<Integer, Double> dirs = getDirectionVectors(graph, splits, ordering_n, noise);

        /* compute coords */
        return computeCoords(graph, dirs, ordering_n, useWeights);
    }

    /**
     * get splits:
     * depth search / cross each split just once
     * add taxa to currently crossed splits.
     *
     * @param ordering the cyclic ordering
     */
    private static HashMap<Integer, ArrayList<Node>> getSplits(PhyloGraph graph, Node[] ordering) {

        /* the splits */
        HashMap<Integer, ArrayList<Node>> splits = new HashMap<>();

        /* stack for nodes which still have to be visited */
        Stack<Node> toVisit = new Stack<>();
        /* Boolean-stack to determine whether current Node is backtracking-node */
        Stack<Boolean> backtrack = new Stack<>();
        /* Edge-stack to determine enter-edge */
        Stack<Edge> edges = new Stack<>();
        /* collect already seen nodes */
        ArrayList<Node> seen = new ArrayList<>();
        /* collect currently crossed split-ids */
        ArrayList<Integer> crossedSplits = new ArrayList<>();

        // init..
        if (ordering[0] == null)
            System.err.println("null");
        toVisit.push(ordering[0]);
        backtrack.push(false);
        Edge enter = null;

        // start traversal
        while (!toVisit.empty()) {
            // current Node
            Node u = toVisit.pop();
            // enter-edge
            if (!edges.isEmpty()) enter = edges.pop();
            // are we backtracking?
            boolean backtracking = backtrack.pop();

            /* first visit (not backtracking) */
            if (!backtracking) {   //  && !seen.contains(u)
                if (enter != null) {
                    // current split-id
                    Integer cId = graph.getSplit(enter);
                    crossedSplits.add(cId);
                    if (!splits.containsKey(cId))
                        splits.put(cId, new ArrayList<Node>());
                }
                seen.add(u);

                /* if the current Node is a taxa-node, add it to currently crossed splits */
                if (graph.getNode2Taxa(u).size() != 0) {
                    for (Integer crossedSplit : crossedSplits) {
                        ArrayList<Node> s = splits.get(crossedSplit);
                        s.add(u);
                    }
                }

                /*
                 * push adjacent nodes (if not already seen)
                 * and current node (backtrack)
                 */
                for (Edge e : u.adjacentEdges()) {
                    Integer sId = graph.getSplit(e);
                    Node v = graph.getOpposite(u, e);
                    if (!seen.contains(v) && !crossedSplits.contains(sId)) {
                        if (u == null)
                            System.err.println("null");
                        toVisit.push(u);
                        backtrack.push(true);
                        if (v == null)
                            System.err.println("null");
                        toVisit.push(v);
                        backtrack.push(false);
                        // push edge twice (visit & backtrack)
                        edges.push(e);
                        edges.push(e);
                    }
                }

                /* backtrack */
            } else {
                // backtracking -> remove crossed split
                if (enter != null) {
                    Integer cId = graph.getSplit(enter);
                    crossedSplits.remove(cId);
                }

            }
        } // end while
        return splits;
    }

    /**
     * sort a split according to the cyclic ordering.
     *
     * @param ordering the cyclic ordering
     * @param split    the split which has to be sorted
     */
    private static void sortSplit(Node[] ordering, ArrayList<Node> split) {

        // convert Node[] to List in order to use List.indexOf(..)
        List<Node> orderingList = Arrays.asList(ordering);
        ArrayList<Node> t1 = new ArrayList<>(split.size());
        ArrayList<Node> t2 = new ArrayList<>(split.size());
        for (int i = 0; i < split.size(); i++) {
            int index = orderingList.indexOf(split.get(i)) - 1;
            if (index == -1) index = ordering.length - 1;
            // split doesn't contain previous taxa in the cyclic ordering
            // => the following (split.cardinality) taxa in the cyclic ordering
            //      give the sorted split.
            if (!(split.contains(ordering[index]))) {
                int j = 1;
                // get both sides of the split
                for (; j < split.size() + 1; j++) {
                    t1.add(ordering[(index + j) % ordering.length]);
                }
                for (int k = j; k < j + (ordering.length - split.size()); k++) {
                    t2.add(ordering[(index + k) % ordering.length]);
                }
                break;
            }
        }
        // chose the split that doesn't contain the first taxon in the
        // cyclic ordering, because coordinates are computed starting there.
        split.clear();
        if (t2.contains(ordering[0]))
            split.addAll(t1);
        else
            split.addAll(t2);
    }


    /**
     * determine the direction vectors for each split.
     * angle: ((leftSplitBoundary + rightSplitBoundary)/amountOfTaxa)*Pi
     *
     * @param splits   the sorted splits
     * @param ordering the cyclic ordering
     * @param noise    alter split-angles randomly by a small amount to prevent occlusion of edges
     * @return direction vectors for each split
     */
    private static HashMap<Integer, Double> getDirectionVectors(PhyloGraph graph, HashMap<Integer, ArrayList<Node>> splits, Node[] ordering, boolean noise) {
        final Random rand = new Random(666);    // add noise, if necessary
        final HashMap<Integer, Double> dirs = new HashMap<>(splits.size());
        final List<Node> orderingList = Arrays.asList(ordering);

        Edge currentEdge = graph.getFirstEdge();
        int currentSplit;

        for (int j = 0; j < graph.getNumberOfEdges(); j++) {
            //We do a loop on the edges to keep the angles of the splits which have already been computed
            double angle;
            currentSplit = graph.getSplit(currentEdge);
            Integer splitId = currentSplit;
            if (!dirs.containsKey(splitId)) {
                if (graph.getAngle(currentEdge) > 0.000000001) {
                    //This is an old edge, we attach its angle to its split
                    angle = graph.getAngle(currentEdge);
                    dirs.put(currentSplit, angle);
                } else {
                    //This is a new edge, so we give it an angle according to the equal angle algorithm
                    final ArrayList<Node> split = splits.get(splitId);
                    int xp = 0;
                    int xq = 0;

                    if (split.size() > 0) {
                        xp = orderingList.indexOf(split.get(0));
                        xq = orderingList.indexOf(split.get(split.size() - 1));
                    }

                    angle = 180 + ((((double) xp + (double) xq) / (double) ordering.length) * 180);

                    if (noise && split.size() > 1) {
                        angle += 3 * rand.nextFloat();
                    }
                    dirs.put(splitId, angle);
                }
            } else {
                angle = dirs.get(currentSplit);
            }

            graph.setAngle(currentEdge, angle);
            currentEdge = graph.getNextEdge(currentEdge);
        }
        return dirs;
    }

    /**
     * compute coords for each node.
     * depth first traversal / cross each split just once before backtracking
     *
     * @param dirs       the direction vectors for each split
     * @param ordering   the cyclic ordering
     * @param useWeights scale edges by edge weights?
     * @return node array of coordinates
     */
    public static NodeArray computeCoords(PhyloGraph graph, HashMap<Integer, Double> dirs, Node[] ordering, boolean useWeights) {
        NodeArray<Point2D> coords = new NodeArray<>(graph);

        /* stack for nodes which still have to be visited */
        Stack<Node> toVisit = new Stack<>();
        /* Boolean-stack to determine wether current Node is backtracking-node */
        Stack<Boolean> backtrack = new Stack<>();
        /* Edge-stack to determine enter-edge */
        Stack<Edge> edges = new Stack<>();
        /* collect already seen nodes */
        ArrayList<Node> seen = new ArrayList<>();
        /* collect already computed nodes to check equal locations */
        HashMap<Node, Point2D> locations = new HashMap<>();
        /* collect currently crossed split-ids */
        ArrayList<Integer> crossedSplits = new ArrayList<>();
        /* current node-location */
        Point2D currentPoint = new Point2D(0, 0);

        // init..
        toVisit.push(ordering[0]);
        backtrack.push(false);
        Edge enter = null;

        // start traversal
        while (!toVisit.empty()) {
            // current Node
            Node u = toVisit.pop();
            // enter-edge
            if (!edges.isEmpty()) enter = edges.pop();

            // are we backtracking?
            boolean backtracking = backtrack.pop();

            /* visit */
            if (!backtracking) {
                if (enter != null) {
                    // current split-id
                    Integer cId = graph.getSplit(enter);
                    double w = (useWeights ? graph.getWeight(enter) : 1.0);
                    crossedSplits.add(cId);

                    double angle = dirs.get(cId);
                    currentPoint = GeometryUtils.translateByAngle(currentPoint, angle, -w);
                }

                // set location, check equal locations
                Point2D loc = new Point2D(currentPoint.getX(), currentPoint.getY());
                // equal locations: append labels
                if (locations.containsValue(loc)) {
                    Node twinNode;
                    String tLabel = graph.getLabel(u);

                    for (Node v : locations.keySet()) {
                        if (locations.get(v).equals(loc)) {
                            twinNode = v;
                            if (graph.getLabel(twinNode) != null)
                                tLabel = (tLabel != null) ? tLabel + ", " + graph.getLabel(twinNode) : graph.getLabel(twinNode);
                            graph.setLabel(twinNode, null);
                            graph.setLabel(u, tLabel);
                        }
                    }
                }
                coords.set(u, loc);
                locations.put(u, loc);

                seen.add(u);

                /*
                 * push adjacent nodes (if not already seen)
                 * and current node (backtrack)
                 */
                for (Edge e : u.adjacentEdges()) {
                    Integer sId = graph.getSplit(e);
                    Node v = graph.getOpposite(u, e);
                    if (!seen.contains(v) && !crossedSplits.contains(sId)) {
                        toVisit.push(u);
                        backtrack.push(true);
                        toVisit.push(v);
                        backtrack.push(false);
                        // push edge twice (visit & backtrack)
                        edges.push(e);
                        edges.push(e);
                    }
                }

                /* backtrack */
            } else {
                if (enter != null) {
                    Integer cId = graph.getSplit(enter);
                    crossedSplits.remove(cId);
                    double w = (useWeights ? graph.getWeight(enter) : 1.0);
                    double angle = dirs.get(cId);
                    currentPoint = GeometryUtils.translateByAngle(currentPoint, angle, w);
                }
            }
        } // end while
        return coords;
    }
}
