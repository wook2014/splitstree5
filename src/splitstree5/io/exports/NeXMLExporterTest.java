/*
 * NeXMLExporterTest.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.*;
import splitstree5.io.imports.NeXML.NexmlFileParser;
import splitstree5.io.imports.NeXML.NexmlNetworkImporter;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NeXMLExporterTest {

    private NeXMLExporter neXMLExporter = new NeXMLExporter();

    @Test
    public void exportCharacters() throws Exception {

        File file = new File("test/exports/TEST_NeXML_list.xml");
        Writer writer = new BufferedWriter(new FileWriter(file));

        File file2 = new File("test/exports/TEST_NeXML.xml");
        Writer writer2 = new BufferedWriter(new FileWriter(file2));

        TaxaBlock taxa = new TaxaBlock();
        CharactersBlock characters = new CharactersBlock();

        NexusStreamParser np = new NexusStreamParser(new FileReader("test/neXML/M1000.nex"));
        np.matchIgnoreCase("#nexus");
        new TaxaNexusInput().parse(np, taxa);
        new CharactersNexusInput().parse(np, taxa, characters);
        // todo problem when reading M1000: must have "labels=left" attribute

        List<DataBlock> list = new ArrayList<>();
        list.add(taxa);
        list.add(characters);

        neXMLExporter.export(writer, list);
        neXMLExporter.writeStart(writer2);
        neXMLExporter.export(writer2, taxa);
        neXMLExporter.export(writer2, taxa, characters);
        neXMLExporter.writeEnd(writer2);
        writer.close();
        writer2.close();
    }

    @Test
    public void exportTrees() throws Exception {

        File file_trees = new File("test/exports/TEST_NeXML_trees.xml");
        Writer writer_trees = new BufferedWriter(new FileWriter(file_trees));

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        nexmlFileParser.parse("test/neXML/trees.xml", taxaBlock, treesBlock);

        neXMLExporter.writeStart(writer_trees);
        neXMLExporter.export(writer_trees, taxaBlock, treesBlock);
        neXMLExporter.writeEnd(writer_trees);
        writer_trees.close();
    }


    @Test
    public void exportNetwork() throws Exception {

        File file = new File("test/exports/TEST_NeXML_network.xml");
        Writer writer = new BufferedWriter(new FileWriter(file));

        TaxaBlock taxa = new TaxaBlock();
        NetworkBlock networkBlock = new NetworkBlock();
        NexmlNetworkImporter nexmlNetworkImporter = new NexmlNetworkImporter();
        ProgressListener pl = new ProgressPercentage();
        nexmlNetworkImporter.parse(pl, "test/neXML/trees.xml", taxa, networkBlock);

        // test metadata
        NetworkBlock.NodeData nodeData = networkBlock.getNodeData(networkBlock.getGraph().getFirstNode());
        nodeData.put("x", "1.2");
        nodeData.put("y", "3.4");
        networkBlock.getNode2data().put(networkBlock.getGraph().getFirstNode(), nodeData);

        neXMLExporter.writeStart(writer);
        neXMLExporter.export(writer, taxa, networkBlock);
        neXMLExporter.writeEnd(writer);
        writer.close();
    }

}