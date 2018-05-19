package splitstree5.io.imports.NeXML;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.TaxaNexusOutput;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.TreesNexusOutput;

import java.io.StringWriter;

public class NexmlTreesImporterTest {

    private NexmlTreesImporter nexmlTreesImporter = new NexmlTreesImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlTreesImporter.parse(pl, "test/neXML/trees.xml", taxaBlock, treesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        TreesNexusFormat treesNexusFormat = new TreesNexusFormat();
        treesNexusFormat.setOptionTranslate(false);
        new TreesNexusOutput().write(w1, taxaBlock, treesBlock);
        System.err.println(w1.toString());
        System.err.println("Partial: " + treesBlock.isPartial());

    }

}