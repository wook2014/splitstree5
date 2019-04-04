/*
 *  Copyright (C) 2019 Daniel H. Huson
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
import jloda.fx.control.ProgressPane;
import jloda.fx.util.AService;
import jloda.fx.util.NotificationManager;
import jloda.fx.util.ProgramPropertiesFX;
import jloda.fx.util.TaskWithProgressListener;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import jloda.util.Pair;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.imports.NewickTreeImporter;
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
        final File previousDir = new File(ProgramPropertiesFX.get("ImportDir", ""));
        final FileChooser fileChooser = new FileChooser();
        if (previousDir.isDirectory())
            fileChooser.setInitialDirectory(previousDir);
        fileChooser.setTitle("Open Import File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Newick tree file", ImporterManager.completeExtensions(NewickTreeImporter.extensions, true)));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files: *.* *.gz", "*.*", "*.gz"));
// show file browser

        final List<File> selectedFiles = fileChooser.showOpenMultipleDialog(parentMainWindow.getStage());
        if (selectedFiles != null && selectedFiles.size() > 0) {
            ProgramPropertiesFX.put("ImportDir", selectedFiles.get(0).getParentFile());
            final AService<Pair<TaxaBlock, TreesBlock>> service = new AService<>(new TaskWithProgressListener<Pair<TaxaBlock, TreesBlock>>() {
                @Override
                public Pair<TaxaBlock, TreesBlock> call() throws Exception {
                    try {
                        final TaxaBlock allTaxa = new TaxaBlock();
                        final HashSet<Taxon> taxaFound = new HashSet<>();
                        final TreesBlock allTrees = new TreesBlock();
                        getProgressListener().setTasks("Importing trees", selectedFiles.size() + " files...");
                        System.err.println("Importing trees from " + selectedFiles.size() + " files...");

                        getProgressListener().setMaximum(selectedFiles.size());
                        getProgressListener().setProgress(0);
                        for (File file : selectedFiles) {
                            getProgressListener().setSubtask(file.getName());
                            //System.err.println("Reading file: " + file);
                            final NewickTreeImporter newickTreeImporter = new NewickTreeImporter();
                            if (!newickTreeImporter.isApplicable(file.getPath()))
                                System.err.println("Skipping, not a tree file in Newick format: " + file);
                            else {
                                final TaxaBlock taxaBlock = new TaxaBlock();
                                final TreesBlock treesBlock = new TreesBlock();

                                newickTreeImporter.parse(getProgressListener(), file.getPath(), taxaBlock, treesBlock);

                                // name trees after the file that contains them:
                                if (treesBlock.getNTrees() == 1)
                                    treesBlock.getTree(1).setName(Basic.replaceFileSuffix(file.getName(), ""));
                                else if (treesBlock.getNTrees() > 1) {
                                    for (int t = 1; t <= treesBlock.getNTrees(); t++) {
                                        treesBlock.getTree(t).setName(Basic.replaceFileSuffix(file.getName(), "-" + t));
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
                            getProgressListener().incrementProgress();
                        }
                        System.err.println("Trees: " + allTrees.size());
                        return new Pair<>(allTaxa, allTrees);
                    } catch (Exception ex) {
                        Basic.caught(ex);
                        throw ex;
                    }
                }
            });

            service.setOnRunning((e) -> parentMainWindow.getMainWindowController().getBottomPane().getChildren().add(new ProgressPane(service)));

            service.setOnFailed((e) -> NotificationManager.showError("Error: " + service.getException().getMessage()));

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
