/*
 *  NexmlCharactersImporter.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.io.imports.NeXML;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.imports.CharactersFormat;
import splitstree5.io.imports.NeXML.handlers.NexmlCharactersHandler;
import splitstree5.io.imports.interfaces.IImportCharacters;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class NexmlCharactersImporter extends CharactersFormat implements IToCharacters, IImportCharacters {

    // todo applicable : iterate through the file ; use setSymbols

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, CharactersBlock characters) throws CanceledException, IOException {
        try {
            progressListener.setProgress(-1);

            File file = new File(fileName);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlCharactersHandler handler = new NexmlCharactersHandler();
            saxParser.parse(file, handler);
            taxaBlock.addTaxaByNames(handler.getTaxaLabels());
            //characters.set(handler.getMatrix());
            char[][] matrix = handler.getMatrix();
            characters.setDimension(matrix.length, matrix[0].length);
            for (int i = 1; i <= matrix.length; i++) {
                String line = String.valueOf(matrix[i]);
                checkIfCharactersValid(line, -1, "" + getMissing() + getMatchChar() + getGap());
                for (int j = 1; j <= matrix[0].length; j++) {
                    characters.set(i, j, matrix[i - 1][j - 1]);
                }
            }
            characters.setDataType(handler.getDataType());

            // add new characters
            if (characters.getDataType().equals(CharactersType.Standard)) {
                for (String state : handler.getStates2symbols().keySet()) {
                    Character symbol = handler.getStates2symbols().get(state);
                    if (!symbol.equals('0') && !symbol.equals('1')
                            && !symbol.equals(characters.getGapCharacter())
                            && !symbol.equals(characters.getMissingCharacter()))
                        characters.setSymbols(characters.getSymbols() + symbol);
                }
            }

            characters.setGapCharacter(getGap());
            characters.setMissingCharacter(getMissing());

            progressListener.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList("xml");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {

        String firstLine = Basic.getFirstLineFromFile(new File(fileName));
        if (firstLine == null || !firstLine.equals("<nex:nexml") && !firstLine.startsWith("<?xml version="))
            return false;

        try (BufferedReader ins =
                     new BufferedReader(new InputStreamReader(Basic.getInputStreamPossiblyZIPorGZIP(fileName)))) {
            String aLine;
            while ((aLine = ins.readLine()) != null) {
                if (aLine.contains("<characters"))
                    return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
