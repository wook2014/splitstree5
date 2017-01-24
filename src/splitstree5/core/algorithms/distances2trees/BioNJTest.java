package splitstree5.core.algorithms.distances2trees;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;

import static junit.framework.Assert.assertEquals;


public class BioNJTest {

    private BioNJ bioNJ = new BioNJ();

    @Test
    public void testCompute() throws Exception {

        String output = "(red-purple:18.0625,purple-reddish:12.9375,(purple:16.25,(purple-blue:19.35," +
                        "(blue:20.328125,(green:13.9375,(((red:30.071428,yellow:24.928572):34.729168,yellowish:14.270833)" +
                        ":12.78125,greenish:14.71875):12.5625):18.25):18.546875):19.4):9.25)";

        String[] names = {"red-purple",
                "red",
                "yellow",
                "yellowish",
                "greenish",
                "green",
                "blue",
                "purple-blue",
                "purple",
                "purple-reddish"};

        Taxon[] taxons = new Taxon[10];
        TaxaBlock taxaBlock = new TaxaBlock();
        for (int i = 0; i < names.length; i++) {
            taxons[i] = new Taxon();
            taxons[i].setName(names[i]);
            taxaBlock.getTaxa().add(taxons[i]);
        }
        DistancesBlock distancesBlock = new DistancesBlock();
        double[][] dist = {
                {0.0,	61.0,	105.0,	121.0,	131.0,	124.0,	106.0,	85.0,	52.0,	31.0},
                {61.0,	0.0,	55.0,	99.0,	116.0,	124.0,	130.0,	125.0,	99.0,	80.0},
                {105.0,	55.0,	0.0,	54.0,	81.0,	101.0,	121.0,	131.0,	128.0,	118.0},
                {121.0,	99.0,	54.0,	0.0,	33.0,	55.0,	85.0,	106.0,	121.0,	124.0},
                {131.0,	116.0,	81.0,	33.0,	0.0,	35.0,	65.0,	93.0,	116.0,	121.0},
                {124.0,	124.0,	101.0,	55.0,	35.0,	0.0,	41.0,	73.0,	102.0,	115.0},
                {106.0,	130.0,	121.0,	85.0,	65.0,	41.0,	0.0,	45.0,	84.0,	94.0},
                {85.0,	125.0,	131.0,	106.0,	93.0,	73.0,	45.0,	0.0,	45.0,	62.0},
                {52.0,	99.0,	128.0,	121.0,	116.0,	102.0,	84.0,	45.0,	0.0,	30.0},
                {31.0,	80.0,	118.0,	124.0,	121.0,	115.0,	94.0,	62.0,	30.0,	0.0}};
        distancesBlock.set(dist);

        ProgressListener pl = new ProgressPercentage();
        TreesBlock treesBlock = new TreesBlock();

        bioNJ.compute(pl, taxaBlock, distancesBlock, treesBlock);
        System.out.println("output: " + treesBlock.getTrees().get(0).toString());
        assertEquals(output, treesBlock.getTrees().get(0).toString());

    }

}