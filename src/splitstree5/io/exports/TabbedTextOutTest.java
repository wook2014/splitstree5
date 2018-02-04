package splitstree5.io.exports;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.algorithms.characters2distances.Uncorrected_P;
import splitstree5.core.algorithms.distances2trees.NeighborJoining;
import splitstree5.core.algorithms.trees2splits.TreeSelector;
import splitstree5.core.datablocks.*;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

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
        ProgressListener pl = new ProgressPercentage();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae_char.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new CharactersNexusInput().parse(np, taxa, character, null);
        /*new DistancesNexusInput().parse(np, taxa, distances, null);
        new SplitsNexusInput().parse(np, taxa, splits, null);*/

        // algorithms
        Uncorrected_P uncorrected_p = new Uncorrected_P();
        NeighborJoining nj = new NeighborJoining();
        TreeSelector treeSelector = new TreeSelector();
        uncorrected_p.compute(pl, taxa, character, distances);
        nj.compute(pl, taxa, distances, trees);
        treeSelector.compute(pl, taxa, trees, splits);

        tabbedTextOut.export(writer, taxa);
        tabbedTextOut.export(writer, taxa, character);
        tabbedTextOut.export(writer, taxa, distances);
        tabbedTextOut.export(writer, taxa, splits);
        writer.close();

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/algae_tabbed.txt"));
        String algae = new String(encoded1, StandardCharsets.UTF_8);

        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_TabbedText.txt"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        //System.err.println(algae);
        //System.err.println(export);
        assertEquals(algae, export);

    }

}