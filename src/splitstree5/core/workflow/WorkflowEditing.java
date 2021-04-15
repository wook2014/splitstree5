/*
 * WorkflowEditing.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.util.Basic;
import jloda.util.Pair;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.CharactersFilter;
import splitstree5.core.algorithms.filters.SplitsFilter;
import splitstree5.core.algorithms.filters.TreesFilter;
import splitstree5.core.algorithms.trees2trees.RootByMidpointAlgorithm;
import splitstree5.core.algorithms.trees2trees.RootByOutGroupAlgorithm;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TreesBlock;

import java.util.ArrayList;

/**
 * methods for editing the workflow graph
 * Daniel Huson, 5.2018
 */
public class WorkflowEditing {
    /**
     * creates a short chain of nodes leading to a view. If the type of view is already present, returns the node
     *
     * @param currentDataNode
     * @param desiredParentClass
     * @param algorithm1class
     * @param data1class
     * @return connector and view node
     * @throws Exception
     */
    public static Pair<Connector, DataNode> findOrCreatePath(Workflow workflow, DataNode currentDataNode, Class<? extends DataBlock> desiredParentClass,
                                                             Class<? extends Algorithm> algorithm1class, Class<? extends DataBlock> data1class) {
        return findOrCreatePath(workflow, currentDataNode, desiredParentClass, new Pair<>(algorithm1class, data1class));
    }

    /**
     * creates a short chain of nodes leading to a view. If the type of view is already present, returns the node
     *
     * @param currentDataNode
     * @param desiredParentClass
     * @param algorithm1class
     * @param data1class
     * @param algorithm2class    can be null
     * @param data2class         can be null if algorithm2class is also null
     * @return connector and view node
     */
    public static Pair<Connector, DataNode> findOrCreatePath(Workflow workflow, DataNode currentDataNode, Class<? extends DataBlock> desiredParentClass,
                                                             Class<? extends Algorithm> algorithm1class, Class<? extends DataBlock> data1class,
                                                             Class<? extends Algorithm> algorithm2class, Class<? extends DataBlock> data2class) {
        return findOrCreatePath(workflow, currentDataNode, desiredParentClass, new Pair<>(algorithm1class, data1class), new Pair<>(algorithm2class, data2class));
    }

    /**
     * creates a short chain of nodes leading to a view. If the type of view is already present, returns the node
     *
     * @param currentDataNode
     * @param desiredParentClass
     * @param path
     * @return connector and view node
     */
    public static Pair<Connector, DataNode> findOrCreatePath(Workflow workflow, DataNode currentDataNode, Class<? extends DataBlock> desiredParentClass, Pair<Class<? extends Algorithm>, Class<? extends DataBlock>>... path) {
        final DataNode parent = workflow.getAncestorForClass(currentDataNode, desiredParentClass);
        if (parent != null) {
            try {
                Pair<Connector, DataNode> result = workflow.matchPath(parent, path);
                if (result != null)
                    return result;
                else {
                    Connector firstConnector = null;
                    DataNode lastDataNode = parent;

                    for (Pair<Class<? extends Algorithm>, Class<? extends DataBlock>> pair : path) {
                        final DataNode dataNode = workflow.createDataNode(pair.getSecond().getConstructor().newInstance());
                        final Connector connector = workflow.createConnector(lastDataNode, dataNode, (Algorithm) (pair.getFirst().getConstructor().newInstance()));
                        if (firstConnector == null)
                            firstConnector = connector;
                        lastDataNode = dataNode;
                    }
                    if (firstConnector != null) {
                        firstConnector.forceRecompute();
                        return new Pair<>(firstConnector, lastDataNode);
                    }
                }
            } catch (Exception ex) {
                Basic.caught(ex);
            }
        }
        return null;
    }

    /**
     * find the filter for this node, if it exists, otherwise insert one and return it
     *
     * @param dataNode
     * @return filter or null
     */
    public static Connector findOrInsertFilter(Workflow workflow, DataNode dataNode) {
        if (dataNode != null && dataNode.getDataBlock() != null && dataNode.getParent() != null && dataNode.getParent().getAlgorithm() != null) {
            final DataBlock dataBlock = dataNode.getDataBlock();
            final Connector inConnector = dataNode.getParent();

            if (dataBlock instanceof CharactersBlock) {
                if (inConnector.getAlgorithm() instanceof CharactersFilter)
                    return inConnector;
                else {
                    final DataNode<CharactersBlock> newDataNode = workflow.createDataNode(new CharactersBlock());
                    ArrayList<Connector<CharactersBlock, ? extends DataBlock>> connectors = new ArrayList<>(dataNode.getChildren());
                    dataNode.getChildren().clear();
                    for (Connector<CharactersBlock, ? extends DataBlock> connector : connectors) {
                        final DataNode child = connector.getChild();
                        final Algorithm algorithm = connector.getAlgorithm();
                        workflow.delete(connector, true, false);
                        workflow.createConnector(newDataNode, child, algorithm);
                    }
                    return workflow.createConnector(dataNode, newDataNode, new CharactersFilter());
                }
            } else if (dataBlock instanceof SplitsBlock) {
                if (inConnector.getAlgorithm() instanceof SplitsFilter)
                    return inConnector;
                else {
                    final DataNode<SplitsBlock> newDataNode = workflow.createDataNode(new SplitsBlock());
                    ArrayList<Connector<SplitsBlock, ? extends DataBlock>> connectors = new ArrayList<>(dataNode.getChildren());
                    dataNode.getChildren().clear();
                    for (Connector<SplitsBlock, ? extends DataBlock> connector : connectors) {
                        final DataNode child = connector.getChild();
                        final Algorithm algorithm = connector.getAlgorithm();
                        workflow.delete(connector, true, false);
                        workflow.createConnector(newDataNode, child, algorithm);
                    }
                    return workflow.createConnector(dataNode, newDataNode, new SplitsFilter());
                }
            } else if (dataBlock instanceof TreesBlock) {
                if (inConnector.getAlgorithm() instanceof TreesFilter)
                    return inConnector;
                else {
                    final DataNode<TreesBlock> newDataNode = workflow.createDataNode(new TreesBlock());
                    ArrayList<Connector<TreesBlock, ? extends DataBlock>> connectors = new ArrayList<>(dataNode.getChildren());
                    dataNode.getChildren().clear();
                    for (Connector<TreesBlock, ? extends DataBlock> connector : connectors) {
                        final DataNode child = connector.getChild();
                        final Algorithm algorithm = connector.getAlgorithm();
                        workflow.delete(connector, true, false);
                        workflow.createConnector(newDataNode, child, algorithm);
                    }
                    return workflow.createConnector(dataNode, newDataNode, new TreesFilter());
                }
            }
        }
        return null;
    }

    /**
     * finds tree trees block above this node and then finds or add tree-rooting algorithm
     *
     * @param dataNode
     * @return tree rooting algorithm node or null
     */
    public static Pair<Connector, DataNode> findOrInsertTreeRootAlgorithm(Workflow workflow, DataNode dataNode, Algorithm<TreesBlock, TreesBlock> algorithm) {
        final Pair<DataNode<TreesBlock>, Connector<TreesBlock, ? extends DataBlock>> pair = workflow.getAncestorAndDescendantForClass(dataNode, TreesBlock.class);

        final DataNode<TreesBlock> treesNode = pair.getFirst();


        if (treesNode != null) {
            if (pair.getSecond() != null) {
                final Connector<TreesBlock, ? extends DataBlock> child = pair.getSecond();
                final DataNode<TreesBlock> treeNode = workflow.createDataNode(new TreesBlock());
                final Connector<TreesBlock, TreesBlock> rootConnector = workflow.createConnector(treesNode, treeNode, algorithm);
                child.changeParent(treeNode);
                return new Pair<>(rootConnector, rootConnector.getChild());
            } else {
                for (Connector<TreesBlock, ? extends DataBlock> child : treesNode.getChildren()) {
                    if (child.getAlgorithm() instanceof RootByOutGroupAlgorithm || child.getAlgorithm() instanceof RootByMidpointAlgorithm) {
                        child.setAlgorithm((Algorithm) algorithm);
                        return new Pair<>(child, child.getChild());
                    }
                }
                if (treesNode.getChildren().size() > 0) {
                    final Connector<TreesBlock, ? extends DataBlock> child = treesNode.getChildren().get(0);
                    final DataNode<TreesBlock> treeNode = workflow.createDataNode(new TreesBlock());
                    final Connector<TreesBlock, TreesBlock> rootConnector = workflow.createConnector(treesNode, treeNode, algorithm);
                    child.changeParent(treeNode);
                    return new Pair<>(rootConnector, rootConnector.getChild());
                }
            }
        }
        return null;
    }
}
