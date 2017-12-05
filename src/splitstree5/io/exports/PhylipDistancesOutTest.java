package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

public class PhylipDistancesOutTest {
    @Test
    public void export() throws Exception {


        File file1 = new File("test/exports/TEST_PhylipDist.aln");
        Writer writer1 = new BufferedWriter(new FileWriter(file1));
        File file2 = new File("test/exports/TEST_PhylipDistTri.aln");
        Writer writer2 = new BufferedWriter(new FileWriter(file2));

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distances = new DistancesBlock();
        taxa.addTaxaByNames(DistancesNexusIO.parse(new NexusStreamParser(
                new FileReader("test/distances/algaeCod.nex")),
                taxa, distances, null));

        PhylipDistancesOut.export(writer1, taxa, distances);
        PhylipDistancesOut.setOptionTriangular(true);
        PhylipDistancesOut.export(writer2, taxa, distances);
    }

}