/*
 * LogDetTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on 22.02.2017.
 *
 * @author Daria
 */
public class LogDetTest {

    final LogDet logDet = new LogDet();

    @Test
    public void testCompute() throws Exception {

        String inputFile = "test//characters//bees_dna_interleave.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        logDet.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        double[][] must_dist = {
                {0.0, 0.08840697494340972, 0.10929642036356128, 0.09666305856063594, 0.006158003731885308, 0.07668734238595977},
                {0.08840697494340972, 0.0, 0.08704838233386236, 0.08912904045753299, 0.09185514250183825, 0.1025178572437495},
                {0.10929642036356128, 0.08704838233386236, 0.0, 0.11406122758106056, 0.11283547273859765, 0.1083728721443404},
                {0.09666305856063594, 0.08912904045753299, 0.11406122758106056, 0.0, 0.09997871489180964, 0.09546887605217587},
                {0.006158003731885308, 0.09185514250183825, 0.11283547273859765, 0.09997871489180964, 0.0, 0.08003562509457929},
                {0.07668734238595977, 0.1025178572437495, 0.1083728721443404, 0.09546887605217587, 0.08003562509457929, 0.0}
        };

        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            assertArrayEquals(must_dist[i], distancesBlock.getDistances()[i], 0);
        }

    }

}