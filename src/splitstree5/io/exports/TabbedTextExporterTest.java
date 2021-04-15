/*
 * TabbedTextExporterTest.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.exports;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.algorithms.characters2distances.Uncorrected_P;
import splitstree5.core.algorithms.distances2trees.NeighborJoining;
import splitstree5.core.algorithms.trees2splits.TreeSelectorSplits;
import splitstree5.core.datablocks.*;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class TabbedTextExporterTest {

    private TabbedTextExporter tabbedTextExporter = new TabbedTextExporter();

    @Test
    public void export() throws Exception {

        File file = new File("test/exports/TEST_TabbedText.txt");


        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock character = new CharactersBlock();
        TreesBlock trees = new TreesBlock();
        DistancesBlock distances = new DistancesBlock();
        SplitsBlock splits = new SplitsBlock();
        ProgressListener pl = new ProgressPercentage();

        try (NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/algae_char.nex"))) {
            np.matchIgnoreCase("#nexus");
            new TaxaNexusInput().parse(np, taxa);
            new CharactersNexusInput().parse(np, taxa, character);
        }
        /*new DistancesNexusInput().parse(np, taxa, distances, null);
        new SplitsNexusInput().parse(np, taxa, splits, null);*/

        // algorithms
        Uncorrected_P uncorrected_p = new Uncorrected_P();
        NeighborJoining nj = new NeighborJoining();
        TreeSelectorSplits treeSelector = new TreeSelectorSplits();
        uncorrected_p.compute(pl, taxa, character, distances);
        nj.compute(pl, taxa, distances, trees);
        treeSelector.compute(pl, taxa, trees, splits);

        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            tabbedTextExporter.export(writer, taxa);
            tabbedTextExporter.export(writer, taxa, character);
            tabbedTextExporter.export(writer, taxa, distances);
            tabbedTextExporter.export(writer, taxa, splits);
        }

        byte[] encoded1 = Files.readAllBytes(Paths.get("test/notNexusFiles/algae_tabbed.txt"));
        String algae = new String(encoded1, StandardCharsets.UTF_8);

        byte[] encoded2 = Files.readAllBytes(Paths.get("test/exports/TEST_TabbedText.txt"));
        String export = new String(encoded2, StandardCharsets.UTF_8);

        //System.err.println(algae);
        //System.err.println(save);
        assertEquals(algae, export);

    }

}