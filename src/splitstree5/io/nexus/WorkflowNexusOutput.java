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

package splitstree5.io.nexus;

import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import splitstree5.core.Document;
import splitstree5.core.datablocks.SplitsTree5Block;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowNode;
import splitstree5.io.exports.NexusExporter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Queue;

/**
 * write workflow in nexus
 * Daniel Huson, 2.2018
 */
public class WorkflowNexusOutput {
    /**
     * save the workflow in nexus format
     *
     * @param document
     * @throws IOException
     */
    public void save(Document document) throws IOException {
        final Workflow workflow = document.getWorkflow();
        SplitsTree5Block splitsTree5Block = new SplitsTree5Block();
        splitsTree5Block.setOptionNumberOfDataNodes(workflow.getNumberOfDataNodes());
        splitsTree5Block.setOptionNumberOfAlgorithms(workflow.getNumberOfConnectorNodes());

        final NexusExporter nexusExporter = new NexusExporter();
        nexusExporter.setPrependTaxa(false);

        final String fileName = document.getFileName();

        try (Writer w = new BufferedWriter(new FileWriter(document.getFileName()))) {
            w.write("#nexus\n");
            new SplitsTree5NexusOutput().write(w, splitsTree5Block);
            nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock());
            nexusExporter.export(w, workflow.getTaxaFilter().getAlgorithm());
            nexusExporter.export(w, workflow.getWorkingTaxaNode().getDataBlock());

            final Queue<WorkflowNode> queue = new LinkedList<>();
            queue.add(workflow.getWorkingDataNode());
            while (queue.size() > 0) {
                final WorkflowNode node = queue.poll();
                if (node instanceof DataNode)
                    nexusExporter.export(w, workflow.getWorkingTaxaBlock(), ((DataNode) node).getDataBlock());
                else
                    nexusExporter.export(w, ((Connector) node).getAlgorithm());
                queue.addAll(node.getChildren());
            }
        }
        // todo: RecentFilesManager.getInstance().addRecentFile(fileName);
        NotificationManager.showInformation("Saved " + splitsTree5Block.size() + " blocks to file: " + fileName);
        if (!document.getFileName().endsWith(".tmp"))
            RecentFilesManager.getInstance().addRecentFile(fileName);

    }
}
