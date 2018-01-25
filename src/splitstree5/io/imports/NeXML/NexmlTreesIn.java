package splitstree5.io.imports.NeXML;

import jloda.phylo.PhyloTree;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.NeXML.handlers.NexmlTreesHandler;
import splitstree5.io.imports.interfaces.IImportTrees;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class NexmlTreesIn implements IToTrees, IImportTrees {
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
        return Arrays.asList("xml");
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        return false;
    }
}
