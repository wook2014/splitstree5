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
 * Import Characters in FastA format.
 * Daria Evseeva, 07.2017
 */
public class FastaIn extends CharactersFormat implements IToCharacters, IImportCharacters {
    private static final String[] possibleIDs = {"gb", "emb", "ena", "dbj", "pir", "prf", "sp", "pdb", "pat", "bbs", "gnl", "ref", "lcl"};

    /**
     * parse a file
     *
     * @param progressListener
     * @param inputFile
     * @param taxa
     * @param characters
     * @throws IOException
     */
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, CharactersBlock characters) throws IOException, CanceledException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        final ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;
        int counter = 0;

        try (FileInputIterator it = new FileInputIterator(inputFile)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            int currentSequenceLength = 0;
            StringBuilder currentSequence = new StringBuilder("");

            while (it.hasNext()) {
                final String line = it.next();

                counter++;
                if (line.startsWith(";") || line.isEmpty())
                    continue;
                if (line.equals(">"))
                    throw new IOExceptionWithLineNumber("No taxa label given at line: " + counter, counter);

                if (line.startsWith(">")) {
                    addTaxaName(line, taxonNamesFound, counter);
                    ntax++;

                    if (ntax > 1 && currentSequence.toString().isEmpty())
                        throw new IOExceptionWithLineNumber("No sequence is given at line " + counter, counter);

                    if (nchar != 0 && nchar != currentSequenceLength)
                        throw new IOExceptionWithLineNumber("Sequences must be the same length. " +
                                "Wrong number of chars at the line: " + (counter - 1) + ". Length " + nchar + " expected", counter - 1);

                    if (!currentSequence.toString().equals("")) matrix.add(currentSequence.toString());
                    nchar = currentSequenceLength;
                    currentSequenceLength = 0;
                    currentSequence = new StringBuilder("");
                } else {
                    String allowedChars = "" + getMissing() + getMatchChar() + getGap();
                    checkIfCharactersValid(line, counter, allowedChars);
                    String add = line.replaceAll("\\s+", "");
                    currentSequenceLength += add.length();
                    currentSequence.append(add);
                }
                progressListener.setProgress(it.getProgress());
            }

            if (currentSequence.length() == 0)
                throw new IOExceptionWithLineNumber("Line: "+counter+": Sequence " + ntax + " is zero", counter);
            matrix.add(currentSequence.toString());
            if (nchar != currentSequenceLength)
                throw new IOExceptionWithLineNumber("Wrong number of chars at the line: " + counter + ". Length " + nchar + " expected", counter);

        }
        /*System.err.println("ntax: " + ntax + " nchar: " + nchar);
        for (String s : matrix) {
            System.err.println(s);
        }
        for (String s : taxonNamesFound) {
            System.err.println(s);
        }*/

        taxa.addTaxaByNames(taxonNamesFound);
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

    /*private static void addTaxaName(String infoLine, ArrayList<String> taxonNamesFound) throws IOException {

        if (infoLine.contains("[organism=")) {
            int index1 = infoLine.indexOf("[organism=") + 10;
            int index2 = infoLine.indexOf(']');

            // todo test whole line or only taxon name???
            if (taxonNamesFound.contains(infoLine.substring(index1, index2))) {
                throw new IOException("Double taxon name: " + infoLine.substring(index1, index2));
            }
            taxonNamesFound.add(infoLine.substring(index1, index2));
            return;
        }

        // check SeqID
        // todo ? boolean doubleID; first ID

        // todo option make names unique

        infoLine = infoLine.toLowerCase();
        String foundID = "";
        for (String id : possibleIDs) {
            if (infoLine.contains(">" + id + "|") || infoLine.contains("|" + id + "|")) {
                foundID = id;
                break;
            }
        }

        if (!foundID.equals("")) {
            String afterID = infoLine.substring(infoLine.indexOf(foundID) + foundID.length());
            int index1 = afterID.indexOf('|') + 1;
            int index2 = afterID.substring(index1 + 1).indexOf('|') + 2;

            if (taxonNamesFound.contains(afterID.substring(index1, index2))) {
                throw new IOException("Double taxon name: " + afterID.substring(index1, index2));
            }
            System.err.println("name-" + afterID.substring(index1, index2).toUpperCase());
            taxonNamesFound.add(afterID.substring(index1, index2).toUpperCase());
            return;
        }


        if (taxonNamesFound.contains(infoLine.substring(1))) {
            throw new IOException("Double taxon name: " + infoLine.substring(1));
        }
        taxonNamesFound.add(infoLine.substring(1));
    }*/

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("fasta", "fas", "fa", "seq", "fsa", "fna");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        String line = Basic.getFirstLineFromFileIgnoreEmptyLines(new File(fileName), ";", 1000);
        return line != null && line.startsWith(">");
    }
}
