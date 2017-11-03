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
import splitstree5.io.nexus.DistancesNexusIO;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on 05.03.2017.
 * @author Daria
 */
public class HKY85Test {

    final HKY85 hky85 = new HKY85();

    @Test
    public void compute() throws Exception {

        // TEST 0
        String inputFile = "test//characters//bees_dna_interleave.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock = new TaxaBlock();
        CharactersBlock charactersBlock = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile)), taxaBlock, charactersBlock, format);
        taxaBlock.addTaxaByNames(taxonNames);
        DistancesBlock distancesBlock = new DistancesBlock();

        hky85.compute(pl, taxaBlock, charactersBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//bees-HKY85.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4, null));


        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.00000001);
        }

        // TEST 1
        String inputFile1 = "test//characters//algae_rna_interleave.nex";
        ProgressListener pl1 = new ProgressPercentage();
        TaxaBlock taxaBlock1 = new TaxaBlock();
        CharactersBlock charactersBlock1 = new CharactersBlock();

        CharactersNexusFormat format1 = new CharactersNexusFormat();
        List<String> taxonNames1 = CharactersNexusIO.parse(new NexusStreamParser(new FileReader(inputFile1)), taxaBlock1,
                charactersBlock1, format1);
        taxaBlock1.addTaxaByNames(taxonNames1);
        DistancesBlock distancesBlock1 = new DistancesBlock();

        hky85.compute(pl1, taxaBlock1, charactersBlock1, distancesBlock1);

        final TaxaBlock taxaFromSplitsTree41 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree41 = new DistancesBlock();
        taxaFromSplitsTree41.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//algae-HKY85.nex")),
                        taxaFromSplitsTree41, distancesFromSplitsTree41, null));


        for(int i = 0; i<distancesBlock1.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree41.getDistances()[i], distancesBlock1.getDistances()[i], 0.00000001);
        }

    }

}