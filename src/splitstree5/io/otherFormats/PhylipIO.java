package splitstree5.io.otherFormats;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Daria on 27.09.2017.
 */
public class PhylipIO  extends CharactersFormat {

    public static void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {

        Map<String, String> taxa2seq = new LinkedHashMap<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();

        int ntax = 0;
        int nchar = 0;
        int sequenceInLineLength = 0;
        int counter = 0;
        int readLines = 0;
        boolean standard = true;

        boolean readDim = true;

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            String line;
            String sequence = "";
            boolean startedNewSequence = false;

            while ((line = in.readLine()) != null) {

                counter ++;
                if (line.equals("")) continue;

                if(readDim){
                    int separateIndex = 0;

                    boolean betweenNumbers = false;
                    for( char c : line.toCharArray()){
                        separateIndex ++;
                        if(Character.isDigit(c)) betweenNumbers = true;
                        if(c == ' ' && betweenNumbers) break;
                    }
                    ntax = Integer.parseInt(line.substring(0,separateIndex).replace(" ", ""));
                    nchar = Integer.parseInt(line.substring(separateIndex).replace(" ", ""));
                    readDim = false;
                } else {
                    readLines ++;
                    //taxa2seq.put(line.substring(0, 10).replace(" ",""), line.substring(10).replace(" ", ""));
                    labels.add(line.substring(0, 10).replace(" ",""));
                    sequences.add(line.substring(10).replace(" ", ""));
                    if(readLines == ntax){
                        System.err.print("-----"+labels.get(labels.size()-1));
                        for(String seq : sequences){
                            if(seq.length() != nchar){
                                standard = false;
                                break;
                            }
                        }
                    }
                    if(readLines>ntax && standard)
                        throw new IOException("Unexpected symbol at the line "+counter);
                }
            }
        }

        if(standard)
            setCharactersStandard(labels, sequences, ntax, nchar, taxa, characters);
        else
            setCharactersInterleaved(labels, sequences, ntax, nchar, taxa, characters);
        //if(taxa2seq.isEmpty())
          //  throw new IOException("No sequences were found");

        //determineFormat(taxa2seq, ntax, nchar);

        //ntax = taxa2seq.size();
        //nchar = taxa2seq.get(taxa2seq.keySet().iterator().next()).length();
        /*for(String s : taxa2seq.keySet()){
            if(nchar != taxa2seq.get(s).length())
                throw new IOException("Sequences must be the same length." +
                        "Wrong number of chars at the sequence " + s);
            else
                nchar =  taxa2seq.get(s).length();
        }*/

        System.err.println("ntax: "+ntax+" nchar: "+nchar);
        for(String s : taxa2seq.keySet()){
            System.err.println(s);
            System.err.println(taxa2seq.get(s));
        }

        //taxa.clear();
        //taxa.addTaxaByNames(taxa2seq.keySet());
        //setCharacters(taxa2seq, ntax, nchar, characters);

        //format.setInterleave(true);
        //format.setColumnsPerBlock(sequenceInLineLength);
    }

    private static void setCharactersStandard(ArrayList<String> labels, ArrayList<String> sequences,
                                              int ntax, int nchar, TaxaBlock taxa, CharactersBlock characters) throws IOException {
        taxa.clear();
        taxa.addTaxaByNames(labels);
        characters.clear();
        characters.setDimension(ntax, nchar);

        int labelsCounter = 1;
        String foundSymbols = "";
        Map<String, String> symbolsCounter = new LinkedHashMap<>();

        for(String seq : sequences){
            for(int j=1; j<=nchar; j++){

                char symbol = seq.charAt(j-1);
                if(foundSymbols.indexOf(Character.toLowerCase(symbol)) == -1)
                    foundSymbols+=Character.toLowerCase(symbol);

                characters.set(labelsCounter, j, symbol);
            }
            labelsCounter++;
        }

        estimateDataType(foundSymbols, characters);

    }

    private static void setCharactersInterleaved(ArrayList<String> labels, ArrayList<String> sequences,
                                                 int ntax, int nchar, TaxaBlock taxa, CharactersBlock characters){
        taxa.clear();
        taxa.addTaxaByNames(labels);
        characters.clear();
        characters.setDimension(ntax, nchar);

        // todo test it!
        boolean multi = true;
        for(int i=0; i<ntax; i++){
            for(int j=ntax+i; j<labels.size(); j=j+ntax){
                if(!sequences.get(i).equals(sequences.get(j))){
                    multi = false;
                    break;
                }
            }
            if(!multi) break;
        }

        if(!multi){
            for(int i=ntax; i<labels.size(); i++){
                sequences.set(i, labels.get(i)+sequences.get(i));
            }
        }

    }
}

