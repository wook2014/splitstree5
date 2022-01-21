/*
 * WorkflowNexusOutput.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.nexus.workflow;

import jloda.fx.window.NotificationManager;
import jloda.util.Pair;
import jloda.util.ProgramProperties;
import splitstree5.core.datablocks.SplitsTree5Block;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowNode;
import splitstree5.io.exports.NexusExporter;
import splitstree5.io.nexus.SplitsTree5NexusOutput;

import java.io.*;
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
     * @param workflow
     * @param file     file or stdout
     * @throws IOException
     */
    public void save(Workflow workflow, final File file, boolean asWorkflowOnly) throws IOException {
        if (file.getParentFile() != null && file.getParentFile().isDirectory())
            ProgramProperties.put("SaveDir", file.getParent());
        try (Writer w = new BufferedWriter(file.getName().equals("stdout") ? new OutputStreamWriter(System.out) : new FileWriter(file))) {
            final int count = save(workflow, w, asWorkflowOnly);
            NotificationManager.showInformation("Saved " + count + " blocks to file: " + file.getPath());
        }

    }

    /**
     * write a workflow
     *
     * @param workflow
     * @param w
     * @param asWorkflowOnly
     * @return
     * @throws IOException
     */
    public int save(Workflow workflow, Writer w, boolean asWorkflowOnly) throws IOException {
        SplitsTree5Block splitsTree5Block = new SplitsTree5Block();
        splitsTree5Block.setOptionNumberOfDataNodes(workflow.getNumberOfDataNodes());
        splitsTree5Block.setOptionNumberOfAlgorithms(workflow.getNumberOfConnectorNodes());

        final NexusExporter nexusExporter = new NexusExporter();
        nexusExporter.setAsWorkflowOnly(asWorkflowOnly);
        nexusExporter.setPrependTaxa(false);

        w.write("#nexus [SplitsTree5]\n");

        (new SplitsTree5NexusOutput()).write(w, splitsTree5Block);

        if (!asWorkflowOnly)
            w.write("\n[\n" + workflow.getDocument().methodsTextProperty().get().replaceAll("\\[", "(").replaceAll("]", ")") + "]\n");

        setupExporter(workflow.getTopTaxaNode(), nexusExporter);
        nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock());

        if (workflow.getTopTraitsNode() != null) {
            setupExporter(workflow.getTopTraitsNode(), nexusExporter);
            nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock(), workflow.getTopTraitsNode().getDataBlock());
        }

        setupExporter(workflow.getTaxaFilter(), nexusExporter);
        nexusExporter.export(w, workflow.getTaxaFilter().getAlgorithm());

        setupExporter(workflow.getWorkingTaxaNode(), nexusExporter);
        nexusExporter.export(w, workflow.getWorkingTaxaNode().getDataBlock());

        if (workflow.getWorkingTraitsNode() != null) {
            setupExporter(workflow.getWorkingTraitsNode(), nexusExporter);
            nexusExporter.export(w, workflow.getWorkingTaxaBlock(), workflow.getWorkingTraitsNode().getDataBlock());
        }

        setupExporter(workflow.getTopDataNode(), nexusExporter);
        nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock(), workflow.getTopDataNode().getDataBlock());

        setupExporter(workflow.getTopFilter(), nexusExporter);
        nexusExporter.export(w, workflow.getTopFilter().getAlgorithm());

        final Queue<WorkflowNode> queue = new LinkedList<>();
        queue.add(workflow.getWorkingDataNode());
        while (queue.size() > 0) {
            final WorkflowNode node = queue.poll();
            if (node instanceof DataNode) {
                final DataNode dataNode = (DataNode) node;
                setupExporter(dataNode, nexusExporter);
                nexusExporter.export(w, workflow.getWorkingTaxaBlock(), dataNode.getDataBlock());
            } else {
                final Connector connector = (Connector) node;
                setupExporter(connector, nexusExporter);
                nexusExporter.export(w, connector.getAlgorithm());
            }
            queue.addAll(node.getChildren());
        }

        return splitsTree5Block.size();
    }

    /**
     * sets up the exporter so that it reports title and links
     *
     * @param dataNode
     * @param nexusExporter
     */
    private void setupExporter(DataNode dataNode, NexusExporter nexusExporter) {
        nexusExporter.setTitle(dataNode.getTitle());
        if (dataNode.getParent() != null)
            nexusExporter.setLink(new Pair<>(dataNode.getParent().getAlgorithm().getBlockName(), dataNode.getParent().getTitle()));
    }

    /**
     * sets up the exporter so that it reports title and links
     *
     * @param connector
     * @param nexusExporter
     */
    private void setupExporter(Connector connector, NexusExporter nexusExporter) {
        nexusExporter.setTitle(connector.getTitle());
        if (connector.getParent() != null)
            nexusExporter.setLink(new Pair<>(connector.getParent().getDataBlock().getBlockName(), connector.getParent().getTitle()));
    }
}
