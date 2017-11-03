package splitstree5.core.algorithms.trees2distances;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.algorithms.distances2trees.NeighborJoining;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;
import splitstree5.io.nexus.TreesNexusIO;

import java.io.FileReader;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created on 07.06.2017.
 * @author Daria
 */

public class AverageDistancesTest {

    final AverageDistances averageDistances = new AverageDistances();

    @Test
    public void compute() throws Exception {

        // TEST 1
        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxaBlock);
        TreesNexusIO.parse(np, taxaBlock, treesBlock, null);

        ProgressListener pl = new ProgressPercentage();
        DistancesBlock distancesBlock = new DistancesBlock();
        averageDistances.compute(pl, taxaBlock, treesBlock, distancesBlock);

        final TaxaBlock taxaFromSplitsTree4 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree4 = new DistancesBlock();
        taxaFromSplitsTree4.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test/distances/trees6-averageDist.nex")),
                        taxaFromSplitsTree4, distancesFromSplitsTree4, null));

        for(int i = 0; i<distancesBlock.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree4.getDistances()[i], distancesBlock.getDistances()[i], 0.000001);
        }

        // TEST 2
        TaxaBlock taxaBlock2 = new TaxaBlock();
        TreesBlock treesBlock2 = new TreesBlock();
        NexusStreamParser np2 = new NexusStreamParser(new FileReader("test/nexus/colors.nex"));
        np2.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np2, taxaBlock2);

        // compute NJ tree from distances
        DistancesBlock colors = new DistancesBlock();
        DistancesNexusIO.parse(np2, taxaBlock2, colors, null);
        final NeighborJoining nj = new NeighborJoining();
        nj.compute(new ProgressPercentage(), taxaBlock2, colors, treesBlock2);

        // compute averageDist
        ProgressListener pl2 = new ProgressPercentage();
        DistancesBlock distancesBlock2 = new DistancesBlock();
        averageDistances.compute(pl2, taxaBlock2, treesBlock2, distancesBlock2);

        final TaxaBlock taxaFromSplitsTree42 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree42 = new DistancesBlock();
        taxaFromSplitsTree42.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test/distances/colors-NJ-averageDist.nex")),
                        taxaFromSplitsTree42, distancesFromSplitsTree42, null));

        for(int i = 0; i<distancesBlock2.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree42.getDistances()[i], distancesBlock2.getDistances()[i], 0.00001);
        }


        // TEST 3
        TaxaBlock taxaBlock3 = new TaxaBlock();
        TreesBlock treesBlock3 = new TreesBlock();
        NexusStreamParser np3 = new NexusStreamParser(new FileReader("test/trees/dolphins-NJ.nex"));
        np3.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np3, taxaBlock3);
        TreesNexusIO.parse(np3, taxaBlock3, treesBlock3, null);

        ProgressListener pl3 = new ProgressPercentage();
        DistancesBlock distancesBlock3 = new DistancesBlock();
        averageDistances.compute(pl3, taxaBlock3, treesBlock3, distancesBlock3);

        final TaxaBlock taxaFromSplitsTree43 = new TaxaBlock();
        final DistancesBlock distancesFromSplitsTree43 = new DistancesBlock();
        taxaFromSplitsTree43.addTaxaByNames
                (DistancesNexusIO.parse(new NexusStreamParser(new FileReader("test/distances/dolphins-NJ-averageDist.nex")),
                        taxaFromSplitsTree43, distancesFromSplitsTree43, null));

        for(int i = 0; i<distancesBlock3.getDistances().length; i++){
            assertArrayEquals(distancesFromSplitsTree43.getDistances()[i], distancesBlock3.getDistances()[i], 0.000001);
        }

    }

}