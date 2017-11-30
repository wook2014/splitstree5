package splitstree5.io.imports.NeXML;

import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.ClustalIn;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class NexmlFileParserTest {
    @Test
    public void parseTaxa() throws Exception {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        nexmlFileParser.parseTaxa("test/neXML/taxa.xml", taxaBlock);

        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        System.err.println(w1.toString());

    }

    @Test
    public void parseCharecters() throws IOException {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        nexmlFileParser.parseCharacters("test/neXML/M4097_seq.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        //TaxaNexusIO.write(w1, taxaBlock);
        CharactersNexusIO.write(w1, taxaBlock, charactersBlock, null);
        System.err.println(w1.toString());
    }

}