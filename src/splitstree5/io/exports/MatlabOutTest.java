package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.DistancesNexusInput;
import splitstree5.io.nexus.SplitsNexusInput;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class MatlabOutTest {

    private MatlabOut matlabOut = new MatlabOut();

    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_Matlab.txt");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        DistancesBlock distances = new DistancesBlock();
        SplitsBlock splits = new SplitsBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new DistancesNexusInput().parse(np, taxa, distances, null);
        new SplitsNexusInput().parse(np, taxa, splits, null);

        matlabOut.export(writer, taxa);
        matlabOut.export(writer, taxa, splits);
        matlabOut.export(writer, taxa, distances);
        writer.close();

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/algae.m"));
        String algae = new String(encoded1, StandardCharsets.UTF_8);

        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_Matlab.txt"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        System.err.println(algae);
        System.err.println(export);
        assertEquals(algae, export);
    }

}