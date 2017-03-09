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

        String output1 = "(red-purple:18.0625,purple-reddish:12.9375,(purple:16.25,(purple-blue:19.35," +
                "(blue:20.328125,(green:13.9375,(((red:30.071428,yellow:24.928572):34.729168,yellowish:14.270833)" +
                ":12.78125,greenish:14.71875):12.5625):18.25):18.546875):19.4):9.25)";

        String[] names1 = {"red-purple",
                "red",
                "yellow",
                "yellowish",
                "greenish",
                "green",
                "blue",
                "purple-blue",
                "purple",
                "purple-reddish"};

        Taxon[] taxons1 = new Taxon[10];
        TaxaBlock taxaBlock1 = new TaxaBlock();
        for (int i = 0; i < names1.length; i++) {
            taxons1[i] = new Taxon();
            taxons1[i].setName(names1[i]);
            taxaBlock1.getTaxa().add(taxons1[i]);
        }
        DistancesBlock distancesBlock1 = new DistancesBlock();
        double[][] dist1 = {
                {0.0, 61.0, 105.0, 121.0, 131.0, 124.0, 106.0, 85.0, 52.0, 31.0},
                {61.0, 0.0, 55.0, 99.0, 116.0, 124.0, 130.0, 125.0, 99.0, 80.0},
                {105.0, 55.0, 0.0, 54.0, 81.0, 101.0, 121.0, 131.0, 128.0, 118.0},
                {121.0, 99.0, 54.0, 0.0, 33.0, 55.0, 85.0, 106.0, 121.0, 124.0},
                {131.0, 116.0, 81.0, 33.0, 0.0, 35.0, 65.0, 93.0, 116.0, 121.0},
                {124.0, 124.0, 101.0, 55.0, 35.0, 0.0, 41.0, 73.0, 102.0, 115.0},
                {106.0, 130.0, 121.0, 85.0, 65.0, 41.0, 0.0, 45.0, 84.0, 94.0},
                {85.0, 125.0, 131.0, 106.0, 93.0, 73.0, 45.0, 0.0, 45.0, 62.0},
                {52.0, 99.0, 128.0, 121.0, 116.0, 102.0, 84.0, 45.0, 0.0, 30.0},
                {31.0, 80.0, 118.0, 124.0, 121.0, 115.0, 94.0, 62.0, 30.0, 0.0}};
        distancesBlock1.set(dist1);


        // TEST 2
        final String output2 = "(Tobacco:0.008836137,Rice:0.017279206,(Marchantia:0.010631585," +
                "(Chlorella:0.031623945,(Chlamydomonas:0.06331665," +
                "((Euglena:0.0679614,Olithodiscus:0.06720343):0.008690414,Anacystis_nidulans:0.07131875):0.008862138)" +
                ":0.0036498776):0.024320895):0.011652796)";


        String[] names2 = {"Tobacco",
                "Rice",
                "Marchantia",
                "Chlamydomonas",
                "Chlorella",
                "Euglena",
                "Anacystis_nidulans",
                "Olithodiscus"};
        Taxon[] taxons2 = new Taxon[8];
        TaxaBlock taxaBlock2 = new TaxaBlock();
        for (int i = 0; i < names2.length; i++) {
            taxons2[i] = new Taxon();
            taxons2[i].setName(names2[i]);
            taxaBlock2.getTaxa().add(taxons2[i]);
        }
        DistancesBlock distancesBlock2 = new DistancesBlock();
        double[][] dist2 = {
                {0.0, 0.026115343, 0.02937976, 0.112445414, 0.07836644, 0.13626374, 0.12295974, 0.14052288},
                {0.026115343, 0.0, 0.041304346, 0.12104689, 0.088202864, 0.14379802, 0.13152175, 0.14472252},
                {0.02937976, 0.041304346, 0.0, 0.099236645, 0.06394708, 0.12294182, 0.12065218, 0.132753},
                {0.112445414, 0.12104689, 0.099236645, 0.0, 0.09955752, 0.14207049, 0.14285715, 0.15611354},
                {0.07836644, 0.088202864, 0.06394708, 0.09955752, 0.0, 0.1160221, 0.11797133, 0.11589404},
                {0.13626374, 0.14379802, 0.12294182, 0.14207049, 0.1160221, 0.0, 0.15916575, 0.13516484},
                {0.12295974, 0.13152175, 0.12065218, 0.14285715, 0.11797133, 0.15916575, 0.0, 0.13601741},
                {0.14052288, 0.14472252, 0.132753, 0.15611354, 0.11589404, 0.13516484, 0.13601741, 0.0}};
        distancesBlock2.set(dist2);

        ProgressListener pl1 = new ProgressPercentage();
        TreesBlock treesBlock1 = new TreesBlock();

        bioNJ.compute(pl1, taxaBlock1, distancesBlock1, treesBlock1);
        System.out.println("output: " + treesBlock1.getTrees().get(0).toString());
        assertEquals(output1, treesBlock1.getTrees().get(0).toString());

        ProgressListener pl2 = new ProgressPercentage();
        TreesBlock treesBlock2 = new TreesBlock();

        bioNJ.compute(pl2, taxaBlock2, distancesBlock2, treesBlock2);
        System.out.println("output: " + treesBlock2.getTrees().get(0).toString());
        assertEquals(output2, treesBlock2.getTrees().get(0).toString());


    }

}