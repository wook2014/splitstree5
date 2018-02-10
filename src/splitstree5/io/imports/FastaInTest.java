package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,01.07.2017.
 */
public class FastaInTest {

    private FastaIn fastaIn = new FastaIn();

    @Test
    public void parseI() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            fastaIn.parse(progress, "test/notNexusFiles/smallTest.fasta", taxaBlock, charactersBlock);
        }

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
        System.err.println(w.toString());

        System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

    }

    @Test
    public void parseII() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            fastaIn.parse(progress, "test/notNexusFiles/algae.fasta", taxaBlock, charactersBlock);
        }

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new CharactersNexusOutput().write(w, taxaBlock, charactersBlock);
        System.err.println(w.toString());

        System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

    }

    @Test
    public void isApplicable() throws IOException {
        Set<String> applicableFiles = new HashSet<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (fastaIn.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, new HashSet<>(Arrays.asList("algae.fasta", "algae_splits.fasta", "ncbi.fasta", "smallTest.fasta", "trees49_splits.fasta")));
    }

}