package splitstree5.io.exports;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.exports.interfaces.IExportCharacters;
import splitstree5.io.exports.interfaces.IExportTaxa;
import splitstree5.io.exports.interfaces.IExportTrees;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NeXMLOut implements IFromTaxa, IExportTaxa, IFromChararacters, IExportCharacters, IFromTrees, IExportTrees {

    public enum CharactersOutputType {cell, matrix, both}
    private CharactersOutputType charactersOutputType = CharactersOutputType.matrix;
    private boolean exportSingleBlock = true;

    public XMLStreamWriter startWriter;

    @Override
    public void export(Writer w, TaxaBlock taxa) throws IOException {

        try {

            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xmlWriter =
                    xMLOutputFactory.createXMLStreamWriter(w);

            //xmlWriter.writeStartDocument();
            writeNewLineWithTabs(xmlWriter, 1);
            xmlWriter.writeStartElement("otus");
            xmlWriter.writeAttribute("id", "taxa1");

            //xmlWriter.writeStartElement("otu");
            //xmlWriter.writeCharacters("\n\t");
            for (String label : taxa.getLabels()) {
                xmlWriter.writeCharacters("\n\t\t");
                xmlWriter.writeEmptyElement("otu");
                xmlWriter.writeAttribute("id", label);
            }
            //xmlWriter.writeEndElement();
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
            writeNewLineWithTabs(xmlWriter, 2);
            xmlWriter.writeStartElement("matrix");

            int ntax = taxa.getNtax();
            int nchar = characters.getNchar();

            for (int i = 1; i <= ntax; i++) {
                writeNewLineWithTabs(xmlWriter, 3);
                xmlWriter.writeStartElement("row");
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

            for (PhyloTree tree : trees.getTrees()) {
                writeNewLineWithTabs(xmlWriter, 2);
                xmlWriter.writeStartElement("tree");

                NodeSet nodes = tree.getNodesAsSet();
                EdgeSet edges = tree.getEdgesAsSet();

                for (Node node : nodes) {
                    writeNewLineWithTabs(xmlWriter, 2);
                    xmlWriter.writeEmptyElement("node");
                    xmlWriter.writeAttribute("id", "n" + node.getId());
                }

                for (Edge edge : edges) {
                    writeNewLineWithTabs(xmlWriter, 2);
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
    public List<String> getExtensions() {
        return Collections.singletonList("xml");
    }

    public void writeStart(Writer w) throws XMLStreamException, IOException {

        XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlStreamWriter =
                xMLOutputFactory.createXMLStreamWriter(w);

        xmlStreamWriter.writeStartDocument();
        xmlStreamWriter.writeCharacters("\n");

        xmlStreamWriter.writeStartElement("nex:nexml");
        xmlStreamWriter.writeAttribute("generator", "SplitsTree5");
        w.write("\n\t");
        xmlStreamWriter.writeAttribute("version", "0.9");
        w.write("\n\t");
        xmlStreamWriter.writeNamespace("nex", "http://www.nexml.org/2009");
        w.write("\n\t");
        xmlStreamWriter.writeDefaultNamespace("http://www.nexml.org/2009");
        w.write("\n\t");
        xmlStreamWriter.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
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
