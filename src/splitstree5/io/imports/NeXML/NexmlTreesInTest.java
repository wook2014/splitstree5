package splitstree5.io.imports.NeXML;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.TreesNexusIO;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class NexmlTreesInTest {

    private NexmlTreesIn nexmlTreesIn = new NexmlTreesIn();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlTreesIn.parse(pl,"test/neXML/simple.xml", taxaBlock, treesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        TreesNexusFormat treesNexusFormat = new TreesNexusFormat();
        treesNexusFormat.setTranslate(false);
        TreesNexusIO.write(w1, taxaBlock, treesBlock, treesNexusFormat);
        System.err.println(w1.toString());

    }

}