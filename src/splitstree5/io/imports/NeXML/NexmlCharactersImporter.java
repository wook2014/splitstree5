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
import java.util.Objects;

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
                for (int j = 1; j <= matrix[0].length; j++) {
                    characters.set(i, j, matrix[i - 1][j - 1]);
                }
            }
            characters.setDataType(handler.getDataType());

            // add new characters
            if (characters.getDataType().equals(CharactersType.Standard)){
                for (String state : handler.getStates2symbols().keySet()){
                    Character symbol = handler.getStates2symbols().get(state);
                    if (!symbol.equals('0') && !symbol.equals('1')
                            && !symbol.equals(characters.getGapCharacter())
                            && !symbol.equals(characters.getMissingCharacter()))
                        characters.setSymbols(characters.getSymbols() + symbol);
                }
            }

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
