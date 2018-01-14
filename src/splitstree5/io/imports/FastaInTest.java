package splitstree5.io.imports;

import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

/**
 * Daria Evseeva,01.07.2017.
 */
public class FastaInTest {

    private FastaIn fastaIn = new FastaIn();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            fastaIn.parse(progress, "test/notNexusFiles/fasta/smallTest.fasta", taxaBlock, charactersBlock);
        }

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        CharactersNexusIO.write(w, taxaBlock, charactersBlock, null);
        System.err.println(w.toString());

        System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

    }

}