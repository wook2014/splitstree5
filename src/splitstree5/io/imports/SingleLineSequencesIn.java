package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportCharacters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SingleLineSequencesIn extends CharactersFormat implements IToCharacters, IImportCharacters {

    //public static final List<String> extensions = null;

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
                        throw new IOException("Sequences must be the same length. Wrong number of chars at the line " + counter);
                    ntax++;
                    taxonNames.add("Sequence " + ntax);
                    matrix.add(line);
                }
                progressListener.setProgress(it.getProgress());
            }
        }

        System.err.println("ntax: " + ntax + " nchar: " + nchar);
        for (String s : matrix) {
            System.err.println(s);
        }
        for (String s : taxonNames) {
            System.err.println(s);
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
        String line = Basic.getFirstLineFromFile(new File(fileName));
        String allowedChars = "" + getMissing() + getMatchChar() + getGap();
        if (line == null) return false;
        try {
            checkIfCharactersValid(line, 1, allowedChars);
        } catch (IOExceptionWithLineNumber exception) {
            return false;
        }
        return true;
    }

    /**
     * From ST4 (added "only-letters" check)
     * todo : can we import this data?
     *
     * @param input0
     * @return true, if can handle this import
     */
    public boolean isApplicable(Reader input0) throws IOException {
        BufferedReader input = new BufferedReader(input0);
        String aline;

        int length = -1;
        while ((aline = input.readLine()) != null) {
            if (aline.length() > 0 && !aline.startsWith("#")) {
                if (length == -1) {
                    length = aline.length();
                    if (aline.charAt(0) == '>')
                        return false;
                    if (aline.charAt(0) == '(')
                        return false;
                    for (int pos = 0; pos < aline.length(); pos++)
                        if (Character.isSpaceChar(aline.charAt(pos))
                                || !isAcceptable(aline.charAt(pos))) // todo throw an exception here?
                            return false;
                } else if (aline.length() != length)
                    return false;
            }
        }
        return length > 0;
    }

    private boolean isAcceptable(char c) {
        return Character.isLetter(c) || c == getGap() || c == getMissing() || c == getMatchChar();
    }
}
