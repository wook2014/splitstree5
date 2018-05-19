package splitstree5.io.imports.NeXML.handlers;

import jloda.graph.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import splitstree5.core.datablocks.NetworkBlock;

import java.util.ArrayList;

public class NexmlNetworkHandler extends DefaultHandler {

    private boolean bReadingNetwork = false;
    private boolean bReadOneNetwork = false;

    NetworkBlock networkBlock;
    private ArrayList<String> taxaLabels = new ArrayList<>();


    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes) throws SAXException {

        // Taxa INFO
        if (qName.equalsIgnoreCase("otus")) {
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            System.out.println("Label : " + label);
            System.out.println("ID : " + id);
        } else if (qName.equalsIgnoreCase("otu")) {
            //otu = true;
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            if (label != null) {
                System.out.println("Label : " + label);
                taxaLabels.add(label);
            } else {
                System.out.println("Label = ID : " + id);
                taxaLabels.add(id);
            }
        }

        // Network INFO
        else if (qName.equalsIgnoreCase("network")) {
            //networkBlock = new NetworkBlock();
            //graph = new PhyloGraph();
            bReadingNetwork = true;
        } else if (qName.equalsIgnoreCase("node") && bReadingNetwork) {
            String id = attributes.getValue("id");
            String label = attributes.getValue("label");
            String otu = attributes.getValue("otu");
            System.out.println("node " + id + " has label " + otu);

            Node node = networkBlock.getGraph().newNode();
            NetworkBlock.NodeData nodeData = networkBlock.getNodeData(node);
            nodeData.put("id", id);
            if (label != null)
                nodeData.put("label", label);
            networkBlock.getNode2data().put(node, nodeData);
            if (otu != null)
                networkBlock.getGraph().addTaxon(node, taxaLabels.indexOf(otu));

        } else if (qName.equalsIgnoreCase("edge") && bReadingNetwork) {
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");
            String weightString = attributes.getValue("length");

            Double weight = 1.0;
            if (weightString != null)
                weight = Double.parseDouble(weightString);

            // todo add edge data
            Node sourceNode = networkBlock.getGraph().getLastNode();
            Node targetNode = networkBlock.getGraph().getFirstNode();
            for (Node n : networkBlock.getGraph().getNodesAsSet()) {
                if (networkBlock.getNodeData(n).get("id").equals(source))
                    sourceNode = n;
                if (networkBlock.getNodeData(n).get("id").equals(target))
                    targetNode = n;
            }

            networkBlock.getGraph().setWeight(networkBlock.getGraph().newEdge(sourceNode, targetNode), weight);
        }

    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("otus")) {
            System.out.println("End Element :" + qName);
        } else if (qName.equalsIgnoreCase("network")) {
            bReadingNetwork = false;
            bReadOneNetwork = true; // todo read only one network
            //trees.add(tree);
        }
    }


    public ArrayList<String> getTaxaLabels() {
        return taxaLabels;
    }

    public void setNetworkBlock(NetworkBlock networkBlock) {
        this.networkBlock = networkBlock;
    }
}
