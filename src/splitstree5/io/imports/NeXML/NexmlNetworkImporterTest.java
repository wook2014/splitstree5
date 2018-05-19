package splitstree5.io.imports.NeXML;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.NetworkNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.StringWriter;

public class NexmlNetworkImporterTest {

    private NexmlNetworkImporter nexmlNetworkImporter = new NexmlNetworkImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        NetworkBlock networkBlock = new NetworkBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlNetworkImporter.parse(pl, "test/neXML/trees.xml", taxaBlock, networkBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new NetworkNexusOutput().write(w1, taxaBlock, networkBlock);
        System.err.println(w1.toString());

    }

}