package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.SplitsNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FastaSplitsImporterTest {

    private FastaSplitsImporter fastaSplitsImporter = new FastaSplitsImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        SplitsBlock splitsBlock = new SplitsBlock();
        ProgressListener pl = new ProgressPercentage();
        fastaSplitsImporter.parse(pl, "test/notNexusFiles/algae_splits.fasta", taxaBlock, splitsBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        //new TaxaNexusOutput().write(w, taxaBlock);
        new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);
        System.err.println(w.toString());

        String splits = "\n" +
                "BEGIN SPLITS;\n" +
                "DIMENSIONS ntax=8 nsplits=22;\n" +
                "FORMAT labels=no weights=yes confidences=no intervals=no;\n" +
                "PROPERTIES fit=-1.0 cyclic;\n" +
                "CYCLE 1 3 5 4 6 8 7 2;\n" +
                "MATRIX\n" +
                "\t[1, size=1] \t 1.0 \t 1 3 4 5 6 7 8,\n" +
                "\t[2, size=3] \t 1.0 \t 1 3 4 5 6,\n" +
                "\t[3, size=3] \t 1.0 \t 1 3 5,\n" +
                "\t[4, size=2] \t 1.0 \t 1 3,\n" +
                "\t[5, size=1] \t 1.0 \t 1,\n" +
                "\t[6, size=1] \t 1.0 \t 1 2 3 4 5 6 8,\n" +
                "\t[7, size=2] \t 1.0 \t 1 2 3 4 5 6,\n" +
                "\t[8, size=3] \t 1.0 \t 1 2 3 4 5,\n" +
                "\t[9, size=4] \t 1.0 \t 1 2 3 5,\n" +
                "\t[10, size=3] \t 1.0 \t 1 2 3,\n" +
                "\t[11, size=2] \t 1.0 \t 1 2,\n" +
                "\t[12, size=1] \t 1.0 \t 1 2 3 4 5 6 7,\n" +
                "\t[13, size=2] \t 1.0 \t 1 2 3 4 5 7,\n" +
                "\t[14, size=4] \t 1.0 \t 1 2 3 7,\n" +
                "\t[15, size=3] \t 1.0 \t 1 2 7,\n" +
                "\t[16, size=1] \t 1.0 \t 1 2 3 4 5 7 8,\n" +
                "\t[17, size=2] \t 1.0 \t 1 2 3 5 7 8,\n" +
                "\t[18, size=4] \t 1.0 \t 1 2 7 8,\n" +
                "\t[19, size=1] \t 1.0 \t 1 2 3 5 6 7 8,\n" +
                "\t[20, size=1] \t 1.0 \t 1 2 3 4 6 7 8,\n" +
                "\t[21, size=2] \t 1.0 \t 1 2 4 6 7 8,\n" +
                "\t[22, size=1] \t 1.0 \t 1 2 4 5 6 7 8,\n" +
                ";\n" +
                "END; [SPLITS]\n";

        assertEquals(w.toString(), splits);

    }

    @Test
    public void isApplicable() throws IOException {
        Set<String> applicableFiles = new HashSet<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (fastaSplitsImporter.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, new HashSet<>(Arrays.asList("algae_splits.fasta", "trees49_splits.fasta")));
    }

}