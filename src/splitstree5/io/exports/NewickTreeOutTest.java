package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;
import splitstree5.io.nexus.TreesNexusIO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class NewickTreeOutTest {
    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_Newick.txt");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees49-taxa.nex"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxa);
        TreesNexusIO.parse(np, taxa, treesBlock, null);

        NewickTreeOut.export(writer, treesBlock, null);

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/trees49.tre"));
        String fromST4 = new String(encoded1, StandardCharsets.UTF_8);

        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_Newick.txt"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        System.err.println(fromST4);
        System.err.println(export);
        assertEquals(fromST4, export);

    }

}