/*
 * NewickTreeImporter.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.imports;

import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.*;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.io.imports.interfaces.IImportTrees;
import splitstree5.io.imports.utils.SimpleNewickParser;
import splitstree5.utils.TreesUtilities;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Newick tree importer
 * Daria Evseeva,04.10.2017.
 */

// todo trees inner labels: bootstrap(confidence values) % or labels


public class NewickTreeImporter implements IToTrees, IImportTrees {
    public static final List<String> extensions = new ArrayList<>(Arrays.asList("new", "nwk", "tree", "tre", "treefile"));

    private boolean optionConvertMultiLabeledTree = false;

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
        taxa.clear();
        trees.clear();

        int lineno = 0;
        try (FileLineIterator it = new FileLineIterator(inputFile)) {
            progressListener.setMaximum(it.getMaximumProgress());
            progressListener.setProgress(0);

            final Map<String, Integer> taxName2Id = new HashMap<>(); // starts at 1
            final Set<String> taxonNamesFound = new HashSet<>();
            final ArrayList<String> orderedTaxonNames = new ArrayList<>();

            final SimpleNewickParser newickParser = new SimpleNewickParser();
            newickParser.setEnforceLabelDoesNotStartWithADigit(true);
            boolean partial = false;
            final ArrayList<String> parts = new ArrayList<>();

            // read in the trees
            while (it.hasNext()) {
                lineno++;
                final String line = Basic.removeComments(it.next(), '[', ']');
                if (line.endsWith(";")) {
                    final String treeLine;
                    if (parts.size() > 0) {
                        parts.add(line);
                        treeLine = Basic.toString(parts, "");
                        parts.clear();
                    } else
                        treeLine = line;
                    final PhyloTree tree;
                    try {
                        tree = newickParser.parse(treeLine);
                    } catch (IOException ex) {
                        throw new IOExceptionWithLineNumber(lineno, ex);
                    }
                    if (TreesUtilities.hasNumbersOnLeafNodes(tree)) {
                        throw new IOExceptionWithLineNumber(lineno, "Leaf labels must begin with a letter");
                    }
                    if (TreesUtilities.hasNumbersOnInternalNodes(tree)) {
                        TreesUtilities.changeNumbersOnInternalNodesToEdgeConfidencies(tree);
                    }
                    final List<String> leafLabelList = IterationUtils.asList(newickParser.leafLabels());
                    final Set<String> leafLabelSet = new HashSet<>(leafLabelList);
                    final boolean multiLabeled = (leafLabelSet.size() < leafLabelList.size());

                    if (multiLabeled) {
                        if (isOptionConvertMultiLabeledTree()) {
                            final Set<String> seen = new HashSet<>();
                            for (Node v : tree.nodes()) {
                                String label = tree.getLabel(v);
                                if (label != null) {
                                    int count = 1;
                                    while (seen.contains(label)) {
                                        label = tree.getLabel(v) + "-" + (++count);
                                    }
                                    if (count > 1)
                                        tree.setLabel(v, label);
                                    seen.add(label);
                                }
                            }
                        } else {
                            for (String z : leafLabelSet) {
                                leafLabelList.remove(z);
                            }
                            throw new IOExceptionWithLineNumber(lineno, "Name appears multiple times in tree:" + leafLabelList.get(0));
                        }
                    }

                    if (taxonNamesFound.size() == 0) {
                        for (String name : newickParser.leafLabels()) {
                            taxonNamesFound.add(name);
                            orderedTaxonNames.add(name);
                            taxName2Id.put(name, orderedTaxonNames.size());
                        }
                    } else {
                        if (!taxonNamesFound.equals(IterationUtils.asSet(newickParser.leafLabels()))) {
                            partial = true;
                            for (String name : newickParser.leafLabels()) {
                                if (!taxonNamesFound.contains(name)) {
                                    System.err.println("Additional taxon name: " + name);
                                    taxonNamesFound.add(name);
                                    orderedTaxonNames.add(name);
                                    taxName2Id.put(name, orderedTaxonNames.size());
                                }
                            }
                        }
                    }
                    for (Node v : tree.nodes()) {
                        final String label = tree.getLabel(v);
                        if (label != null && label.length() > 0) {
                            if (taxonNamesFound.contains(label)) { // need to check that this is a taxon name, could also be a number placed on the root...
                                tree.addTaxon(v, taxName2Id.get(label));
                            }
                        }
                    }
                    trees.getTrees().add(tree);
                    tree.setName("tree-" + trees.size());

                    progressListener.setProgress(it.getProgress());
                } else
                    parts.add(line);
            }
            if (parts.size() > 0)
                System.err.println("Ignoring trailing lines at end of file:\n" + Basic.abbreviateDotDotDot(Basic.toString(parts, "\n"), 400));
            taxa.addTaxaByNames(orderedTaxonNames);
            trees.setPartial(partial);
            trees.setRooted(true);
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

    public boolean isOptionConvertMultiLabeledTree() {
        return optionConvertMultiLabeledTree;
    }

    public void setOptionConvertMultiLabeledTree(boolean optionConvertMultiLabeledTree) {
        this.optionConvertMultiLabeledTree = optionConvertMultiLabeledTree;
    }
}
