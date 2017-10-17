package splitstree5.io.otherFormats;

import org.junit.*;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.TaxaNexusIO;
import splitstree5.io.nexus.TreesNexusIO;

import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * Created by Daria on 17.10.2017.
 */
public class NewickTreeTest {
    @org.junit.Test
    public void parse() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NewickTree.parse("test/notNexusFiles/colors-nj.tre", taxaBlock, treesBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        TreesNexusIO.write(w, taxaBlock, treesBlock, null);
        System.err.println(w.toString());

    }

}