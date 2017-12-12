package splitstree5.io.imports;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class StockholmIn extends CharactersFormat{

    public void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        ArrayList<String> taxonNamesFound = new ArrayList<>();
        ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;

        int counter = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            counter ++;
            String line;
            int sequenceLength = 0;
            String sequence = "";
            boolean startedNewSequence = false;

            if(!in.readLine().equals("# STOCKHOLM 1.0"))
                throw new IOException("STOCKHOLM header expected");

            while ((line = in.readLine()) != null) {
                counter++;

                if (line.startsWith("#"))
                    continue;
                if (line.equals("//"))
                    break;

                int labelIndex = line.indexOf(' '); //todo exceptions here
                String label = line.substring(0, labelIndex);
                String seq = line.substring(labelIndex).replaceAll(" ", "");

                taxonNamesFound.add(label);
                matrix.add(seq);
                ntax++;

                if(nchar == 0)
                    nchar = seq.length();
                else
                    if(nchar != seq.length())
                        throw new IOException("Sequences must be the same length. line: "+counter);
            }

            /*if (sequence.length() == 0)
                throw new IOException("Sequence " + ntax + " is zero");
            matrix.add(sequence);
            if (nchar != sequenceLength)
                throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence " + ntax);*/

        }
        System.err.println("ntax: "+ntax+" nchar: "+nchar);
        for(String s : matrix){
            System.err.println(s);
        }
        for(String s : taxonNamesFound){
            System.err.println(s);
        }

        taxa.addTaxaByNames(taxonNamesFound);
        characters.clear();
        characters.setDimension(ntax, nchar);
        readMatrix(matrix, characters);
    }

    private static void readMatrix(ArrayList<String> matrix, CharactersBlock characters) throws IOException {
        // todo check if valid and set parameters here

        System.err.println("ddddddddddddddddd");
        Map<Character, Integer> frequency = new LinkedHashMap<>();
        StringBuilder foundSymbols = new StringBuilder("");
        for(int i = 1; i<=characters.getNtax(); i++){
            for(int j = 1; j<=characters.getNchar(); j++){
                char symbol = Character.toLowerCase(matrix.get(i-1).charAt(j-1));
                if(foundSymbols.toString().indexOf(symbol) == -1) {
                    foundSymbols.append(symbol);
                    frequency.put(symbol, 1);
                } else
                    frequency.put(symbol, frequency.get(symbol)+1);
                characters.set(i, j, matrix.get(i-1).charAt(j-1));
            }
        }

        estimateDataType(foundSymbols.toString(), characters, frequency);
    }
}
