/*
 * ForceCalculatorTask.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.gui.graph3dtab;

import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloSplitsGraph;
import splitstree5.gui.graphtab.base.NodeViewBase;

import java.util.*;

/**
 * Embeds a 3D splits graph
 * Original author: Christopher Juerges on 07/12/16.
 * Modified by Daniel Huson, 1.2018
 */
public class ForceCalculatorTask extends Task<NodeArray<Point3D>> {

    private final PhyloSplitsGraph graph;
    private final HashMap<Integer, List<Edge>> split2edges;

    private double maxDist;

    private final NodeArray<Point3D> nodeLocations;

    private double numOfSteps;
    private boolean linear;
    private boolean withZpush;

    /**
     * constructor
     *
     * @param graph
     * @param node2view
     * @param numOfSteps
     * @param linear
     * @param withZpush
     */
    public ForceCalculatorTask(PhyloSplitsGraph graph, NodeArray<NodeViewBase> node2view, int numOfSteps, boolean linear, boolean withZpush) {
        this.graph = graph;
        this.withZpush = withZpush;
        this.linear = linear;
        this.numOfSteps = numOfSteps;

        split2edges = new HashMap<>();
        for (Edge edge : graph.edges()) {
            int splitId = graph.getSplit(edge);
            if (!split2edges.containsKey(splitId)) {
                split2edges.put(splitId, new ArrayList<>());
            }
            split2edges.get(splitId).add(edge);
        }

        nodeLocations = new NodeArray<>(graph);
        for (Node v : graph.nodes()) {
            nodeLocations.put(v, ((NodeView3D) node2view.get(v)).getLocation());
        }
    }

    /**
     * Calculates a linear force-vector which acts from a given point to the other one
     *
     * @param from the point from which the force acts
     * @param to   the point on which the first point acts
     * @return the resulting force vector that acts on the 'to' point
     */
    private Point3D calculateLinearForceVector(Point3D from, Point3D to) {
        Point3D dist = to.subtract(from);
        Point3D force = new Point3D(dist.getX(), dist.getY(), dist.getZ());
        return force.multiply((maxDist - force.magnitude()) / force.magnitude());
    }

    /**
     * Calculates a logarithmic force-vector which acts from a given point to the other one
     *
     * @param from the point from which the force acts
     * @param to   the point on which the first point acts
     * @return the resulting force vector that acts on the 'to' point
     */
    private Point3D calculateLogForceVector(Point3D from, Point3D to) {
        Point3D dist = to.subtract(from);
        Point3D force = new Point3D(dist.getX(), dist.getY(), dist.getZ());
        double c = maxDist * 0.5;
        double forceStrength = maxDist - (c * Math.log(dist.magnitude() / maxDist) + maxDist);
        return force.multiply(forceStrength / force.magnitude());
    }

    /**
     * shift some random splits by a small amount
     *
     * @param count
     */
    private void shiftRandomSplits(int count) {
        final Integer[] keys = split2edges.keySet().toArray(new Integer[split2edges.size()]);
        final Random random = new Random(666);
        while (count-- > 0) {
            final Edge edge = split2edges.get(keys[random.nextInt(keys.length)]).get(0);
            final Point3D v1 = nodeLocations.get(edge.getSource());
            final Point3D v2 = nodeLocations.get(edge.getTarget());

            double edgeLength = v1.subtract(v2).magnitude();
            double zOffset = edgeLength * (0.02 * (0.5 - random.nextDouble()));

            Point3D v1New = new Point3D(v1.getX(), v1.getY(), v1.getZ() + zOffset);
            Point3D v1NewVec = v1New.subtract(v2).multiply(edgeLength / v1New.subtract(v2).magnitude());
            v1New = v2.add(v1NewVec);
            Point3D moveVec = v1New.subtract(v1);

            final NodeSet splitNodes = new NodeSet(graph);
            getSplitAssociatedNodes(graph.getSplit(edge), edge.getSource(), splitNodes);
            splitNodes.forEach(v -> {
                nodeLocations.put(v, new Point3D(nodeLocations.get(v).getX() + moveVec.getX(),
                        nodeLocations.get(v).getY() + moveVec.getY(),
                        nodeLocations.get(v).getZ() + moveVec.getZ()));
            });
        }
    }

    private void calculateStep(NodeArray<Point3D> nodeLocations, boolean linear) {
        final NodeSet leftNodes = new NodeSet(graph);
        final NodeSet leftSplit = new NodeSet(graph);
        final NodeSet rightSplit = new NodeSet(graph);

        // For each split
        for (List<Edge> edges : split2edges.values()) {
            if (edges != null) {
                getSplitAssociatedNodes(graph.getSplit(edges.get(0)), edges.get(0).getSource(), leftSplit);
                getSplitAssociatedNodes(graph.getSplit(edges.get(0)), edges.get(0).getTarget(), rightSplit);

                leftNodes.clear();

                // For each edge get left node
                edges.forEach(edge -> {
                    if (leftSplit.contains(edge.getSource())) {
                        leftNodes.add(edge.getSource());
                        // rightNodes.add(edge.getTarget());
                    } else if (rightSplit.contains(edge.getSource())) {
                        leftNodes.add(edge.getTarget());
                        // rightNodes.add(edge.getSource());
                    } else {
                        try {
                            throw new Exception("ForceCalculation: Node is not contained in any split!");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                // Calculation big force vector for each node in the current split
                Point3D vec = new Point3D(0, 0, 0);

                for (Node aNode : leftSplit) {
                    for (Node otherNode : rightSplit) {
                        Point3D dist = nodeLocations.get(aNode).subtract(nodeLocations.get(otherNode));
                        double length = new Point3D(dist.getX(), dist.getY(), dist.getZ()).magnitude();
                        Point3D newVec;
                        if (length < maxDist) {
                            if (linear) {
                                newVec = calculateLinearForceVector(nodeLocations.get(otherNode), nodeLocations.get(aNode));
                            } else {
                                newVec = calculateLogForceVector(nodeLocations.get(otherNode), nodeLocations.get(aNode));
                            }
                            vec.add(newVec);
                        }
                        if (isCancelled())
                            return;
                    }
                }

                final Node nodeId = leftNodes.iterator().next();
                Node correspondingNodeId = getCorrespondingNodeFromSplit(nodeId, graph.getSplit(edges.get(0)));
                Point3D bondDist = nodeLocations.get(nodeId).subtract(nodeLocations.get(correspondingNodeId));
                double bondLength = bondDist.magnitude();
                Point3D newForceVec = bondDist.add(vec.getX(), vec.getY(), vec.getZ());
                Point3D move0 = newForceVec.multiply(bondLength / new Point3D(newForceVec.getX(), newForceVec.getY(), newForceVec.getZ()).magnitude());
                final Point3D move = nodeLocations.get(correspondingNodeId).add(move0).subtract(nodeLocations.get(nodeId));

                if (!Double.isNaN(move.getX()) || !Double.isNaN(move.getY()) || !Double.isNaN(move.getZ())) {
                    // move all nodes in split
                    leftSplit.forEach(id -> {
                        nodeLocations.put(id, nodeLocations.get(id).add(move));
                    });
                }
            }
        }
    }

    /**
     * determines all nodes on one side of split
     *
     * @param splitId
     * @param v
     * @return nodes on one side of split
     */
    private void getSplitAssociatedNodes(int splitId, Node v, NodeSet nodes) {
        nodes.clear();

        final Stack<Node> stack = new Stack<>();
        stack.push(v);
        nodes.add(v);
        while (stack.size() > 0) {
            v = stack.pop();
            for (Edge e : v.adjacentEdges()) {
                if (graph.getSplit(e) != splitId) {
                    final Node w = e.getOpposite(v);
                    if (!nodes.contains(w)) {
                        nodes.add(w);
                        stack.push(w);
                    }
                }
            }
        }
    }

    /**
     * if this node is adjacent to an edge representing the given split, returns the oppposite node
     *
     * @param v
     * @param splitId
     * @return opposite or null
     */
    private Node getCorrespondingNodeFromSplit(Node v, int splitId) {
        for (Edge e : v.adjacentEdges()) {
            if (graph.getSplit(e) == splitId)
                return e.getOpposite(v);
        }
        return null;
    }

    @Override
    protected NodeArray<Point3D> call() throws Exception {
        updateTitle("Relaxing");
        updateProgress(0, numOfSteps);
        if (withZpush) {
            shiftRandomSplits(3);
        }

        maxDist = calculateMaxDist(nodeLocations.values());
        for (int i = 0; i < numOfSteps; i++) {
            calculateStep(nodeLocations, linear);
            updateProgress(i, numOfSteps - 1);
            if (isCancelled())
                return null;
        }
        center(nodeLocations);

        return nodeLocations;
    }


    /**
     * get the max distance
     *
     * @param points
     * @return max distance between any two points
     */
    public static double calculateMaxDist(Iterable<Point3D> points) {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (Point3D point : points) {
            minX = Math.min(point.getX(), minX);
            minY = Math.min(point.getY(), minY);
            minZ = Math.min(point.getZ(), minZ);
            maxX = Math.max(point.getX(), maxX);
            maxY = Math.max(point.getY(), maxY);
            maxZ = Math.max(point.getZ(), maxZ);
        }
        return (new Point3D(maxX, maxY, maxZ)).distance(new Point3D(minX, minY, minZ));
    }


    /**
     * center coordinates
     *
     * @param points
     */
    public static void center(NodeArray<Point3D> points) {
        double averageX = 0;
        double averageY = 0;
        double averageZ = 0;
        int count = 0;
        for (Point3D point : points.values()) {
            averageX += point.getX();
            averageY += point.getY();
            averageZ += point.getZ();
            count++;
        }
        if (count > 0) {
            final Point3D center = new Point3D(averageX / count, averageY / count, averageZ / count);
            for (Node v : points.keys()) {
                points.put(v, points.get(v).subtract(center));
            }
        }
    }


}
