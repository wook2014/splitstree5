/*
 * UpholtTest.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.core.algorithms.characters2distances;

import jloda.util.parse.NexusStreamParser;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.*;

import java.io.FileReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on 22.02.2017.
 *
 * @author Daria
 */
public class UpholtTest {

    final Upholt upholt = new Upholt();

    @Test
    public void compute() throws Exception {

        //String inputFile = "test//characters//dolphins_binary.nex";
        String inputFile = "test//characters//aflp.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        upholt.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final StringWriter w = new StringWriter();
        w.write("#nexus\n");
        new TaxaNexusOutput().write(w, taxaBlock);
        new DistancesNexusOutput().write(w, taxaBlock, distancesBlock);
        System.err.println(w.toString());

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (new DistancesNexusInput().parse(new NexusStreamParser(new FileReader("test//distances//aflp-upholt.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4));


        for (int i = 0; i < distancesBlock.getDistances().length; i++) {
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }

    }

}