package splitstree5.io.exports;

import jloda.util.ProgressListener;
import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.NetworkBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.imports.NeXML.NexmlNetworkImporter;
import splitstree5.io.nexus.CharactersNexusInput;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NeXMLExporterTest {

    private NeXMLExporter neXMLExporter = new NeXMLExporter();

    @Test
    public void export() throws Exception {

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
        neXMLExporter.writeEnd();
        writer.close();
        writer2.close();


        // test trees block
        /*File file_trees = new File("test/exports/TEST_NeXML_trees.xml");
        Writer writer_trees = new BufferedWriter(new FileWriter(file_trees));

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        nexmlFileParser.parse("test/neXML/simple.xml", taxaBlock, treesBlock);

        List<DataBlock> trees_list = new ArrayList<>();
        trees_list.add(taxaBlock);
        trees_list.add(treesBlock);
        //neXMLExporter.export(writer_trees, trees_list);
        neXMLExporter.writeStart(writer_trees);
        neXMLExporter.export(writer_trees, taxaBlock, treesBlock);
        neXMLExporter.writeEnd();
        writer_trees.close();*/
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
        neXMLExporter.writeEnd();
        writer.close();
    }

}