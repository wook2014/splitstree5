package splitstree5.io.imports.NeXML;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class NexmlTreesHandler extends DefaultHandler {

    //boolean otu = false;
    boolean bReadingTree = false;

    private PhyloTree tree;
    private ArrayList<String> taxaLabels = new ArrayList<>();
    private ArrayList<PhyloTree> trees = new ArrayList<>();

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("otus")) {
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            System.out.println("Label : " + label);
            System.out.println("ID : " + id);
        } else if (qName.equalsIgnoreCase("otu")) {
            //otu = true;
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            if(label != null){
                System.out.println("Label : " + label);
                taxaLabels.add(label);
            } else {
                System.out.println("Label = ID : " + id);
                taxaLabels.add(id);
            }
        }
        // TREES INFO
        else if (qName.equalsIgnoreCase("tree")) {
            tree = new PhyloTree();
            bReadingTree = true;
        } else if (qName.equalsIgnoreCase("node") && bReadingTree) {
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            String otu = attributes.getValue("otu");
            boolean root = Boolean.parseBoolean(attributes.getValue("root"));

            Node node = new Node(tree);
            node.setData(id);
            System.out.println("-----"+node.getData());
            //tree.setLabel(node, label);

            if(root) tree.setRoot(node);
            if(otu != null) tree.setLabel(node, label);

        } else if (qName.equalsIgnoreCase("edge")&& bReadingTree) {
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");
            Double weight = Double.parseDouble(attributes.getValue("length")); // todo : check if possible

            Node sourceNode = tree.getLastNode();
            Node targetNode = tree.getFirstNode();
            for(Node n : tree.getNodesAsSet()){
                if (n.getData().equals(source)) sourceNode = n;
                if (n.getData().equals(target)) targetNode = n;
            }

            //Edge e = new Edge(tree, sourceNode, targetNode, weight);

            tree.setWeight(tree.newEdge(sourceNode, targetNode), weight);
        }

    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("otus")) {
            System.out.println("End Element :" + qName);
        }else if (qName.equalsIgnoreCase("tree")){
            bReadingTree = false;
            trees.add(tree);
        }
    }

    public ArrayList<String> getTaxaLabels(){
        return this.taxaLabels;
    }

    public ArrayList<PhyloTree> getTrees(){
        return this.trees;
    }
}
