package splitstree5.io.imports.NeXML.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.nexus.stateLabeler.StateLabeler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NexmlCharactersHandler extends DefaultHandler {

    // todo multiple symbols like (01), use Statelabeler?
    private boolean otu = false;
    private boolean bSeq = false;
    private boolean bCells = false;

    private ArrayList<String> taxaLabels = new ArrayList<>();
    private ArrayList<String> matrix = new ArrayList<>();
    private CharactersType dataType = CharactersType.Unknown;
    private StringBuilder tmp;
    private String currentStatesID;
    private int nchar = 0;

    private HashMap<String, Character> states2symbols;
    private ArrayList<String> uncertain_state_set_IDs;
    private ArrayList<String> polymorphic_state_set_IDs;

    private HashMap<String, HashMap<String, Character>> id2states;
    private HashMap<String, HashMap<String, Character>> column2state;

    private StateLabeler stateLabeler;
    private Map<Integer, String> charLabeler;

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
        else if (qName.equalsIgnoreCase("characters")) {
            String type = attributes.getValue("xsi:type");
            System.out.println(type);
            if (type.contains("Cells"))
                bCells = true;
            type = type.replaceAll("nex:", "");
            type = type.replaceAll("Seqs", "");
            type = type.replaceAll("Cells", "");
            dataType = CharactersType.valueOfIgnoreCase(type);
            System.out.println(dataType);
        } else if (qName.equalsIgnoreCase("format") && bCells) {
            column2state = new HashMap<>();
            id2states = new HashMap<>();
        } else if (qName.equalsIgnoreCase("states") && bCells) {
            states2symbols = new HashMap<>();
            uncertain_state_set_IDs = new ArrayList<>();
            polymorphic_state_set_IDs = new ArrayList<>();
            currentStatesID = attributes.getValue("id");
            //column2state.keySet().add(id);
            /*switch (dataType) {
                case protein:
                    stateLabeler = new ProteinStateLabeler();
                    break;
                case microsat:
                    stateLabeler = new MicrostatStateLabeler();
                    break;
                default:
                case unknown:
                    stateLabeler = new StandardStateLabeler()
                    break;
            }*/
        } else if ((qName.equalsIgnoreCase("state") ||
                qName.equalsIgnoreCase("uncertain_state_set") ||
                qName.equalsIgnoreCase("polymorphic_state_set")) && bCells) {
            String id = attributes.getValue("id");
            Character symbol = attributes.getValue("symbol").charAt(0);
            states2symbols.put(id, symbol);
            if (qName.equalsIgnoreCase("uncertain_state_set"))
                uncertain_state_set_IDs.add(id);
            if (qName.equalsIgnoreCase("polymorphic_state_set"))
                polymorphic_state_set_IDs.add(id);
        } else if (qName.equalsIgnoreCase("char") && bCells) {
            String id = attributes.getValue("id");
            String states = attributes.getValue("states");
            column2state.put(id, id2states.get(states));
        } else if (qName.equalsIgnoreCase("row")) {
            tmp = new StringBuilder();
        } else if (qName.equalsIgnoreCase("cell") && bCells) {
            String column = attributes.getValue("char");
            String state = attributes.getValue("state");
            tmp.append(column2state.get(column).get(state));
            //tmp.append(states2symbols.get(state));
        } else if (qName.equalsIgnoreCase("seq")) {
            bSeq = true;
            tmp = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri,
                           String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("otus")) {
            System.out.println("End Element :" + qName);
        } else if (qName.equalsIgnoreCase("states") && bCells) {
            id2states.put(currentStatesID, states2symbols);
        } else if (qName.equalsIgnoreCase("row") && bCells) {
            matrix.add(tmp.toString());
            System.out.println(tmp);
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

    //////////// GETTERS ///////////////

    public ArrayList<String> getTaxaLabels() {
        return this.taxaLabels;
    }

    public char[][] getMatrix() throws IOException {
        int ntax, nchar;
        if (matrix.size() == 0)
            throw new IOException("No Sequences was found");
        ntax = matrix.size();
        nchar = matrix.get(0).length();
        char[][] charMatrix = new char[ntax][nchar];

        System.err.println("Matrix");
        for (int i = 0; i < ntax; i++) {
            System.err.println();
            for (int j = 0; j < nchar; j++) {
                // todo : check if char corresponds to the datatype
                charMatrix[i][j] = matrix.get(i).charAt(j);
                System.err.print(charMatrix[i][j]);
            }
        }
        return charMatrix;
    }

    public CharactersType getDataType() {
        return this.dataType;
    }

    public HashMap<String, Character> getStates2symbols() {
        return this.states2symbols;
    }

    public ArrayList<String> getPolymorphic_state_set_IDs() {
        return this.polymorphic_state_set_IDs;
    }

    public ArrayList<String> getUncertain_state_set_IDs() {
        return this.uncertain_state_set_IDs;
    }
}
