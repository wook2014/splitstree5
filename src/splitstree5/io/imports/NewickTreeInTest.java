package splitstree5.io.imports;

import jloda.util.ProgressPercentage;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.TaxaNexusIO;
import splitstree5.io.nexus.TreesNexusFormat;
import splitstree5.io.nexus.TreesNexusIO;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Daria Evseeva,17.10.2017.
 */
public class NewickTreeInTest {

    private NewickTreeIn newickTreeIn = new NewickTreeIn();

    @org.junit.Test
    public void parse() throws Exception {

        String test1 = "(red-purple:18.0625,purple-reddish:12.9375," +
                "(purple:16.25,(purple-blue:19.35,(blue:20.328125,(green:13.9375,(greenish:14.71875," +
                "((red:30.071428,yellow:24.928572):34.729168,yellowish:14.270833)" +
                ":12.78125):12.5625):18.25):18.546875):19.4):9.25)";

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        try (ProgressPercentage progress = new ProgressPercentage("Test")) {
            newickTreeIn.parse(progress, "test/notNexusFiles/colors-nj.tre", taxaBlock, treesBlock);
        }
        //NewickTreeIn.parse("test/notNexusFiles/trees3.tre", taxaBlock, treesBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        TreesNexusFormat tnf = new TreesNexusFormat();
        tnf.setTranslate(false);
        TreesNexusIO.write(w, taxaBlock, treesBlock, tnf);
        System.err.println(w.toString());
        assertEquals(test1, treesBlock.getTrees().get(0).toString());
    }

}