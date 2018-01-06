/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.DistancesNexusIO;

import java.io.FileReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * test the Hamming distances computation
 * <p>
 * Created on 2/3/17.
 *
 * @author huson
 */
public class HammingDistancesTest {
    @Test
    public void testSimpleDistances() throws Exception {
        final TaxaBlock taxa = new TaxaBlock();
        final CharactersBlock characters = new CharactersBlock();
        final DistancesBlock distances = new DistancesBlock();
        taxa.addTaxaByNames(CharactersNexusIO.parse(new NexusStreamParser(new FileReader("test/nexus/characters-simple.nex")), taxa, characters, null));

        final HammingDistances hammingDistances = new HammingDistances();

        hammingDistances.compute(new ProgressPercentage(), taxa, characters, distances);

        StringWriter w = new StringWriter();
        DistancesNexusIO.write(w, taxa, distances, null);
        System.err.println(w.toString());

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames(DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test/nexus/distances-simple.nex")), taxaFromSplitsTree4, distancesFromSplitsTree4, null));

        StringWriter wFromSplitsTree4 = new StringWriter();
        DistancesNexusIO.write(wFromSplitsTree4, taxaFromSplitsTree4, distancesFromSplitsTree4, null);
        //System.err.println(wFromSplitsTree4.toString());

        assertEquals(w.toString(), wFromSplitsTree4.toString());

    }

}