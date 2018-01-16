package splitstree5.io.imports;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.StringWriter;

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

        clustalIn.parse(pl, "test/notNexusFiles/prot1.aln", taxaBlock, charactersBlock, format);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        CharactersNexusIO.write(w1, taxaBlock, charactersBlock, format);
        System.err.println(w1.toString());
        System.err.println(format.isInterleave());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/protein.aln", taxaBlock, charactersBlock, format);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock);
        CharactersNexusIO.write(w2, taxaBlock, charactersBlock, format);
        System.err.println(w2.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/conservation.aln", taxaBlock, charactersBlock, format);
        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        TaxaNexusIO.write(w3, taxaBlock);
        CharactersNexusIO.write(w3, taxaBlock, charactersBlock, format);
        System.err.println(w3.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/dna-ncbi.aln", taxaBlock, charactersBlock, format);
        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        TaxaNexusIO.write(w4, taxaBlock);
        CharactersNexusIO.write(w4, taxaBlock, charactersBlock, format);
        System.err.println(w4.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());

        clustalIn.parse(pl, "test/notNexusFiles/dna-ncbi-num.aln", taxaBlock, charactersBlock, format);
        // printing
        final StringWriter w5 = new StringWriter();
        w5.write("#nexus\n");
        TaxaNexusIO.write(w5, taxaBlock);
        CharactersNexusIO.write(w5, taxaBlock, charactersBlock, format);
        System.err.println(w5.toString());
        //System.err.println("Ambiguous : " + charactersBlock.isHasAmbiguousStates());
    }

}