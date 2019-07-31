/*
 *  TreeSelectorSplitsTest.java Copyright (C) 2019 Daniel H. Huson
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
 * Created on 24.01.2017.
 *
 * @author Daria
 */

public class TreeSelectorSplitsTest {

    final TreeSelectorSplits treeSelector = new TreeSelectorSplits();

    @Test
    public void testCompute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxaBlock);
        new TreesNexusInput().parse(np, taxaBlock, treesBlock);

        for (int which = 1; which <= treesBlock.getNTrees(); which++) {

            System.err.println("COMPUTE SPLITS FOR TREE" + which);

            SplitsBlock splitsBlock = new SplitsBlock();
            treeSelector.setOptionWhich(which);
            treeSelector.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

            final StringWriter w = new StringWriter();
            w.write("#nexus\n");
            new TaxaNexusOutput().write(w, taxaBlock);
            new TreesNexusOutput().write(w, taxaBlock, treesBlock);
            new SplitsNexusOutput().write(w, taxaBlock, splitsBlock);
            System.err.println(w.toString());

            // compare splits

            String fileName = "test/splits/tree selector/trees6-" + which + ".nex";

            TaxaBlock taxaFromST4 = new TaxaBlock();
            SplitsBlock splitsFromST4 = new SplitsBlock();
            NexusStreamParser np4 = new NexusStreamParser(new FileReader(fileName));
            np4.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np4, taxaFromST4);
            new SplitsNexusInput().parse(np4, taxaFromST4, splitsFromST4);

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