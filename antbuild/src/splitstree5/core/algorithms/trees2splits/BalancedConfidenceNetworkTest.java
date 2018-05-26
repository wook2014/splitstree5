package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.io.nexus.*;

import java.io.FileReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Created on 09.06.2017.
 *
 * @author Daria
 */

public class BalancedConfidenceNetworkTest {

    final private BalancedConfidenceNetwork bcn = new BalancedConfidenceNetwork();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/trees/colors-NJ.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxaBlock);
        new TreesNexusInput().parse(np, taxaBlock, treesBlock);

        final SplitsBlock splitsBlock = new SplitsBlock();
        bcn.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new TreesNexusOutput().write(w, taxaBlock, treesBlock);
        new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);
        System.err.println(w.toString());

        // compare splits
        TaxaBlock taxaFromST4 = new TaxaBlock();
        SplitsBlock splitsFromST4 = new SplitsBlock();
        NexusStreamParser np4 = new NexusStreamParser(new FileReader("test/splits/colors-BCN.nex"));
        np4.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np4, taxaFromST4);
        new SplitsNexusInput().parse(np4, taxaFromST4, splitsFromST4);

        double[] confidence = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        assertEquals(splitsBlock.getSplits().size(), splitsFromST4.getSplits().size());
        for (int i = 0; i < splitsBlock.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST4.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getConfidence(), confidence[i], 0.0);

            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

    }

}