package splitstree5.io.imports.NeXML;

import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class NexmlCharactersInTest {

    private NexmlCharactersIn nexmlCharactersIn = new NexmlCharactersIn();

    @Test
    public void parseSeq() throws IOException, CanceledException {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        CharactersNexusFormat charactersNexusFormat = new CharactersNexusFormat();
        ProgressListener pl = new ProgressPercentage();

        nexmlCharactersIn.parse(pl, "test/neXML/M4097_seq.xml", taxaBlock, charactersBlock, charactersNexusFormat);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        CharactersNexusIO.write(w1, taxaBlock, charactersBlock, null);
        System.err.println(w1.toString());

    }

    @Test
    public void parseCells() throws IOException, CanceledException {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        CharactersNexusFormat charactersNexusFormat = new CharactersNexusFormat();
        ProgressListener pl = new ProgressPercentage();

        nexmlCharactersIn.parse(pl, "test/neXML/M4311_cell.xml", taxaBlock, charactersBlock, charactersNexusFormat);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        CharactersNexusIO.write(w1, taxaBlock, charactersBlock, null);
        System.err.println(w1.toString());
    }

}