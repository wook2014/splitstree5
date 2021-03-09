/*
 * NetworkBlock.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.datablocks;

import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloGraph;
import splitstree5.core.algorithms.interfaces.IFromNetwork;
import splitstree5.core.algorithms.interfaces.IToNetwork;

import java.util.HashMap;

/**
 * implements a network
 * Daniel Huson, 2.2018
 */
public class NetworkBlock extends DataBlock {
    public static final String BLOCK_NAME = "NETWORK";

    public enum Type {HaplotypeNetwork, Other}

    private final PhyloGraph graph;
    private final NodeArray<NodeData> node2data;
    private final EdgeArray<EdgeData> edge2data;

    private Type networkType;

    public NetworkBlock() {
        this(null);
    }

    public NetworkBlock(String name) {
        this(name, new PhyloGraph());
    }

    public NetworkBlock(String name, PhyloGraph graph) {
        this.graph = graph;
        node2data = new NodeArray<>(graph);
        edge2data = new EdgeArray<>(graph);
        if (name != null)
            setName(name);

        //getNetworkNodes().addListener((InvalidationListener) observable -> setShortDescription(getInfo()));
    }


    public void clear() {
        graph.clear();
        node2data.clear();
        edge2data.clear();
        networkType = Type.Other;
    }

    public PhyloGraph getGraph() {
        return graph;
    }

    public NodeArray<NodeData> getNode2data() {
        return node2data;
    }

    public EdgeArray<EdgeData> getEdge2data() {
        return edge2data;
    }

    @Override
    public int size() {
        return graph.getNumberOfNodes();
    }

    @Override
    public Class<IFromNetwork> getFromInterface() {
        return IFromNetwork.class;
    }

    @Override
    public Class<IToNetwork> getToInterface() {
        return IToNetwork.class;
    }

    @Override
    public String getInfo() {
        return graph.getNumberOfNodes() + " nodes and " + graph.getNumberOfEdges() + " edges";
    }

    public NodeData getNodeData(Node v) {
        NodeData nodeData = node2data.getValue(v);
        if (nodeData == null) {
            nodeData = new NodeData();
            node2data.put(v, nodeData);
        }
        return nodeData;
    }

    public EdgeData getEdgeData(Edge e) {
        EdgeData edgeData = edge2data.getValue(e);
        if (edgeData == null) {
            edgeData = new EdgeData();
            edge2data.put(e, edgeData);
        }
        return edgeData;
    }

    public int getNumberOfNodes() {
        return graph.getNumberOfNodes();
    }

    public int getNumberOfEdges() {
        return graph.getNumberOfEdges();
    }

    public static class NodeData extends HashMap<String, String> {
        public enum BasicKey {x, y, h, w, label}
    }

    public static class EdgeData extends HashMap<String, String> {
        enum BasicKey {type, c1, c2, label}
    }

    public Type getNetworkType() {
        return networkType;
    }

    public void setNetworkType(Type networkType) {
        this.networkType = networkType;
    }

    @Override
    public String getBlockName() {
        return BLOCK_NAME;
    }
}
