/*
 *  AverageConsensusTest.java Copyright (C) 2019 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

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

public class AverageConsensusTest {

    final AverageConsensus algorithm = new AverageConsensus();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/trees/beesUPGMA.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxaBlock);
        new TreesNexusInput().parse(np, taxaBlock, treesBlock);

        final SplitsBlock splitsBlock = new SplitsBlock();
        algorithm.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

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
        NexusStreamParser np4 = new NexusStreamParser(new FileReader("test/splits/bees-AverageConsensus.nex"));
        np4.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np4, taxaFromST4);
        new SplitsNexusInput().parse(np4, taxaFromST4, splitsFromST4);

        for (int i = 0; i < splitsBlock.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST4.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence(), 0.0);
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        // TEST 2
        TaxaBlock taxaBlock2 = new TaxaBlock();
        TreesBlock treesBlock2 = new TreesBlock();
        NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np2.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np2, taxaBlock2);
        new TreesNexusInput().parse(np2, taxaBlock2, treesBlock2);

        final SplitsBlock splitsBlock2 = new SplitsBlock();
        algorithm.compute(new ProgressPercentage(), taxaBlock2, treesBlock2, splitsBlock2);

        // printing
        final StringWriter w2 = new StringWriter();
        w2.write("#nexus\n");
        new TaxaNexusOutput().write(w2, taxaBlock2);
        new TreesNexusOutput().write(w2, taxaBlock2, treesBlock2);
        new SplitsNexusOutput().write(w2, taxaBlock2, splitsBlock2);
        System.err.println(w2.toString());

        // compare splits
        TaxaBlock taxaFromST42 = new TaxaBlock();
        SplitsBlock splitsFromST42 = new SplitsBlock();
        NexusStreamParser np42 = new NexusStreamParser(new FileReader("test/splits/trees6-AverageConsensus.nex"));
        np42.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np42, taxaFromST42);
        new SplitsNexusInput().parse(np42, taxaFromST42, splitsFromST42);

        for (int i = 0; i < splitsBlock2.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock2.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST42.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
            assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence(), 0.0);
            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        // TEST 3
        TaxaBlock taxaBlock3 = new TaxaBlock();
        TreesBlock treesBlock3 = new TreesBlock();
        NexusStreamParser np3 = new NexusStreamParser(new FileReader("test/trees/dolphins-NJ.nex"));
        np3.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np3, taxaBlock3);
        new TreesNexusInput().parse(np3, taxaBlock3, treesBlock3);

        final SplitsBlock splitsBlock3 = new SplitsBlock();
        algorithm.compute(new ProgressPercentage(), taxaBlock3, treesBlock3, splitsBlock3);

        // printing
        final StringWriter w3 = new StringWriter();
        w3.write("#nexus\n");
        new TaxaNexusOutput().write(w3, taxaBlock3);
        new TreesNexusOutput().write(w3, taxaBlock3, treesBlock3);
        new SplitsNexusOutput().write(w3, taxaBlock3, splitsBlock3);
        System.err.println(w3.toString());

        // compare splits
        TaxaBlock taxaFromST43 = new TaxaBlock();
        SplitsBlock splitsFromST43 = new SplitsBlock();
        NexusStreamParser np43 = new NexusStreamParser(new FileReader("test/splits/dolphins-AverageConsensus.nex"));
        np43.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np43, taxaFromST43);
        new SplitsNexusInput().parse(np43, taxaFromST43, splitsFromST43);

        assertEquals(splitsBlock3.size(), splitsFromST43.size());
        for (int i = 0; i < splitsBlock3.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock3.getSplits().get(i);
            if (splitsFromST43.getSplits().contains(aSplit)) {
                int index = splitsFromST43.getSplits().indexOf(aSplit);
                ASplit aSplitST4 = splitsFromST43.getSplits().get(index);
                assertEquals(aSplit.getA(), aSplitST4.getA());
                assertEquals(aSplit.getB(), aSplitST4.getB());
                assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0000001);
                assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence(), 0.0);
                assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
            }
            splitsFromST43.getSplits().remove(aSplit);
        }
        assertEquals(0, splitsFromST43.size());
    }
}