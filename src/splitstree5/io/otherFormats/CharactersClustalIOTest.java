package splitstree5.io.otherFormats;

import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.fasta.CharactersFastaIO;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * Created by Daria on 05.08.2017.
 */
public class CharactersClustalIOTest {
    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        //CharactersClustalIO.parse("test/notNexusFiles/prot1.aln", taxaBlock, charactersBlock);
        CharactersClustalIO.parse("test/notNexusFiles/protein.aln", taxaBlock, charactersBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        CharactersNexusIO.write(w, taxaBlock, charactersBlock, null);
        System.err.println(w.toString());

        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

    }

}