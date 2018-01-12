/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.dialogs.imports;

import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.algorithms.characters2distances.HammingDistances;
import splitstree5.core.algorithms.distances2splits.NeighborNet;
import splitstree5.core.algorithms.filters.TreeFilter;
import splitstree5.core.algorithms.interfaces.IToDistances;
import splitstree5.core.algorithms.interfaces.IToSplits;
import splitstree5.core.algorithms.interfaces.IToTrees;
import splitstree5.core.algorithms.views.SplitsNetworkAlgorithm;
import splitstree5.core.algorithms.views.TreeEmbedder;
import splitstree5.core.datablocks.*;
import splitstree5.core.workflow.UpdateState;
import splitstree5.core.workflow.Workflow;
import splitstree5.io.imports.*;
import splitstree5.main.MainWindow;
import splitstree5.utils.Alert;

import java.io.IOException;

/**
 * performs an import
 * Daniel Huson, 1.2018
 */
public class Importer {
    public static void apply(IImporter importer, String filename) {
        if (importer != null) {
            try {
                final MainWindow mainWindow = new MainWindow();
                final Document document = mainWindow.getDocument();
                document.setFileName(filename);
                Workflow workflow = document.getWorkflow();
                TaxaBlock taxaBlock = new TaxaBlock();

                if (importer instanceof IImportCharacters) {
                    final CharactersBlock dataBlock = new CharactersBlock();
                    ((IImportCharacters) importer).parse(filename, taxaBlock, dataBlock);
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    final ADataNode<DistancesBlock> distances = workflow.createDataNode(new DistancesBlock());
                    workflow.createConnector(workflow.getWorkingDataNode(), distances, new HammingDistances());
                    final ADataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                    workflow.createConnector(distances, splits, new NeighborNet());
                    final ADataNode<SplitsNetworkViewBlock> splitsView = workflow.createDataNode(new SplitsNetworkViewBlock());
                    workflow.createConnector(splits, splitsView, new SplitsNetworkAlgorithm());
                } else if (importer instanceof IToDistances) {
                    final DistancesBlock dataBlock = new DistancesBlock();
                    ((IImportDistances) importer).parse(filename, taxaBlock, dataBlock);
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    final ADataNode<SplitsBlock> splits = workflow.createDataNode(new SplitsBlock());
                    workflow.createConnector(workflow.getWorkingDataNode(), splits, new NeighborNet());
                    final ADataNode<SplitsNetworkViewBlock> splitsView = workflow.createDataNode(new SplitsNetworkViewBlock());
                    workflow.createConnector(splits, splitsView, new SplitsNetworkAlgorithm());

                } else if (importer instanceof IToTrees) {
                    final TreesBlock dataBlock = new TreesBlock();
                    ((IImportTrees) importer).parse(filename, taxaBlock, dataBlock);
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    final ADataNode<TreesBlock> trees = workflow.createDataNode(new TreesBlock());
                    workflow.createConnector(workflow.getWorkingDataNode(), trees, new TreeFilter());
                    final ADataNode<TreeViewBlock> treesView = workflow.createDataNode(new TreeViewBlock());
                    workflow.createConnector(trees, treesView, new TreeEmbedder());
                } else if (importer instanceof IToSplits) {
                    final SplitsBlock dataBlock = new SplitsBlock();
                    ((IImportSplits) importer).parse(filename, taxaBlock, dataBlock);
                    workflow.setupTopAndWorkingNodes(taxaBlock, dataBlock);
                    final ADataNode<SplitsNetworkViewBlock> splitsView = workflow.createDataNode(new SplitsNetworkViewBlock());
                    workflow.createConnector(workflow.getWorkingDataNode(), splitsView, new SplitsNetworkAlgorithm());
                }
                document.setupTaxonSelectionModel();
                document.getWorkflow().getTopTaxaNode().setState(UpdateState.VALID);
                document.setDirty(true);

                mainWindow.show(new Stage(), 100, 100);
            } catch (IOException ex) {
                new Alert("Import failed: " + ex.getMessage());
            }
        }
    }
}
