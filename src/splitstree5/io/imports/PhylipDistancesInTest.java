package splitstree5.io.imports;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

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

        phylipDistancesIn.parse(pl,"test/notNexusFiles/square.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        DistancesNexusIO.write(w1, taxaBlock, distancesBlock, null);
        System.err.println(w1.toString());
        String s1 = w1.toString();

        phylipDistancesIn.parse(pl,"test/notNexusFiles/triangular.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock);
        DistancesNexusIO.write(w2, taxaBlock, distancesBlock, null);
        System.err.println(w2.toString());
        String s2 = w2.toString();

        phylipDistancesIn.parse(pl,"test/notNexusFiles/triangularEOL.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        TaxaNexusIO.write(w3, taxaBlock);
        DistancesNexusIO.write(w3, taxaBlock, distancesBlock, null);
        System.err.println(w3.toString());

        // todo
        phylipDistancesIn.parse(pl,"test/notNexusFiles/squareEOL-bf.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        TaxaNexusIO.write(w4, taxaBlock);
        DistancesNexusIO.write(w4, taxaBlock, distancesBlock, null);
        System.err.println(w4.toString());
    }

}