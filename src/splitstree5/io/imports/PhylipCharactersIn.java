package splitstree5.io.imports;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Daria Evseeva,27.09.2017.
 */
public class PhylipCharactersIn extends CharactersFormat {

    public void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> sequences = new ArrayList<>();

        int ntax = 0;
        int nchar = 0;
        int counter = 0;
        int readLines = 0;
        boolean standard = true;
        boolean sameLengthNtax = true;

        boolean readDim = true;

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            String line;

            while ((line = in.readLine()) != null) {

                counter++;
                if (line.equals("")) continue;

                if (readDim) {
                    int separateIndex = 0;
                    boolean betweenNumbers = false;
                    for (char c : line.toCharArray()) {
                        separateIndex++;
                        if (Character.isDigit(c)) betweenNumbers = true;
                        if (c == ' ' && betweenNumbers) break;
                    }
                    ntax = Integer.parseInt(line.substring(0, separateIndex).replace(" ", ""));
                    nchar = Integer.parseInt(line.substring(separateIndex).replace(" ", ""));
                    readDim = false;
                } else {
                    readLines++;
                    labels.add(line.substring(0, 10).replace(" ", ""));
                    sequences.add(line.substring(10).replace(" ", ""));
                    if (readLines == ntax) {
                        int seqLength = sequences.get(0).length();
                        for (String seq : sequences) {
                            if (seq.length() != seqLength) {
                                standard = false;
                                sameLengthNtax = false;
                                break;
                            }
                        }
                        if (!sameLengthNtax || seqLength != nchar) standard = false;
                    }
                    if (readLines > ntax && standard)
                        throw new IOException("Unexpected symbol at the line " + counter);
                }
            }
        }

        if (sequences.isEmpty())
            throw new IOException("No sequences were found");

        if (standard)
            setCharactersStandard(labels, sequences, ntax, nchar, taxa, characters);
        else if (sameLengthNtax) {
            setCharactersInterleaved(labels, sequences, ntax, nchar, taxa, characters);
            format.setInterleave(true);
            format.setColumnsPerBlock(sequences.get(0).length());
        } else
            setCharactersStandardEOL(labels, sequences, ntax, nchar, taxa, characters);

        //ntax = taxa2seq.size();
        //nchar = taxa2seq.get(taxa2seq.keySet().iterator().next()).length();
        /*for(String s : taxa2seq.keySet()){
            if(nchar != taxa2seq.get(s).length())
                throw new IOException("Sequences must be the same length." +
                        "Wrong number of chars at the sequence " + s);
            else
                nchar =  taxa2seq.get(s).length();
        }*/
    }

    private void setCharactersStandard(ArrayList<String> labels, ArrayList<String> sequences,
                                              int ntax, int nchar, TaxaBlock taxa, CharactersBlock characters) throws IOException {
        taxa.clear();
        taxa.addTaxaByNames(labels);
        characters.clear();
        characters.setDimension(ntax, nchar);

        int labelsCounter = 1;
        StringBuilder foundSymbols = new StringBuilder("");
        Map<Character, Integer> frequency = new LinkedHashMap<>();

        for (String seq : sequences) {
            for (int j = 1; j <= nchar; j++) {

                char symbol = Character.toLowerCase(seq.charAt(j - 1));
                if (foundSymbols.toString().indexOf(symbol) == -1) {
                    foundSymbols.append(symbol);
                    frequency.put(symbol, 1);
                } else
                    frequency.put(symbol, frequency.get(symbol) + 1);

                characters.set(labelsCounter, j, Character.toUpperCase(symbol));
            }
            labelsCounter++;
        }
        estimateDataType(foundSymbols.toString(), characters, frequency);

    }

    private void setCharactersInterleaved(ArrayList<String> labels, ArrayList<String> sequences,
                                                 int ntax, int nchar, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        List<String> taxaNames = labels.subList(0, ntax);

        taxa.clear();
        taxa.addTaxaByNames(taxaNames);
        characters.clear();
        characters.setDimension(ntax, nchar);

        boolean multi = true;
        for (int i = 0; i < ntax; i++) {
            for (int j = ntax + i; j < labels.size(); j = j + ntax) {
                if (!labels.get(i).equals(labels.get(j))) {
                    multi = false;
                    break;
                }
            }
            if (!multi) break;
        }
        if (!multi)
            for (int i = ntax; i < labels.size(); i++) {
                sequences.set(i, labels.get(i) + sequences.get(i));
            }

        StringBuilder foundSymbols = new StringBuilder("");
        Map<Character, Integer> frequency = new LinkedHashMap<>();
        int seqLength = 0;
        for (int i = 0; i < sequences.size(); i++) {
            for (int j = 1; j <= sequences.get(i).length(); j++) {
                char symbol = Character.toLowerCase(sequences.get(i).charAt(j - 1));
                if (foundSymbols.toString().indexOf(symbol) == -1) {
                    foundSymbols.append(symbol);
                    frequency.put(symbol, 1);
                } else
                    frequency.put(symbol, frequency.get(symbol) + 1);
                int taxaIndex = (i % ntax) + 1;
                /*for(int seq=taxaIndex; seq<i; seq=seq+ntax)
                    charIndex+=sequences.get(seq).length();
                System.err.println("charIndex " +charIndex);
                System.err.println("charIndex i " +i);
                System.err.println("charIndex j " +j);*/
                /*while (characters.get(taxaIndex, charIndex) != '\u0000')
                    charIndex+=sequences.get(taxaIndex).length();*/ // todo only if blocks have the same length
                characters.set(taxaIndex, j + seqLength, Character.toUpperCase(symbol));
            }
            if (i % ntax == ntax - 1)
                seqLength = +sequences.get(i).length();
        }
        estimateDataType(foundSymbols.toString(), characters, frequency);
    }

    private void setCharactersStandardEOL(ArrayList<String> labels, ArrayList<String> sequences,
                                                 int ntax, int nchar, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        List<String> taxaNames = new ArrayList<>();
        characters.clear();
        characters.setDimension(ntax, nchar);

        StringBuilder foundSymbols = new StringBuilder("");
        int iterator = 0;
        Map<Character, Integer> frequency = new LinkedHashMap<>();
        while (iterator < labels.size()) {
            taxaNames.add(labels.get(iterator));
            int shift = 0;
            for (int j = 0; j < nchar; j++) {
                char symbol;
                if (j - shift == sequences.get(iterator).length()) {
                    iterator++;
                    sequences.set(iterator, labels.get(iterator) + sequences.get(iterator));
                    shift += j;
                }
                symbol = Character.toLowerCase(sequences.get(iterator).charAt(j - shift));
                if (foundSymbols.toString().indexOf(symbol) == -1) {
                    foundSymbols.append(symbol);
                    frequency.put(symbol, 1);
                } else
                    frequency.put(symbol, frequency.get(symbol) + 1);

                characters.set(taxaNames.size(), j + 1, Character.toUpperCase(symbol));
            }
            iterator++;
        }

        estimateDataType(foundSymbols.toString(), characters, frequency);
        taxa.clear();
        taxa.addTaxaByNames(taxaNames);
    }
}

