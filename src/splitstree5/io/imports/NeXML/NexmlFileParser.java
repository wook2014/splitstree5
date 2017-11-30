package splitstree5.io.imports.NeXML;

import splitstree5.core.datablocks.TaxaBlock;

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

    // TODO
    public void parseCharacters(String inputFile){
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTaxaHandler handler = new NexmlTaxaHandler();
            saxParser.parse(file, handler);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseTrees(String inputFile){
        try {
            File file = new File(inputFile);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTaxaHandler handler = new NexmlTaxaHandler();
            saxParser.parse(file, handler);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
