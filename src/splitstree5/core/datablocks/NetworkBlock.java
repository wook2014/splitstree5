/*
 *  Copyright (C) 2017 Daniel H. Huson
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
package splitstree5.core.datablocks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import splitstree5.core.algorithms.interfaces.IFromNetwork;
import splitstree5.core.algorithms.interfaces.IToNetwork;

import java.util.*;

/**
 * A network block
 * Daniel Huson, 7.2017
 */
public class NetworkBlock extends ADataBlock {
    private int ntax;
    private int topNodeId = 0;
    private int topEdgeId = 0;

    private final ObservableList<Integer> nodes = FXCollections.observableArrayList();
    private final ObservableList<Integer> edges = FXCollections.observableArrayList();

    private final Map<Integer, NetworkNode> id2Node = new HashMap<>();
    private final Map<Integer, NetworkEdge> id2Edge = new HashMap<>();

    private final Map<Integer, Integer> taxonId2NodeId = new TreeMap<>();

    /**
     * constructor
     */
    public NetworkBlock() {
    }

    /**
     * named constructor
     *
     * @param name
     */
    public NetworkBlock(String name) {
        this();
        setName(name);
    }

    /**
     * shallow copy
     *
     * @param that
     */
    public void copy(NetworkBlock that) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void clear() {
        super.clear();
        setShortDescription("");
        topNodeId = 0;
        topEdgeId = 0;
        nodes.clear();
        edges.clear();
        id2Node.clear();
        id2Edge.clear();
    }

    public void setNtax(int n) {
        this.ntax = n;
    }

    @Override
    public int size() {
        return getNumVertices();
    }

    public int getNtax() {
        return ntax;
    }

    public int getNumVertices() {
        return nodes.size();
    }

    public int getNumEdges() {
        return edges.size();
    }

    @Override
    public Class getFromInterface() {
        return IFromNetwork.class;
    }

    @Override
    public Class getToInterface() {
        return IToNetwork.class;
    }

    @Override
    public String getInfo() {
        return "a network with" + getNumVertices() + "nodes and " + getNumEdges() + " edges";
    }

    public int newNode() {
        final int id = (++topNodeId);
        id2Node.put(id, new NetworkNode());
        nodes.add(id);
        return id;
    }

    public void deleteNode(int nodeId) {
        setNodeId2TaxonId(nodeId, null, true);
        for (Integer edgeId : new ArrayList<>(getAllEdges(nodeId))) {
            deleteEdge(edgeId);
        }
        id2Node.remove(nodeId);
        nodes.remove((Integer) nodeId); // need to cast to make sure object is removed, not index!!!!
    }

    public void setNodeCoordinates(int nodeId, float x, float y) {
        setNodeCoordinates(nodeId, x, y, 0);
    }

    public void setNodeCoordinates(int nodeId, float x, float y, float z) {
        id2Node.get(nodeId).x = x;
        id2Node.get(nodeId).y = y;
        id2Node.get(nodeId).z = z;
    }

    public void setNodeId2TaxonId(int nodeId, Integer taxonId, boolean clear) {
        if (clear) {
            final ArrayList<Integer> toDelete = new ArrayList<>();
            for (Integer taxon : taxonId2NodeId.keySet()) {
                if (taxonId2NodeId.get(taxon) == nodeId)
                    toDelete.add(taxon);
            }
            taxonId2NodeId.keySet().removeAll(toDelete);
        }
        if (taxonId != null)
            taxonId2NodeId.put(taxonId, nodeId);
    }

    public Integer getNodeIdForTaxonId(int taxonId) {
        return taxonId2NodeId.get(taxonId);
    }

    public float getNodeCoordinateX(int nodeId) {
        return id2Node.get(nodeId).x;
    }

    public float getNodeCoordinateY(int nodeId) {
        return id2Node.get(nodeId).y;
    }

    public float getNodeCoordinateZ(int nodeId) {
        return id2Node.get(nodeId).z;
    }

    public int newEdge(int nodeId1, int nodeId2) {
        final int id = (++topEdgeId);
        id2Edge.put(id, new NetworkEdge(nodeId1, nodeId2));
        id2Node.get(nodeId1).outEdges.add(id);
        id2Node.get(nodeId1).inEdges.add(id);
        edges.add(id);
        return id;
    }

    public void deleteEdge(int edgeId) {
        final NetworkEdge edge = id2Edge.get(edgeId);
        id2Node.get(edge.target).inEdges.remove(edgeId);
        id2Node.get(edge.source).outEdges.remove(edgeId);
        id2Edge.remove(edgeId);
        edges.remove((Integer) edgeId); // need to cast to make sure object is removed, not index!!!!
    }

    public void setNodeLabel(int nodeId, String label) {
        id2Node.get(nodeId).label = label;
    }

    public String getNodeLabel(int nodeId) {
        return id2Node.get(nodeId).label;
    }

    public void setEdgeLabel(int edgeId, String label) {
        id2Edge.get(edgeId).label = label;
    }

    public String getEdgeLabel(int edgeId) {
        return id2Edge.get(edgeId).label;
    }

    public int getSource(int edgeId) {
        return id2Edge.get(edgeId).source;
    }

    public int getTarget(int edgeId) {
        return id2Edge.get(edgeId).target;
    }

    public AbstractList<Integer> getInEdges(int nodeId) {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int index) {
                return id2Node.get(nodeId).inEdges.get(index);
            }

            @Override
            public int size() {
                return id2Node.get(nodeId).inEdges.size();
            }
        };
    }

    public AbstractList<Integer> getOutEdges(int nodeId) {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int index) {
                return id2Node.get(nodeId).outEdges.get(index);
            }

            @Override
            public int size() {
                return id2Node.get(nodeId).outEdges.size();
            }
        };
    }

    public AbstractList<Integer> getAllEdges(int nodeId) {
        return new AbstractList<Integer>() {
            final NetworkNode node = id2Node.get(nodeId);

            @Override
            public Integer get(int index) {
                if (index < node.inEdges.size())
                    return node.inEdges.get(index);
                else
                    return node.outEdges.get(index - node.inEdges.size());
            }

            @Override
            public int size() {
                return node.inEdges.size() + node.outEdges.size();
            }
        };
    }

    public void setSplitId(int edgeId, int splitId) {
        id2Edge.get(edgeId).splitId = splitId;
    }

    public int getSplitId(int edgeId) {
        return id2Edge.get(edgeId).splitId;
    }

    public void setWeight(int edgeId, float weight) {
        id2Edge.get(edgeId).weight = weight;
    }

    public float getWeight(int edgeId) {
        return id2Edge.get(edgeId).weight;
    }

    private class NetworkNode {
        private String label;
        private float x;
        private float y;
        private float z;
        private final ArrayList<Integer> inEdges = new ArrayList<>();
        private final ArrayList<Integer> outEdges = new ArrayList<>();
    }

    private class NetworkEdge {
        private String label;
        private int source;
        private int target;
        private int splitId;
        private float weight;

        public NetworkEdge(int source, int target) {
            this.source = source;
            this.target = target;
        }
    }
}
