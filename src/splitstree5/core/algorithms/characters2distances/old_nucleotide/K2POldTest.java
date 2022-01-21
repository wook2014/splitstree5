/*
 * K2POldTest.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms.characters2distances.old_nucleotide;

import jloda.util.parse.NexusStreamParser;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
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
 * @deprecated
 */
public class K2POldTest {

    final K2P_old k2p = new K2P_old();

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

        k2p.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        double[][] must_dist = {
                {0.0, 0.09603833147344673, 0.11138422480739832, 0.10277816036683766, 0.004444457450660376, 0.07946208583724378},
                {0.09603833147344673, 0.0, 0.09935978585579598, 0.09605998846944912, 0.099391629224611, 0.10789879791010017},
                {0.11138422480739832, 0.09935978585579598, 0.0, 0.12685267099401598, 0.11479840513801558, 0.11132616896161009},
                {0.10277816036683766, 0.09605998846944912, 0.12685267099401598, 0.0, 0.10616261188368532, 0.10613955554417535},
                {0.004444457450660376, 0.099391629224611, 0.11479840513801558, 0.10616261188368532, 0.0, 0.08273873144025545},
                {0.07946208583724378, 0.10789879791010017, 0.11132616896161009, 0.10613955554417535, 0.08273873144025545, 0.0}
        };

        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            assertArrayEquals(must_dist[i], distancesBlock.getDistances()[i], 0);
        }

    }

}