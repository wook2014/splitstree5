/*
 *  Copyright (C) 2019 Daniel H. Huson
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
 *  Copyright (C) 2019 Daniel H. Huson
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

import jloda.fx.util.NotificationManager;
import jloda.fx.util.ProgramProperties;
import jloda.util.Basic;
import jloda.util.Pair;
import splitstree5.core.datablocks.SplitsTree5Block;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.core.workflow.WorkflowNode;
import splitstree5.io.exports.NexusExporter;
import splitstree5.io.nexus.SplitsTree5NexusOutput;

import java.io.*;
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
     * @param workflow
     * @param file file or stdout
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

        final Map<String, Integer> nodeType2Count = new HashMap<>();
        final Map<WorkflowNode, String> node2title = new HashMap<>();

        w.write("#nexus [SplitsTree5]\n");

        (new SplitsTree5NexusOutput()).write(w, splitsTree5Block);

        if (!asWorkflowOnly)
            w.write("\n[\n" + workflow.getDocument().methodsTextProperty().get().replaceAll("\\[", "(").replaceAll("]", ")") + "]\n");

        nexusExporter.setTitle("TopTaxa");
        node2title.put(workflow.getTopTaxaNode(), "TopTaxa");
        nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock());

        if (workflow.getTopTraitsNode() != null) {
            workflow.getTopTraitsNode().setTitle("TopTraits");
            node2title.put(workflow.getTopTraitsNode(), "TopTraits");
            setupExporter(workflow.getTopTraitsNode(), nexusExporter, nodeType2Count, node2title);
            nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock(), workflow.getTopTraitsNode().getDataBlock());
        }

        node2title.put(workflow.getTaxaFilter(), "TaxaFilter");
        setupExporter(workflow.getTaxaFilter(), nexusExporter, nodeType2Count, node2title);
        nexusExporter.export(w, workflow.getTaxaFilter().getAlgorithm());

        node2title.put(workflow.getWorkingTaxaNode(), "WorkingTaxa");
        setupExporter(workflow.getWorkingTaxaNode(), nexusExporter, nodeType2Count, node2title);
        nexusExporter.export(w, workflow.getWorkingTaxaNode().getDataBlock());

        if (workflow.getWorkingTraitsNode() != null) {
            node2title.put(workflow.getWorkingTraitsNode(), "Traits");
            setupExporter(workflow.getWorkingTraitsNode(), nexusExporter, nodeType2Count, node2title);
            nexusExporter.export(w, workflow.getWorkingTaxaBlock(), workflow.getWorkingTraitsNode().getDataBlock());
        }

        node2title.put(workflow.getTopDataNode(), "TopData");
        setupExporter(workflow.getTopDataNode(), nexusExporter, nodeType2Count, node2title);
        nexusExporter.export(w, workflow.getTopTaxaNode().getDataBlock(), workflow.getTopDataNode().getDataBlock());

        node2title.put(workflow.getTopFilter(), "TopFilter");
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

        for (WorkflowNode node : workflow.dataNodes()) {
            node.setTitle(null);
        }
        for (WorkflowNode node : workflow.connectors()) {
            node.setTitle(null);
        }
        return splitsTree5Block.size();
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
        final String title;
        if (node2title.get(dataNode) != null)
            title = node2title.get(dataNode);
        else {
            title = Basic.capitalize(dataNode.getDataBlock().getBlockName(), true) + count;
            nodeType2Count.put(dataNode.getDataBlock().getBlockName(), count + 1);
            node2title.put(dataNode, title);
        }

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
        final String title;
        if (node2title.get(connector) != null)
            title = node2title.get(connector);
        else {
            title = "Algorithm" + count;
            nodeType2Count.put(nodeType, count + 1);
            node2title.put(connector, title);
        }
        nexusExporter.setTitle(title);
        if (connector.getParent() != null)
            nexusExporter.setLink(new Pair<>(connector.getParent().getDataBlock().getBlockName(), node2title.get(connector.getParent())));
    }
}
