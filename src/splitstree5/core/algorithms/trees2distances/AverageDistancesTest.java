package splitstree5.core.algorithms.trees2distances;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.*;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Daria on 07.06.2017.
 */
public class AverageDistancesTest {

    final AverageDistances averageDistances = new AverageDistances();

    @Test
    public void compute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/trees/beesUPGMA.nex"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxaBlock);
        TreesNexusIO.parse(np, taxaBlock, treesBlock, null);

        ProgressListener pl = new ProgressPercentage();
        DistancesBlock distancesBlock = new DistancesBlock();
        averageDistances.compute(pl, taxaBlock, treesBlock, distancesBlock);

        /*final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test//distances//mini_rightDiploidCod.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4, null));

        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }*/


    }

}