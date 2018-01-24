package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FastaSplitsOutTest {

    private FastaSplitsOut fastaSplitsOut = new FastaSplitsOut();

    @Test
    public void exportSplitsI() throws Exception {

        File file = new File("test/exports/TEST_FASTA_splits.fasta");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        SplitsBlock splits = new SplitsBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/splits/algae.txt"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxa);
        SplitsNexusIO.parse(np, taxa, splits, null);

        fastaSplitsOut.export(writer, taxa, splits);
        writer.close();

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/fasta/algae_splits.fasta"));
        String fromST4 = new String(encoded1, StandardCharsets.UTF_8);
        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_FASTA_splits.fasta"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        System.err.println(fromST4);
        System.err.println(export);
        assertEquals(fromST4, export);

    }

    @Test
    public void exportSplitsII() throws Exception {

        File file = new File("test/exports/TEST_FASTA_splits_trees49.fasta");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        SplitsBlock splits = new SplitsBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/splits/trees49-SuperNet.nex"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxa);
        SplitsNexusIO.parse(np, taxa, splits, null);

        fastaSplitsOut.export(writer, taxa, splits);
        writer.close();

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/fasta/trees49_splits.fasta"));
        String fromST4 = new String(encoded1, StandardCharsets.UTF_8);
        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_FASTA_splits_trees49.fasta"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        assertEquals(fromST4, export);

    }

}