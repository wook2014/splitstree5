package splitstree5.io.imports.NeXML;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import splitstree5.core.datablocks.characters.CharactersType;

import java.io.IOException;
import java.util.ArrayList;

public class NexmlCharactersHandler extends DefaultHandler {

    boolean otu = false;
    boolean bSeq = false;

    private ArrayList<String> taxaLabels = new ArrayList<>();
    private ArrayList<String> matrix = new ArrayList<>();
    private CharactersType dataType = CharactersType.unknown; // todo
    private StringBuilder tmp;

    @Override
    public void startElement(String uri,
                             String localName, String qName, Attributes attributes) throws SAXException {

        // TAXA INFO
        if (qName.equalsIgnoreCase("otus")) {
            String label = attributes.getValue("id");
            String id = attributes.getValue("label");
            //System.out.println("Label : " + label);
            //System.out.println("ID : " + id);
        } else if (qName.equalsIgnoreCase("otu")) {
            otu = true;
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
        // CHARACTERS INFO
        else if (qName.equalsIgnoreCase("characters")){
            String dataType = attributes.getValue("xsi:type");
            System.out.println(dataType);
        }else if (qName.equalsIgnoreCase("seq")) {
            bSeq = true;
            tmp = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("otus")) {
            System.out.println("End Element :" + qName);
        } else if (qName.equalsIgnoreCase("seq")) {
            matrix.add(tmp.toString());
            //System.out.println(tmp);
            bSeq = false;
        }
    }

    @Override
    public void characters(char[] buffer, int start, int length) throws SAXException {

        if (bSeq) {

            tmp.append(new String(buffer, start, length));

            //System.out.println("Found row : "
              //      + new String(ch, start, length));
            //matrix.add(new String(ch, start, length));
            //bSeq = false;
        }
    }

    public ArrayList<String> getTaxaLabels(){
        return this.taxaLabels;
    }

    public char[][] getMatrix() throws IOException {
        int ntax, nchar;
        if(matrix.size() == 0)
            throw new IOException("No Sequences was found");
        ntax = matrix.size();
        nchar = matrix.get(0).length();
        char[][] charMatrix = new char[ntax][nchar];

        for(int i=0; i<ntax; i++){
            for(int j=0; j<nchar; j++){
                // todo : check if char corresponds to the datatype
                charMatrix[i][j] = matrix.get(i).charAt(j);
            }
        }
        return charMatrix;
    }

}
