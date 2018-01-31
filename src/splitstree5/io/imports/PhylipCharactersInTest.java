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

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,27.09.2017.
 */
public class PhylipCharactersInTest {

    private PhylipCharactersIn phylipCharactersIn = new PhylipCharactersIn();

    @Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();
        CharactersNexusFormat format = new CharactersNexusFormat();
        ProgressListener pl = new ProgressPercentage();

        phylipCharactersIn.parse(pl,"test/notNexusFiles/standard.phy", taxaBlock, charactersBlock);
        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        TaxaNexusIO.write(w1, taxaBlock);
        CharactersNexusIO.write(w1, taxaBlock, charactersBlock, format);
        System.err.println(w1.toString());
        String standard = w1.toString();

        phylipCharactersIn.parse(pl,"test/notNexusFiles/standardEOL.phy", taxaBlock, charactersBlock);
        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock);
        CharactersNexusIO.write(w2, taxaBlock, charactersBlock, format);
        System.err.println(w2.toString());
        String standardEOL = w2.toString();

        phylipCharactersIn.parse(pl,"test/notNexusFiles/interleaved.phy", taxaBlock, charactersBlock);
        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        TaxaNexusIO.write(w3, taxaBlock);
        CharactersNexusIO.write(w3, taxaBlock, charactersBlock, format);
        System.err.println(w3.toString());
        String interleaved = w3.toString();

        phylipCharactersIn.parse(pl,"test/notNexusFiles/interleaved-multi.phy", taxaBlock, charactersBlock);
        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        TaxaNexusIO.write(w4, taxaBlock);
        CharactersNexusIO.write(w4, taxaBlock, charactersBlock, format);
        System.err.println(w4.toString());
        String interleavedMulti = w4.toString();


        //assertEquals(standard, interleaved);
        assertEquals(interleaved, interleavedMulti);
        assertEquals(standard, standardEOL);

    }

}