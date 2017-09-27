package splitstree5.io.otherFormats;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Daria on 27.09.2017.
 */
public class PhylipIO  extends CharactersFormat {

    public static void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {

        Map<String, String> taxa2seq = new LinkedHashMap<>();

        int ntax = 0;
        int nchar = 0;
        int sequenceInLineLength = 0;
        int counter = 0;

        boolean started = true;

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int readLines = 0;
            String sequence = "";
            boolean startedNewSequence = false;

            while ((line = in.readLine()) != null) {
                counter ++;
                if (line.equals(""))
                    continue;

                if(started){
                    int separateIndex = 0;

                    boolean betweenNumbers = false;
                    for( char c : line.toCharArray()){
                        separateIndex ++;
                        if(Character.isDigit(c)) betweenNumbers = true;
                        if(c == ' ' && betweenNumbers) break;
                    }
                    ntax = Integer.parseInt(line.substring(0,separateIndex).replace(" ", ""));
                    nchar = Integer.parseInt(line.substring(separateIndex).replace(" ", ""));
                    started = false;
                } else {
                    readLines ++;
                    taxa2seq.put(line.substring(0, 10).replace(" ",""), line.substring(10).replace(" ", ""));
                }
            }
        }

        if(taxa2seq.isEmpty())
            throw new IOException("No sequences was found");

        determineFormat(taxa2seq, ntax, nchar);

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

        taxa.clear();
        taxa.addTaxaByNames(taxa2seq.keySet());
        //setCharacters(taxa2seq, ntax, nchar, characters);

        format.setInterleave(true);
        format.setColumnsPerBlock(sequenceInLineLength);
    }


    private static void determineFormat(Map<String, String> taxa2seq, int ntax, int nchar){
        //if(taxa2seq.size() == ntax)


    }
}
