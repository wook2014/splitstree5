/*
 * JaccardTest.java Copyright (C) 2020. Daniel H. Huson
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
import static org.junit.Assert.assertEquals;

/**
 * Created on 05.03.2017.
 *
 * @author Daria
 */
public class JaccardTest {

    final Jaccard jaccard = new Jaccard();

    @Test
    public void isApplicable() throws Exception {

        String inputFile1 = "test//characters//bees_dna_interleave.nex";
        TaxaBlock taxaBlock1 = new TaxaBlock();
        CharactersBlock charactersBlock1 = new CharactersBlock();
        CharactersNexusFormat format1 = new CharactersNexusFormat();
        List<String> taxonNames1 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile1)), taxaBlock1, charactersBlock1);
        taxaBlock1.addTaxaByNames(taxonNames1);
        DistancesBlock distancesBlock1 = new DistancesBlock();

        assertEquals(false, jaccard.isApplicable(taxaBlock1, charactersBlock1));

        String inputFile2 = "test//characters//algae_rna_interleave.nex";
        TaxaBlock taxaBlock2 = new TaxaBlock();
        CharactersBlock charactersBlock2 = new CharactersBlock();
        CharactersNexusFormat format2 = new CharactersNexusFormat();
        List<String> taxonNames2 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile2)), taxaBlock2, charactersBlock2);
        taxaBlock2.addTaxaByNames(taxonNames2);
        DistancesBlock distancesBlock2 = new DistancesBlock();

        assertEquals(false, jaccard.isApplicable(taxaBlock2, charactersBlock2));

        String inputFile3 = "test//characters//dolphins_binary.nex";
        TaxaBlock taxaBlock3 = new TaxaBlock();
        CharactersBlock charactersBlock3 = new CharactersBlock();
        CharactersNexusFormat format3 = new CharactersNexusFormat();
        List<String> taxonNames3 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile3)), taxaBlock3, charactersBlock3);
        taxaBlock3.addTaxaByNames(taxonNames3);
        DistancesBlock distancesBlock3 = new DistancesBlock();

        assertEquals(true, jaccard.isApplicable(taxaBlock3, charactersBlock3));

        String inputFile4 = "test//characters//myosin_aa.nex";
        TaxaBlock taxaBlock4 = new TaxaBlock();
        CharactersBlock charactersBlock4 = new CharactersBlock();
        CharactersNexusFormat format4 = new CharactersNexusFormat();
        List<String> taxonNames4 = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile4)), taxaBlock4, charactersBlock4);
        taxaBlock4.addTaxaByNames(taxonNames4);
        DistancesBlock distancesBlock4 = new DistancesBlock();

        assertEquals(false, jaccard.isApplicable(taxaBlock4, charactersBlock4));

    }

    @Test
    public void compute() throws Exception {

        String inputFile = "test//characters//dolphins_binary.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        jaccard.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//dolphinsJaccard.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4));


        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }

    }

}