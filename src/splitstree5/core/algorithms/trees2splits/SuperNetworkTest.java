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

        /*TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees49-taxa.nex"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxaBlock);
        TreesNexusIO.parse(np, taxaBlock, treesBlock, null);

        final SplitsBlock splitsBlock = new SplitsBlock();
        superNetwork.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

        // printing
        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        TreesNexusIO.write(w, taxaBlock, treesBlock, null);
        SplitsNexusIO.write(w, taxaBlock, splitsBlock, null);
        System.err.println(w.toString());

        // compare splits
        TaxaBlock taxaFromST4 = new TaxaBlock();
        SplitsBlock splitsFromST4 = new SplitsBlock();
        NexusStreamParser np4 = new NexusStreamParser(new FileReader("test/splits/trees49-SuperNet.nex"));
        np4.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np4, taxaFromST4);
        SplitsNexusIO.parse(np4, taxaFromST4, splitsFromST4, null);

        assertEquals(splitsBlock.size(), splitsFromST4.size());
        for(int i=0; i<splitsBlock.getSplits().size(); i++){
            ASplit aSplit = splitsBlock.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST4.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }*/


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

        assertEquals(splitsBlock2.size(), splitsFromST42.size());
        for(int i=0; i<splitsBlock2.getSplits().size(); i++){
            ASplit aSplit = splitsBlock2.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST42.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence()); //todo
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        // TEST 3
        TaxaBlock taxaBlock3 = new TaxaBlock();
        TreesBlock treesBlock3 = new TreesBlock();
        NexusStreamParser np3 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np3.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np3, taxaBlock3);
        TreesNexusIO.parse(np3, taxaBlock3, treesBlock3, null);

        final SplitsBlock splitsBlock3 = new SplitsBlock();
        superNetwork.setOptionZRule(false);
        superNetwork.setOptionSuperTree(true);
        superNetwork.setOptionNumberOfRuns(3);
        superNetwork.setOptionEdgeWeights("Mean");
        superNetwork.setOptionApplyRefineHeuristic(true);
        superNetwork.compute(new ProgressPercentage(), taxaBlock3, treesBlock3, splitsBlock3);

        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        TaxaNexusIO.write(w3, taxaBlock3);
        TreesNexusIO.write(w3, taxaBlock3, treesBlock3, null);
        SplitsNexusIO.write(w3, taxaBlock3, splitsBlock3, null);
        System.err.println(w3.toString());

        // compare splits
        TaxaBlock taxaFromST43 = new TaxaBlock();
        SplitsBlock splitsFromST43 = new SplitsBlock();
        NexusStreamParser np43 = new NexusStreamParser(new FileReader("test/splits/trees6-SuperNet-Param.nex"));
        np43.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np43, taxaFromST43);
        SplitsNexusIO.parse(np43, taxaFromST43, splitsFromST43, null);

        assertEquals(splitsBlock3.size(), splitsFromST43.size());
        for(int i=0; i<splitsBlock3.getSplits().size(); i++){
            ASplit aSplit = splitsBlock3.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST43.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence()); //todo
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        // TEST 4
        TaxaBlock taxaBlock4 = new TaxaBlock();
        TreesBlock treesBlock4 = new TreesBlock();
        NexusStreamParser np4 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np4.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np4, taxaBlock4);
        TreesNexusIO.parse(np4, taxaBlock4, treesBlock4, null);

        final SplitsBlock splitsBlock4 = new SplitsBlock();
        superNetwork.setOptionZRule(true);
        superNetwork.setOptionSuperTree(false);
        superNetwork.setOptionNumberOfRuns(3);
        superNetwork.setOptionEdgeWeights("AverageRelative");
        superNetwork.setOptionApplyRefineHeuristic(false);
        superNetwork.compute(new ProgressPercentage(), taxaBlock4, treesBlock4, splitsBlock4);

        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        TaxaNexusIO.write(w4, taxaBlock4);
        TreesNexusIO.write(w4, taxaBlock4, treesBlock4, null);
        SplitsNexusIO.write(w4, taxaBlock4, splitsBlock4, null);
        System.err.println(w4.toString());

        // compare splits
        TaxaBlock taxaFromST44 = new TaxaBlock();
        SplitsBlock splitsFromST44 = new SplitsBlock();
        NexusStreamParser np44 = new NexusStreamParser(new FileReader("test/splits/trees6-SuperNet-AR.nex"));
        np44.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np44, taxaFromST44);
        SplitsNexusIO.parse(np44, taxaFromST44, splitsFromST44, null);

        assertEquals(splitsBlock4.size(), splitsFromST44.size());
        for(int i=0; i<splitsBlock4.getSplits().size(); i++){
            ASplit aSplit = splitsBlock4.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST44.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }
    }

}