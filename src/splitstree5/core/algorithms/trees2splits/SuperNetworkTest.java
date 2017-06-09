package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;
import splitstree5.io.nexus.TreesNexusIO;

import java.io.FileReader;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * Created by Daria on 09.06.2017.
 */
public class SuperNetworkTest {

    final SuperNetwork superNetwork = new SuperNetwork();

    @Test
    public void compute() throws Exception {


        // TEST 2
        TaxaBlock taxaBlock2 = new TaxaBlock();
        TreesBlock treesBlock2 = new TreesBlock();
        NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np2.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np2, taxaBlock2);
        TreesNexusIO.parse(np2, taxaBlock2, treesBlock2, null);

        final SplitsBlock splitsBlock2 = new SplitsBlock();
        superNetwork.compute(new ProgressPercentage(), taxaBlock2, treesBlock2, splitsBlock2);

        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock2);
        TreesNexusIO.write(w2, taxaBlock2, treesBlock2, null);
        SplitsNexusIO.write(w2, taxaBlock2, splitsBlock2, null);
        System.err.println(w2.toString());

        // compare splits
        TaxaBlock taxaFromST42 = new TaxaBlock();
        SplitsBlock splitsFromST42 = new SplitsBlock();
        NexusStreamParser np42 = new NexusStreamParser(new FileReader("test/splits/trees6-SuperNet.nex"));
        np42.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np42, taxaFromST42);
        SplitsNexusIO.parse(np42, taxaFromST42, splitsFromST42, null);

        for(int i=0; i<splitsBlock2.getSplits().size(); i++){
            ASplit aSplit = splitsBlock2.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST42.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }
    }

}