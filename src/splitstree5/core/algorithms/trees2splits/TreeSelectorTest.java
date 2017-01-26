package splitstree5.core.algorithms.trees2splits;

import jloda.util.ProgressPercentage;
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.nexus.NexusFileParser;
import splitstree5.io.nexus.NexusFileWriter;

import static junit.framework.Assert.assertEquals;

public class TreeSelectorTest {

    @Test
    public void testCompute() throws Exception {

        Document document = new Document();
        document.setFileName("test/nexus/trees6-translate.nex");
        NexusFileParser.parse(document);

        ADataNode<TreesBlock> treesNode = document.getDag().getWorkingDataNode();
        System.err.println(NexusFileWriter.toString(document.getDag().getWorkingTaxaNode().getDataBlock(), treesNode.getDataBlock()));
        
        ADataNode<SplitsBlock> splitsNode = new ADataNode<>(new SplitsBlock());

        TreeSelector treeSelector = new TreeSelector();

        for (int which = 1; which <= treesNode.getDataBlock().getNTrees(); which++) {
            System.err.println("Testing: " + which);

            treeSelector.setOptionWhich(which);
            splitsNode.getDataBlock().clear();
            treeSelector.compute(new ProgressPercentage(), document.getDag().getWorkingTaxaNode().getDataBlock(), treesNode.getDataBlock(), splitsNode.getDataBlock());
            System.err.println(NexusFileWriter.toString(document.getDag().getWorkingTaxaNode().getDataBlock(), splitsNode.getDataBlock()));

            if (which == 1) {
                String expected = "\nBEGIN SPLITS;\n" +
                        "DIMENSIONS ntax=6 nsplits=9;\n" +
                        "FORMAT labels=no weights=yes confidences=no;\n" +
                        "PROPERTIES fit=-1.00 compatible;\n" +
                        "CYCLE 1 2 3 5 6 4;\n" +
                        "MATRIX\n" +
                        "[1, size=1] \t 1.0 \t 1 2 3 4 6,\n" +
                        "[2, size=1] \t 1.0 \t 1 2 3 4 5,\n" +
                        "[3, size=2] \t 1.0 \t 1 2 3 4,\n" +
                        "[4, size=1] \t 1.0 \t 1,\n" +
                        "[5, size=1] \t 1.0 \t 1 3 4 5 6,\n" +
                        "[6, size=2] \t 1.0 \t 1 2,\n" +
                        "[7, size=1] \t 1.0 \t 1 2 4 5 6,\n" +
                        "[8, size=3] \t 1.0 \t 1 2 3,\n" +
                        "[9, size=1] \t 1.0 \t 1 2 3 5 6,\n" +
                        ";\n" +
                        "END; [SPLITS]\n";
                assertEquals("tree 1:", expected, NexusFileWriter.toString(document.getDag().getWorkingTaxaNode().getDataBlock(), splitsNode.getDataBlock()));
            }
        }
    }

}