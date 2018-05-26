package splitstree5.io.imports;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusOutput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,05.08.2017.
 */
public class ClustalInTest {

    private ClustalIn clustalIn = new ClustalIn();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        CharactersNexusFormat format = new CharactersNexusFormat();
        ProgressListener pl = new ProgressPercentage();

        clustalIn.parse(pl, "test/notNexusFiles/prot1.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock);
        new CharactersNexusOutput().write(w1, taxaBlock, charactersBlock);
        System.err.println(w1.toString());
        System.err.println(format.isOptionInterleave());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/protein.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock);
        new CharactersNexusOutput().write(w2, taxaBlock, charactersBlock);
        System.err.println(w2.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/conservation.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        new TaxaNexusOutput().write(w3, taxaBlock);
        new CharactersNexusOutput().write(w3, taxaBlock, charactersBlock);
        System.err.println(w3.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/dna-ncbi.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        new TaxaNexusOutput().write(w4, taxaBlock);
        new CharactersNexusOutput().write(w4, taxaBlock, charactersBlock);
        System.err.println(w4.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/dna-ncbi-num.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w5 = new StringWriter();
        w5.write("#nexus\n");
        new TaxaNexusOutput().write(w5, taxaBlock);
        new CharactersNexusOutput().write(w5, taxaBlock, charactersBlock);
        System.err.println(w5.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());
    }

    @Test
    public void isApplicable() throws IOException {
        ArrayList<String> applicableFiles = new ArrayList<>();

        File directory = new File("test/notNexusFiles");
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                if (clustalIn.isApplicable(file.getPath()))
                    applicableFiles.add(Basic.getFileNameWithoutPath(file.getName()));
            }
        }
        System.err.println(applicableFiles);
        assertEquals(applicableFiles, Arrays.asList("conservation.aln", "dna-ncbi-num.aln", "dna-ncbi.aln", "prot1.aln"));
    }

}