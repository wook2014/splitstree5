package splitstree5.core.algorithms.characters2distances;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusIO;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on 21.02.2017.
 *
 * @author Daria
 */
public class F81Test {

    final F81 f81 = new F81();

    @Test
    public void testCompute() throws Exception {

        String inputFile = "test//characters//bees_dna_interleave.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        f81.compute(pl, taxaBlock, charactersBlock, distancesBlock);

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