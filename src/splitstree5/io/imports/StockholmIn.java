package splitstree5.io.imports;

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

public class StockholmIn extends CharactersFormat implements IToCharacters, IImportCharacters {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("stk", "sto", "stockholm"));

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws CanceledException, IOException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        final ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;

        int counter = 0;
        try (FileInputIterator it = new FileInputIterator(fileName)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);

            counter++;
            int sequenceLength = 0;
            String sequence = "";
            boolean startedNewSequence = false;

            if (!it.next().equals("# STOCKHOLM 1.0"))
                throw new IOException("STOCKHOLM header expected");

            while (it.hasNext()) {

                final String line = it.next();
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

                if (nchar == 0)
                    nchar = seq.length();
                else if (nchar != seq.length())
                    throw new IOException("Sequences must be the same length. line: " + counter);
            }

            /*if (sequence.length() == 0)
                throw new IOException("Sequence " + ntax + " is zero");
            matrix.add(sequence);
            if (nchar != sequenceLength)
                throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence " + ntax);*/

        }
        System.err.println("ntax: " + ntax + " nchar: " + nchar);
        for (String s : matrix) {
            System.err.println(s);
        }
        for (String s : taxonNamesFound) {
            System.err.println(s);
        }

        taxa.addTaxaByNames(taxonNamesFound);
        characters.clear();
        characters.setDimension(ntax, nchar);
        readMatrix(matrix, characters);
    }

    @Override
    public List<String> getExtensions() {
        return null;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return false;
    }

    private void readMatrix(ArrayList<String> matrix, CharactersBlock characters) throws IOException {
        // todo check if valid and set parameters here

        Map<Character, Integer> frequency = new LinkedHashMap<>();
        StringBuilder foundSymbols = new StringBuilder("");
        for (int i = 1; i <= characters.getNtax(); i++) {
            for (int j = 1; j <= characters.getNchar(); j++) {
                char symbol = Character.toLowerCase(matrix.get(i - 1).charAt(j - 1));
                if (foundSymbols.toString().indexOf(symbol) == -1) {
                    foundSymbols.append(symbol);
                    frequency.put(symbol, 1);
                } else
                    frequency.put(symbol, frequency.get(symbol) + 1);
                characters.set(i, j, matrix.get(i - 1).charAt(j - 1));
            }
        }

        estimateDataType(foundSymbols.toString(), characters, frequency);
    }
}
