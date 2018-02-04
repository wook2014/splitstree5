package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,03.10.2017.
 */
public class PhylipDistancesInTest {

    private PhylipDistancesIn phylipDistancesIn = new PhylipDistancesIn();

    @org.junit.Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        ProgressListener pl = new ProgressPercentage();

        phylipDistancesIn.parse(pl, "test/notNexusFiles/square.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new DistancesNexusOutput().write(w1, taxaBlock, distancesBlock, null);
        System.err.println(w1.toString());
        String s1 = w1.toString();

        phylipDistancesIn.parse(pl, "test/notNexusFiles/triangular.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock);
        new DistancesNexusOutput().write(w2, taxaBlock, distancesBlock, null);
        System.err.println(w2.toString());
        String s2 = w2.toString();

        phylipDistancesIn.parse(pl, "test/notNexusFiles/triangularEOL.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        new TaxaNexusOutput().write(w3, taxaBlock);
        new DistancesNexusOutput().write(w3, taxaBlock, distancesBlock, null);
        System.err.println(w3.toString());

        phylipDistancesIn.parse(pl, "test/notNexusFiles/squareEOL-bf.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        new TaxaNexusOutput().write(w4, taxaBlock);
        new DistancesNexusOutput().write(w4, taxaBlock, distancesBlock, null);
        System.err.println(w4.toString());
    }

    @Test
    public void isApplicable() throws IOException {
        ArrayList<String> applicableFiles = new ArrayList<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (phylipDistancesIn.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, Arrays.asList("square.dist", "squareEOL-bf.dist", "triangular.dist", "triangularEOL.dist"));
    }

}