package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusInput;

import java.io.*;

public class PhylipDistancesOutTest {

    private PhylipDistancesOut phylipDistancesOut = new PhylipDistancesOut();

    @Test
    public void export() throws Exception {


        File file1 = new File("test/exports/TEST_PhylipDist.aln");
        Writer writer1 = new BufferedWriter(new FileWriter(file1));
        File file2 = new File("test/exports/TEST_PhylipDistTri.aln");
        Writer writer2 = new BufferedWriter(new FileWriter(file2));

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distances = new DistancesBlock();
        taxa.addTaxaByNames(new DistancesNexusInput().parse(new NexusStreamParser(
                        new FileReader("test/distances/algaeCod.nex")),
                taxa, distances));

        phylipDistancesOut.export(writer1, taxa, distances);
        writer1.close();
        phylipDistancesOut.setOptionTriangular(true);
        phylipDistancesOut.export(writer2, taxa, distances);
        writer2.close();
    }

}