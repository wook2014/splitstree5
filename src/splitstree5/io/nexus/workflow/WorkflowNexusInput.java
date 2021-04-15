/*
 * WorkflowNexusInput.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.nexus.workflow;

import javafx.application.Platform;
import javafx.stage.Stage;
import jloda.fx.control.ProgressPane;
import jloda.fx.util.AService;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.util.TaskWithProgressListener;
import jloda.fx.window.NotificationManager;
import jloda.util.*;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * read workflow in nexus format
 * Daniel Huson, 2.2018
 */
public class WorkflowNexusInput extends TaskWithProgressListener<MainWindow> {
    private final MainWindow parentWindow;
    private final String fileName;

    /**
     * parse a workflow in nexus format
     *
     * @param parentWindow
     * @param fileName
     */
    public static AService<MainWindow> open(MainWindow parentWindow, String fileName) {
        final AService<MainWindow> service = new AService<>();
        service.setExecutor(Platform::runLater); // todo: make this runnable in a separate thread
        service.setCallable(new WorkflowNexusInput(parentWindow, fileName));
        service.setOnCancelled((e) -> NotificationManager.showWarning("User canceled 'open file'"));
        service.setOnFailed((e) -> NotificationManager.showError("Open file failed:\n" + (service.getException().getMessage())));
        service.setOnRunning((e) -> parentWindow.getMainWindowController().getBottomPane().getChildren().add(new ProgressPane(service)));
        service.setOnSucceeded((e) -> {
            final Workflow workflow = service.getValue().getWorkflow();
            NotificationManager.showInformation("Opened file: " + Basic.getFileNameWithoutPath(fileName)
                    + "\nLoaded workflow containing " + workflow.getNumberOfDataNodes() + " data nodes and " + workflow.getNumberOfConnectorNodes() + " algorithms");
        });
        service.start();
        return service;
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
     *
     * @return true
     */
    public MainWindow call() throws Exception {
        final ProgressListener progress = getProgressListener();
        progress.setTasks("Loading file", Basic.getFileNameWithoutPath(fileName));

        final MainWindow mainWindow;
        final boolean usingNewWindow;
        if (parentWindow.getWorkflow().getWorkingDataNode() == null) {
            mainWindow = parentWindow;
            usingNewWindow = false;
        } else {
            mainWindow = new MainWindow();
            usingNewWindow = true;
        }
        final Document document = mainWindow.getDocument();
        document.setFileName(fileName);

        final Workflow workflow = mainWindow.getWorkflow();

        final ArrayList<ViewerBlock> viewerBlocks = new ArrayList<>();

        input(progress, workflow, viewerBlocks, fileName);

        Platform.runLater(() -> {
            try {
                if (usingNewWindow) {
                    mainWindow.show(new Stage(), parentWindow.getStage().getX() + 50, parentWindow.getStage().getY() + 50, parentWindow.getStage().getWidth(), parentWindow.getStage().getHeight());
                    mainWindow.getStage().getIcons().addAll(ProgramProperties.getProgramIconsFX());
                }
                if (!fileName.endsWith(".tmp"))
                    RecentFilesManager.getInstance().insertRecentFile(fileName);
                if (mainWindow.getStage() != null)
                    mainWindow.getStage().toFront();
            } catch (Exception ex) {
                Basic.caught(ex);
            }
        });

        Platform.runLater(() -> {
            document.setHasSplitsTree5File(true);
            document.updateMethodsText();
            for (ViewerBlock viewerBlock : viewerBlocks) {
                viewerBlock.getTab().setSkipNextLabelLayout(true);
                viewerBlock.show();
            }
        });
        return mainWindow;
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

    /**
     * input a work flow from a file
     *
     * @param progress
     * @param workflow
     * @param fileName
     * @throws IOException
     * @throws CanceledException
     */
    public static void input(ProgressListener progress, Workflow workflow, ArrayList<ViewerBlock> viewerBlocks, String fileName) throws IOException, CanceledException {
        try (Reader reader = Basic.getReaderPossiblyZIPorGZIP(fileName)) {
            progress.setMaximum((new File(fileName).length() / (Basic.isZIPorGZIPFile(fileName) ? 100 : 20)));
            input(progress, workflow, viewerBlocks, reader);
        }
    }

    /**
     * inpurt a work flow from a reader
     *
     * @param progress
     * @param workflow
     * @param viewerBlocks
     * @param reader
     * @throws IOException
     * @throws CanceledException
     */
    public static void input(ProgressListener progress, Workflow workflow, ArrayList<ViewerBlock> viewerBlocks, Reader reader) throws IOException, CanceledException {
        try (NexusStreamParser np = new NexusStreamParser(reader)) {
            np.matchIgnoreCase("#nexus");
            final SplitsTree5Block splitsTree5Block = new SplitsTree5Block();
            (new SplitsTree5NexusInput()).parse(np, splitsTree5Block);
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
                    title2algorithmAndLink.put(algorithmInput.getTitle(), new Pair<>(algorithm, Basic.toString(algorithmInput.getLink(), " ")));
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
                    title2node.put(dataBlock.getBlockName() + " " + taxaInput.getTitle(), dataNode);
                } else {
                    final TaxaBlock taxaBlock;
                    if (topDataNode == null)
                        taxaBlock = topTaxaNode.getDataBlock();
                    else
                        taxaBlock = workingTaxaNode.getDataBlock();

                    final DataBlock dataBlock = dataInput.parse(np, taxaBlock);
                    if (dataBlock instanceof TraitsBlock)
                        taxaBlock.setTraitsBlock((TraitsBlock) dataBlock);

                    final DataNode dataNode = workflow.createDataNode(dataBlock);
                    if (topDataNode == null && !(dataBlock instanceof IAdditionalBlock)) {
                        topDataNode = dataNode;
                        workflow.setTopDataNode(topDataNode);
                    } else {
                        if (workingDataNode == null && !(dataBlock instanceof IAdditionalBlock)) {
                            workingDataNode = dataNode;
                            workflow.setWorkingDataNode(dataNode);
                        }
                        if (dataInput.getLink() != null) {
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
                    }
                    title2node.put(dataBlock.getBlockName() + " " + dataInput.getTitle(), dataNode);
                }
                progress.setProgress(np.lineno());
            }
        } catch (Exception ex) {
            Basic.caught(ex);
            throw ex;
        }
    }
}
