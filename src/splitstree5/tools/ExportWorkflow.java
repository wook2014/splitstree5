/*
 *  ExportWorkflow.java Copyright (C) 2019 Daniel H. Huson
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
import javafx.stage.Stage;
import jloda.fx.util.ArgsOptions;
import jloda.fx.window.NotificationManager;
import jloda.fx.util.ProgramExecutorService;
import jloda.util.*;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.dialogs.exporter.ExportManager;
import splitstree5.io.nexus.workflow.WorkflowNexusInput;
import splitstree5.main.MainWindow;
import splitstree5.main.Version;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * runs a workflow on one or more input files
 * Daniel Huson, 9.2018
 */
public class ExportWorkflow extends Application {
    private static String[] args;

    @Override
    public void init() {
        ProgramProperties.setProgramName("ExportWorkflow");
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
        ExportWorkflow.args = args;
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
        final ArgsOptions options = new ArgsOptions(args, ExportWorkflow.class, "Runs a SplitsTree5 workflow on input data");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("Copyright (C) 2019 Daniel H. Huson. This program comes with ABSOLUTELY NO WARRANTY.");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input Output:");
        final File inputWorkflowFile = new File(options.getOptionMandatory("-w", "workflow", "File containing SplitsTree5 workflow", ""));
        final String[] nodeNames = options.getOption("-n", "node", "Title(s) of node(s) to be exported", new String[0]);
        final String[] exportFormats = options.getOption("-e", "exporter", "Name of exporter(s) to use", ExportManager.getInstance().getExporterNames(), new String[0]);
        String[] outputFiles = options.getOption("-o", "output", "Output file(s) (or directory or stdout)", new String[]{"stdout"});

        options.comment(ArgsOptions.OTHER);
        final boolean methods = options.getOption("-m", "methods", "Report methods used by worflow", false);
        final boolean silent = options.getOption("-s", "silent", "Silent mode (hide all stderr output)", false);
        if (silent)
            Basic.hideSystemErr();
        options.done();

        if (!inputWorkflowFile.canRead())
            throw new IOException("File not found or unreadable: " + inputWorkflowFile);

        if (nodeNames.length != exportFormats.length)
            throw new IOException("Number of specified nodes " + nodeNames.length + " does not match number of specified formats " + exportFormats.length);

        // setup and check output files:
        if (nodeNames.length == 0 && !methods) {
            throw new IOException("No node name specified");
        } else { // one or more nodes
            if (outputFiles.length == 1) {
                File output = new File(outputFiles[0]);
                if (output.isDirectory()) {
                    outputFiles = new String[nodeNames.length];
                    for (int i = 0; i < nodeNames.length; i++) {
                        final String suffix = ExportManager.getInstance().getExporterByName(exportFormats[i]).getExtensions().get(0);
                        outputFiles[i] = Basic.replaceFileSuffix(inputWorkflowFile.getPath(), "-" + nodeNames[i] + "." + suffix);
                    }
                } else if (nodeNames.length > 1 && !outputFiles[0].equals("stdout")) {
                    throw new IOException("Too few output files specified");
                }
            }
            if (!outputFiles[0].equals("stdout") && outputFiles.length != nodeNames.length) {
                throw new IOException("Number of output files " + outputFiles.length + " does not match number of specified nodes" + nodeNames.length);
            }
        }

        if (!WorkflowNexusInput.isApplicable(inputWorkflowFile.getPath()))
            throw new IOException("Invalid workflow in file: " + inputWorkflowFile);

        final MainWindow mainWindow = new MainWindow();
        final Workflow workflow = mainWindow.getWorkflow();

        try (final ProgressListener progress = new ProgressPercentage("Loading workflow from file: " + inputWorkflowFile)) {
            WorkflowNexusInput.input(progress, workflow, new ArrayList<>(), inputWorkflowFile.getPath());
        }

        workflow.getDocument().updateMethodsText();

        final DataNode<TaxaBlock> topTaxaNode = workflow.getTopTaxaNode();
        if (topTaxaNode == null)
            throw new IOException("Incomplete workflow: top taxon node not found");
        final DataNode topDataNode = workflow.getTopDataNode();
        if (topDataNode == null)
            throw new IOException("Incomplete workflow: top data node not found");

        System.err.println("Loaded workflow has " + workflow.getNumberOfDataNodes() + " nodes and " + workflow.getNumberOfConnectorNodes() + " connections");

        if (methods) {
            System.out.println(mainWindow.getDocument().methodsTextProperty().getValue());
            System.out.flush();
        }

        for (int i = 0; i < nodeNames.length; i++) {
            final DataNode dataNode = workflow.findDataNode(nodeNames[i]);
            if (dataNode == null)
                throw new IOException("Node with title '" + nodeNames[i] + "': not found");
            final String outputFile = (outputFiles.length == 1 ? outputFiles[0] : outputFiles[i]);
            System.err.println("Exporting node '" + nodeNames[i] + "' to " + outputFile);
            ExportManager.getInstance().exportFile(outputFile, workflow.getWorkingTaxaBlock(), dataNode.getDataBlock(), exportFormats[i]);
        }
    }
}
