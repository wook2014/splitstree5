/*
 *  RunWorkflow.java Copyright (C) 2020 Daniel H. Huson
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
import jloda.fx.util.ArgsOptions;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.ResourceManagerFX;
import jloda.fx.window.NotificationManager;
import jloda.util.*;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowDataLoader;
import splitstree5.dialogs.exporter.ExportManager;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.io.nexus.workflow.WorkflowNexusInput;
import splitstree5.io.nexus.workflow.WorkflowNexusOutput;
import splitstree5.main.MainWindow;
import splitstree5.main.SplitsTree5;
import splitstree5.main.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import static splitstree5.dialogs.importer.ImporterManager.UNKNOWN_FORMAT;

/**
 * runs a workflow on one or more input files
 * Daniel Huson, 9.2018
 */
public class
RunWorkflow extends Application {
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
        ResourceManagerFX.addResourceRoot(SplitsTree5.class, "splitstree5/resources");

        final ArgsOptions options = new ArgsOptions(args, RunWorkflow.class, "Exports data from a SplitsTree5 workflow");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("Copyright (C) 2019 Daniel H. Huson. This program comes with ABSOLUTELY NO WARRANTY.");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input Output:");
        final File inputWorkflowFile = new File(options.getOptionMandatory("-w", "workflow", "File containing SplitsTree5 workflow", ""));
        String[] inputFiles = options.getOptionMandatory("-i", "input", "File(s) containing input data (or directory)", new String[0]);
        final String inputFormat = options.getOption("-f", "format", "Input format", ImporterManager.getInstance().getAllFileFormats(), UNKNOWN_FORMAT);
        String[] outputFiles = options.getOption("-o", "output", "Output file(s) (or directory or stdout)", new String[]{"stdout"});

        final String nodeName = options.getOption("-n", "node", "Title of node to be exported", "");
        final String exportFormat = options.getOption("-e", "exporter", "Name of exporter to use", ExportManager.getInstance().getExporterNames(), "");

        options.comment(ArgsOptions.OTHER);
        final String inputFileExtension = options.getOption("-x", "inputExt", "File extension for input files (when providing directory for input)", "");
        final boolean inputRecursively = options.getOption("-r", "recursive", "Recursively visit all sub-directories (when providing directory for input)", false);

        final String defaultPreferenceFile;
        if (ProgramProperties.isMacOS())
            defaultPreferenceFile = System.getProperty("user.home") + "/Library/Preferences/SplitsTree5.def";
        else
            defaultPreferenceFile = System.getProperty("user.home") + File.separator + ".SplitsTree5.def";
        final String propertiesFile = options.getOption("-p", "propertiesFile", "Properties file", defaultPreferenceFile);


        final boolean silent = options.getOption("-s", "silent", "Silent mode (hide all stderr output)", false);
        if (silent)
            Basic.hideSystemErr();
        options.done();

        ProgramProperties.load(propertiesFile);

        if (!inputWorkflowFile.canRead())
            throw new IOException("File not found or unreadable: " + inputWorkflowFile);

        if ((nodeName.length() == 0) != (exportFormat.length() == 0))
            throw new IOException("Must specify both node name and exporter, or none");

        final boolean exportCompleteWorkflow = (nodeName.length() == 0);

        // Setup and check input files:
        if (inputFiles.length == 1) {
            final File input = new File(inputFiles[0]);
            if (input.isDirectory()) {
                final ArrayList<File> inputList = Basic.getAllFilesInDirectory(input, inputRecursively, inputFileExtension);
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
        } else { // one or more input files
            if (outputFiles.length == 1) {
                File output = new File(outputFiles[0]);
                if (output.isDirectory()) {
                    final String extension;
                    if (exportCompleteWorkflow)
                        extension = ".stree5";
                    else {
                        extension = "." + ExportManager.getInstance().getExporterByName(exportFormat).getExtensions().get(0);
                    }

                    outputFiles = new String[inputFiles.length];
                    for (int i = 0; i < inputFiles.length; i++) {
                        final File input = new File(inputFiles[i]);
                        String name = Basic.replaceFileSuffix(input.getName(), "-out" + extension);
                        outputFiles[i] = (new File(output.getPath(), name)).getPath();
                    }
                } else if (inputFiles.length > 1 && !outputFiles[0].equals("stdout")) {
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
        System.err.println("Number of input taxa: " + workflow.getTopTaxaNode().getDataBlock().getNtax());

        for (int i = 0; i < inputFiles.length; i++) {
            final String inputFile = inputFiles[i];
                System.err.println("++++ Processing " + inputFile + " (" + (i + 1) + " of " + inputFiles.length + ") ++++");

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

            WorkflowDataLoader.load(workflow, inputFile, inputFormat);

            // listens for end of update and counts down latch:
            workflow.updatingProperty().addListener(listener);

            try {
                // update workflow:
                topTaxaNode.getChildren().get(0).forceRecompute();
                // wait for end of update:
                latch.await();

                // save updated workflow:
                try {
                    final File outputFile = new File((outputFiles.length == inputFiles.length ? outputFiles[i] : outputFiles[0]));
                    System.err.println("Saving to file: " + outputFile);
                    if (exportCompleteWorkflow) {
                        (new WorkflowNexusOutput()).save(workflow, outputFile, false);
                        System.err.println("done");
                        System.err.println("Saved workflow has " + workflow.getNumberOfDataNodes() + " nodes and " + workflow.getNumberOfConnectorNodes() + " connections");
                    } else {
                        final DataNode dataNode = workflow.findDataNode(nodeName);

                        if (dataNode == null)
                            throw new IOException("Node with title '" + nodeName + "': not found");

                        System.err.println("Exporting node '" + nodeName + "' to file: " + outputFile);
                        ExportManager.getInstance().exportFile(outputFile.getPath(), workflow.getWorkingTaxaBlock(), dataNode.getDataBlock(), exportFormat);
                    }
                } catch (IOException e) {
                    System.err.println("Save FAILED: " + e.getMessage());
                }
            } finally {
                // remove listener
                workflow.updatingProperty().removeListener(listener);
            }
        }
    }
}
