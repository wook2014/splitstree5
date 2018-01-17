package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.*;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.*;

public class TabbedTextOutTest {

    private TabbedTextOut tabbedTextOut = new TabbedTextOut();

    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_TabbedText.txt");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock character = new CharactersBlock();
        TreesBlock trees = new TreesBlock();
        DistancesBlock distances = new DistancesBlock();
        SplitsBlock splits = new SplitsBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae_char.nex"));
        np.matchIgnoreCase("#nexus");

        TaxaNexusIO.parse(np, taxa);
        CharactersNexusIO.parse(np, taxa, character, null);
        DistancesNexusIO.parse(np, taxa, distances, null);
        SplitsNexusIO.parse(np, taxa, splits, null);

        tabbedTextOut.export(writer, taxa);
        tabbedTextOut.export(writer, taxa, character);
        tabbedTextOut.export(writer, taxa, distances);
        tabbedTextOut.export(writer, taxa, splits);
        writer.close();

        /*byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/algae.m"));
        String algae = new String(encoded1, StandardCharsets.UTF_8);

        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_Matlab.txt"));
        String export = new String(encoded2, StandardCharsets.UTF_8);*/

        //System.err.println(algae);
        //System.err.println(export);
        //assertEquals(algae, export);

    }

}