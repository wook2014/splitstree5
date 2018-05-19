package splitstree5.io.imports.NeXML;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.IOException;
import java.io.StringWriter;

public class NexmlCharactersImporterTest {

    private NexmlCharactersImporter nexmlCharactersIn = new NexmlCharactersImporter();

    @Test
    public void parseSeq() throws IOException, CanceledException {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlCharactersIn.parse(pl, "test/neXML/M4097_seq.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
        System.err.println(w1.toString());

    }

    @Test
    public void parseCells() throws IOException, CanceledException {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        nexmlCharactersIn.parse(pl, "test/neXML/M4311_cell.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
        System.err.println(w1.toString());
    }

}