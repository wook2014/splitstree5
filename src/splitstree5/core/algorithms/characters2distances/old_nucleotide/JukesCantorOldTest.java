/*
 * JukesCantorOldTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances.old_nucleotide;

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
 * Created on 21.02.2017.
 *
 * @author Daria
 * @deprecated
 */
public class JukesCantorOldTest {

    final JukesCantor_old jukesCantor = new JukesCantor_old();

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

        jukesCantor.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        double[][] must_dist = {
                {0.0, 0.09599253481571762, 0.11125592197089967, 0.10273794284816345, 0.004444457450660452, 0.0793897539017834},
                {0.09599253481571762, 0.0, 0.09935765543591227, 0.09599253481571743, 0.09935765543591227, 0.10783711263212005},
                {0.11125592197089967, 0.09935765543591227, 0.0, 0.12683640148525838, 0.11469038704527285, 0.11125592197089948},
                {0.10273794284816345, 0.09599253481571743, 0.12683640148525838, 0.0, 0.10613353438688591, 0.10613353438688591},
                {0.004444457450660452, 0.09935765543591227, 0.11469038704527285, 0.10613353438688591, 0.0, 0.08268103729669674},
                {0.0793897539017834, 0.10783711263212005, 0.11125592197089948, 0.10613353438688591, 0.08268103729669674, 0.0}
        };

        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            assertArrayEquals(must_dist[i], distancesBlock.getDistances()[i], 0);
        }
    }

}