package splitstree5.io.exports;

import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.*;
import splitstree5.io.nexus.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

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

        List<ADataBlock> list = new ArrayList<>();
        list.add(taxa);
        list.add(characters);

        neXMLOut.export(writer, list);
        neXMLOut.export(writer2, taxa);
        neXMLOut.export(writer2, taxa, characters);
        writer.close();
        writer2.close();

    }

}