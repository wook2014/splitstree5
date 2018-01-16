package splitstree5.io.imports;

import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.CanceledException;
import jloda.util.FileInputIterator;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.interfaces.IImportTrees;
import splitstree5.io.imports.utils.SimpleNewickParser;
import splitstree5.utils.TreesUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Newick tree importer
 * Daria Evseeva,04.10.2017.
 */
public class NewickTreeIn implements IToTrees, IImportTrees {
    public static final List<String> extensions = new ArrayList<>(Arrays.asList("new", "nwk", "tree", "tre", "treefile"));

    boolean optionConvertMultiLabeledTree = false;

    /**
     * parse trees
     *
     * @param progressListener
     * @param inputFile
     * @param taxa
     * @param trees
     * @throws IOException
     * @throws CanceledException
     */
    public void parse(ProgressListener progressListener, String inputFile, TaxaBlock taxa, TreesBlock trees) throws IOException, CanceledException {
        try {
            taxa.clear();
            trees.clear();

            try (FileInputIterator it = new FileInputIterator(inputFile)) {
                progressListener.setMaximum(it.getMaximumProgress());
                progressListener.setProgress(0);

                final HashSet<String> leafLabels = new HashSet<>();
                final SimpleNewickParser newickParser = new SimpleNewickParser();
                boolean partial = false;
                final ArrayList<String> parts = new ArrayList<>();

                // read in the trees
                int count = 0;
                while (it.hasNext()) {
                    final String line = Basic.removeComments(it.next(), '[', ']');
                    if (line.endsWith(";")) {
                        final String treeLine;
                        if (parts.size() > 0) {
                            parts.add(line);
                            treeLine = Basic.toString(parts, "");
                            parts.clear();
                        } else
                            treeLine = line;
                        final PhyloTree tree = newickParser.parse(treeLine);
                        if (TreesUtilities.hasNumbersOnInternalNodes(tree)) {
                            TreesUtilities.changeNumbersOnInternalNodesToEdgeConfidencies(tree);
                        }
                        if (leafLabels.size() == 0) {
                            leafLabels.addAll(newickParser.getLeafLabels());

                        } else {
                            if (!leafLabels.equals(newickParser.getLeafLabels())) {
                                partial = true;
                                leafLabels.addAll(newickParser.getLeafLabels());
                            }
                        }
                        tree.setName(String.format("Tree-%05d", (++count)));
                        trees.getTrees().add(tree);
                        progressListener.setProgress(it.getProgress());
                    }
                }
                if (parts.size() > 0)
                    System.err.println("Ignoring trailing lines at end of file:\n" + Basic.abbreviateDotDotDot(Basic.toString(parts, "\n"), 400));
                taxa.addTaxaByNames(leafLabels);
                trees.setPartial(partial);
                trees.setRooted(true);
            }
        } catch (Exception ex) {
            Basic.caught(ex);
            throw ex;
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
