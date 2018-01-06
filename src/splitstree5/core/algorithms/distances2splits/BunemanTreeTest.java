package splitstree5.core.algorithms.distances2splits;

import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.FileReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Created on 29.05.2017.
 *
 * @author Daria
 */

public class BunemanTreeTest {

    final BunemanTree algorithm = new BunemanTree();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        DistancesBlock distancesBlock = new DistancesBlock();
        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/distances7-taxa.nex"))) {
            np.matchIgnoreCase("#nexus");
            TaxaNexusIO.parse(np, taxaBlock);
            DistancesNexusIO.parse(np, taxaBlock, distancesBlock, null);
            assertEquals(taxaBlock.getNtax(), 7);
            assertEquals(distancesBlock.getNtax(), 7);
        }

        final SplitsBlock splitsBlock = new SplitsBlock();

        algorithm.compute(new ProgressPercentage(), taxaBlock, distancesBlock, splitsBlock);

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        TaxaNexusIO.write(w, taxaBlock);
        DistancesNexusIO.write(w, taxaBlock, distancesBlock, null);
        SplitsNexusIO.write(w, taxaBlock, splitsBlock, null);
        System.err.println(w.toString());

        // compare splits

        TaxaBlock taxaFromST4 = new TaxaBlock();
        SplitsBlock splitsFromST4 = new SplitsBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/splits/distances7-Buneman.txt"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxaFromST4);
        SplitsNexusIO.parse(np, taxaFromST4, splitsFromST4, null);

        for (int i = 0; i < splitsBlock.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST4.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());

            /*System.out.println(aSplit.getA()+"--"+aSplitST4.getA());
            System.out.println(aSplit.getB()+"--"+aSplitST4.getB());
            System.out.println(aSplit.getWeight()+"--"+aSplitST4.getWeight());
            System.out.println(aSplit.getConfidence()+"--"+aSplitST4.getConfidence());
            System.out.println(aSplit.getLabel()+"--"+aSplitST4.getLabel());*/
        }

        // Test 2

        TaxaBlock taxaBlock2 = new TaxaBlock();
        DistancesBlock distancesBlock2 = new DistancesBlock();
        try (NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/distances/algaeBaseFreqTaxa.nex"))) {
            np2.matchIgnoreCase("#nexus");
            TaxaNexusIO.parse(np2, taxaBlock2);
            DistancesNexusIO.parse(np2, taxaBlock2, distancesBlock2, null);
            assertEquals(taxaBlock2.getNtax(), 8);
            assertEquals(distancesBlock2.getNtax(), 8);
        }

        final SplitsBlock splitsBlock2 = new SplitsBlock();

        algorithm.compute(new ProgressPercentage(), taxaBlock2, distancesBlock2, splitsBlock2);

        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        TaxaNexusIO.write(w2, taxaBlock2);
        DistancesNexusIO.write(w2, taxaBlock2, distancesBlock2, null);
        SplitsNexusIO.write(w2, taxaBlock2, splitsBlock2, null);
        System.err.println(w2.toString());

        // compare splits

        TaxaBlock taxaFromST42 = new TaxaBlock();
        SplitsBlock splitsFromST42 = new SplitsBlock();
        NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/splits/algae-Buneman.txt"));
        np2.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np2, taxaFromST42);
        SplitsNexusIO.parse(np2, taxaFromST42, splitsFromST42, null);

        for (int i = 0; i < splitsBlock2.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock2.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST42.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.000001);
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }
    }

}