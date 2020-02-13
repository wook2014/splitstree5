/*
 * NexmlTreesHandler.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.imports.NeXML.handlers;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class NexmlTreesHandler extends DefaultHandler {

    // tree 't1'=[&R] (((3:0.234,2:0.3243):0.324,(5:0.32443,4:0.2342):0.3247):0.34534,1:0.4353);
    private boolean bReadingTree = false;
    private boolean partial = false;
    private boolean rooted = false;

    private PhyloTree tree;
    private ArrayList<String> treeLabels = new ArrayList<>();
    private ArrayList<String> taxaLabels = new ArrayList<>();
    private ArrayList<PhyloTree> trees = new ArrayList<>();
    private HashMap<String, Node> id2node = new HashMap<>();

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes) throws SAXException {

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
        // TREES INFO
        else if (qName.equalsIgnoreCase("tree")) {
            tree = new PhyloTree();
            treeLabels = new ArrayList<>();
            id2node = new HashMap<>();
            bReadingTree = true;
        } else if (qName.equalsIgnoreCase("node") && bReadingTree) {
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            String otu = attributes.getValue("otu");
            boolean root = Boolean.parseBoolean(attributes.getValue("root"));

            Node node = tree.newNode();
            if (root) {
                tree.setRoot(node);
                rooted = true;
            }
            id2node.put(id, node);

            if (otu != null) {
                tree.setLabel(node, otu);
                treeLabels.add(otu);
                tree.addTaxon(node, taxaLabels.indexOf(otu));
            }

        } else if (qName.equalsIgnoreCase("rootedge") && bReadingTree) {
            String sWeight = attributes.getValue("length");
            Double weight;
            if (sWeight == null)
                weight = 1.0;
            else
                weight = Double.parseDouble(attributes.getValue("length"));

            Node sourceNode = tree.newNode();
            Node targetNode = tree.getRoot();
            tree.setRoot(sourceNode);
            tree.setWeight(tree.newEdge(sourceNode, targetNode), weight);

        } else if (qName.equalsIgnoreCase("edge") && bReadingTree) {
            String id = attributes.getValue("id");
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");

            String sWeight = attributes.getValue("length");
            Double weight;
            if (sWeight == null)
                weight = 1.0;
            else
                weight = Double.parseDouble(attributes.getValue("length"));

            Node sourceNode = null;
            Node targetNode = null;

            for (String key : id2node.keySet()) {
                if (key.equals(source))
                    sourceNode = id2node.get(key);
                if (key.equals(target))
                    targetNode = id2node.get(key);
            }

            if (sourceNode == null)
                throw new SAXException("Edge " + id + " contains not defined source node id=" + source);
            else if (targetNode == null)
                throw new SAXException("Edge " + id + " contains not defined target node id=" + target);
            else
                tree.setWeight(tree.newEdge(sourceNode, targetNode), weight);
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("otus")) {
            //System.out.println("End Element :" + qName);
        } else if (qName.equalsIgnoreCase("tree")) {
            bReadingTree = false;
            trees.add(tree);

            // if a tree already set as partial, no further check
            if (partial || taxaLabels.size() != treeLabels.size())
                partial = true;

            treeContainsAllTaxa(tree);
        }
    }

    public ArrayList<String> getTaxaLabels() {
        return this.taxaLabels;
    }

    public ArrayList<PhyloTree> getTrees() {
        return this.trees;
    }

    public boolean isPartial() {
        return this.partial;
    }

    public boolean isRooted() {
        return this.rooted;
    }

    private boolean treeContainsAllTaxa(PhyloTree tree) {

        int numOfLabelsInTree = 0;

        for (String s : tree.nodeLabels()) {
            numOfLabelsInTree++;
        }
        //System.err.println(tree.nodeLabels().iterator().next());

        //Iterable<String> iterator = tree.nodeLabels();
        //iterator.forEach(System.err.println("ffff"));

        /*while (iterator.spliterator().hasNext()) {
            //iterator.next();
            System.err.println(iterator.next());
            numOfLabelsInTree++;
        }*/
        /*System.err.println("üüüüüüüüü" + numOfLabelsInTree);
        //return numOfLabelsInTree == taxaLabels.size();

        Iterator<String> source = tree.nodeLabels().iterator();
        ArrayList<String> target = (ArrayList<String>) makeCollection(tree.nodeLabels());
        //tree.nodeLabels().forEach(target::add);

        for (String s : target)
            System.err.println("Label "+s);*/


        //tree.nodeLabels().forEach(y -> System.out.println(y));


        return true;
        // todo : how to use iterator?
    }

    public static Collection<String> makeCollection(Iterable<String> iter) {
        Collection<String> list = new ArrayList<String>();
        for (String item : iter) {
            list.add(item);
        }
        return list;
    }
}
