package splitstree5.io.otherFormats;

import jloda.phylo.PhyloTree;
import jloda.util.Alert;
import jloda.util.Basic;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.utils.nexus.TreesUtilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by Daria on 04.10.2017.
 */
public class NewickTree {

    String datatype = null;
    /*
    Implement first the Importer Interface
    */
    public static String Description = "Newick Tree Files (*.new,*.tre, *.tree)";
    private boolean optionConvertMultiLabeledTree = false;

    public static void parse(String inputFile, TaxaBlock taxa, TreesBlock trees) throws IOException {

        taxa.clear();
        trees.clear();
        int ntax;

        try (BufferedReader in = new BufferedReader(new FileReader(inputFile))) {
            // importing first tree and generating taxa object
            String str;
            HashSet<String> labels = new HashSet<>();
            boolean haveWarnedMultipleLabels = false;

            // read in the trees
            int count = 1, size = 0;
            boolean partial = false;
            String aLine;
            while ((aLine = in.readLine()) != null) {
                if (aLine.trim().length() == 0 || aLine.startsWith("#"))
                    continue; // skip empty lines and comment lines
                System.err.println("Tree: " + aLine);
                str = "";
                while (aLine != null && (!aLine.contains(";"))) {
                    str += aLine;
                    aLine = in.readLine();
                }
                if (aLine != null) str += aLine;
                if (str.trim().length() != 0) {
                    str = str.replaceAll(" ", "").replaceAll("\t", "");
                    PhyloTree tree = new PhyloTree();
                    tree.parseBracketNotation(Basic.removeComments(str, '[', ']'), true);
                    /*try {
                        tree.parseBracketNotation(Basic.removeComments(str, '[', ']'), true);
                        if (TreesUtilities.hasNumbersOnInternalNodes(tree))
                            TreesUtilities.changeNumbersOnInternalNodesToEdgeConfidencies(tree);
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                        throw ex;
                    }

                    if (getOptionConvertMultiLabeledTree()) {
                        try {
                            Document doc = convertMultiTree2Splits(tree);
                            StringWriter sw = new StringWriter();
                            doc.write(sw);
                            //System.err.println(sw.toString());
                            return sw.toString();
                        } catch (NotMultiLabeledException ex) {
                            Basic.caught(ex);
                        }
                    } else {
                        if (tree.getInputHasMultiLabels() && !haveWarnedMultipleLabels) {
                            new Alert("One or more trees contain multiple occurrences of the same taxon-label,"
                                    + " these have been made unique by adding suffixes .1, .2 etc");
                            haveWarnedMultipleLabels = true;
                        }
                    }*/
                    for (String label : tree.getNodeLabels()) {
                        labels.add(label);
                    }
                    // this is for partial Trees
                    if (size == 0) size = labels.size();
                    if (labels.size() != size) partial = true;
                    //treesString.append("tree t").append(count++).append("=").append(str).append("\n");
                    trees.getTrees().add(tree);
                }
            }

            taxa.addTaxaByNames(labels);

        }
    }

    public boolean getOptionConvertMultiLabeledTree() {
        return optionConvertMultiLabeledTree;
    }

    public void setOptionConvertMultiLabeledTree(boolean optionConvertMultiLabeledTree) {
        this.optionConvertMultiLabeledTree = optionConvertMultiLabeledTree;
    }
}
