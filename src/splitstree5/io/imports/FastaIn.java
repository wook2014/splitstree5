package splitstree5.io.imports;

import jloda.util.Basic;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Import Characters in FastA format.
 * Daria Evseeva, 07.2017
 */
public class FastaIn extends CharactersFormat implements IToCharacters, IImportCharacters {

    private static final String[] possibleIDs =
            {"gb", "emb", "ena", "dbj", "pir", "prf", "sp", "pdb", "pat", "bbs", "gnl", "ref", "lcl"};

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("fasta", "fas", "fa", "seq", "fsa", "fna"));

    public void parse(String inputFile, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        ArrayList<String> taxonNamesFound = new ArrayList<>();
        ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;
        int counter = 0;

        try (BufferedReader in = new BufferedReader(Basic.getReaderPossiblyZIPorGZIP(inputFile))) {
            String line;
            int sequenceLength = 0;
            StringBuilder sequence = new StringBuilder("");
            boolean startedNewSequence = false;

            while ((line = in.readLine()) != null) {
                counter++;
                if (line.startsWith(";"))
                    continue;
                if (line.equals(">"))
                    throw new IOException("No taxa label given in line: " + counter);

                if (line.startsWith(">")) {
                    startedNewSequence = true;
                    addTaxaName(line, taxonNamesFound, counter);
                    if (taxonNamesFound.contains(line.substring(1))) {
                        System.err.println("");
                    }
                    ntax++;
                } else {
                    String allowedChars = ""+getMissing()+getMatchChar()+getGap();
                    checkIfCharactersValid(line, counter, allowedChars);
                    if (startedNewSequence) {
                        if (!sequence.toString().equals("")) matrix.add(sequence.toString());
                        if (nchar != 0 && nchar != sequenceLength) {
                            throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence "
                                    + (ntax - 1) + " in line: " + counter);
                        }
                        nchar = sequenceLength;
                        sequenceLength = 0;
                        sequence = new StringBuilder("");
                        startedNewSequence = false;
                    }
                    String add = line.replaceAll("\\s+", "");
                    sequenceLength += add.length();
                    sequence.append(line.replaceAll("\\s+", ""));
                }
            }

            if (sequence.length() == 0)
                throw new IOException("Sequence " + ntax + " is zero");
            matrix.add(sequence.toString());
            if (nchar != sequenceLength)
                throw new IOException("Sequences must be the same length. Wrong number of chars at the sequence " + ntax);

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
        return extensions;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        String line = Basic.getFirstLineFromFile(new File(fileName));
        return line != null && line.startsWith(">");
    }
}
