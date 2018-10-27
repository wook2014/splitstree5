package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SingleLineSequencesImporterTest {

    private SingleLineSequencesImporter singleLineSequencesImporter = new SingleLineSequencesImporter();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        singleLineSequencesImporter.parse(pl, "test/notNexusFiles/singleLineDNA.txt", taxaBlock, charactersBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
        System.err.println(w.toString());

    }

    @Test
    public void isApplicable() throws IOException {
        ArrayList<String> applicableFiles = new ArrayList<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (singleLineSequencesImporter.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, Arrays.asList("singleLineDNA.txt"));
    }

}