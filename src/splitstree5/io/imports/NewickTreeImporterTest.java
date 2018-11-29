package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.TaxaNexusOutput;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.TreesNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,17.10.2017.
 */
public class NewickTreeImporterTest {

    private NewickTreeImporter newickTreeImporter = new NewickTreeImporter();

    @org.junit.Test
    public void parse() throws Exception {

        String test1 = "(red-purple:18.0625,purple-reddish:12.9375," +
                "(purple:16.25,(purple-blue:19.35,(blue:20.328125,(green:13.9375,(greenish:14.71875," +
                "((red:30.071428,yellow:24.928572):34.729168,yellowish:14.270833)" +
                ":12.78125):12.5625):18.25):18.546875):19.4):9.25)";

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            newickTreeImporter.parse(progress, "test/notNexusFiles/colors-nj.tre", taxaBlock, treesBlock);
        }
        //NewickTreeImporter.parse("test/notNexusFiles/trees3.tre", taxaBlock, treesBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        TreesNexusFormat tnf = new TreesNexusFormat();
        tnf.setOptionTranslate(false);
        new TreesNexusOutput().write(w, taxaBlock, treesBlock);
        System.err.println(w.toString());
        assertEquals(test1, treesBlock.getTrees().get(0).toString());
    }

    @Test
    public void isApplicable() throws IOException {
        ArrayList<String> applicableFiles = new ArrayList<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (newickTreeImporter.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, Arrays.asList("colors-nj.tre", "trees3.tre", "trees49.tre"));
    }

}