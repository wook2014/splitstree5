package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;

import java.io.*;
import java.util.List;

public class ClustalOutTest {

    private ClustalOut clustalOut = new ClustalOut();

    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_Clustal.aln");
        Writer writer = new BufferedWriter(new FileWriter(file));


        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock characters = new CharactersBlock();
        CharactersNexusFormat format = new CharactersNexusFormat();

        List<String> taxonNames = new CharactersNexusInput().parse(
                new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                taxa, characters);
        taxa.addTaxaByNames(taxonNames);

        clustalOut.export(writer, taxa, characters);
        writer.close();
    }

}