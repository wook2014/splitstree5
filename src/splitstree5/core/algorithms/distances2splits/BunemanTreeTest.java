/*
 * BunemanTreeTest.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package splitstree5.core.algorithms.distances2splits;

import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.io.nexus.*;

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
            new TaxaNexusInput().parse(np, taxaBlock);
            new DistancesNexusInput().parse(np, taxaBlock, distancesBlock);
            assertEquals(taxaBlock.getNtax(), 7);
            assertEquals(distancesBlock.getNtax(), 7);
        }

        final SplitsBlock splitsBlock = new SplitsBlock();

        algorithm.compute(new ProgressPercentage(), taxaBlock, distancesBlock, splitsBlock);

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new DistancesNexusOutput().write(w, taxaBlock, distancesBlock);
        new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);
        System.err.println(w.toString());

        // compare splits

        TaxaBlock taxaFromST4 = new TaxaBlock();
        SplitsBlock splitsFromST4 = new SplitsBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/splits/distances7-Buneman.txt"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxaFromST4);
        new SplitsNexusInput().parse(np, taxaFromST4, splitsFromST4);

        for (int i = 0; i < splitsBlock.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST4.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            //assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence(), 0.0);
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
            new TaxaNexusInput().parse(np2, taxaBlock2);
            new DistancesNexusInput().parse(np2, taxaBlock2, distancesBlock2);
            assertEquals(taxaBlock2.getNtax(), 8);
            assertEquals(distancesBlock2.getNtax(), 8);
        }

        final SplitsBlock splitsBlock2 = new SplitsBlock();

        algorithm.compute(new ProgressPercentage(), taxaBlock2, distancesBlock2, splitsBlock2);

        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock2);
        new DistancesNexusOutput().write(w2, taxaBlock2, distancesBlock2);
        new SplitsNexusOutput().write(w2, taxaBlock2, splitsBlock2);
        System.err.println(w2.toString());

        // compare splits

        TaxaBlock taxaFromST42 = new TaxaBlock();
        SplitsBlock splitsFromST42 = new SplitsBlock();
        NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/splits/algae-Buneman.txt"));
        np2.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np2, taxaFromST42);
        new SplitsNexusInput().parse(np2, taxaFromST42, splitsFromST42);

        for (int i = 0; i < splitsBlock2.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock2.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST42.getSplits().get(i);
            //System.err.println("split" + i+aSplit.getB());
            System.err.println("split A" + i + " from st4" + aSplitST4.getA());
            System.err.println("split B" + i + " from st4" + aSplitST4.getB());
            System.err.println("split A" + i + " from st5" + aSplit.getA());
            System.err.println("split B" + i + " from st5" + aSplit.getB());
            assertEquals(aSplit.getA(), aSplitST4.getA());

            //todo problem in parser: the read B set from ST4 is a complete set
            //assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.000001);
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence(), 0.0);
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }
    }

}