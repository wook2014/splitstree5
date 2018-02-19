package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportCharacters;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Daria Evseeva,27.09.2017.
 */
public class PhylipCharactersIn extends CharactersFormat implements IToCharacters, IImportCharacters {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("phy", "phylip"));

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, CharactersBlock characters)
            throws CanceledException, IOException {

        final ArrayList<String> labels = new ArrayList<>();
        final ArrayList<String> sequences = new ArrayList<>();

        int ntax = 0;
        int nchar = 0;
        int counter = 0;
        int readLines = 0;
        int interleavedBlockLinesLength=0;
        boolean standard = true;
        boolean sameLengthNtax = true;
        boolean readDimensions = true;

        try (FileInputIterator it = new FileInputIterator(fileName)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            while (it.hasNext()) {
                final String line = it.next();
                counter++;
                if (line.equals("")) continue;
                if (readDimensions) {
                    StringTokenizer tokens = new StringTokenizer(line);
                    ntax = Integer.parseInt(tokens.nextToken());
                    nchar = Integer.parseInt(tokens.nextToken());
                    readDimensions = false;
                } else {
                    readLines++;

                    if (line.length() <= 10)
                        throw new IOExceptionWithLineNumber("Line "+counter+" is shorter then 10 symbols. \n" +
                                "Phylip characters format : first 10 symbols = taxa label + sequence", counter);

                    // interleaved
                    if (readLines > ntax && sameLengthNtax) {
                        if (readLines % ntax == 1) interleavedBlockLinesLength = line.length();
                        else {
                            if (interleavedBlockLinesLength != 0 && line.length() != interleavedBlockLinesLength)
                                throw new IOExceptionWithLineNumber( "Error for interleaved format matrix." +
                                        "\nLine " + counter + " must have the same length as line "+ (counter-1), counter);
                        }
                    }

                    String allowedChars = "" + getMissing() + getMatchChar() + getGap();
                    checkIfCharactersValid(line.substring(10).replaceAll("\\s+", ""), counter, allowedChars);

                    labels.add(cutSpacesAtTheEnd(line.substring(0, 10)));
                    sequences.add(line.substring(10).replaceAll("\\s+", ""));

                    /*
                     After reading ntax of lines check :
                     1. all lines have the same length?
                     2. this length is nchar?
                     if 1 and 2 -> standard
                     if 1 and not 2 -> interleaved
                     else standard with EOLs
                     */
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

                    // no more lines are allowed for standard after reading ntax lines
                    if (readLines > ntax && standard)
                        throw new IOExceptionWithLineNumber("Unexpected symbol at the line " + counter +
                                "\nCan only read " + ntax + " lines " + nchar + " symbols long.", counter);
                }
                progressListener.setProgress(it.getProgress());
            }
        }

        if (sequences.isEmpty())
            throw new IOException("No sequences were found");

        if (standard)
            setCharactersStandard(labels, sequences, ntax, nchar, taxa, characters);
        else if (sameLengthNtax)
            setCharactersInterleaved(labels, sequences, ntax, nchar, taxa, characters);
        else
            setCharactersStandardEOL(labels, sequences, ntax, nchar, taxa, characters);

    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        String line = Basic.getFirstLineFromFile(new File(fileName));

        if (line == null) return false;
        StringTokenizer tokens = new StringTokenizer(line);
        if (tokens.countTokens() != 2) return false;

        while (tokens.hasMoreTokens())
            try {
                double d = Double.parseDouble(tokens.nextToken());
            } catch (NumberFormatException nfe) {
                return false;
            }
        return true;
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

    /**
     * If a sting has form : "Label   "
     * turn into "Label"
     *
     * @param s any string
     * @return string without spaces at the last positions
     */

    private static String cutSpacesAtTheEnd(String s){
        while (s.charAt(s.length()-1) == ' ')
            s = s.substring(0, s.length()-1);
        return s;
    }
}

