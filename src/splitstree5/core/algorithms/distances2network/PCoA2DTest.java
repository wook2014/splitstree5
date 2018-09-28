package splitstree5.core.algorithms.distances2network;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusInput;
import splitstree5.io.nexus.NetworkNexusOutput;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class PCoA2DTest {

    private PCoA2D pCoA2D = new PCoA2D();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        NetworkBlock networkBlock = new NetworkBlock();
        ProgressListener pl = new ProgressPercentage();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new DistancesNexusInput().parse(np, taxa, distancesBlock);

        pCoA2D.compute(pl, taxa, distancesBlock, networkBlock);

        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxa);
        new NetworkNexusOutput().write(w1, taxa, networkBlock);
        System.err.println(w1.toString());
    }
}