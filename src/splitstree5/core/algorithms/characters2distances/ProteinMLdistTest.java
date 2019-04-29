/*
 *  ProteinMLdistTest.java Copyright (C) 2019 Daniel H. Huson
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

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.DistancesNexusInput;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on Feb 2017.
 *
 * @author Daria
 */

public class ProteinMLdistTest {

    final ProteinMLdist pML = new ProteinMLdist();

    @Test
    public void testCompute() throws Exception {

        String inputFile = "test//characters//myosin_aa.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        pML.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//myosinProtML.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4));


        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }

        // test 2

        List<String> taxonNames2 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock);
        taxaBlock.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();


        pML.setOptionModel(ProteinMLdist.Model.pmb);
        pML.compute(pl, taxaBlock, charactersBlock, distancesBlock2);

        final TaxaBlock taxaFromSplitsTree4_2 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4_2 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//myosinProtML_pmb.nex")),
                        taxaFromSplitsTree4_2, distancesFromSplitsTree4_2));


        for (int i = 0; i < distancesBlock2.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree4_2.getDistances()[i], distancesBlock2.getDistances()[i], 0.000001);
        }
    }

}