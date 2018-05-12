package splitstree5.io.imports.NeXML;

import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.NeXML.handlers.NexmlTreesHandler;
import splitstree5.io.imports.interfaces.IImportTrees;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class NexmlTreesIn implements IToTrees, IImportTrees {

    // todo : check partial trees
    // network :nodedata add (id, ..), (label, ...), (taxalabel, ...)
    // output x,y coordinate as metadata, check in importer


    @Override
    public void parse(ProgressListener progressListener, String fileName, TaxaBlock taxa, TreesBlock trees) throws CanceledException, IOException {
        try {
            File file = new File(fileName);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            NexmlTreesHandler handler = new NexmlTreesHandler();
            saxParser.parse(file, handler);
            taxa.addTaxaByNames(handler.getTaxaLabels());
            for (PhyloTree t : handler.getTrees()) {
                trees.getTrees().add(t);
            }
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
                if (aLine.contains("<tree"))
                    return true;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
