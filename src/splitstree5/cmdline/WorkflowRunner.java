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

package splitstree5.cmdline;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import jloda.fx.NotificationManager;
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

import static splitstree5.dialogs.importer.ImporterManager.UNKNOWN;

/**
 * applies a worflow to an input file
 */
public class WorkflowRunner extends Application {
    private static String[] args;

    @Override
    public void init() throws Exception {
        ProgramProperties.setProgramName("WorkflowRunner");
        ProgramProperties.setProgramVersion(Version.SHORT_DESCRIPTION);
        NotificationManager.setEchoToConsole(false);

        PeakMemoryUsageMonitor.start();
    }

    @Override
    public void stop() throws Exception {
        System.err.println("Total time:  " + PeakMemoryUsageMonitor.getSecondsSinceStartString());
        System.err.println("Peak memory: " + PeakMemoryUsageMonitor.getPeakUsageString());
        System.exit(0);
    }

    /**
     * add functional annotations to DNA alignments
     */
    public static void main(String[] args) {
        args = new String[]{"-w", "/Users/huson/IdeaProjects/community/splitstree5/examples/benjamin1.spt5",
                "-i", "/Users/huson/IdeaProjects/community/splitstree5/examples/trees.tre",
                "-o", "stdout",
                "-v"};
        WorkflowRunner.args = args;
        Application.launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final ArgsOptions options = new ArgsOptions(args, WorkflowRunner.class, "Run a SplitsTree5 workflow on input data");
        options.setVersion(ProgramProperties.getProgramVersion());
        options.setLicense("Copyright (C) 2018 Daniel H. Huson. This program comes with ABSOLUTELY NO WARRANTY.");
        options.setAuthors("Daniel H. Huson");

        options.comment("Input Output");
        final File inputWorkflowFile = new File(options.getOptionMandatory("-w", "workflow", "File containing SplitsTree5 workflow", ""));
        final File inputFile = new File(options.getOptionMandatory("-i", "input", "File containing input data", ""));
        final String inputFormat = options.getOption("-f", "format", "Input format", ImporterManager.getInstance().getAllFileFormats(), "Unknown");
        final File outputFile = new File(options.getOption("-o", "output", "Output file (or stdout)", "stdout"));

        options.done();

        if (!inputWorkflowFile.canRead())
            throw new IOException("File not found or unreadable: " + inputWorkflowFile);
        if (!inputFile.canRead())
            throw new IOException("File not found or unreadable: " + inputFile);


        if (!WorkflowNexusInput.isApplicable(inputWorkflowFile.getPath()))
            throw new IOException("Workflow no valid: " + inputWorkflowFile);

        final MainWindow mainWindow = new MainWindow();
        final Workflow workflow = mainWindow.getWorkflow();

        WorkflowNexusInput.input(new ProgressPercentage("Loading workflow from file: " + inputWorkflowFile), workflow, new ArrayList<>(), inputWorkflowFile.getPath());

        final DataNode<TaxaBlock> topTaxaNode = workflow.getTopTaxaNode();
        if (topTaxaNode == null)
            throw new IOException("Workflow does not have top taxon node");
        final DataNode topDataNode = workflow.getTopDataNode();
        if (topDataNode == null)
            throw new IOException("Workflow does not have top data node");

        final String dataType = ImporterManager.getInstance().getDataType(inputFile.getPath());
        final String fileFormat = (inputFormat.length() == 0 ? ImporterManager.getInstance().getFileFormat(inputFile.getPath()) : inputFormat);
        if (!dataType.equals(UNKNOWN) && !fileFormat.equals(UNKNOWN)) {

            final IImporter importer = ImporterManager.getInstance().getImporterByDataTypeAndFileFormat(dataType, fileFormat);
            if (importer == null)
                throw new IOException("Can't open file '" + inputFile + "': Unknown data type or file format");
            else {
                Pair<TaxaBlock, DataBlock> pair = ImportService.apply(new ProgressPercentage("Loading input data from file: " + inputFile + " (data type: '" + dataType + "' format: '" + fileFormat + "')"),
                        importer, inputFile.getPath());
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
        workflow.updatingProperty().addListener((c, o, n) -> {
            if (n)
                System.err.println("Updating workflow...");
            if (!n) {
                System.err.println("done");
                try {
                    System.err.println("Saving to file: " + outputFile);
                    (new WorkflowNexusOutput()).save(workflow, outputFile);
                    System.err.println("done");
                } catch (IOException e) {
                    System.err.println("Save FAILED: " + e.getMessage());
                }
                Platform.exit();
            }
        });
        topTaxaNode.getChildren().get(0).forceRecompute();
    }
}
