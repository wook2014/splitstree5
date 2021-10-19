/*
 * WorkflowDataLoader.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.core.workflow;

import jloda.util.CanceledException;
import jloda.util.Pair;
import jloda.util.progress.ProgressListener;
import jloda.util.progress.ProgressPercentage;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.dialogs.importer.ImportService;
import splitstree5.dialogs.importer.ImporterManager;
import splitstree5.io.exports.NexusExporter;
import splitstree5.io.imports.interfaces.IImporter;
import splitstree5.io.nexus.NexusParser;
import splitstree5.io.nexus.TaxaNexusInput;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static splitstree5.dialogs.importer.ImporterManager.UNKNOWN_FORMAT;

/**
 * workflow data loader
 * Daniel Huson, 11.2018
 */
public class WorkflowDataLoader {
    /**
     * loads data into a file
     *
     * @param workflow
     * @param inputFile
     * @param inputFormat
     * @throws IOException
     */
    public static void load(Workflow workflow, String inputFile, String inputFormat) throws IOException, CanceledException {
        final DataNode<TaxaBlock> topTaxaNode = workflow.getTopTaxaNode();
        if (topTaxaNode == null)
            throw new IOException("Workflow does not have top taxon node");
        final DataNode topDataNode = workflow.getTopDataNode();
        if (topDataNode == null)
            throw new IOException("Workflow does not have top data node");

        final ImporterManager.DataType dataType = ImporterManager.getInstance().getDataType(inputFile);
        final String fileFormat = (inputFormat.equalsIgnoreCase(UNKNOWN_FORMAT) ? ImporterManager.getInstance().getFileFormat(inputFile) : inputFormat);

        if (!dataType.equals(ImporterManager.DataType.Unknown) && !fileFormat.equalsIgnoreCase(UNKNOWN_FORMAT)) {
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
                    System.err.println("Number of input taxa: " + workflow.getTopTaxaNode().getDataBlock().getNtax());
                }
            }
        } else
            throw new IOException("Unknown data or file format: " + inputFile);

    }
}
