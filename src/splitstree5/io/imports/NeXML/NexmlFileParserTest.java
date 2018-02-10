package splitstree5.io.imports.NeXML;

import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.TreesNexusOutput;

import java.io.IOException;
import java.io.StringWriter;

public class NexmlFileParserTest {
    @Test
    public void parseTaxa() throws Exception {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        nexmlFileParser.parse("test/neXML/taxa.xml", taxaBlock);

        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        System.err.println(w1.toString());

    }

    @Test
    public void parseCharacters() throws IOException {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        nexmlFileParser.parse("test/neXML/M4097_seq.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        //new TaxaNexusOutput().write(w1, taxaBlock);
        new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
        System.err.println(w1.toString());
    }

    @Test
    public void parseCharactersCells() throws IOException {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        nexmlFileParser.parse("test/neXML/M4311_cell.xml", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
        System.err.println(w1.toString());
    }

    @Test
    public void parseTrees() throws IOException {

        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();

        nexmlFileParser.parse("test/neXML/simple.xml", taxaBlock, treesBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        TreesNexusFormat treesNexusFormat = new TreesNexusFormat();
        treesNexusFormat.setOptionTranslate(false);
        new TreesNexusOutput().write(w1, taxaBlock, treesBlock);
        System.err.println(w1.toString());
    }

}