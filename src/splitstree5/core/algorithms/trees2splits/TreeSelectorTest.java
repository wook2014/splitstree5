package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import org.junit.Test;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.ASplit;
import splitstree5.io.nexus.SplitsNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;
import splitstree5.io.nexus.TreesNexusIO;

import java.io.FileReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Created on 24.01.2017.
 * @author Daria
 */

public class TreeSelectorTest {

    final TreeSelector treeSelector = new TreeSelector();

    @Test
    public void testCompute() throws Exception {

        TaxaBlock taxaBlock = new TaxaBlock();
        TreesBlock treesBlock = new TreesBlock();
        NexusStreamParser np = new NexusStreamParser(new FileReader("test/nexus/trees6-translate.nex"));
        np.matchIgnoreCase("#nexus");
        TaxaNexusIO.parse(np, taxaBlock);
        TreesNexusIO.parse(np, taxaBlock, treesBlock, null);

        for(int which = 1; which<=treesBlock.getNTrees(); which++){

            System.err.println("COMPUTE SPLITS FOR TREE"+which);

            SplitsBlock splitsBlock = new SplitsBlock();
            treeSelector.setOptionWhich(which);
            treeSelector.compute(new ProgressPercentage(), taxaBlock, treesBlock, splitsBlock);

            final StringWriter w = new StringWriter();
            w.write("#nexus\n");
            TaxaNexusIO.write(w, taxaBlock);
            TreesNexusIO.write(w, taxaBlock, treesBlock, null);
            SplitsNexusIO.write(w, taxaBlock, splitsBlock, null);
            System.err.println(w.toString());

            // compare splits

            String fileName = "test/splits/tree selector/trees6-"+which+".nex";

            TaxaBlock taxaFromST4 = new TaxaBlock();
            SplitsBlock splitsFromST4 = new SplitsBlock();
            NexusStreamParser np4 = new NexusStreamParser(new FileReader(fileName));
            np4.matchIgnoreCase("#nexus");
            TaxaNexusIO.parse(np4, taxaFromST4);
            SplitsNexusIO.parse(np4, taxaFromST4, splitsFromST4, null);

            assertEquals(splitsBlock.size(), splitsFromST4.size());
            for (int i = 0; i < splitsBlock.getNsplits(); i++) {
                ASplit aSplit = splitsBlock.getSplits().get(i);
                if(splitsFromST4.getSplits().contains(aSplit)){
                    int index = splitsFromST4.getSplits().indexOf(aSplit);
                    ASplit aSplitST4 = splitsFromST4.getSplits().get(index);
                    assertEquals(aSplit.getA(), aSplitST4.getA());
                    assertEquals(aSplit.getB(), aSplitST4.getB());
                    assertEquals(aSplit.getWeight(), aSplitST4.getWeight());
                    assertEquals(aSplit.getConfidence(), aSplitST4.getConfidence());
                    assertEquals(aSplit.getLabel(), aSplitST4.getLabel());
                }
                splitsFromST4.getSplits().remove(aSplit);
            }
            assertEquals(0, splitsFromST4.size());

        }

        /*Document document = new Document();
        document.setFileName("test/nexus/trees6-translate.nex");
        NexusFileParser.parse(document);

        ADataNode<TreesBlock> treesNode = document.getWorkflow().getWorkingDataNode();
        System.err.println(NexusFileWriter.toString(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), treesNode.getDataBlock()));

        ADataNode<SplitsBlock> splitsNode = new ADataNode<>(new SplitsBlock());

        TreeSelector treeSelector = new TreeSelector();

        for (int which = 1; which <= treesNode.getDataBlock().getNTrees(); which++) {
            System.err.println("Testing: " + which);

            treeSelector.setOptionWhich(which);
            splitsNode.getDataBlock().clear();
            treeSelector.compute(new ProgressPercentage(), document.getWorkflow().getWorkingTaxaNode().getDataBlock(), treesNode.getDataBlock(), splitsNode.getDataBlock());
            System.err.println(NexusFileWriter.toString(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), splitsNode.getDataBlock()));

            if (which == 1) {
                String expected = "\nBEGIN SPLITS;\n" +
                        "\tDIMENSIONS ntax=6 nsplits=9;\n" +
                        "\tFORMAT labels=no weights=yes confidences=no;\n" +
                        "\tPROPERTIES fit=-1.00 compatible;\n" +
                        "\tCYCLE 1 2 3 5 6 4;\n" +
                        "MATRIX\n" +
                        "\t[1, size=1] \t 1.0 \t 1 2 3 4 5,\n" +
                        "\t[2, size=1] \t 1.0 \t 1,\n" +
                        "\t[3, size=1] \t 1.0 \t 1 3 4 5 6,\n" +
                        "\t[4, size=2] \t 1.0 \t 1 2,\n" +
                        "\t[5, size=1] \t 1.0 \t 1 2 4 5 6,\n" +
                        "\t[6, size=3] \t 1.0 \t 1 2 3,\n" +
                        "\t[7, size=1] \t 1.0 \t 1 2 3 5 6,\n" +
                        "\t[8, size=2] \t 1.0 \t 1 2 3 4,\n" +
                        "\t[9, size=1] \t 1.0 \t 1 2 3 4 6,\n" +
                        ";\n" +
                        "END; [SPLITS]\n";
                assertEquals("tree 1:", expected, NexusFileWriter.toString(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), splitsNode.getDataBlock()));
            }
        }*/
    }

}