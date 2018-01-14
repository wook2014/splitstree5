package splitstree5.io.imports;

import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.utils.TreesUtilities;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Newick tree importer
 * Daria Evseeva,04.10.2017.
 */
public class NewickTreeIn implements IToTrees, IImportTrees {
    public static final List<String> extensions = new ArrayList<>(Arrays.asList("new", "nwk", "tree", "tre"));

    /*
    Implement first the Importer Interface
    */
    private boolean optionConvertMultiLabeledTree = false;

    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, TreesBlock trees) throws IOException, CanceledException {

        taxa.clear();
        trees.clear();

        try (FileInputIterator it = new FileInputIterator(inputFile)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);
            // importing first tree and generating taxa object
            //String str;
            HashSet<String> labels = new HashSet<>();
            boolean haveWarnedMultipleLabels = false;

            // read in the trees
            int count = 1, size = 0;
            boolean partial = false;
            while (it.hasNext()) {
                String aLine = it.next();

                if (aLine.trim().length() == 0 || aLine.startsWith("#"))
                    continue; // skip empty lines and comment lines
                //System.err.println("Tree: " + aLine);
                //str = "";
                final StringBuilder s = new StringBuilder();
                s.append(aLine);
                while (!aLine.contains(";") && it.hasNext()) {
                    //str += aLine;
                    s.append(aLine);
                    aLine = it.next();
                }
                String str = s.toString().trim();

                if (str.length() > 0) {
                    str = str.replaceAll(" ", "").replaceAll("\t", "");
                    PhyloTree tree = new PhyloTree();
                    tree.parseBracketNotation(Basic.removeComments(str, '[', ']'), true);
                    try {
                        tree.parseBracketNotation(Basic.removeComments(str, '[', ']'), true);
                        if (TreesUtilities.hasNumbersOnInternalNodes(tree))
                            TreesUtilities.changeNumbersOnInternalNodesToEdgeConfidencies(tree);
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                        throw ex;
                    }

                    // todo : convertMultiTree2Splits: return taxa and splits in the case of a multi-labeled tree

/*
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

                    final Set<String> treeNodeLabels = tree.getNodeLabels();
                    if (labels.size() == 0)
                        labels.addAll(treeNodeLabels);
                    else {
                        if (!treeNodeLabels.equals(labels)) {
                            partial = true;
                            labels.addAll(treeNodeLabels);
                        }
                    }
                    tree.setName(String.format("Tree-%05d", trees.size()));
                    trees.getTrees().add(tree);
                }
            }
            taxa.addTaxaByNames(labels);
            trees.setPartial(partial);
            progressListener.setProgress(it.getProgress());
        }
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public boolean isApplicable(String fileName) throws IOException {
        final String line = Basic.getFirstLineFromFile(new File(fileName));
        return line != null && line.startsWith("(");
    }

    public boolean getOptionConvertMultiLabeledTree() {
        return optionConvertMultiLabeledTree;
    }

    public void setOptionConvertMultiLabeledTree(boolean optionConvertMultiLabeledTree) {
        this.optionConvertMultiLabeledTree = optionConvertMultiLabeledTree;
    }
}
