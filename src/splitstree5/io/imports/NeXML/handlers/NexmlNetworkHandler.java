/*
 *  NexmlNetworkHandler.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.io.imports.NeXML.handlers;

import jloda.graph.Edge;
import jloda.graph.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import splitstree5.core.datablocks.NetworkBlock;

import java.util.ArrayList;

public class NexmlNetworkHandler extends DefaultHandler {

    private boolean bReadingNetwork = false;
    private boolean bReadingNode = false;
    private boolean bReadingEdge = false;
    private boolean bReadOneNetwork = false;

    NetworkBlock networkBlock;
    Object currentElement;
    private ArrayList<String> taxaLabels = new ArrayList<>();


    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes) throws SAXException {

        // todo delete "sp5" in metadata before parsing
        // todo reachTextFX (StyleClassesTextArea), WebView von javaFX

        // Taxa INFO
        if (qName.equalsIgnoreCase("otus")) {
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            //System.out.println("Label : " + label);
            //System.out.println("ID : " + id);
        } else if (qName.equalsIgnoreCase("otu")) {
            //otu = true;
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            if (label != null) {
                //System.out.println("Label : " + label);
                taxaLabels.add(label);
            } else {
                //System.out.println("Label = ID : " + id);
                taxaLabels.add(id);
            }
        }

        // Network INFO
        else if (qName.equalsIgnoreCase("network")) {
            //networkBlock = new NetworkBlock();
            //graph = new PhyloGraph();
            bReadingNetwork = true;
        } else if (qName.equalsIgnoreCase("node") && bReadingNetwork) {
            bReadingNode = true;
            String id = attributes.getValue("id");
            String label = attributes.getValue("label");
            String otu = attributes.getValue("otu");
            //System.out.println("node " + id + " has label " + otu);

            Node node = networkBlock.getGraph().newNode();
            NetworkBlock.NodeData nodeData = networkBlock.getNodeData(node);
            nodeData.put("nex:id", id);
            if (label != null)
                nodeData.put("nex:label", label);
            networkBlock.getNode2data().put(node, nodeData);
            if (otu != null)
                networkBlock.getGraph().addTaxon(node, taxaLabels.indexOf(otu));

            currentElement = node;

        } else if (qName.equalsIgnoreCase("edge") && bReadingNetwork) {
            bReadingEdge = true;
            String id = attributes.getValue("id");
            String label = attributes.getValue("label");
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");
            String weightString = attributes.getValue("length");

            Double weight = 1.0;
            if (weightString != null)
                weight = Double.parseDouble(weightString);

            Node sourceNode = networkBlock.getGraph().getLastNode();
            Node targetNode = networkBlock.getGraph().getFirstNode();
            for (Node n : networkBlock.getGraph().getNodesAsSet()) {
                if (networkBlock.getNodeData(n).get("nex:id").equals(source))
                    sourceNode = n;
                if (networkBlock.getNodeData(n).get("nex:id").equals(target))
                    targetNode = n;
            }

            Edge edge = networkBlock.getGraph().newEdge(sourceNode, targetNode);
            networkBlock.getGraph().setWeight(edge, weight);

            // Save original labels
            NetworkBlock.EdgeData edgeData = networkBlock.getEdgeData(edge);
            edgeData.put("nex:id", id);
            if (label != null)
                edgeData.put("nex:label", label);
            networkBlock.getEdge2data().put(edge, edgeData);

            currentElement = edge;

            // Metadata
        } else if (qName.equalsIgnoreCase("meta") && bReadingNetwork) {
            //System.out.println("READING META");

            if (currentElement instanceof Node) {
                NetworkBlock.NodeData nodeData = networkBlock.getNodeData((Node) currentElement);

                for (int i = 0; i<attributes.getLength(); i++)
                    nodeData.put("metadata_"+attributes.getLocalName(i), attributes.getValue(i));


                networkBlock.getNode2data().put((Node) currentElement, nodeData);
            }

            if (currentElement instanceof Edge) {
                NetworkBlock.EdgeData edgeData = networkBlock.getEdgeData((Edge) currentElement);

                for (int i = 0; i<attributes.getLength(); i++)
                    edgeData.put("metadata_"+attributes.getLocalName(i), attributes.getValue(i));

                networkBlock.getEdge2data().put((Edge) currentElement, edgeData);
            }

        }

    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("otus")) {
            //System.out.println("End Element :" + qName);
        } else if (qName.equalsIgnoreCase("network")) {
            bReadingNetwork = false;
            bReadOneNetwork = true; // todo read only one network
            //trees.add(tree);
        } else if (qName.equalsIgnoreCase("node")) {
            bReadingNode = false;
        } else if (qName.equalsIgnoreCase("edge")) {
            bReadingEdge = false;
        }
    }


    public ArrayList<String> getTaxaLabels() {
        return taxaLabels;
    }

    public void setNetworkBlock(NetworkBlock networkBlock) {
        this.networkBlock = networkBlock;
    }
}
