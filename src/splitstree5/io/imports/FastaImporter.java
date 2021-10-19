/*
 * FastaImporter.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package splitstree5.io.imports;

import jloda.util.*;
import jloda.util.progress.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.imports.interfaces.IImportCharacters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Import Characters in FastA format.
 * Daria Evseeva, 07.2017
 */
public class FastaImporter extends CharactersFormat implements IToCharacters, IImportCharacters {
    private static final String[] possibleIDs =
            {"gb", "emb", "ena", "dbj", "pir", "prf", "sp", "pdb", "pat", "bbs", "gnl", "ref", "lcl"};

    private boolean optionFullLabels = false;
    private boolean optionPIRFormat = false;

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

        try (FileLineIterator it = new FileLineIterator(inputFile)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            int currentSequenceLength = 0;
            StringBuilder currentSequence = new StringBuilder("");
            boolean ignoreNext = false;

            while (it.hasNext()) {
                final String line = it.next();

                counter++;
                if (line.startsWith(";") || line.isEmpty())
                    continue;
                if (line.equals(">"))
                    throw new IOExceptionWithLineNumber("No taxa label given", counter);

                if (line.startsWith(">")) {
                    if (optionPIRFormat) ignoreNext = true;

                    if (optionFullLabels)
                        addTaxaName(line, taxonNamesFound, counter);
                    else
                        addTaxaName(cutLabel(line), taxonNamesFound, counter);

                    ntax++;

                    if (ntax > 1 && currentSequence.toString().isEmpty())
                        throw new IOExceptionWithLineNumber("No sequence", counter);

                    if (nchar != 0 && nchar != currentSequenceLength)
                        throw new IOExceptionWithLineNumber("Sequences must be the same length. " +
                                "Wrong number of chars, Length " + nchar + " expected", counter - 1);

                    if (!currentSequence.toString().equals("")) matrix.add(currentSequence.toString());
                    nchar = currentSequenceLength;
                    currentSequenceLength = 0;
                    currentSequence = new StringBuilder("");
                } else {
                    if (ignoreNext) {
                        ignoreNext = false;
                        continue;
                    }
                    String tmpLine;
                    if (optionPIRFormat && line.charAt(line.length() - 1) == '*')
                        tmpLine = line.substring(0, line.length() - 1); // cut the last symbol
                    else
                        tmpLine = line;
                    String allowedChars = "" + getMissing() + getMatchChar() + getGap();
                    checkIfCharactersValid(tmpLine, counter, allowedChars);
                    String add = tmpLine.replaceAll("\\s+", "");
                    currentSequenceLength += add.length();
                    currentSequence.append(add);
                }
                progressListener.setProgress(it.getProgress());
            }

            if (currentSequence.length() == 0)
                throw new IOExceptionWithLineNumber("Sequence " + ntax + " is zero", counter);
            matrix.add(currentSequence.toString());
            if (nchar != currentSequenceLength)
                throw new IOExceptionWithLineNumber("Wrong number of chars. Length " + nchar + " expected", counter);
        }

        taxa.addTaxaByNames(taxonNamesFound);
        characters.setDimension(ntax, nchar);
        characters.setGapCharacter(getGap());
        characters.setMissingCharacter(getMissing());
        readMatrix(matrix, characters);
    }

    private void readMatrix(ArrayList<String> matrix, CharactersBlock characters) throws IOException {
        StringBuilder foundSymbols = new StringBuilder("");
        for (int i = 1; i <= characters.getNtax(); i++) {
            for (int j = 1; j <= characters.getNchar(); j++) {
                char symbol = Character.toLowerCase(matrix.get(i - 1).charAt(j - 1));
                if (foundSymbols.toString().indexOf(symbol) == -1) {
                    foundSymbols.append(symbol);
                }
                characters.set(i, j, symbol);
            }
        }
        characters.setDataType(CharactersType.guessType(CharactersType.union(foundSymbols.toString())));
    }

    private static String cutLabel(String infoLine) {

        if (infoLine.contains("[organism=")) {
            int index1 = infoLine.indexOf("[organism=") + 10;
            int index2 = infoLine.indexOf(']');
            return ">" + infoLine.substring(index1, index2);
        }

        // check if the info line contains any of databases IDs
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
            int index1;
            int index2;
            if (foundID.equals("pir") || foundID.equals("prf") || foundID.equals("pat") || foundID.equals("gnl")) {
                if (foundID.equals("pir") || foundID.equals("prf"))
                    index1 = afterID.indexOf('|') + 2;
                else
                    index1 = afterID.indexOf('|') + 1;
                return ">" + afterID.substring(index1).toUpperCase();
            } else {
                index1 = afterID.indexOf('|') + 1;
                index2 = afterID.substring(index1 + 1).indexOf('|') + 2;
                return ">" + afterID.substring(index1, index2).toUpperCase();
            }
        }
        return ">" + infoLine.substring(1).toUpperCase();
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("fasta", "fas", "fa", "seq", "fsa", "fna");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
		String line = FileUtils.getFirstLineFromFileIgnoreEmptyLines(new File(fileName), ";", 20);
        return line != null && line.startsWith(">");
    }

    public boolean isOptionFullLabels() {
        return optionFullLabels;
    }

    public void setOptionFullLabels(boolean fullLabels) {
        this.optionFullLabels = fullLabels;
    }

    public boolean isOptionPIRFormat() {
        return optionPIRFormat;
    }

    public void setOptionPIRFormat(boolean pirFormat) {
        this.optionPIRFormat = pirFormat;
    }
}
