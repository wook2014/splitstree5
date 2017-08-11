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

        CharactersClustalIO.parse("test/notNexusFiles/prot1.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        CharactersNexusIO.write(w1, taxaBlock, charactersBlock, null);
        System.err.println(w1.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        CharactersClustalIO.parse("test/notNexusFiles/protein.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock);
        CharactersNexusIO.write(w2, taxaBlock, charactersBlock, null);
        System.err.println(w2.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        CharactersClustalIO.parse("test/notNexusFiles/conservation.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        TaxaNexusIO.write(w3, taxaBlock);
        CharactersNexusIO.write(w3, taxaBlock, charactersBlock, null);
        System.err.println(w3.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        CharactersClustalIO.parse("test/notNexusFiles/dna-ncbi.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        TaxaNexusIO.write(w4, taxaBlock);
        CharactersNexusIO.write(w4, taxaBlock, charactersBlock, null);
        System.err.println(w4.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        CharactersClustalIO.parse("test/notNexusFiles/dna-ncbi-num.aln", taxaBlock, charactersBlock);
        // printing
        final StringWriter w5 = new StringWriter();
        w5.write("#nexus\n");
        TaxaNexusIO.write(w5, taxaBlock);
        CharactersNexusIO.write(w5, taxaBlock, charactersBlock, null);
        System.err.println(w5.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());
    }

}