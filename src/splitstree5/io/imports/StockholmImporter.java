/*
 * StockholmImporter.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.imports;

import jloda.util.CanceledException;
import jloda.util.FileLineIterator;
import jloda.util.FileUtils;
import jloda.util.IOExceptionWithLineNumber;
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

public class StockholmImporter extends CharactersFormat implements IToCharacters, IImportCharacters {

    public static final List<String> extensions = new ArrayList<>(Arrays.asList("stk", "sto", "stockholm"));

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, CharactersBlock characters) throws CanceledException, IOException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        final ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;

        int counter = 0;
        try (FileLineIterator it = new FileLineIterator(fileName)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);

            counter++;

            if (!it.next().toUpperCase().startsWith("# STOCKHOLM"))
                throw new IOExceptionWithLineNumber("STOCKHOLM header expected", counter);

            while (it.hasNext()) {

                final String line = it.next();
                counter++;
                if (line.startsWith("#"))
                    continue;
                if (line.equals("//"))
                    break;

                int labelIndex = line.indexOf(' ');
                if (labelIndex == -1)
                    throw new IOExceptionWithLineNumber(" No separator between taxa and sequence is found!", counter);

                String label = line.substring(0, labelIndex);
                String seq = line.substring(labelIndex).replaceAll("\\s+", "");
                seq = seq.replaceAll("\\.", "-");

                taxonNamesFound.add(label);
                String allowedChars = "" + getMissing() + getMatchChar() + getGap();
                checkIfCharactersValid(seq, counter, allowedChars);
                matrix.add(seq);
                ntax++;

                if (nchar == 0)
                    nchar = seq.length();
                else if (nchar != seq.length())
                    throw new IOExceptionWithLineNumber("Sequences must be the same length." +
                            "Length " + nchar + " expected.", counter);

                progressListener.setProgress(it.getProgress());
            }
        }

        taxa.addTaxaByNames(taxonNamesFound);
        characters.setDimension(ntax, nchar);
        characters.setGapCharacter(getGap());
        characters.setMissingCharacter(getMissing());
        readMatrix(matrix, characters);
    }

    @Override
    public List<String> getExtensions() {
        return null;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
		String line = FileUtils.getFirstLineFromFile(new File(fileName));
        return line != null && line.replaceAll("\\s+", "").toUpperCase().startsWith("#STOCKHOLM");
    }

    private void readMatrix(ArrayList<String> matrix, CharactersBlock characters) throws IOException {
        StringBuilder foundSymbols = new StringBuilder("");
        for (int i = 1; i <= characters.getNtax(); i++) {
            for (int j = 1; j <= characters.getNchar(); j++) {
                char symbol = Character.toLowerCase(matrix.get(i - 1).charAt(j - 1));
                if (foundSymbols.toString().indexOf(symbol) == -1) {
                    foundSymbols.append(symbol);
                }
                characters.set(i, j, matrix.get(i - 1).charAt(j - 1));
            }
        }
        characters.setDataType(CharactersType.guessType(CharactersType.union(foundSymbols.toString())));
    }
}
