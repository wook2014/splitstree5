package splitstree5.io.imports.NeXML;

import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToNetwork;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.NeXML.handlers.NexmlNetworkHandler;
import splitstree5.io.imports.interfaces.IImportNetwork;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class NexmlNetworkImporter implements IToNetwork, IImportNetwork {

    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, NetworkBlock dataBlock)
            throws CanceledException, IOException {

        try {
            progressListener.setProgress(-1);

            File file = new File(fileName);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlNetworkHandler handler = new NexmlNetworkHandler();
            dataBlock.clear();
            handler.setNetworkBlock(dataBlock);
            saxParser.parse(file, handler);
            taxaBlock.addTaxaByNames(handler.getTaxaLabels());

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
                if (aLine.contains("<network"))
                    return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
