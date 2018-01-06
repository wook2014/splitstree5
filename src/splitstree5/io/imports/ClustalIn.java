package splitstree5.io.imports;

/**
 * Created by Daria on 05.08.2017.
 */

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The sequence alignment outputs from CLUSTAL software often are given the default extension .aln.
 * CLUSTAL is an interleaved format. In a page-wide arrangement the
 * sequence name is in the first column and a part of the sequence’s data is right justified.
 * <p>
 * http://www.clustal.org/download/clustalw_help.txt
 */

public class ClustalIn extends CharactersFormat {

    public void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {

        Map<String, String> taxa2seq = new LinkedHashMap<>();

        int ntax = 0;
        int nchar = 0;
        int sequenceInLineLength = 0;
        int counter = 0;
        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            String line;
            String sequence = "";
            boolean startedNewSequence = false;

            while ((line = in.readLine()) != null) {
                counter++;
                if (line.startsWith("CLUSTAL"))
                    continue;
                if (!line.equals("") && hasAlphabeticalSymbols(line)) {

                    int lastSeqIndex = line.length();
                    while (Character.isDigit(line.charAt(lastSeqIndex - 1)))
                        lastSeqIndex--;
                    line = line.substring(0, lastSeqIndex);

                    int labelIndex = line.indexOf(' ');
                    String label = line.substring(0, labelIndex);

                    line = line.replace(" ", "");
                    line = line.replace("\t", "");

                    if (sequenceInLineLength == 0) sequenceInLineLength = line.substring(labelIndex).length();

                    if (!taxa2seq.containsKey(label)) {
                        taxa2seq.put(label, line.substring(labelIndex));
                    } else {
                        taxa2seq.put(label, taxa2seq.get(label) + line.substring(labelIndex));
                    }
                }
            }
        }

        if (taxa2seq.isEmpty())
            throw new IOException("No sequences were found");

        ntax = taxa2seq.size();
        nchar = taxa2seq.get(taxa2seq.keySet().iterator().next()).length();
        for (String s : taxa2seq.keySet()) {
            if (nchar != taxa2seq.get(s).length())
                throw new IOException("Sequences must be the same length." +
                        "Wrong number of chars at the sequence " + s);
            else
                nchar = taxa2seq.get(s).length();
        }

        System.err.println("ntax: " + ntax + " nchar: " + nchar);
        for (String s : taxa2seq.keySet()) {
            System.err.println(s);
            System.err.println(taxa2seq.get(s));
        }

        taxa.clear();
        taxa.addTaxaByNames(taxa2seq.keySet());
        setCharacters(taxa2seq, ntax, nchar, characters);

        format.setInterleave(true);
        format.setColumnsPerBlock(sequenceInLineLength);
    }

    private static void setCharacters(Map<String, String> taxa2seq, int ntax, int nchar, CharactersBlock characters) throws IOException {
        characters.clear();
        characters.setDimension(ntax, nchar);

        int labelsCounter = 1;
        StringBuilder foundSymbols = new StringBuilder("");
        Map<Character, Integer> frequency = new LinkedHashMap<>();

        for (String label : taxa2seq.keySet()) {
            for (int j = 1; j <= nchar; j++) {

                char symbol = Character.toLowerCase(taxa2seq.get(label).charAt(j - 1));
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

    private static boolean hasAlphabeticalSymbols(String line) {
        for (char c : line.toCharArray()) {
            if (Character.isLetter(c)) return true;
        }
        return false;
    }
}
