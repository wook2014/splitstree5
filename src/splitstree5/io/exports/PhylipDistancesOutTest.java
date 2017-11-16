package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class PhylipDistancesOutTest {
    @Test
    public void export() throws Exception {


        File file = new File("test/exports/TEST_PhylipDist.aln");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distances = new DistancesBlock();
        taxa.addTaxaByNames(DistancesNexusIO.parse(new NexusStreamParser(
                new FileReader("test/distances/algaeCod.nex")),
                taxa, distances, null));

        PhylipDistancesOut.export(writer, taxa, distances);

    }

}