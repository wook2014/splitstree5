package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;

import java.io.*;
import java.util.List;

public class FastaOutTest {

    private FastaOut fastaOut = new FastaOut();

    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_FASTA.fasta");
        Writer writer = new BufferedWriter(new FileWriter(file));


        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock characters = new CharactersBlock();
        CharactersNexusFormat format = new CharactersNexusFormat();

        List<String> taxonNames = CharactersNexusIO.parse(
                new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                taxa, characters, format);
        taxa.addTaxaByNames(taxonNames);

        fastaOut.export(writer, taxa, characters);
        writer.close();

    }

}