package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;

import java.io.*;
import java.util.List;

public class PhylipCharactersOutTest {

    private PhylipCharactersOut phylipCharactersOut = new PhylipCharactersOut();

    @Test
    public void export() throws Exception {

        // interleaved
        File fileI = new File("test/exports/TEST_PHYL_INTER.phy");
        Writer writerI = new BufferedWriter(new FileWriter(fileI));


        TaxaBlock taxaI = new TaxaBlock();
        CharactersBlock charactersI = new CharactersBlock();
        CharactersNexusFormat formatI = new CharactersNexusFormat();

        List<String> taxonNamesI = new CharactersNexusInput().parse(
                new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                taxaI, charactersI, formatI);
        taxaI.addTaxaByNames(taxonNamesI);

        phylipCharactersOut.export(writerI, taxaI, charactersI);
        writerI.close();


        // standard
        phylipCharactersOut.setOptionInterleaved(false);
        File file = new File("test/exports/TEST_PHYL.phy");
        Writer writer = new BufferedWriter(new FileWriter(file));


        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock characters = new CharactersBlock();
        CharactersNexusFormat format = new CharactersNexusFormat();

        List<String> taxonNames = new CharactersNexusInput().parse(
                new NexusStreamParser(new FileReader("test/characters/microsat1.nex")),
                taxa, characters, format);
        taxa.addTaxaByNames(taxonNames);

        phylipCharactersOut.export(writer, taxa, characters);
        writer.close();

    }

}