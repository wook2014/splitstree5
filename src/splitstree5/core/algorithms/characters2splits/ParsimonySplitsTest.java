/*
 * ParsimonySplitsTest.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2splits;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.SplitsNexusOutput;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.io.nexus.TaxaNexusOutput;

import java.io.FileReader;
import java.io.StringWriter;

public class ParsimonySplitsTest {

    private final ParsimonySplits parsimonySplits = new ParsimonySplits();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxa = new TaxaBlock();
        SplitsBlock splits = new SplitsBlock();
        CharactersBlock characters = new CharactersBlock();
        ProgressListener pl = new ProgressPercentage();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae_char.nex"));
        //NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/small_test.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new CharactersNexusInput().parse(np, taxa, characters);

        parsimonySplits.compute(pl, taxa, characters, splits);

        final StringWriter w1 = new StringWriter();
        w1.write("#nexus\n");
        new TaxaNexusOutput().write(w1, taxa);
        new SplitsNexusOutput().write(w1, taxa, splits);
        System.err.println(w1.toString());

        /*TaxaBlock taxaFromST4 = new TaxaBlock();
        SplitsBlock splitsFromSt4 = new SplitsBlock();
        NexusStreamParser np1 = new NexusStreamParser(new FileReader("test/nexus/algae.nex"));
        np1.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np1, taxaFromST4);
        new SplitsNexusInput().parse(np1, taxaFromST4, splitsFromSt4, null);*/

    }

}