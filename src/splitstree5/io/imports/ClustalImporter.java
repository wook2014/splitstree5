/*
 *  ClustalImporter.java Copyright (C) 2019 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package splitstree5.io.imports;

/**
 * Daria Evseeva,05.08.2017.
 */

import jloda.util.*;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.interfaces.IImportCharacters;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The sequence alignment outputs from CLUSTAL software often are given the default extension .aln.
 * CLUSTAL is an interleaved format. In a page-wide arrangement the
 * sequence name is in the first column and a part of the sequence’s data is right justified.
 * <p>
 * http://www.clustal.org/download/clustalw_help.txt
 */

public class ClustalImporter extends CharactersFormat implements IToCharacters, IImportCharacters {

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, CharactersBlock characters)
            throws CanceledException, IOException {
        final Map<String, String> taxa2seq = new LinkedHashMap<>();

        int ntax;
        int nchar;
        int sequenceInLineLength = 0;
        int counter = 0;
        try (FileInputIterator it = new FileInputIterator(fileName)) {

            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);

            while (it.hasNext()) {
                final String line = it.next();
                counter++;
                if (line.toUpperCase().startsWith("CLUSTAL"))
                    continue;
                if (!line.equals("") && hasAlphabeticalSymbols(line)) {

                    String tmpLine = line;

                    // cut numbers from the end
                    int lastSeqIndex = tmpLine.length();
                    while (Character.isDigit(tmpLine.charAt(lastSeqIndex - 1)))
                        lastSeqIndex--;
                    tmpLine = tmpLine.substring(0, lastSeqIndex);

                    String label = "";
                    int labelIndex = tmpLine.indexOf(' ');
                    if (labelIndex == -1 || labelIndex == 0)
                        throw new IOExceptionWithLineNumber("No taxa label is given", counter);
                    else
                        label = tmpLine.substring(0, labelIndex);

                    tmpLine = tmpLine.replaceAll("\\s+", "");

                    if (sequenceInLineLength == 0) sequenceInLineLength = tmpLine.substring(labelIndex).length();

                    String allowedChars = "" + getMissing() + getMatchChar() + getGap();
                    checkIfCharactersValid(tmpLine.substring(labelIndex), counter, allowedChars);
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
            throw new IOExceptionWithLineNumber("No sequences were found", counter);

        ntax = taxa2seq.size();
        nchar = taxa2seq.get(taxa2seq.keySet().iterator().next()).length();
        for (String s : taxa2seq.keySet()) {
            if (nchar != taxa2seq.get(s).length())
                throw new IOException("Sequences must be the same length." +
                        "Wrong number of chars in the sequence " + s);
            else
                nchar = taxa2seq.get(s).length();
        }

        /*System.err.println("ntax: " + ntax + " nchar: " + nchar);
        for (String s : taxa2seq.keySet()) {
            System.err.println(s);
            System.err.println(taxa2seq.get(s));
        }*/

        taxa.clear();
        taxa.addTaxaByNames(taxa2seq.keySet());
        setCharacters(taxa2seq, ntax, nchar, characters);

        /*format.setInterleave(true);
        format.setColumnsPerBlock(sequenceInLineLength);*/
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

                characters.set(labelsCounter, j, symbol);
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

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("aln", "clustal");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        String line = Basic.getFirstLineFromFile(new File(fileName));
        return line != null && line.toUpperCase().startsWith("CLUSTAL");
    }
}
