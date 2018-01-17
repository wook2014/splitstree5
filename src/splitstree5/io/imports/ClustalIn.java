package splitstree5.io.imports;

/**
 * Daria Evseeva,05.08.2017.
 */

import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportCharacters;
import splitstree5.io.nexus.CharactersNexusFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The sequence alignment outputs from CLUSTAL software often are given the default extension .aln.
 * CLUSTAL is an interleaved format. In a page-wide arrangement the
 * sequence name is in the first column and a part of the sequence’s data is right justified.
 * <p>
 * http://www.clustal.org/download/clustalw_help.txt
 */

public class ClustalIn extends CharactersFormat implements IToCharacters, IImportCharacters {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("aln", "clustal"));

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format)
            throws CanceledException, IOException
    {
        final Map<String, String> taxa2seq = new LinkedHashMap<>();

        int ntax = 0;
        int nchar = 0;
        int sequenceInLineLength = 0;
        int counter = 0;
        try (FileInputIterator it = new FileInputIterator(fileName)) {

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);

            String sequence = "";
            boolean startedNewSequence = false;

            while (it.hasNext()) {
                final String line = it.next();
                counter++;
                if (line.startsWith("CLUSTAL"))
                    continue;
                if (!line.equals("") && hasAlphabeticalSymbols(line)) {

                    String tmpLine = line;
                    int lastSeqIndex = tmpLine.length();
                    while (Character.isDigit(tmpLine.charAt(lastSeqIndex - 1)))
                        lastSeqIndex--;
                    tmpLine = tmpLine.substring(0, lastSeqIndex);

                    int labelIndex = tmpLine.indexOf(' ');
                    String label = tmpLine.substring(0, labelIndex);

                    tmpLine = tmpLine.replace(" ", "");
                    tmpLine = tmpLine.replace("\t", "");

                    if (sequenceInLineLength == 0) sequenceInLineLength = tmpLine.substring(labelIndex).length();

                    if (!taxa2seq.containsKey(label)) {
                        taxa2seq.put(label, tmpLine.substring(labelIndex));
                    } else {
                        taxa2seq.put(label, taxa2seq.get(label) + tmpLine.substring(labelIndex));
                    }
                }
                progressListener.setProgress(it.getProgress());
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

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return false;
    }

    private void setCharacters(Map<String, String> taxa2seq, int ntax, int nchar, CharactersBlock characters) throws IOException {
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
