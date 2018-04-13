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

import jloda.fx.NotificationManager;
import jloda.fx.RecentFilesManager;
import jloda.util.Basic;
import jloda.util.Pair;
import splitstree5.core.Document;
import splitstree5.core.datablocks.SplitsTree5Block;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowNode;
import splitstree5.io.exports.NexusExporter;
import splitstree5.io.nexus.SplitsTree5NexusOutput;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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

        final Map<String, Integer> nodeType2Count = new HashMap<>();
        final Map<WorkflowNode, String> node2title = new HashMap<>();

        final String fileName = document.getFileName();

        try (Writer w = new BufferedWriter(new FileWriter(document.getFileName()))) {
            w.write("#nexus\n");
            new SplitsTree5NexusOutput().write(w, splitsTree5Block);

            nexusExporter.setTitle("InputTaxa");
            node2title.put(workflow.getTopTaxaNode(), "InputTaxa");
            nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock());

            setupExporter(workflow.getTaxaFilter(), nexusExporter, nodeType2Count, node2title);
            nexusExporter.export(w, workflow.getTaxaFilter().getAlgorithm());

            setupExporter(workflow.getWorkingTaxaNode(), nexusExporter, nodeType2Count, node2title);
            nexusExporter.export(w, workflow.getWorkingTaxaNode().getDataBlock());

            nexusExporter.setTitle("InputData");
            node2title.put(workflow.getTopDataNode(), "InputData");
            nexusExporter.setLink(null);
            nexusExporter.export(w, workflow.getWorkingTaxaBlock(), workflow.getTopDataNode().getDataBlock());

            setupExporter(workflow.getTopFilter(), nexusExporter, nodeType2Count, node2title);
            nexusExporter.export(w, workflow.getTopFilter().getAlgorithm());

            final Queue<WorkflowNode> queue = new LinkedList<>();
            queue.add(workflow.getWorkingDataNode());
            while (queue.size() > 0) {
                final WorkflowNode node = queue.poll();
                if (node instanceof DataNode) {
                    final DataNode dataNode = (DataNode) node;
                    setupExporter(dataNode, nexusExporter, nodeType2Count, node2title);
                    nexusExporter.export(w, workflow.getWorkingTaxaBlock(), dataNode.getDataBlock());
                } else {
                    final Connector connector = (Connector) node;
                    setupExporter(connector, nexusExporter, nodeType2Count, node2title);
                    nexusExporter.export(w, connector.getAlgorithm());
                }
                queue.addAll(node.getChildren());
            }
        }

        for (WorkflowNode node : workflow.dataNodes()) {
            node.setTitle(null);
        }
        for (WorkflowNode node : workflow.connectors()) {
            node.setTitle(null);
        }

        NotificationManager.showInformation("Saved " + splitsTree5Block.size() + " blocks to file: " + fileName);
        document.setDirty(false);
        document.setHasSplitsTree5File(true);
        if (!document.getFileName().endsWith(".tmp"))
            RecentFilesManager.getInstance().addRecentFile(fileName);
    }

    /**
     * sets up the exporter so that it reports title and links
     *
     * @param dataNode
     * @param nexusExporter
     * @param nodeType2Count
     * @param node2title
     */
    private void setupExporter(DataNode dataNode, NexusExporter nexusExporter, Map<String, Integer> nodeType2Count, Map<WorkflowNode, String> node2title) {
        final Integer count = nodeType2Count.getOrDefault(dataNode.getDataBlock().getBlockName(), 1);
        final String title = Basic.capitalize(dataNode.getDataBlock().getBlockName(), true) + count;
        nodeType2Count.put(dataNode.getDataBlock().getBlockName(), count + 1);
        node2title.put(dataNode, title);
        nexusExporter.setTitle(title);
        if (dataNode.getParent() != null)
            nexusExporter.setLink(new Pair<>(dataNode.getParent().getAlgorithm().getBlockName(), node2title.get(dataNode.getParent())));
    }

    /**
     * sets up the exporter so that it reports title and links
     *
     * @param connector
     * @param nexusExporter
     * @param nodeType2Count
     * @param node2title
     */
    private void setupExporter(Connector connector, NexusExporter nexusExporter, Map<String, Integer> nodeType2Count, Map<WorkflowNode, String> node2title) {
        final String nodeType = connector.getAlgorithm().getBlockName();
        final Integer count = nodeType2Count.getOrDefault(nodeType, 1);
        final String title = "Algorithm" + count;
        nodeType2Count.put(nodeType, count + 1);
        node2title.put(connector, title);
        nexusExporter.setTitle(title);
        if (connector.getParent() != null)
            nexusExporter.setLink(new Pair<>(connector.getParent().getDataBlock().getBlockName(), node2title.get(connector.getParent())));
    }
}
