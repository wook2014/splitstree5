/*
 * SuperNetworkTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.trees2splits;

import jloda.util.progress.ProgressPercentage;
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

public class SuperNetworkTest {

    final SuperNetwork superNetwork = new SuperNetwork();

    @Test
    public void compute() throws Exception {

        // TEXT partial 1

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees6-partial.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxaBlock);
        new TreesNexusInput().parse(np, taxaBlock, treesBlock);

        final SplitsBlock splitsBlock = new SplitsBlock();
        System.err.println(treesBlock.isPartial());
        treesBlock.setPartial(true);
        superNetwork.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

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
        NexusStreamParser np4 = new NexusStreamParser(new FileReader("test/splits/trees6-partial-SuperNet.nex"));
        np4.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np4, taxaFromST4);
        new SplitsNexusInput().parse(np4, taxaFromST4, splitsFromST4);

        double[] confidences = {33.0, 33.0, 33.0, 33.0, 33.0, 33.0, 33.0, 33.0, 33.0, 33.0, 33.0, 28.0, 28.0, 28.0};
        assertEquals(splitsBlock.size(), splitsFromST4.size());
        for (int i = 0; i < splitsBlock.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST4.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);

            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getConfidence(), confidences[i], 0.0);

            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        System.err.print(treesBlock.isPartial());


        // TEXT partial 2

        TaxaBlock taxaBlock1 = new TaxaBlock();
        TreesBlock treesBlock1 = new TreesBlock();
        NexusStreamParser np1 = new NexusStreamParser(new FileReader("test/nexus/trees49-taxa.nex"));
        np1.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np1, taxaBlock1);
        new TreesNexusInput().parse(np1, taxaBlock1, treesBlock1);

        final SplitsBlock splitsBlock1 = new SplitsBlock();
        System.err.println(treesBlock1.isPartial());
        treesBlock1.setPartial(true);
        superNetwork.compute(new ProgressPercentage(), taxaBlock1, treesBlock1, splitsBlock1);

        // printing
        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxaBlock1);
        new TreesNexusOutput().write(w1, taxaBlock1, treesBlock1);
        new SplitsNexusOutput().write(w1, taxaBlock1, splitsBlock1);
        System.err.println(w1.toString());

        // compare splits
        TaxaBlock taxaFromST41 = new TaxaBlock();
        SplitsBlock splitsFromST41 = new SplitsBlock();
        NexusStreamParser np41 = new NexusStreamParser(new FileReader("test/splits/trees49-SuperNet-noFilter.nex"));
        np41.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np41, taxaFromST41);
        new SplitsNexusInput().parse(np41, taxaFromST41, splitsFromST41);

        double[] confidences1 = {4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4367.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4457.0, 4367.0, 4457.0};

        assertEquals(splitsBlock1.size(), splitsFromST41.size());
        for (int i = 0; i < splitsBlock1.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock1.getSplits().get(i);
            if (splitsFromST41.getSplits().contains(aSplit)) {
                int index = splitsFromST41.getSplits().indexOf(aSplit);
                ASplit aSplitST4 = splitsFromST41.getSplits().get(index);
                assertEquals(aSplit.getA(), aSplitST4.getA());
                assertEquals(aSplit.getB(), aSplitST4.getB());
                assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);

                //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
                assertEquals(aSplit.getConfidence(), confidences1[i], 0.0);

                assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
            }
            splitsFromST41.getSplits().remove(aSplit);
        }
        assertEquals(0, splitsFromST41.size());

        System.err.print(treesBlock1.isPartial());


        // TEST 2
        TaxaBlock taxaBlock2 = new TaxaBlock();
        TreesBlock treesBlock2 = new TreesBlock();
        NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np2.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np2, taxaBlock2);
        new TreesNexusInput().parse(np2, taxaBlock2, treesBlock2);

        final SplitsBlock splitsBlock2 = new SplitsBlock();
        superNetwork.compute(new ProgressPercentage(), taxaBlock2, treesBlock2, splitsBlock2);

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
        NexusStreamParser np42 = new NexusStreamParser(new FileReader("test/splits/trees6-SuperNet.nex"));
        np42.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np42, taxaFromST42);
        new SplitsNexusInput().parse(np42, taxaFromST42, splitsFromST42);

        double[] confidences2 = {36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0};
        assertEquals(splitsBlock2.size(), splitsFromST42.size());
        for (int i = 0; i < splitsBlock2.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock2.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST42.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getConfidence(), confidences2[i], 0.0);

            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        // TEST 3
        TaxaBlock taxaBlock3 = new TaxaBlock();
        TreesBlock treesBlock3 = new TreesBlock();
        NexusStreamParser np3 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np3.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np3, taxaBlock3);
        new TreesNexusInput().parse(np3, taxaBlock3, treesBlock3);

        final SplitsBlock splitsBlock3 = new SplitsBlock();
        superNetwork.setOptionZRule(false);
        superNetwork.setOptionSuperTree(true);
        superNetwork.setOptionNumberOfRuns(3);
        superNetwork.setOptionEdgeWeights(SuperNetwork.EdgeWeights.Mean);
        superNetwork.setOptionApplyRefineHeuristic(true);
        superNetwork.compute(new ProgressPercentage(), taxaBlock3, treesBlock3, splitsBlock3);

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
        NexusStreamParser np43 = new NexusStreamParser(new FileReader("test/splits/trees6-SuperNet-Param.nex"));
        np43.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np43, taxaFromST43);
        new SplitsNexusInput().parse(np43, taxaFromST43, splitsFromST43);

        double[] confidences3 = {36.0, 36.0, 36.0, 36.0, 36.0, 36.0, 36.0};
        assertEquals(splitsBlock3.size(), splitsFromST43.size());
        for (int i = 0; i < splitsBlock3.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock3.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST43.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getConfidence(), confidences3[i], 0.0);

            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }

        // TEST 4
        TaxaBlock taxaBlock4 = new TaxaBlock();
        TreesBlock treesBlock4 = new TreesBlock();
        NexusStreamParser np_4 = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np_4.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np_4, taxaBlock4);
        new TreesNexusInput().parse(np_4, taxaBlock4, treesBlock4);

        final SplitsBlock splitsBlock4 = new SplitsBlock();
        superNetwork.setOptionZRule(true);
        superNetwork.setOptionSuperTree(false);
        superNetwork.setOptionNumberOfRuns(3);
        superNetwork.setOptionEdgeWeights(SuperNetwork.EdgeWeights.AverageRelative);
        superNetwork.setOptionApplyRefineHeuristic(false);
        superNetwork.compute(new ProgressPercentage(), taxaBlock4, treesBlock4, splitsBlock4);

        // printing
        final StringWriter w4 = new StringWriter();
        w4.write("#nexus\n");
        new TaxaNexusOutput().write(w4, taxaBlock4);
        new TreesNexusOutput().write(w4, taxaBlock4, treesBlock4);
        new SplitsNexusOutput().write(w4, taxaBlock4, splitsBlock4);
        System.err.println(w4.toString());

        // compare splits
        TaxaBlock taxaFromST44 = new TaxaBlock();
        SplitsBlock splitsFromST44 = new SplitsBlock();
        NexusStreamParser np44 = new NexusStreamParser(new FileReader("test/splits/trees6-SuperNet-AR.nex"));
        np44.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np44, taxaFromST44);
        new SplitsNexusInput().parse(np44, taxaFromST44, splitsFromST44);

        double[] confidences4 = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        assertEquals(splitsBlock4.size(), splitsFromST44.size());
        for (int i = 0; i < splitsBlock4.getSplits().size(); i++) {
            ASplit aSplit = splitsBlock4.getSplits().get(i);
            ASplit aSplitST4 = splitsFromST44.getSplits().get(i);
            assertEquals(aSplit.getA(), aSplitST4.getA());
            assertEquals(aSplit.getB(), aSplitST4.getB());
            assertEquals(aSplit.getWeight(), aSplitST4.getWeight(), 0.0);
            //assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
            assertEquals(aSplit.getConfidence(), confidences4[i], 0.0);

            assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
        }
    }

}