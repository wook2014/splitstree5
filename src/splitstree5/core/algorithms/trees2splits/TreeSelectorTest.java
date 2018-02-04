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
 * Created on 24.01.2017.
 *
 * @author Daria
 */

public class TreeSelectorTest {

    final TreeSelector treeSelector = new TreeSelector();

    @Test
    public void testCompute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxaBlock);
        new TreesNexusInput().parse(np, taxaBlock, treesBlock, null);

        for (int which = 1; which <= treesBlock.getNTrees(); which++) {

            System.err.println("COMPUTE SPLITS FOR TREE" + which);

            SplitsBlock splitsBlock = new SplitsBlock();
            treeSelector.setOptionWhich(which);
            treeSelector.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

            final StringWriter w = new StringWriter();
            w.write("#nexus\n");
            new TaxaNexusOutput().write(w, taxaBlock);
            new TreesNexusOutput().write(w, taxaBlock, treesBlock, null);
            new SplitsNexusOutput().write(w, taxaBlock, splitsBlock, null);
            System.err.println(w.toString());

            // compare splits

            String fileName = "test/splits/tree selector/trees6-" + which + ".nex";

            TaxaBlock taxaFromST4 = new TaxaBlock();
            SplitsBlock splitsFromST4 = new SplitsBlock();
            NexusStreamParser np4 = new NexusStreamParser(new FileReader(fileName));
            np4.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np4, taxaFromST4);
            new SplitsNexusInput().parse(np4, taxaFromST4, splitsFromST4, null);

            assertEquals(splitsBlock.size(), splitsFromST4.size());
            for (int i = 0; i < splitsBlock.getNsplits(); i++) {
                ASplit aSplit = splitsBlock.getSplits().get(i);
                if (splitsFromST4.getSplits().contains(aSplit)) {
                    int index = splitsFromST4.getSplits().indexOf(aSplit);
                    ASplit aSplitST4 = splitsFromST4.getSplits().get(index);
                    assertEquals(aSplit.getA(), aSplitST4.getA());
                    assertEquals(aSplit.getB(), aSplitST4.getB());
                    assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
                    assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence(), 0.0);
                    assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
                }
                splitsFromST4.getSplits().remove(aSplit);
            }
            assertEquals(0, splitsFromST4.size());

        }
    }

}