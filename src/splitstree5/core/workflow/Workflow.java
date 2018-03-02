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

package splitstree5.core.workflow;

import com.sun.istack.internal.Nullable;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import jloda.fx.ASelectionModel;
import jloda.util.Basic;
import jloda.util.Pair;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.ReportConnector;
import splitstree5.core.algorithms.filters.CharactersFilter;
import splitstree5.core.algorithms.filters.SplitsFilter;
import splitstree5.core.algorithms.filters.TreesFilter;
import splitstree5.core.datablocks.*;
import splitstree5.core.topfilters.*;

import java.util.*;

/**
 * The workflow graph
 * <p/>
 * Daniel Huson, 2016
 */
public class Workflow {
    private final IntegerProperty size = new SimpleIntegerProperty();

    private final ObservableSet<Connector> connectorNodes = FXCollections.observableSet();
    private final ObservableSet<DataNode> dataNodes = FXCollections.observableSet();

    private final ObservableSet<DataNode> topNodes = FXCollections.observableSet();
    private final ObservableSet<DataNode> workingNodes = FXCollections.observableSet();


    private final ObjectProperty<DataNode<TaxaBlock>> topTaxaNode = new SimpleObjectProperty<>();
    private final ObjectProperty<DataNode<TraitsBlock>> topTraitsNode = new SimpleObjectProperty<>();
    private final ObjectProperty<Connector<TaxaBlock, TaxaBlock>> taxaFilter = new SimpleObjectProperty<>();

    private final ObjectProperty<DataNode<TaxaBlock>> workingTaxaNode = new SimpleObjectProperty<>();

    private final BooleanProperty hasWorkingTaxonNodeForFXThread = new SimpleBooleanProperty(false);

    private final ObjectProperty<DataNode<TraitsBlock>> workingTraitsNode = new SimpleObjectProperty<>();

    private final BooleanProperty hasWorkingTraitsNodeForFXThread = new SimpleBooleanProperty(false);

    private final ObjectProperty<DataNode> topDataNode = new SimpleObjectProperty<>();
    private ObjectProperty<ATopFilter<? extends DataBlock>> topFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<DataNode> workingDataNode = new SimpleObjectProperty<>();

    private final BooleanProperty updating = new SimpleBooleanProperty();
    private final ObservableSet<WorkflowNode> invalidNodes = FXCollections.observableSet();

    private final ASelectionModel<WorkflowNode> nodeSelectionModel = new ASelectionModel<>();

    private final LongProperty topologyChanged = new SimpleLongProperty(0);

    private final BitSet pathIds = new BitSet();

    private final Document document;

    /**
     * constructor
     */
    public Workflow(final Document document) {
        this.document = document;

        invalidNodes.addListener((InvalidationListener) observable -> {
            updating.set(invalidNodes.size() > 0);
            // System.err.println("Workflow updating: " + updating.get());
        });

        connectorNodes.addListener((InvalidationListener) observable -> {
            size.set(connectorNodes.size() + dataNodes.size());
        });

        connectorNodes.addListener((SetChangeListener<Connector>) change -> {
            if (change.wasAdded() || change.wasRemoved()) {
                updateSelectionModel();
            }
        });
        dataNodes.addListener((SetChangeListener<DataNode>) change -> {
            size.set(connectorNodes.size() + dataNodes.size());

            if (change.wasAdded() || change.wasRemoved()) {
                final Set<WorkflowNode> selected = new HashSet<>(nodeSelectionModel.getSelectedItems());
                nodeSelectionModel.selectItems(selected);
                updateSelectionModel();
            }
        });

        // updatingProperty().addListener((observable, oldValue, newValue) -> System.err.println("UPDATING: " + newValue));

        topologyChanged.addListener(observable -> {
            WorkflowNode root = topDataNode.get();
            if (root != null) {
                setPathIdsRec(root, pathIds.nextClearBit(1), pathIds);
            }
        });

        topTaxaNode.addListener((c, o, n) -> {
            if (o != null)
                topNodes.remove(o);
            if (n != null)
                topNodes.add(n);
        });
        topTraitsNode.addListener((c, o, n) -> {
            if (o != null)
                topNodes.remove(o);
            if (n != null)
                topNodes.add(n);
        });

        topDataNode.addListener((c, o, n) -> {
            if (o != null)
                topNodes.remove(o);
            if (n != null)
                topNodes.add(n);
        });

        workingTaxaNode.addListener((c, o, n) -> Platform.runLater(() -> {
            if (o != null)
                workingNodes.remove(o);
            if (n != null)
                workingNodes.add(n);
            hasWorkingTaxonNodeForFXThread.set(n != null);
        }));
        workingTraitsNode.addListener((c, o, n) -> {
            if (o != null)
                workingNodes.remove(o);
            if (n != null)
                workingNodes.add(n);
            Platform.runLater(() -> hasWorkingTraitsNodeForFXThread.set(n != null));
        });
        workingDataNode.addListener((c, o, n) -> {
            if (o != null)
                workingNodes.remove(o);
            if (n != null)
                workingNodes.add(n);
        });

    }

    /**
     * recursively update the pathIds
     *
     * @param v
     * @param currentId
     * @param pathIds
     */
    private void setPathIdsRec(WorkflowNode v, int currentId, BitSet pathIds) {
        if (v.getPathId() == 0) {
            v.setPathId(currentId);
            pathIds.set(currentId);
        } else
            currentId = v.getPathId();

        boolean first = true;
        for (Object obj : v.getChildren()) {
            final WorkflowNode w = (WorkflowNode) obj;
            if (first) {
                setPathIdsRec(w, currentId, pathIds);
                first = false;
            } else
                setPathIdsRec(w, pathIds.nextClearBit(currentId + 1), pathIds);
        }
    }


    /**
     * setup the top nodes, taxa filter, top filter and working nodes
     *
     * @param topTaxaBlock
     * @param topDataBlock
     */
    public void setupTopAndWorkingNodes(TaxaBlock topTaxaBlock, DataBlock topDataBlock) {
        topTaxaBlock.setName("Input" + topTaxaBlock.getName());
        setTopTaxaNode(createDataNode(topTaxaBlock));
        setWorkingTaxaNode(createDataNode((TaxaBlock) topTaxaBlock.newInstance()));

        taxaFilter.set(new Connector<>(getTopTaxaNode().getDataBlock(), getTopTaxaNode(), getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter()));
        register(taxaFilter.get());
        topDataBlock.setName("Input" + topDataBlock.getName());
        setTopDataNode(createDataNode(topDataBlock));
        setWorkingDataNode(createDataNode(topDataBlock.newInstance()));

        if (topDataBlock instanceof CharactersBlock) {
            topFilter.set(new CharactersTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode()));
        } else if (topDataBlock instanceof DistancesBlock) {
            topFilter.set(new DistancesTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode()));
        } else if (topDataBlock instanceof SplitsBlock) {
            topFilter.set(new SplitsTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode()));
        } else if (topDataBlock instanceof TreesBlock) {
            topFilter.set(new TreesTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode()));
        } else
            throw new RuntimeException("No top filter for block of type: " + Basic.getShortName(topDataBlock.getClass()));
        register(topFilter.get());
    }

    /**
     * creates a new data node
     *
     * @param dataBlock
     * @param <D>
     * @return data node
     */
    public <D extends DataBlock> DataNode<D> createDataNode(D dataBlock) {
        dataBlock.setDocument(document);
        return addDataNode(new DataNode<>(dataBlock));
    }

    /**
     * Adds a dataNode created outside of the Workflow
     *
     * @param dataNode
     */
    public <D extends DataBlock> DataNode<D> addDataNode(DataNode<D> dataNode) {
        if (!dataNodes.contains(dataNode))
            register(dataNode);
        incrementTopologyChanged();
        return dataNode;
    }

    public void createTopTraitsAndWorkingTraitsNodes(TraitsBlock topTraitsBlock) {
        topTraitsBlock.setName("Input" + topTraitsBlock.getName());
        topTraitsNode.set(createDataNode(topTraitsBlock));
        topTaxaNode.get().getDataBlock().setTraitsBlock(topTraitsBlock);
        final TraitsBlock workingTraitsBlock = (TraitsBlock) topTraitsBlock.newInstance();
        workingTraitsNode.set(createDataNode(workingTraitsBlock));
        workingTaxaNode.get().getDataBlock().setTraitsBlock(workingTraitsBlock);
        incrementTopologyChanged();
    }

    /**
     * creates a new connector node
     *
     * @param parent
     * @param child
     * @param algorithm
     * @param <P>
     * @param <C>
     * @return connector node
     */
    public <P extends DataBlock, C extends DataBlock> Connector<P, C> createConnector(DataNode<P> parent, DataNode<C> child, Algorithm<P, C> algorithm) {
        final Connector<P, C> connector = addConnector(new Connector<>(getWorkingTaxaNode().getDataBlock(), parent, child, algorithm));
        if (algorithm != null)
            algorithm.setConnector(connector);
        return connector;
    }

    /**
     * creates a reporter node
     *
     * @param parent
     * @param <P>
     * @return connector node
     */
    public <P extends DataBlock> Connector createReporter(DataNode<P> parent) {
        return addConnector(new ReportConnector<P>(getWorkingTaxaNode().getDataBlock(), parent));
    }

    /**
     * Adds a connector created outside of the Workflow
     *
     * @param connector
     */
    public <P extends DataBlock, C extends DataBlock> Connector<P, C> addConnector(Connector<P, C> connector) {
        if (!connectorNodes.contains(connector))
            register(connector);
        if (connector.getAlgorithm() != null)
            connector.getAlgorithm().setConnector(connector);
        incrementTopologyChanged();
        return connector;
    }

    public DataNode<TaxaBlock> getTopTaxaNode() {
        return topTaxaNode.get();
    }

    public void setTopTaxaNode(DataNode<TaxaBlock> topTaxaNode) {
        this.topTaxaNode.set(topTaxaNode);
    }

    public DataNode<TaxaBlock> getWorkingTaxaNode() {
        return workingTaxaNode.get();
    }

    public TaxaBlock getWorkingTaxaBlock() {
        return getWorkingTaxaNode().getDataBlock();
    }


    public void setWorkingTaxaNode(DataNode<TaxaBlock> workingTaxaNode) {
        this.workingTaxaNode.set(workingTaxaNode);
    }

    public DataNode<TraitsBlock> getTopTraitsNode() {
        return topTraitsNode.get();
    }

    public DataNode<TraitsBlock> getWorkingTraitsNode() {
        return workingTraitsNode.get();
    }

    public DataNode getTopDataNode() {
        return topDataNode.get();
    }


    public void setTopDataNode(DataNode topDataNode) {
        this.topDataNode.set(topDataNode);
    }

    public DataNode getWorkingDataNode() {
        return workingDataNode.get();
    }


    public void setWorkingDataNode(DataNode workingDataNode) {
        this.workingDataNode.set(workingDataNode);
    }


    public Connector<TaxaBlock, TaxaBlock> getTaxaFilter() {
        return taxaFilter.get();
    }

    public ATopFilter<? extends DataBlock> getTopFilter() {
        return topFilter.get();
    }


    /**
     * is the graph currently being updated?
     *
     * @return updating property
     */
    public ReadOnlyBooleanProperty updatingProperty() {
        return ReadOnlyBooleanProperty.readOnlyBooleanProperty(updating);
    }

    public ObservableSet<DataNode> dataNodes() {
        return dataNodes;
    }

    public ObservableSet<Connector> connectors() {
        return connectorNodes;
    }


    public BooleanProperty hasWorkingTaxonNodeForFXThreadProperty() {
        return hasWorkingTaxonNodeForFXThread;
    }

    public BooleanProperty hasWorkingTraitsNodeForFXThreadProperty() {
        return hasWorkingTraitsNodeForFXThread;
    }

    /**
     * delete the given node, or only all below, or both node and all below. Note that setting both delete and all below to false doesn't make sense
     *
     * @param node
     * @param deleteNode       delete the node
     * @param deleteNodesBelow delete all its dependents
     */
    public void delete(WorkflowNode node, boolean deleteNode, boolean deleteNodesBelow) {
        if (deleteNodesBelow) {
            final ArrayList<WorkflowNode> children = new ArrayList<>(node.getChildren());
            for (WorkflowNode child : children) {
                delete(child, true, true);
            }
        }
        if (deleteNode) {
            node.disconnect();

            if (node instanceof DataNode)
                dataNodes.remove(node);
            else if (node instanceof Connector) {
                connectorNodes.remove(node);
                ((Connector) node).getService().cancel();
            }
            if (invalidNodes.contains(node))
                invalidNodes.remove(node);
        }
        topologyChanged.set(topologyChanged.get() + 1);
    }

    public void delete(Collection<WorkflowNode> nodes) {
        for (WorkflowNode node : nodes) {
            delete(node, true, false);
        }
    }

    /**
     * delete the unique path to this node, this node and all its descendants
     *
     * @param node
     */
    public void deleteNodeAndPathAndDescendants(WorkflowNode node) {
        // find highest node above that only has one child:
        while (node.getParent() != null && node.getParent() != getWorkingDataNode() && node.getParent().getChildren().size() == 1) {
            node = node.getParent();
        }
        // delete it and everything below:
        delete(node, true, true);

    }

    /**
     * duplicates the given set of nodes
     *
     * @param nodes
     * @return new nodes
     */
    public Collection<WorkflowNode> duplicate(Collection<WorkflowNode> nodes) {
        final DataNode root = getTopDataNode(); // only allow duplication below top data node
        final Set<WorkflowNode> newNodes = new HashSet<>();
        if (root != null) {
            for (Object v : root.getChildren()) {
                duplicateRec(root, root, nodes, newNodes);
            }
        }
        return newNodes;
    }

    /**
     * recursively does the work
     *
     * @param parent
     * @param parentCopy
     * @param nodesToDuplicate
     * @param newNodes
     */
    private void duplicateRec(DataNode parent, DataNode parentCopy, Collection<WorkflowNode> nodesToDuplicate, Set<WorkflowNode> newNodes) {
        ArrayList<Connector> children = new ArrayList<>(parent.getChildren());
        for (Connector connector : children) {
            if (!newNodes.contains(connector)) {
                if (nodesToDuplicate.contains(connector)) {
                    final DataNode childCopy = createDataNode(connector.getChild().getDataBlock().newInstance());
                    newNodes.add(childCopy);
                    final Connector connectorCopy = createConnector(parentCopy, childCopy, connector.getAlgorithm().newInstance());
                    newNodes.add(connectorCopy);
                    duplicateRec(connector.getChild(), childCopy, nodesToDuplicate, newNodes);
                } else {
                    duplicateRec(connector.getChild(), connector.getChild(), nodesToDuplicate, newNodes);
                }
            }
        }
    }

    /**
     * recompute the first nodes encountered in nodes
     *
     * @param nodes
     */
    public void recomputeTop(Collection<WorkflowNode> nodes) {
        final DataNode root = getTopDataNode(); // only allow duplication below top data node
        if (root != null) {
            final Stack<WorkflowNode> stack = new Stack<>();
            stack.push(root);
            while (stack.size() > 0) {
                WorkflowNode node = stack.pop();
                if (node instanceof Connector && nodes.contains(node)) {
                    ((Connector) node).forceRecompute();
                } else {
                    stack.addAll(node.getChildren());
                }
            }

        }


    }

    /**
     * clear the whole Workflow
     */
    public void clear() {
        final ArrayList<WorkflowNode> toRemove = new ArrayList<>(dataNodes);
        toRemove.addAll(connectorNodes);
        for (WorkflowNode node : toRemove)
            delete(node, true, false);
        invalidNodes.clear();
        setTopTaxaNode(null);
        setTopDataNode(null);
        setWorkingTaxaNode(null);
        setWorkingDataNode(null);
    }

    /**
     * registers a newly created node
     *
     * @param node
     */
    private void register(final WorkflowNode node) {
        if (node instanceof DataNode) {
            dataNodes.add((DataNode) node);
            ((DataNode) node).getDataBlock().setDocument(document);
        } else {
            connectorNodes.add((Connector) node);
        }
        node.stateProperty().addListener((ObservableValue<? extends UpdateState> c, UpdateState o, UpdateState n) -> {
            if (n != UpdateState.VALID && n != UpdateState.FAILED) {
                if (!invalidNodes.contains(node))
                    invalidNodes.add(node);
            } else {
                if (invalidNodes.contains(node))
                    invalidNodes.remove(node);
            }
        });
    }

    public int getNumberOfDataNodes() {
        return dataNodes.size();
    }

    public int getNumberOfConnectorNodes() {
        return connectorNodes.size();
    }

    public long getNumberOfNodes() {
        return dataNodes.size() + connectorNodes.size();
    }


    public ReadOnlyIntegerProperty size() {
        return ReadOnlyIntegerProperty.readOnlyIntegerProperty(size);
    }

    public ASelectionModel<WorkflowNode> getNodeSelectionModel() {
        return nodeSelectionModel;
    }

    public ObservableSet<DataNode> getTopNodes() {
        return topNodes;
    }

    public ObservableSet<DataNode> getWorkingNodes() {
        return workingNodes;
    }

    public void reconnect(WorkflowNode parent, WorkflowNode node, ObservableList<WorkflowNode> children) {
        if (parent != null) {
            if (parent instanceof Connector)
                ((Connector) parent).getChildren().add(node);
            else
                ((DataNode) parent).getChildren().add(node);
        }
        for (WorkflowNode child : children) {
            if (node instanceof Connector)
                ((Connector) node).getChildren().add(child);
            else
                ((DataNode) node).getChildren().add(child);
        }
        if (node instanceof DataNode)
            dataNodes.add((DataNode) node);
        else if (node instanceof Connector)
            connectorNodes.add((Connector) node);
    }

    /**
     * gets node and all its descendants
     *
     * @param parent
     * @return node and all below
     */
    private Set<WorkflowNode> getAllDescendants(DataNode parent) {
        final Set<WorkflowNode> all = new HashSet<>();
        Stack<WorkflowNode> stack = new Stack<>();
        stack.push(parent);
        while (stack.size() > 0) {
            WorkflowNode node = stack.pop();
            all.add(node);
            stack.addAll(node.getChildren());
        }
        return all;
    }


    public void incrementTopologyChanged() {
        Platform.runLater(() -> topologyChanged.set(topologyChanged.get() + 1));
    }

    public LongProperty getTopologyChanged() {
        return topologyChanged;
    }

    public void updateSelectionModel() {
        getNodeSelectionModel().setItems(dataNodes, connectorNodes);
    }

    public void cancelAll() {
        for (Connector connector : connectorNodes) {
            connector.getService().cancel();
        }
    }

    /**
     * finds the lowest ancestor whose datablock is of the given class. If no such ancestor found, but a single node of the given class exists, then returns that
     *
     * @param dataNode
     * @param clazz
     * @return lowest ancestor whose datablock is of the given class, or any datablock of the given class, if only one such exists, or null
     */
    public DataNode getAncestor(DataNode dataNode, Class<? extends DataBlock> clazz) {
        while (dataNode != null) {
            if (dataNode.getDataBlock().getClass().isAssignableFrom(clazz))
                break;
            if (dataNode.getParent() != null)
                dataNode = dataNode.getParent().getParent();
            else
                dataNode = null;
        }
        if (dataNode == null) {
            for (DataNode aNode : dataNodes) {
                if (aNode != getTopDataNode() && aNode != getTopTaxaNode() && aNode != getTopTraitsNode() && aNode.getDataBlock().getClass().isAssignableFrom(clazz)) {
                    if (dataNode == null)
                        dataNode = aNode;
                    else
                        return null; // multiple such nodes, can't decide, return null
                }
            }
        }
        return dataNode;
    }

    /**
     * creates a short chain of nodes leading to a view. If the type of view is already present, returns the node
     *
     * @param currentDataNode
     * @param desiredParentClass
     * @param algorithm1class
     * @param data1class
     * @param algorithm2class can be null
     * @param data2class can be null if algorithm2class is also null
     * @return connector and view node
     * @throws Exception
     */
    public Pair<Connector, DataNode> findOrCreatePath(DataNode currentDataNode, Class<? extends DataBlock> desiredParentClass,
                                                      Class<? extends Algorithm> algorithm1class, Class<? extends DataBlock> data1class,
                                                      @Nullable Class<? extends Algorithm> algorithm2class, @Nullable Class<? extends DataBlock> data2class) {
        try {
            final DataNode parent = getAncestor(currentDataNode, desiredParentClass);
            if (parent != null) {
                Pair<Connector, DataNode> result = matchPath(parent, desiredParentClass, algorithm1class, data1class, algorithm2class, data2class);
                if (result != null)
                    return result;

                final DataNode data1node = createDataNode(data1class.newInstance());
                final Connector connector1 = createConnector(parent, data1node, (Algorithm) algorithm1class.newInstance());
                if (algorithm2class == null) {
                    connector1.forceRecompute();
                    return new Pair<>(connector1, data1node);
                } else {
                    DataNode data2node = createDataNode(data2class.newInstance());
                    createConnector(data1node, data2node, (Algorithm) algorithm2class.newInstance());
                    connector1.forceRecompute();
                    return new Pair<>(connector1, data2node);
                }
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
        return null;
    }

    /**
     * tries to match a path from data node data1 to data3 using exactly the given types of data nodes and algorithms
     *
     * @param dataNode
     * @param data1class
     * @param algorithm1class
     * @param data2class
     * @param algorithm2class
     * @param data3class
     * @return first connector and last data node, if matched, else null
     */
    private Pair<Connector, DataNode> matchPath(DataNode dataNode, Class<? extends DataBlock> data1class, Class<? extends Algorithm> algorithm1class, Class<? extends DataBlock> data2class, Class<? extends Algorithm> algorithm2class, Class<? extends DataBlock> data3class) {
        if (dataNode.getDataBlock().getClass().isAssignableFrom(data1class)) {
            for (Object c1 : dataNode.getChildren()) {
                final Connector connector1 = (Connector) c1;
                final DataNode dataNode2 = connector1.getChild();
                if (dataNode2.getDataBlock().getClass().isAssignableFrom(data2class)) {
                    if (algorithm2class == null) {
                        if (!connector1.getAlgorithm().getClass().isAssignableFrom(algorithm1class)) {
                            try {
                                connector1.setAlgorithm(algorithm1class.newInstance());
                                if (connector1.getAlgorithm().isApplicable(getWorkingTaxaBlock(), dataNode.getDataBlock()))
                                    connector1.forceRecompute();
                            } catch (Exception e) {
                                Basic.caught(e);
                            }
                        }
                        return new Pair<>(connector1, dataNode2);
                    }
                    for (Object c2 : dataNode2.getChildren()) {
                        final Connector connector2 = (Connector) c2;
                        final DataNode dataNode3 = connector2.getChild();
                        if (dataNode3.getDataBlock().getClass().isAssignableFrom(data3class)) {
                            if (!connector1.getAlgorithm().getClass().isAssignableFrom(algorithm1class)) {
                                try {
                                    connector2.setAlgorithm(algorithm2class.newInstance());
                                    if (connector2.getAlgorithm().isApplicable(getWorkingTaxaBlock(), dataNode2.getDataBlock()))
                                        connector2.forceRecompute();
                                } catch (Exception e) {
                                    Basic.caught(e);
                                }
                            }
                            return new Pair<>(connector1, dataNode3);
                        }
                    }
                }
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
    public Connector findOrInsertFilter(DataNode dataNode) {
        if (dataNode != null && dataNode.getDataBlock() != null && dataNode.getParent() != null && dataNode.getParent().getAlgorithm() != null) {
            final DataBlock dataBlock = dataNode.getDataBlock();
            final Connector inConnector = dataNode.getParent();

            if (dataBlock instanceof CharactersBlock) {
                if (inConnector.getAlgorithm() instanceof CharactersFilter)
                    return inConnector;
                else {
                    final DataNode<CharactersBlock> newDataNode = createDataNode(new CharactersBlock());
                    ArrayList<Connector<CharactersBlock, ? extends DataBlock>> connectors = new ArrayList<>(dataNode.getChildren());
                    dataNode.getChildren().clear();
                    for (Connector<CharactersBlock, ? extends DataBlock> connector : connectors) {
                        final DataNode child = connector.getChild();
                        final Algorithm algorithm = connector.getAlgorithm();
                        delete(connector, true, false);
                        createConnector(newDataNode, child, algorithm);
                    }
                    return createConnector(dataNode, newDataNode, new CharactersFilter());
                }
            } else if (dataBlock instanceof SplitsBlock) {
                if (inConnector.getAlgorithm() instanceof SplitsFilter)
                    return inConnector;
                else {
                    final DataNode<SplitsBlock> newDataNode = createDataNode(new SplitsBlock());
                    ArrayList<Connector<SplitsBlock, ? extends DataBlock>> connectors = new ArrayList<>(dataNode.getChildren());
                    dataNode.getChildren().clear();
                    for (Connector<SplitsBlock, ? extends DataBlock> connector : connectors) {
                        final DataNode child = connector.getChild();
                        final Algorithm algorithm = connector.getAlgorithm();
                        delete(connector, true, false);
                        createConnector(newDataNode, child, algorithm);
                    }
                    return createConnector(dataNode, newDataNode, new SplitsFilter());
                }
            } else if (dataBlock instanceof TreesBlock) {
                if (inConnector.getAlgorithm() instanceof TreesFilter)
                    return inConnector;
                else {
                    final DataNode<TreesBlock> newDataNode = createDataNode(new TreesBlock());
                    ArrayList<Connector<TreesBlock, ? extends DataBlock>> connectors = new ArrayList<>(dataNode.getChildren());
                    dataNode.getChildren().clear();
                    for (Connector<TreesBlock, ? extends DataBlock> connector : connectors) {
                        final DataNode child = connector.getChild();
                        final Algorithm algorithm = connector.getAlgorithm();
                        delete(connector, true, false);
                        createConnector(newDataNode, child, algorithm);
                    }
                    return createConnector(dataNode, newDataNode, new TreesFilter());
                }
            }
        }
        return null;
    }

    /**
     * can data of this type be loaded into the current workflow?
     *
     * @param dataBlock
     * @return true if can be loaded
     */
    public boolean canLoadData(DataBlock dataBlock) {
        return getTopDataNode() != null && getTopDataNode().getDataBlock() != null && getTopDataNode().getDataBlock().getClass().isAssignableFrom(dataBlock.getClass())
                && getWorkingDataNode() != null;
    }

    /**
     * load the data into the current workflow
     *
     * @param taxaBlock
     * @param dataBlock
     */
    public void loadData(TaxaBlock taxaBlock, DataBlock dataBlock) {
        getTopTaxaNode().getDataBlock().copy(taxaBlock);
        getTopDataNode().getDataBlock().copy(taxaBlock, dataBlock);
    }
}
