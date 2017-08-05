package splitstree5.io.otherFormats;

/**
 * Created by Daria on 05.08.2017.
 */

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

/**
 * The sequence alignment outputs from CLUSTAL software often are given the default extension .aln.
 * CLUSTAL is an interleaved format. In a page-wide arrangement the
 * sequence name is in the first column and a part of the sequence’s data is right justified.
 *
 * http://www.clustal.org/download/clustalw_help.txt
 */

public class CharactersClustalIO {

    public static void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        //ArrayList<String> taxonNamesFound = new ArrayList<>();
        //ArrayList<String> matrix = new ArrayList<>();

        Hashtable<String, String> taxa2seq = new Hashtable<>();

        int ntax = 0;
        int nchar = 0;

        int counter = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            counter ++;
            String line;
            int sequenceLength = 0;
            String sequence = "";
            boolean startedNewSequence = false;


            while ((line = in.readLine()) != null) {

                if (line.startsWith("CLUSTAL"))
                    continue;
                if(!line.equals("") /* todo test conservation string here*/){
                    String label = line.substring(0, line.indexOf(' '));
                    if (!taxa2seq.contains(label))
                        taxa2seq.put(label, line.substring(line.indexOf(' ')));
                    //else
                      //  taxa2seq.put(label, taxa2seq.get(label)+line.substring(line.indexOf(' ')));
                }
            }
        }
        /*System.err.println("ntax: "+ntax+" nchar: "+nchar);
        for(String s : matrix){
            System.err.println(s);
        }*/
        for(String s : taxa2seq.keySet()){
            System.err.println(s);
            System.err.println(taxa2seq.get(s));
        }
        taxa.addTaxaByNames(taxa2seq.keySet());
        /*characters.clear();
        characters.setDimension(ntax, nchar);
        readMatrix(matrix, characters);*/
    }
}
