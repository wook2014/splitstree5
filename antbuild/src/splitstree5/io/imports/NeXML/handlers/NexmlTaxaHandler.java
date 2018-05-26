package splitstree5.io.imports.NeXML.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class NexmlTaxaHandler extends DefaultHandler {

    boolean otu = false;
    private ArrayList<String> taxaLabels = new ArrayList<>();

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("otus")) {
            String label = attributes.getValue("label");
            String id = attributes.getValue("id");
            System.out.println("Label : " + label);
            System.out.println("ID : " + id);
        } else if (qName.equalsIgnoreCase("otu")) {
            otu = true;
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
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("otus")) {
            System.out.println("End Element :" + qName);
        }
    }

    public ArrayList<String> getTaxaLabels() {
        return this.taxaLabels;
    }
}
