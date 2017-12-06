package splitstree5.io.imports;

import org.junit.Test;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.NeXML.NexmlFileParser;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class MatlabInTest {

    private MatlabIn matlabIn = new MatlabIn();

    @Test
    public void parseTaxa() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        matlabIn.parseTaxa("test/notNexusFiles/algae.m", taxaBlock);

        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        System.err.println(w1.toString());

    }

}