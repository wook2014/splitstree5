/*
 *  Copyright (C) 2018 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
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
 */

package splitstree5.dialogs.importer;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import jloda.fx.CallableService;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.Pair;
import jloda.util.ProgramProperties;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.TaskWithProgressListener;
import splitstree5.io.imports.NewickTreeIn;
import splitstree5.main.MainWindow;

import java.io.File;
import java.util.*;

/**
 * import Trees from multiple files in Newick format
 * Daniel Huson, 6.2018
 */
public class ImportMultipleTreeFilesDialog {
    /**
     * run dialog for import of multiple trees files to be merged into one
     *
     * @param parentMainWindow
     */
    public static void apply(MainWindow parentMainWindow) {
        final File previousDir = new File(ProgramProperties.get("ImportDir", ""));
        final FileChooser fileChooser = new FileChooser();
        if (previousDir.isDirectory())
            fileChooser.setInitialDirectory(previousDir);
        fileChooser.setTitle("Open Import File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Newick tree file", ImporterManager.completeExtensions(NewickTreeIn.extensions, true)));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files: *.* *.gz", "*.*", "*.gz"));
// show file browser

        final List<File> selectedFiles = fileChooser.showOpenMultipleDialog(parentMainWindow.getStage());
        if (selectedFiles != null && selectedFiles.size() > 0) {
            final CallableService<Pair<TaxaBlock, TreesBlock>> service = new CallableService<>(new TaskWithProgressListener<Pair<TaxaBlock, TreesBlock>>() {
                @Override
                public Pair<TaxaBlock, TreesBlock> call() throws Exception {
                    final TaxaBlock allTaxa = new TaxaBlock();
                    final HashSet<Taxon> taxaFound = new HashSet<>();
                    final TreesBlock allTrees = new TreesBlock();
                    for (File file : selectedFiles) {
                        final NewickTreeIn newickTreeIn = new NewickTreeIn();
                        if (!newickTreeIn.isApplicable(file.getPath()))
                            System.err.println("Skipping, not a tree file in Newick format: " + file);
                        else {
                            final TaxaBlock taxaBlock = new TaxaBlock();
                            final TreesBlock treesBlock = new TreesBlock();
                            newickTreeIn.parse(getProgressListener(), file.getPath(), taxaBlock, treesBlock);
                            // name trees after the file that contains them:
                            if (treesBlock.getNTrees() == 1)
                                treesBlock.getTree(1).setName(Basic.replaceFileSuffix(file.getName(), ""));
                            else if (treesBlock.getNTrees() > 1) {
                                for (int t = 1; t <= treesBlock.getNTrees(); t++) {
                                    treesBlock.getTree(1).setName(Basic.replaceFileSuffix(file.getName(), "-" + t));
                                }
                            }

                            if (treesBlock.isPartial())
                                allTrees.setPartial(true);

                            // ensure all trees use the same taxon ids:
                            for (Taxon taxon : taxaBlock.getTaxa()) {
                                if (!taxaFound.contains(taxon)) {
                                    taxaFound.add(taxon);
                                    allTaxa.add(taxon);

                                    if (allTrees.getNTrees() > 0)
                                        allTrees.setPartial(true); // this is not the first file and need to add taxa, partial!
                                }
                            }

                            for (PhyloTree tree : treesBlock.getTrees()) {
                                fixTaxonNumbers(taxaBlock, allTaxa, tree);
                            }
                            allTrees.getTrees().addAll(treesBlock.getTrees());
                        }
                    }
                    return new Pair<>(allTaxa, allTrees);
                }
            });

            service.setOnSucceeded((e) -> {
                Platform.runLater(() -> {
                    final Pair<TaxaBlock, TreesBlock> pair = service.getValue();
                    DataLoader.load(false, "Trees", pair.getFirst(), pair.getSecond(), parentMainWindow);
                });
            });

            service.start();
        }
    }

    /**
     * adjust taxa in tree so that the match new taxon block
     *
     * @param oldTaxaBlock
     * @param newTaxaBlock
     * @param tree
     */
    private static void fixTaxonNumbers(TaxaBlock oldTaxaBlock, TaxaBlock newTaxaBlock, PhyloTree tree) {
        final Map<Integer, Integer> old2new = new HashMap<>();
        for (int t = 1; t <= oldTaxaBlock.getNtax(); t++) {
            old2new.put(t, newTaxaBlock.indexOf(oldTaxaBlock.get(t)));
        }

        for (Node v : tree.nodes()) {
            if (tree.getNumberOfTaxa(v) > 0) {
                final ArrayList<Integer> taxa = new ArrayList<>();
                for (Integer t : tree.getTaxa(v)) {
                    taxa.add(old2new.get(t));
                }
                tree.clearTaxa(v);
                for (int t : taxa) {
                    tree.addTaxon(v, t);
                }
            }
        }
    }
}
