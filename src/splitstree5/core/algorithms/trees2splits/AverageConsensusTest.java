package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.algorithms.characters2distances.Uncorrected_P;
import splitstree5.core.algorithms.distances2trees.NeighborJoining;
import splitstree5.core.datablocks.*;
import splitstree5.core.misc.ASplit;
import splitstree5.io.nexus.*;

import java.io.FileReader;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

public class AverageConsensusTest {

    final AverageConsensus algorithm = new AverageConsensus();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/trees/beesUPGMA.nex"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxaBlock);
        TreesNexusIO.parse(np, taxaBlock, treesBlock, null);

        final SplitsBlock splitsBlock = new SplitsBlock();
        algorithm.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

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
        NexusStreamParser np4 = new NexusStreamParser(new FileReader("test/splits/bees-AverageConsensus.nex"));
        np4.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np4, taxaFromST4);
        SplitsNexusIO.parse(np4, taxaFromST4, splitsFromST4, null);

        for(int i=0; i<splitsBlock.getSplits().size(); i++){
            ASplit aSplit = splitsBlock.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST4.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        // TEST 2
        TaxaBlock taxaBlock2 = new TaxaBlock();
        TreesBlock treesBlock2 = new TreesBlock();
        NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np2.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np2, taxaBlock2);
        TreesNexusIO.parse(np2, taxaBlock2, treesBlock2, null);

        final SplitsBlock splitsBlock2 = new SplitsBlock();
        algorithm.compute(new ProgressPercentage(), taxaBlock2, treesBlock2, splitsBlock2);

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
        NexusStreamParser np42 = new NexusStreamParser(new FileReader("test/splits/trees6-AverageConsensus.nex"));
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

        // TEST 3
        /*TaxaBlock taxaBlock3 = new TaxaBlock();
        TreesBlock treesBlock3 = new TreesBlock();
        NexusStreamParser np3 = new NexusStreamParser(new FileReader("test/trees/dolphins-NJ.nex"));
        np3.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np3, taxaBlock3);
        TreesNexusIO.parse(np3, taxaBlock3, treesBlock3, null);

        final SplitsBlock splitsBlock3 = new SplitsBlock();
        algorithm.compute(new ProgressPercentage(), taxaBlock3, treesBlock3, splitsBlock3);

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
        NexusStreamParser np43 = new NexusStreamParser(new FileReader("test/splits/dolphins-AverageConsensus.nex"));
        np43.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np43, taxaFromST43);
        SplitsNexusIO.parse(np43, taxaFromST43, splitsFromST43, null);

        assertEquals(splitsBlock3.size(), splitsFromST43.size());
        for(int i=0; i<splitsBlock3.getSplits().size(); i++){
            ASplit aSplit = splitsBlock3.getSplits().get(i);
            if(splitsFromST43.getSplits().contains(aSplit)){
                int index = splitsFromST43.getSplits().indexOf(aSplit);
                ASplit aSplitST4 = splitsFromST43.getSplits().get(index);
                assertEquals(aSplit.getA(), aSplitST4.getA());
                assertEquals(aSplit.getB(), aSplitST4.getB());
                assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
                assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
                assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
            }
        }
        assertEquals(0, splitsFromST43.size());*/
    }
}