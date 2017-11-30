package splitstree5.io.imports.NeXML;

import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.ClustalIn;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class NexmlFileParserTest {
    @Test
    public void parseTaxa() throws Exception {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();

        TaxaBlock taxaBlock = new TaxaBlock();
        //CharactersBlock charactersBlock = new CharactersBlock();
        //CharactersNexusFormat format = new CharactersNexusFormat();

        nexmlFileParser.parseTaxa("test/neXML/taxa.xml", taxaBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        //CharactersNexusIO.write(w1, taxaBlock, charactersBlock, format);
        System.err.println(w1.toString());

    }

}