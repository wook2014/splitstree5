package splitstree5.io.imports.NeXML;

import jloda.phylo.PhyloTree;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.algorithms.interfaces.IToTaxa;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.NeXML.handlers.NexmlCharactersHandler;
import splitstree5.io.imports.NeXML.handlers.NexmlTaxaHandler;
import splitstree5.io.imports.NeXML.handlers.NexmlTreesHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

public class NexmlFileParser implements IToTaxa, IToCharacters, IToTrees {

    public void parse(String inputFile, TaxaBlock taxa) {
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTaxaHandler handler = new NexmlTaxaHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters) {
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlCharactersHandler handler = new NexmlCharactersHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
            //characters.set(handler.getMatrix());
            char[][] matrix = handler.getMatrix();
            characters.setDimension(matrix.length, matrix[0].length);
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[0].length; j++) {
                    characters.set(i + 1, j + 1, matrix[i][j]);
                }
            }
            characters.setDataType(handler.getDataType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse(String inputFile, TaxaBlock taxa, TreesBlock trees) {
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTreesHandler handler = new NexmlTreesHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
            for (PhyloTree t : handler.getTrees()) {
                trees.getTrees().add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
