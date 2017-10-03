package splitstree5.io.otherFormats;

import org.junit.*;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * Created by Daria on 03.10.2017.
 */
public class PhylipDistancesIOTest {
    @org.junit.Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();

        PhylipDistancesIO.parse("test/notNexusFiles/square.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        DistancesNexusIO.write(w1,taxaBlock, distancesBlock, null);
        System.err.println(w1.toString());

        PhylipDistancesIO.parse("test/notNexusFiles/triangular.dist", taxaBlock, distancesBlock);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock);
        DistancesNexusIO.write(w2,taxaBlock, distancesBlock, null);
        System.err.println(w2.toString());
    }

}