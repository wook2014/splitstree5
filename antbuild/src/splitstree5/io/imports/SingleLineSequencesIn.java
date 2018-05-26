package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportCharacters;
import splitstree5.io.imports.interfaces.IImportNoAutoDetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SingleLineSequencesIn extends CharactersFormat implements IToCharacters, IImportCharacters, IImportNoAutoDetect {

    private static int numberOfLinesToCheckInApplicable = 10;

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, CharactersBlock characters)
            throws CanceledException, IOException {
        final ArrayList<String> taxonNames = new ArrayList<>();
        final ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;
        int counter = 0;

        try (FileInputIterator it = new FileInputIterator(inputFile)) {

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);

            while (it.hasNext()) {
                final String line = it.next();
                counter++;
                if (line.length() > 0 && !line.startsWith("#")) {
                    if (nchar == 0)
                        nchar = line.length();
                    else if (nchar != line.length())
                        throw new IOExceptionWithLineNumber("Sequences must have the same length", counter);
                    ntax++;
                    taxonNames.add("Sequence " + ntax);
                    String allowedChars = "" + getMissing() + getMatchChar() + getGap();
                    checkIfCharactersValid(line, counter, allowedChars);
                    matrix.add(line);
                }
                progressListener.setProgress(it.getProgress());
            }
        }

        taxa.addTaxaByNames(taxonNames);
        characters.clear();
        characters.setDimension(ntax, nchar);
        readMatrix(matrix, characters);

    }

    private void readMatrix(ArrayList<String> matrix, CharactersBlock characters) throws IOException {

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

    @Override
    public List<String> getExtensions() {
        return null;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        String firstLine = Basic.getFirstLineFromFile(new File(fileName));
        int lineLength;
        if (firstLine == null)
            return false;
        else
            lineLength = firstLine.length();

        BufferedReader input = new BufferedReader(new FileReader(fileName));
        String line;
        int counter = 0;

        while ((line = input.readLine()) != null && counter <= numberOfLinesToCheckInApplicable) {
            counter++;
            if (line.equals(""))
                continue;
            if (lineLength != line.length() || !isLineAcceptable(line))
                return false;
        }
        return true;
    }

    private boolean isLineAcceptable(String s) {
        for (char c : s.toCharArray()) {
            if (!Character.isLetter(c) && c != getGap() && c != getMissing() && c != getMatchChar())
                return false;
        }
        return true;
    }
}