package splitstree5.io.imports.NeXML;

import jloda.phylo.PhyloTree;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.io.File;

import javax.xml.parsers.*;

public class NexmlFileParser {

    public void parseTaxa(String inputFile, TaxaBlock taxa){
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTaxaHandler handler = new NexmlTaxaHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseCharacters(String inputFile, TaxaBlock taxa, CharactersBlock characters){
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlCharactersHandler handler = new NexmlCharactersHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
            characters.set(handler.getMatrix());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseTrees(String inputFile, TaxaBlock taxa, TreesBlock trees){
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTreesHandler handler = new NexmlTreesHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
            for (PhyloTree t : handler.getTrees()){
                trees.getTrees().add(t);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
