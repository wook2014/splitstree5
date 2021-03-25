/*
 * SingleLineSequencesImporter.java Copyright (C) 2020. Daniel H. Huson
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
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.imports.interfaces.IImportCharacters;
import splitstree5.io.imports.interfaces.IImportNoAutoDetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SingleLineSequencesImporter extends CharactersFormat implements IToCharacters, IImportCharacters, IImportNoAutoDetect {

    private static int numberOfLinesToCheckInApplicable = 10;

    @Override
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, CharactersBlock characters)
            throws CanceledException, IOException {
        final ArrayList<String> taxonNames = new ArrayList<>();
        final ArrayList<String> matrix = new ArrayList<>();
        int ntax = 0;
        int nchar = 0;
        int counter = 0;

        try (FileLineIterator it = new FileLineIterator(inputFile)) {

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
                characters.set(i, j, matrix.get(i - 1).charAt(j - 1));
            }
        }
        characters.setDataType(CharactersType.guessType(CharactersType.union(foundSymbols.toString())));
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
