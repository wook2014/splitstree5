/*
 *  NeXMLExporter.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.io.exports;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloGraph;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IFromNetwork;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.datablocks.*;
import splitstree5.io.exports.interfaces.IExportCharacters;
import splitstree5.io.exports.interfaces.IExportNetwork;
import splitstree5.io.exports.interfaces.IExportTaxa;
import splitstree5.io.exports.interfaces.IExportTrees;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NeXMLExporter implements
        IFromTaxa, IExportTaxa, IFromChararacters, IFromNetwork,
        IExportCharacters, IFromTrees, IExportTrees, IExportNetwork {

    public enum CharactersOutputType {cell, matrix, both}

    private CharactersOutputType charactersOutputType = CharactersOutputType.matrix;
    private boolean exportSingleBlock = true;

    private XMLStreamWriter startWriter;

    @Override
    public void export(Writer w, TaxaBlock taxa) throws IOException {

        try {

            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter =
                    xMLOutputFactory.createXMLStreamWriter(w);

            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeStartElement("otus");
            xmlWriter.writeAttribute("id", "otus1");
            xmlWriter.writeAttribute("label", "TaxaBlock");

            int i = 0;
            for (String label : taxa.getLabels()) {
                i++;
                writeNewLineWithTabs(xmlWriter, 2);
                xmlWriter.writeEmptyElement("otu");
                xmlWriter.writeAttribute("id", "otu" + i);
                xmlWriter.writeAttribute("label", label);
            }
            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeEndElement();
            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeEndDocument();
            xmlWriter.flush();
        } catch (XMLStreamException xmlEx) {
            xmlEx.printStackTrace();
        }
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        try {
            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter =
                    xMLOutputFactory.createXMLStreamWriter(w);

            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeStartElement("characters");
            xmlWriter.writeAttribute("id", "characters1");
            xmlWriter.writeAttribute("label", "charactersBlock");
            xmlWriter.writeAttribute("otus", "otus1");

            if (charactersOutputType.equals(CharactersOutputType.matrix)) {
                String dataType = "nex:" + characters.getDataType().name() + "Seqs";
                xmlWriter.writeAttribute("xsi:type", dataType);

                writeNewLineWithTabs(xmlWriter, 2);
                xmlWriter.writeStartElement("format");
                writeNewLineWithTabs(xmlWriter, 3);
                xmlWriter.writeStartElement("states");
                xmlWriter.writeAttribute("id", "states1");

                int x = 0;
                for (char c : characters.getDataType().getSymbols().toCharArray()) {
                    x++;
                    writeNewLineWithTabs(xmlWriter, 4);
                    xmlWriter.writeEmptyElement("state");
                    xmlWriter.writeAttribute("id", "s" + x);
                    xmlWriter.writeAttribute("symbol", c + "");
                }
                writeNewLineWithTabs(xmlWriter, 3);
                xmlWriter.writeEndElement();

                for (int c = 0; c < characters.getNchar(); c++) {
                    writeNewLineWithTabs(xmlWriter, 3);
                    xmlWriter.writeEmptyElement("char");
                    xmlWriter.writeAttribute("id", "c" + c);
                    xmlWriter.writeAttribute("states", "states1");
                }

                writeNewLineWithTabs(xmlWriter, 2);
                xmlWriter.writeEndElement();
                ////////////////

                writeNewLineWithTabs(xmlWriter, 2);
                xmlWriter.writeStartElement("matrix");
                xmlWriter.writeAttribute("aligned", "1");

                int ntax = taxa.getNtax();
                int nchar = characters.getNchar();

                for (int i = 1; i <= ntax; i++) {
                    writeNewLineWithTabs(xmlWriter, 3);
                    xmlWriter.writeStartElement("row");
                    xmlWriter.writeAttribute("id", "row" + i);
                    xmlWriter.writeAttribute("label", taxa.getLabel(i));
                    xmlWriter.writeAttribute("otus", "otu1");

                    writeNewLineWithTabs(xmlWriter, 4);
                    xmlWriter.writeStartElement("seq");
                    xmlWriter.writeAttribute("label", taxa.getLabel(i));
                    for (int j = 1; j <= nchar; j++) {
                        xmlWriter.writeCharacters((characters.get(i, j) + "").toUpperCase());
                    }
                    xmlWriter.writeEndElement(); // seq
                    writeNewLineWithTabs(xmlWriter, 3);
                    xmlWriter.writeEndElement(); //row
                }
            }

            if (charactersOutputType.equals(CharactersOutputType.cell) ||
                    charactersOutputType.equals(CharactersOutputType.both)) {
                String dataType = "nex:" + characters.getDataType().name() + "Cells";
                xmlWriter.writeAttribute("xsi:type", dataType);
            }

            writeNewLineWithTabs(xmlWriter, 2);
            xmlWriter.writeEndElement(); // matrix
            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeEndElement(); //characters
        } catch (XMLStreamException xmlEx) {
            xmlEx.printStackTrace();
        }
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, TreesBlock trees) throws IOException {

        export(w, taxa);
        try {
            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter =
                    xMLOutputFactory.createXMLStreamWriter(w);
            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeStartElement("trees");
            xmlWriter.writeAttribute("otus", "otus1");
            xmlWriter.writeAttribute("id", "trees1");
            xmlWriter.writeAttribute("label", "TreesBlock");

            int treesCounter = 0;
            for (PhyloTree tree : trees.getTrees()) {
                treesCounter++;

                writeNewLineWithTabs(xmlWriter, 2);
                xmlWriter.writeStartElement("tree");
                xmlWriter.writeAttribute("id", "tree"+treesCounter);
                xmlWriter.writeAttribute("label", "tree"+treesCounter);
                xmlWriter.writeAttribute("xsi:type", "nex:FloatTree");

                NodeSet nodes = tree.getNodesAsSet();
                EdgeSet edges = tree.getEdgesAsSet();

                for (Node node : nodes) {
                    writeNewLineWithTabs(xmlWriter, 3);
                    xmlWriter.writeEmptyElement("node");
                    xmlWriter.writeAttribute("id", "n" + node.getId());
                    xmlWriter.writeAttribute("label", "n" + node.getId());
                    if (tree.getRoot() != null && tree.getRoot().equals(node))
                        xmlWriter.writeAttribute("root", "true");
                    if (node.isLeaf())
                        xmlWriter.writeAttribute("otu", tree.getLabel(node));
                }

                for (Edge edge : edges) {
                    writeNewLineWithTabs(xmlWriter, 3);
                    xmlWriter.writeEmptyElement("edge");
                    xmlWriter.writeAttribute("source", "n" + edge.getSource().getId());
                    xmlWriter.writeAttribute("target", "n" + edge.getTarget().getId());
                    xmlWriter.writeAttribute("id", "e" + edge.getId());
                    xmlWriter.writeAttribute("length", tree.getWeight(edge) + "");
                }
                writeNewLineWithTabs(xmlWriter, 2);
                xmlWriter.writeEndElement();
            }
            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeEndElement(); //trees
        } catch (XMLStreamException xmlEx) {
            xmlEx.printStackTrace();
        }
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, NetworkBlock network) throws IOException {

        export(w, taxa);
        try {
            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter =
                    xMLOutputFactory.createXMLStreamWriter(w);
            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeStartElement("trees");
            xmlWriter.writeAttribute("otus", "otus1");
            xmlWriter.writeAttribute("id", "trees1");
            xmlWriter.writeAttribute("label", "NetworkBlock");

            PhyloGraph graph = network.getGraph();
            writeNewLineWithTabs(xmlWriter, 2);
            xmlWriter.writeStartElement("network");
            xmlWriter.writeAttribute("id", "network1");
            xmlWriter.writeAttribute("xsi:type", "nex:FloatNetwork");

            // Nodes and edges
            NodeSet nodes = graph.getNodesAsSet();
            EdgeSet edges = graph.getEdgesAsSet();

            HashMap<Integer, String> id2nexId = new HashMap<>();

            for (Node node : nodes) {

                writeNewLineWithTabs(xmlWriter, 3);
                xmlWriter.writeEmptyElement("node");

                // check original id/labels
                if (network.getNodeData(node).containsKey("nex:id")) {
                    xmlWriter.writeAttribute("id", network.getNodeData(node).get("nex:id"));
                    id2nexId.put(node.getId(), network.getNodeData(node).get("nex:id"));
                } else
                    xmlWriter.writeAttribute("id", "n" + node.getId());

                if (network.getNodeData(node).containsKey("nex:label"))
                    xmlWriter.writeAttribute("label", network.getNodeData(node).get("nex:label"));


                // Taxon
                int i;
                Object o = graph.getTaxa(node).iterator().next();
                if (o != null) {
                    i  = (int) o;
                    xmlWriter.writeAttribute("otu", taxa.getLabel(i + 1));
                }

                // Metadata
                NetworkBlock.NodeData nodeData = network.getNodeData(node);
                nodeData.remove("nex:id");
                nodeData.remove("nex:label");
                if (!nodeData.keySet().isEmpty()) {
                    writeNewLineWithTabs(xmlWriter, 4);
                    xmlWriter.writeEmptyElement("meta");
                    for (String key : nodeData.keySet())
                        if(key.contains("metadata_"))
                            xmlWriter.writeAttribute(key.replace("metadata_", ""), nodeData.get(key));
                        else
                            xmlWriter.writeAttribute("sp5:" + key, nodeData.get(key));
                }
            }

            xmlWriter.writeCharacters("\n");

            for (Edge edge : edges) {
                writeNewLineWithTabs(xmlWriter, 3);
                xmlWriter.writeEmptyElement("edge");

                // ID
                if (network.getEdgeData(edge).containsKey("nex:id"))
                    xmlWriter.writeAttribute("id", network.getEdgeData(edge).get("nex:id"));
                else
                    xmlWriter.writeAttribute("id", "e" + edge.getId());

                // Label
                if (network.getEdgeData(edge).containsKey("nex:label"))
                    xmlWriter.writeAttribute("label", network.getEdgeData(edge).get("nex:label"));

                // Source
                if (id2nexId.containsKey(edge.getSource().getId()))
                    xmlWriter.writeAttribute("source", id2nexId.get(edge.getSource().getId()));
                else
                    xmlWriter.writeAttribute("source", "n" + edge.getSource().getId());

                // Target
                if (id2nexId.containsKey(edge.getTarget().getId()))
                    xmlWriter.writeAttribute("target", id2nexId.get(edge.getTarget().getId()));
                else
                    xmlWriter.writeAttribute("target", "n" + edge.getTarget().getId());

                // Weight
                xmlWriter.writeAttribute("length", graph.getWeight(edge) + "");

                // Metadata
                NetworkBlock.EdgeData edgeData = network.getEdgeData(edge);
                edgeData.remove("nex:id");
                edgeData.remove("nex:label");
                if (!edgeData.keySet().isEmpty()) {
                    writeNewLineWithTabs(xmlWriter, 4);
                    xmlWriter.writeEmptyElement("meta");
                    for (String key : edgeData.keySet())
                        if(key.contains("metadata_"))
                            xmlWriter.writeAttribute(key.replace("metadata_", ""), edgeData.get(key));
                        else
                            xmlWriter.writeAttribute("sp5:" + key, edgeData.get(key));
                }
            }
            writeNewLineWithTabs(xmlWriter, 2);
            xmlWriter.writeEndElement();

            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeEndElement(); //trees
        } catch (XMLStreamException xmlEx) {
            xmlEx.printStackTrace();
        }
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("xml", "nexml");
    }

    public void writeStart(Writer w) throws XMLStreamException, IOException {

        XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter =
                xMLOutputFactory.createXMLStreamWriter(w);

        xmlStreamWriter.writeStartDocument();
        xmlStreamWriter.writeCharacters("\n");

        xmlStreamWriter.writeStartElement("nex:nexml");
        w.write("\n\t");
        xmlStreamWriter.writeAttribute("generator", "SplitsTree5");
        w.write("\n\t");
        xmlStreamWriter.writeAttribute("version", "0.9");
        w.write("\n\t");
        xmlStreamWriter.writeNamespace("nex", "http://www.nexml.org/2009");
        w.write("\n\t");
        xmlStreamWriter.writeDefaultNamespace("http://www.nexml.org/2009");
        w.write("\n\t");
        xmlStreamWriter.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        w.write("\n\t");
        xmlStreamWriter.writeNamespace("sp5", "https://github.com/danielhuson/splitstree5");
        //w.write("\n\t");
        //xmlStreamWriter.writeNamespace("xml", "http://www.w3.org/XML/1998/namespace");

        xmlStreamWriter.writeCharacters("\n"); //close the element
        xmlStreamWriter.flush();
        startWriter = xmlStreamWriter;
    }

    public void writeEnd() throws XMLStreamException {
        startWriter.writeCharacters("\n");
        startWriter.writeEndElement();
        startWriter.writeEndDocument();
    }

    private void writeNewLineWithTabs(XMLStreamWriter xmlStreamWriter, int numTabs) throws XMLStreamException {
        xmlStreamWriter.writeCharacters("\n");
        for (int i = 0; i < numTabs; i++)
            xmlStreamWriter.writeCharacters("\t");
    }


    // todo : multriple blocks, metadata
    //###########################################################################################
    //############################## for multiple blocks ########################################
    //###########################################################################################

    public void export(Writer w, List<DataBlock> blocks) throws IOException, XMLStreamException {
        XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter =
                xMLOutputFactory.createXMLStreamWriter(w);

        xmlWriter.writeStartDocument();
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeStartElement("nex:nexml");
        xmlWriter.writeAttribute("version", "0.9");
        xmlWriter.writeCharacters("\n\t");

        TaxaBlock topTaxa = null;
        for (DataBlock block : blocks) {
            if (block instanceof TaxaBlock) {
                System.err.println("Found a taxa block");
                topTaxa = (TaxaBlock) block;
                export(xmlWriter, topTaxa);
            }

            if (block instanceof CharactersBlock) {
                if (topTaxa == null)
                    throw new IOException("No taxa block found!");
                System.err.println("Found a characters block");
                export(xmlWriter, topTaxa, (CharactersBlock) block);
            }

            if (block instanceof TreesBlock) {
                if (topTaxa == null)
                    throw new IOException("No taxa block found!");
                System.err.println("Found a trees block");
                export(xmlWriter, topTaxa, (TreesBlock) block);
            }
        }

        xmlWriter.writeCharacters("\n");
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
    }

    private void export(XMLStreamWriter xmlWriter, TaxaBlock taxa) throws IOException, XMLStreamException {
        xmlWriter.writeStartElement("otus");
        xmlWriter.writeAttribute("id", "taxa1");

        for (String label : taxa.getLabels()) {
            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeEmptyElement("otu");
            xmlWriter.writeAttribute("id", label);
        }

        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n");
        xmlWriter.flush();
    }

    private void export(XMLStreamWriter xmlWriter, TaxaBlock taxa, CharactersBlock characters) throws IOException, XMLStreamException {
        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeStartElement("characters");
        xmlWriter.writeCharacters("\n\t\t");
        xmlWriter.writeStartElement("matrix");

        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();

        for (int i = 1; i <= ntax; i++) {
            xmlWriter.writeCharacters("\n\t\t\t");
            xmlWriter.writeStartElement("row");
            xmlWriter.writeCharacters("\n\t\t\t\t");
            xmlWriter.writeStartElement("seq");
            xmlWriter.writeAttribute("label", taxa.getLabel(i));
            for (int j = 1; j <= nchar; j++) {
                xmlWriter.writeCharacters((characters.get(i, j) + "").toUpperCase());
            }
            xmlWriter.writeEndElement(); // seq
            xmlWriter.writeCharacters("\n\t\t\t");
            xmlWriter.writeEndElement(); //row
        }
        xmlWriter.writeCharacters("\n\t\t");
        xmlWriter.writeEndElement(); // matrix
        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeEndElement(); //characters
        xmlWriter.flush();
    }

    private void export(XMLStreamWriter xmlWriter, TaxaBlock taxa, TreesBlock trees) throws IOException, XMLStreamException {

        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeStartElement("trees");

        for (PhyloTree tree : trees.getTrees()) {
            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeStartElement("tree");

            NodeSet nodes = tree.getNodesAsSet();
            EdgeSet edges = tree.getEdgesAsSet();

            for (Node node : nodes) {
                xmlWriter.writeCharacters("\n\t\t");
                xmlWriter.writeEmptyElement("node");
                xmlWriter.writeAttribute("id", "n" + node.getId());
            }

            for (Edge edge : edges) {
                xmlWriter.writeCharacters("\n\t\t");
                xmlWriter.writeEmptyElement("edge");
                xmlWriter.writeAttribute("source", "n" + edge.getSource().getId());
                xmlWriter.writeAttribute("target", "n" + edge.getTarget().getId());
                xmlWriter.writeAttribute("id", "e" + edge.getId());
                xmlWriter.writeAttribute("length", tree.getWeight(edge) + "");
            }

            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeEndElement();
        }
        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeEndElement(); //trees
    }
}
