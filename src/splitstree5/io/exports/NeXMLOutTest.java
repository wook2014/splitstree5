package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.NeXML.NexmlFileParser;
import splitstree5.io.nexus.CharactersNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class NeXMLOutTest {

    private NeXMLOut neXMLOut = new NeXMLOut();

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
        TaxaNexusIO.parse(np, taxa);
        CharactersNexusIO.parse(np, taxa, characters, null);
        // todo problem when reading M1000: must have "labels=left" attribute

        List<DataBlock> list = new ArrayList<>();
        list.add(taxa);
        list.add(characters);

        neXMLOut.export(writer, list);
        neXMLOut.export(writer2, taxa);
        neXMLOut.export(writer2, taxa, characters);
        writer.close();
        writer2.close();


        // test trees block
        File file_trees = new File("test/exports/TEST_NeXML_trees.xml");
        Writer writer_trees = new BufferedWriter(new FileWriter(file_trees));

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexmlFileParser nexmlFileParser = new NexmlFileParser();
        nexmlFileParser.parse("test/neXML/simple.xml", taxaBlock, treesBlock);

        List<DataBlock> trees_list = new ArrayList<>();
        trees_list.add(taxaBlock);
        trees_list.add(treesBlock);
        neXMLOut.export(writer_trees, trees_list);
    }

}