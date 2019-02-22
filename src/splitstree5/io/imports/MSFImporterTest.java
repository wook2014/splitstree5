package splitstree5.io.imports;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class MSFImporterTest {

    private MSFImporter msfImporter = new MSFImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        msfImporter.parse(pl, "test/notNexusFiles/journal.pone.0208606.s007.msf", taxaBlock, charactersBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
        System.err.println(w.toString());

    }
}