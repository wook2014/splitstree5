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

package splitstree5.io.nexus.workflow;

import javafx.application.Platform;
import javafx.stage.Stage;
import jloda.fx.CallableService;
import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import jloda.util.Pair;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.TaxaFilter;
import splitstree5.core.algorithms.filters.TopFilter;
import splitstree5.core.datablocks.*;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.io.nexus.AlgorithmNexusInput;
import splitstree5.io.nexus.SplitsTree5NexusInput;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.main.MainWindow;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * read workflow in nexus format
 * Daniel Huson, 2.2018
 */
public class WorkflowNexusInput implements Callable<Boolean> {

    private final MainWindow parentWindow;
    private final String fileName;

    /**
     * parse a workflow in nexus format
     *
     * @param mainWindow
     * @param fileName
     */
    public static void open(MainWindow mainWindow, String fileName) {
        final CallableService<Boolean> service = new CallableService<>();
        service.setExecutor(Platform::runLater); // todo: make this runnable in a separate thread
        service.setCallable(new WorkflowNexusInput(mainWindow, fileName));
        service.setOnCancelled((e) -> NotificationManager.showWarning("User canceled"));
        service.setOnFailed((e) -> NotificationManager.showError("Open file failed:\n" + (service.getException().getMessage())));
        service.start();
    }

    /**
     * constructor
     *
     * @param parentWindow
     * @param fileName
     */
    private WorkflowNexusInput(MainWindow parentWindow, String fileName) {
        this.parentWindow = parentWindow;
        this.fileName = fileName;
    }

    /**
     * import a workflow
     * @return true
     */
    public Boolean call() {
        try {
            final MainWindow mainWindow;
            final boolean usingNewWindow;
            if (parentWindow.getWorkflow().getWorkingDataNode() == null) {
                mainWindow = parentWindow;
                usingNewWindow=false;
            } else {
                mainWindow = new MainWindow();
                usingNewWindow=true;
            }
            final Document document = mainWindow.getDocument();
            document.setFileName(fileName);

            final Workflow workflow = mainWindow.getWorkflow();

            final ArrayList<ViewerBlock> viewerBlocks = new ArrayList<>();

            try (NexusStreamParser np = new NexusStreamParser(new FileReader(fileName))) {
                np.matchIgnoreCase("#nexus");
                final SplitsTree5Block splitsTree5Block = new SplitsTree5Block();
                new SplitsTree5NexusInput().parse(np, splitsTree5Block);
                // todo: check input based on splitsTree5Block

                final Map<String, DataNode> title2node = new HashMap<>();
                final Map<String, Pair<Algorithm, String>> title2algorithmAndLink = new HashMap<>();

                DataNode<TaxaBlock> topTaxaNode = null;
                Connector<TaxaBlock, TaxaBlock> taxaFilter = null;
                DataNode<TaxaBlock> workingTaxaNode = null;
                DataNode topDataNode = null;
                DataNode workingDataNode = null;

                final NexusDataBlockInput dataInput = new NexusDataBlockInput();

                while (np.peekMatchIgnoreCase("begin")) {
                    if (np.peekMatchBeginBlock("algorithm")) {
                        final AlgorithmNexusInput algorithmInput = new AlgorithmNexusInput();
                        final Algorithm algorithm = algorithmInput.parse(np);
                        title2algorithmAndLink.put(algorithmInput.getTitle(), new Pair<>(algorithm, Basic.toString(algorithmInput.getLink(), "")));
                    } else if (np.peekMatchBeginBlock("taxa")) {
                        final TaxaNexusInput taxaInput = new TaxaNexusInput();
                        final TaxaBlock dataBlock = new TaxaBlock();
                        taxaInput.parse(np, dataBlock);
                        final DataNode<TaxaBlock> dataNode = workflow.createDataNode(dataBlock);
                        if (topTaxaNode == null) {
                            topTaxaNode = dataNode;
                            workflow.setTopTaxaNode(dataNode);
                        } else {
                            if (workingTaxaNode == null) {
                                workingTaxaNode = dataNode;
                                workflow.setWorkingTaxaNode(dataNode);
                            }
                            final Pair<Algorithm, String> algorithmAndLink = title2algorithmAndLink.get(taxaInput.getLink().getSecond());
                            final Algorithm algorithm = algorithmAndLink.getFirst();
                            final DataNode parent = title2node.get(algorithmAndLink.getSecond());
                            final Connector connector = workflow.createConnector(parent, dataNode, algorithmAndLink.getFirst());
                            if (taxaFilter == null && algorithm instanceof TaxaFilter) {
                                taxaFilter = (Connector<TaxaBlock, TaxaBlock>) connector;
                                workflow.setTaxaFilter(taxaFilter);
                            }
                        }
                        title2node.put(dataBlock.getBlockName() + taxaInput.getTitle(), dataNode);
                    } else {
                        final DataBlock dataBlock = dataInput.parse(np, workingTaxaNode.getDataBlock());
                        final DataNode dataNode = workflow.createDataNode(dataBlock);
                        if (topDataNode == null && !(dataBlock instanceof IAdditionalBlock)) {
                            topDataNode = dataNode;
                            workflow.setTopDataNode(topDataNode);
                        } else {
                            if (workingDataNode == null && !(dataBlock instanceof IAdditionalBlock)) {
                                workingDataNode = dataNode;
                                workflow.setWorkingDataNode(dataNode);
                            }
                            final Pair<Algorithm, String> algorithmAndLink = title2algorithmAndLink.get(dataInput.getLink().getSecond());
                            final Algorithm algorithm = algorithmAndLink.getFirst();
                            final DataNode parent = title2node.get(algorithmAndLink.getSecond());
                            if (algorithm instanceof TopFilter) {
                                workflow.createTopFilter(parent, dataNode);
                            } else {
                                workflow.createConnector(parent, dataNode, algorithmAndLink.getFirst());
                                if (dataBlock instanceof ViewerBlock) {
                                    viewerBlocks.add((ViewerBlock) dataBlock);
                                }
                            }
                        }
                        title2node.put(dataBlock.getBlockName() + dataInput.getTitle(), dataNode);
                    }
                }
            }

            Platform.runLater(() -> {
                try {
                    if (usingNewWindow)
                        mainWindow.show(new Stage(), parentWindow.getStage().getX() + 50, parentWindow.getStage().getY() + 50);
                    final String shortDescription = workflow.getTopTaxaNode() != null ? workflow.getTopDataNode().getShortDescription() : "null";
                    NotificationManager.showInformation("Opened file: " + Basic.getFileNameWithoutPath(fileName) + (shortDescription.length() > 0 ? "\nLoaded " + shortDescription : ""));
                    if (!fileName.endsWith(".tmp"))
                        RecentFilesManager.getInstance().addRecentFile(fileName);
                    mainWindow.getStage().toFront();
                } catch (Exception ex) {
                    Basic.caught(ex);
                }
            });

            Platform.runLater(() -> {
                document.updateMethodsText();
                for (ViewerBlock viewerBlock : viewerBlocks) {
                    viewerBlock.getTab().setSkipNextLabelLayout(true);
                    viewerBlock.show();
                }
            });

        } catch (IOException ex) {
            NotificationManager.showError("Open file '" + fileName + "' failed: " + ex.getMessage());
        }
        return true;
    }

    /**
     * does this file look like it contains a workflow?
     *
     * @param fileName
     * @return true, if SplitsTree5Block present
     */
    public static boolean isApplicable(String fileName) {
        try (NexusStreamParser np = new NexusStreamParser(new FileReader(fileName))) {
            if (np.peekMatchIgnoreCase("#nexus")) {
                np.matchIgnoreCase("#nexus");
                return np.peekMatchBeginBlock(SplitsTree5Block.BLOCK_NAME);
            }
        } catch (IOException ex) {
            return false;
        }
        return false;
    }
}
