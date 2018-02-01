package splitstree5.io.exports;

import jloda.graph.Edge;
import jloda.graph.EdgeSet;
import jloda.graph.Node;
import jloda.graph.NodeSet;
import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.algorithms.interfaces.IFromTrees;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.CharactersBlock;
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
import java.util.List;

public class NeXMLOut implements IFromTaxa, /*IExportTaxa,*/ IFromChararacters, /*IExportCharacters,*/ IFromTrees /*IExportTrees*/ {


    /*@Override
    public void export(Writer w, TaxaBlock taxa, TreesBlock trees) throws IOException {

    }

    @Override
    public List<String> getExtensions() {
        return null;
    }*/

    public enum CharactersOutputType {cell, matrix, both}

    private CharactersOutputType charactersOutputType = CharactersOutputType.matrix;

    private boolean exportSingleBlock = true;

    public void export(Writer w, List<ADataBlock> blocks) throws IOException, XMLStreamException {
        XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter =
                xMLOutputFactory.createXMLStreamWriter(w);

        xmlWriter.writeStartDocument();
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeStartElement("nex:nexml");
        xmlWriter.writeAttribute("version", "0.9");
        xmlWriter.writeCharacters("\n\t");

        TaxaBlock topTaxa = null;
        for (ADataBlock block : blocks) {
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

    //########################################################################################
    //############################## for single block ########################################
    //########################################################################################
    public void export(Writer w, TaxaBlock taxa) throws IOException, XMLStreamException {

        XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter =
                xMLOutputFactory.createXMLStreamWriter(w);

        xmlWriter.writeStartDocument();
        xmlWriter.writeCharacters("\n");
        //xmlWriter.writeStartElement("nex:nexml");
        //xmlWriter.writeAttribute("version", "0.9");
        //xmlWriter.writeCharacters("\n\t");

        //xmlWriter.writeStartDocument();
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
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeEndElement();
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeEndDocument();

        xmlWriter.flush();
    }

    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException, XMLStreamException {
        XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter xmlWriter =
                xMLOutputFactory.createXMLStreamWriter(w);

        xmlWriter.writeCharacters("\n");
        xmlWriter.writeStartElement("characters");
        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeStartElement("matrix");

        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();

        for (int i = 1; i <= ntax; i++) {
            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeStartElement("row");
            xmlWriter.writeCharacters("\n\t\t\t");
            xmlWriter.writeStartElement("seq");
            xmlWriter.writeAttribute("label", taxa.getLabel(i));
            for (int j = 1; j <= nchar; j++) {
                xmlWriter.writeCharacters((characters.get(i, j) + "").toUpperCase());
            }
            xmlWriter.writeEndElement(); // seq
            xmlWriter.writeCharacters("\n\t\t");
            xmlWriter.writeEndElement(); //row
        }
        xmlWriter.writeCharacters("\n\t");
        xmlWriter.writeEndElement(); // matrix
        xmlWriter.writeCharacters("\n");
        xmlWriter.writeEndElement(); //characters
    }
}
