package splitstree5.io.imports.NeXML;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToCharacters;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.CharactersFormat;
import splitstree5.io.imports.NeXML.handlers.NexmlCharactersHandler;
import splitstree5.io.imports.interfaces.IImportCharacters;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class NexmlCharactersIn extends CharactersFormat implements IToCharacters, IImportCharacters {

    // todo applicable : iterate through the file ; use setSymbols

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, CharactersBlock characters) throws CanceledException, IOException {
        try {
            // todo somehow use progressListener : progressListener. set(-1)

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
        return false;
    }
}
