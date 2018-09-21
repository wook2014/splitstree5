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

package splitstree5.tools;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.stage.Stage;
import jloda.fx.NotificationManager;
import jloda.fx.ProgramExecutorService;
import jloda.util.*;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.dialogs.importer.ImportService;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.io.exports.NexusExporter;
import splitstree5.io.imports.interfaces.IImporter;
import splitstree5.io.nexus.NexusParser;
import splitstree5.io.nexus.TaxaNexusInput;
import splitstree5.io.nexus.workflow.WorkflowNexusInput;
import splitstree5.io.nexus.workflow.WorkflowNexusOutput;
import splitstree5.main.MainWindow;
import splitstree5.main.Version;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static splitstree5.dialogs.importer.ImporterManager.UNKNOWN;

/**
 * runs a workflow on one or more input files
 * Daniel Huson, 9.2018
 */
public class RunWorkflow extends Application {
    private static String[] args;

    @Override
    public void init() {
        ProgramProperties.setProgramName("RunWorkflow");
        ProgramProperties.setProgramVersion(Version.SHORT_DESCRIPTION);
        NotificationManager.setEchoToConsole(false);

        PeakMemoryUsageMonitor.start();
    }

    @Override
    public void stop() {
        System.err.println("Total time:  " + PeakMemoryUsageMonitor.getSecondsSinceStartString());
        System.err.println("Peak memory: " + PeakMemoryUsageMonitor.getPeakUsageString());
        System.exit(0);
    }

    /**
     * add functional annotations to DNA alignments
     */
    public static void main(String[] args) {
        RunWorkflow.args = args;
        Application.launch();
    }

    @Override
    public void start(Stage primaryStage) {
        ProgramExecutorService.getInstance().submit(() -> {
            try {
                run(args);
            } catch (Exception ex) {
                if (!ex.getMessage().startsWith("Help"))
                    Basic.caught(ex);
            }
            Platform.exit();
        });
    }

    private void run(String[] args) throws Exception {
        final ArgsOptions options = new ArgsOptions(args, RunWorkflow.class, "Runs a SplitsTree5 workflow on input data");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("Copyright (C) 2018 Daniel H. Huson. This program comes with ABSOLUTELY NO WARRANTY.");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input Output:");
        final File inputWorkflowFile = new File(options.getOptionMandatory("-w", "workflow", "File containing SplitsTree5 workflow", ""));
        String[] inputFiles = options.getOptionMandatory("-i", "input", "File(s) containing input data (or directory)", new String[0]);
        String inputFormat = options.getOption("-f", "format", "Input format", ImporterManager.getInstance().getAllFileFormats(), "Unknown");
        String[] outputFiles = options.getOption("-o", "output", "Output file(s0 (or directory or stdout)", new String[]{"stdout"});

        options.comment("Options");
        final String inputFileExtension = options.getOption("-e", "inputExt", "File extension for input files (when providing directory for input)", "");
        final boolean inputRecursively = options.getOption("-r", "recursive", "Recursively visit all sub-directories (when providing directory for input)", false);
        if (options.getOption("-s", "silent", "Silent mode (hide all stderr output)", false))
            Basic.hideSystemErr();
        options.done();

        if (!inputWorkflowFile.canRead())
            throw new IOException("File not found or unreadable: " + inputWorkflowFile);

        // Setup and check input files:
        if (inputFiles.length == 1) {
            final File input = new File(inputFiles[0]);
            if (input.isDirectory()) {
                final ArrayList<File> inputList = Basic.getAllFilesInDirectory(input, inputFileExtension, inputRecursively);
                inputFiles = new String[inputList.size()];
                for (int i = 0; i < inputList.size(); i++) {
                    inputFiles[i] = inputList.get(i).getPath();
                }
                System.err.println("Number of input files found: " + outputFiles.length);
            }
        }

        for (String fileName : inputFiles) {
            if (!(new File(fileName)).canRead())
                throw new IOException("Input file not found or not readable: " + fileName);
        }

        // setup and check output files:
        if (inputFiles.length == 0) {
            throw new IOException("No input file(s)");
        } else if (inputFiles.length == 1) {
            if (outputFiles.length > 1)
                throw new UsageException("Too many output files specified: " + outputFiles.length);
        } else { // more than one file
            if (outputFiles.length == 1) {
                File output = new File(outputFiles[0]);
                if (output.isDirectory()) {
                    outputFiles = new String[inputFiles.length];
                    for (int i = 0; i < inputFiles.length; i++) {
                        final File input = new File(inputFiles[i]);
                        String name = Basic.replaceFileSuffix(input.getName(), "-out.spt5");
                        outputFiles[i] = (new File(output.getPath(), name)).getPath();
                    }
                } else if (!outputFiles[0].equals("stdout")) {
                    throw new IOException("Too few output files specified");
                }
            }
            if (!outputFiles[0].equals("stdout") && outputFiles.length != inputFiles.length) {
                throw new IOException("Number of output files " + outputFiles.length + " does not match number of input files " + inputFiles.length);
            }
        }

        if (!WorkflowNexusInput.isApplicable(inputWorkflowFile.getPath()))
            throw new IOException("Workflow not valid: " + inputWorkflowFile);

        final MainWindow mainWindow = new MainWindow();
        final Workflow workflow = mainWindow.getWorkflow();

        try (final ProgressListener progress = new ProgressPercentage("Loading workflow from file: " + inputWorkflowFile)) {
            WorkflowNexusInput.input(progress, workflow, new ArrayList<>(), inputWorkflowFile.getPath());
        }

        final DataNode<TaxaBlock> topTaxaNode = workflow.getTopTaxaNode();
        if (topTaxaNode == null)
            throw new IOException("Workflow does not have top taxon node");
        final DataNode topDataNode = workflow.getTopDataNode();
        if (topDataNode == null)
            throw new IOException("Workflow does not have top data node");

        System.err.println("Loaded workflow has " + workflow.getNumberOfDataNodes() + " nodes and " + workflow.getNumberOfConnectorNodes() + " connections");

        for (int i = 0; i < inputFiles.length; i++) {
            final String inputFile = inputFiles[i];
            if (inputFiles.length > 1) {
                System.err.println("++++ Processing " + inputFile + " (" + (i + 1) + " of " + inputFiles.length + ") ++++");
            }

            final CountDownLatch latch = new CountDownLatch(1);

            final ChangeListener<Boolean> listener = (c, o, n) -> {
                if (n) {
                    System.err.println("Updating workflow...");
                }
                if (!n) {
                    System.err.println("done");
                    latch.countDown();
                }
            };

            final String dataType = ImporterManager.getInstance().getDataType(inputFile);
            final String fileFormat = (inputFormat.length() == 0 ? ImporterManager.getInstance().getFileFormat(inputFile) : inputFormat);

            if (!dataType.equals(UNKNOWN) && !fileFormat.equals(UNKNOWN)) {
                final IImporter importer = ImporterManager.getInstance().getImporterByDataTypeAndFileFormat(dataType, fileFormat);
                if (importer == null)
                    throw new IOException("Can't open file '" + inputFile + "': Unknown data type or file format");
                else {
                    try (final ProgressListener progress = new ProgressPercentage("Loading input data from file: " + inputFile + " (data type: '" + dataType + "' format: '" + fileFormat + "')")) {
                        Pair<TaxaBlock, DataBlock> pair = ImportService.apply(progress, importer, inputFile);
                        final TaxaBlock inputTaxa = pair.getFirst();
                        final DataBlock inputData = pair.getSecond();
                        final StringWriter w = new StringWriter();
                        NexusExporter exporter = new NexusExporter();
                        exporter.export(w, inputTaxa, inputData);
                        final NexusStreamParser np = new NexusStreamParser(new StringReader(w.toString()));
                        ((new TaxaNexusInput())).parse(np, topTaxaNode.getDataBlock());
                        NexusParser.parse(np, topTaxaNode.getDataBlock(), topDataNode.getDataBlock());
                    }
                }
            }

            // listens for end of update and counts down latch:
            workflow.updatingProperty().addListener(listener);

            // update workflow:
            topTaxaNode.getChildren().get(0).forceRecompute();
            // wait for end of update:
            latch.await();

            // save updated workflow:
            try {
                final File outputFile = new File((outputFiles.length == inputFiles.length ? outputFiles[i] : outputFiles[0]));
                System.err.println("Saving to file: " + outputFile);
                (new WorkflowNexusOutput()).save(workflow, outputFile);
                System.err.println("done");
                System.err.println("Saved workflow has " + workflow.getNumberOfDataNodes() + " nodes and " + workflow.getNumberOfConnectorNodes() + " connections");
            } catch (IOException e) {
                System.err.println("Save FAILED: " + e.getMessage());
            }

            // remove listener
            workflow.updatingProperty().removeListener(listener);
        }
    }
}
