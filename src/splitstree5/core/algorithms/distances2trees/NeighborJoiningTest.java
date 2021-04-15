/*
 * NeighborJoiningTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.algorithms.distances2trees;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.algorithms.characters2distances.Uncorrected_P;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.nexus.CharactersNexusFormat;
import splitstree5.io.nexus.CharactersNexusInput;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * nj test
 * Created on 1/12/17.
 *
 * @author Daniel Huson, Daria Evseeva
 */
public class NeighborJoiningTest {

    private NeighborJoining nj = new NeighborJoining();

    @Test
    public void testCompute() throws Exception {

        // TEST 1
        final String output1 = "(a:1.0,b:4.0,(c:2.0,(f:5.0,(d:3.0,e:2.0):1.0):1.0):1.0)";

        String[] names1 = {"a", "b", "c", "d", "e", "f"};
        Taxon[] taxons1 = new Taxon[6];
        TaxaBlock taxaBlock1 = new TaxaBlock();
        for (int i = 0; i < names1.length; i++) {
            taxons1[i] = new Taxon();
            taxons1[i].setName(names1[i]);
            taxaBlock1.getTaxa().add(taxons1[i]);
        }
        DistancesBlock distancesBlock1 = new DistancesBlock();
        double[][] dist1 = {{0, 5, 4, 7, 6, 8},
                {5, 0, 7, 10, 9, 11},
                {4, 7, 0, 7, 6, 8},
                {7, 10, 7, 0, 5, 9},
                {6, 9, 6, 5, 0, 8},
                {8, 11, 8, 9, 8, 0}};
        distancesBlock1.set(dist1);

        // TEST 2-3
        final String outputFromChar = "(Tobacco:0.008836136,Rice:0.017279206,(Marchantia:0.010631585," +
                "((Chlamydomonas:0.06331665,((Euglena:0.0679614,Olithodiscus:0.06720343)" +
                ":0.008690414,Anacystis_nidulans:0.07131875):0.008862138):0.0036498776,Chlorella:0.031623945)" +
                ":0.024320895):0.011652797)";

        final String outputDirect = "(Tobacco:0.008836137,Rice:0.017279206,(Marchantia:0.010631586," +
                "(Chlorella:0.031623945,(((Euglena:0.0679614,Olithodiscus:0.06720344):0.00869041," +
                "Anacystis_nidulans:0.07131875):0.0088621415,Chlamydomonas:0.06331665):0.0036498758):0.024320897):0.011652796)";


        // DIRECT FROM MATRIX
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

        // FROM CHARACTERS

        String inputFile = "test//characters//algae_rna_interleave.nex";
        ProgressListener pl = new ProgressPercentage();
        TaxaBlock taxaBlock3 = new TaxaBlock();
        CharactersBlock charactersBlock3 = new CharactersBlock();

        CharactersNexusFormat format = new CharactersNexusFormat();
        List<String> taxonNames = new CharactersNexusInput().parse(new NexusStreamParser(new FileReader(inputFile)),
                taxaBlock3, charactersBlock3);
        taxaBlock3.addTaxaByNames(taxonNames);

        DistancesBlock distancesBlock3 = new DistancesBlock();
        final Uncorrected_P uncorrected_p = new Uncorrected_P();
        uncorrected_p.compute(pl, taxaBlock3, charactersBlock3, distancesBlock3);

        // CHECK MATRIX
        for (int i = 0; i < distancesBlock3.getDistances().length; i++) {
            assertArrayEquals(distancesBlock2.getDistances()[i], distancesBlock3.getDistances()[i], 0.000001);
            //assertArrayEquals(distancesBlock2.getDistances()[i], distancesBlock3.getDistances()[i], 0.0);
        }

        // apply algorithm

        ProgressListener pl1 = new ProgressPercentage();
        TreesBlock treesBlock1 = new TreesBlock();
        ProgressListener pl2 = new ProgressPercentage();
        TreesBlock treesBlock2 = new TreesBlock();
        ProgressListener pl3 = new ProgressPercentage();
        TreesBlock treesBlock3 = new TreesBlock();

        nj.compute(pl1, taxaBlock1, distancesBlock1, treesBlock1);
        System.out.println("Test 1");
        System.out.println("output: " + treesBlock1.getTrees().get(0).toString());
        assertEquals(output1, treesBlock1.getTrees().get(0).toString());

        nj.compute(pl2, taxaBlock2, distancesBlock2, treesBlock2);
        System.out.println("Test 2");
        System.out.println("output: " + treesBlock2.getTrees().get(0).toString());
        assertEquals(outputDirect, treesBlock2.getTrees().get(0).toString());

        nj.compute(pl3, taxaBlock3, distancesBlock3, treesBlock3);
        System.out.println("Test 3");
        System.out.println("output: " + treesBlock3.getTrees().get(0).toString());
        assertEquals(outputFromChar, treesBlock3.getTrees().get(0).toString());
    }
}