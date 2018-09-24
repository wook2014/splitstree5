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
import jloda.util.Single;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.*;
import splitstree5.core.topfilters.*;
import splitstree5.io.nexus.AlgorithmNexusInput;
import splitstree5.io.nexus.AlgorithmNexusOutput;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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
        setTopTaxaNode(createDataNode(topTaxaBlock));
        setWorkingTaxaNode(createDataNode((TaxaBlock) topTaxaBlock.newInstance()));
        taxaFilter.set(new Connector<>(getTopTaxaNode().getDataBlock(), getTopTaxaNode(), getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter()));
        register(taxaFilter.get());
        setTopDataNode(createDataNode(topDataBlock));
        setWorkingDataNode(createDataNode(topDataBlock.newInstance()));

        createTopFilter(getTopDataNode(), getWorkingDataNode());
    }

    /**
     * creates the top filter
     *
     * @param parent
     * @param child
     */
    public void createTopFilter(DataNode parent, DataNode child) {
        final DataBlock dataBlock = parent.getDataBlock();
        if (dataBlock instanceof CharactersBlock) {
            topFilter.set(new CharactersTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), parent, child));
        } else if (dataBlock instanceof DistancesBlock) {
            topFilter.set(new DistancesTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), parent, child));
        } else if (dataBlock instanceof SplitsBlock) {
            topFilter.set(new SplitsTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), parent, child));
        } else if (dataBlock instanceof TreesBlock) {
            topFilter.set(new TreesTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), parent, child));
        } else
            throw new RuntimeException("No top filter for block of type: " + Basic.getShortName(dataBlock.getClass()));
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
        if (dataBlock == null)
            System.err.println("null");
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
        setTopTraitsNode(createDataNode(topTraitsBlock));
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
        if (topTaxaNode != null)
            topTaxaNode.getDataBlock().setName("InputTaxa");
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

    public void setTopTraitsNode(DataNode<TraitsBlock> topTraitsNode) {
        if (topTraitsNode != null)
            topTraitsNode.getDataBlock().setName("InputTraits");
        this.topTraitsNode.set(topTraitsNode);
    }

    public DataNode<TraitsBlock> getWorkingTraitsNode() {
        return workingTraitsNode.get();
    }

    public DataNode getTopDataNode() {
        return topDataNode.get();
    }

    public void setTopDataNode(DataNode topDataNode) {
        if (topDataNode != null && !topDataNode.getDataBlock().getName().startsWith("Input"))
            topDataNode.getDataBlock().setName("Input" + topDataNode.getName());
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

    public void setTaxaFilter(Connector<TaxaBlock, TaxaBlock> taxaFilter) {
        this.taxaFilter.set(taxaFilter);
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

    public void delete(Collection<WorkflowNode> nodes) {
        for (WorkflowNode node : nodes) {
            delete(node, true, false);
        }
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

            if (node instanceof DataNode) {
                if (((DataNode) node).getDataBlock() instanceof ViewerBlock)
                    ((ViewerBlock) ((DataNode) node).getDataBlock()).getTab().close();

                dataNodes.remove(node);

            } else if (node instanceof Connector) {
                connectorNodes.remove(node);
                ((Connector) node).getService().cancel();
            }
            if (invalidNodes.contains(node))
                invalidNodes.remove(node);
        }
        topologyChanged.set(topologyChanged.get() + 1);
    }

    /**
     * Reconnect a node that was previously deleted
     *
     * @param parent
     * @param node
     * @param children
     */
    public void reconnect(WorkflowNode parent, WorkflowNode node, ObservableList<WorkflowNode> children) {
        if (parent != null) {
            final ObservableList<WorkflowNode> theChildren;
            if (parent instanceof Connector)
                theChildren = ((Connector) parent).getChildren();
            else
                theChildren = ((DataNode) parent).getChildren();
            if (!theChildren.contains(node))
                theChildren.add(node);
        }
        for (WorkflowNode child : children) {
            final ObservableList<WorkflowNode> theChildren;
            if (node instanceof Connector)
                theChildren = ((Connector) node).getChildren();
            else
                theChildren = ((DataNode) node).getChildren();
            if (!theChildren.contains(child))
                theChildren.add(child);
        }
        if (node instanceof DataNode)
            dataNodes.add((DataNode) node);
        else if (node instanceof Connector)
            connectorNodes.add((Connector) node);
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
                    Algorithm algorithm;
                    try { // try to copy because then we maintain all settings
                        final Writer w = new StringWriter();
                        (new AlgorithmNexusOutput()).write(w, connector.getAlgorithm());
                        NexusStreamParser np = new NexusStreamParser(new StringReader(w.toString()));
                        algorithm = (new AlgorithmNexusInput()).parse(np);
                    } catch (IOException ex) { // copy failed, just make a new instance
                        algorithm = connector.getAlgorithm().newInstance();
                    }
                    final Connector connectorCopy = createConnector(parentCopy, childCopy, algorithm);
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
        final DataNode root = getTopDataNode();
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
    public <T extends DataBlock> DataNode<T> getAncestorForClass(DataNode dataNode, Class<T> clazz) {
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
     * finds the lowest ancestor whose datablock is of the given class. If no such ancestor found, but a single node of the given class exists, then returns that
     *
     * @param dataNode
     * @param clazz
     * @return lowest ancestor whose datablock is of the given class, or any datablock of the given class, if only one such exists, or null
     */
    public <T extends DataBlock> Pair<DataNode<T>, Connector<T, ? extends DataBlock>> getAncestorAndDescendantForClass(DataNode dataNode, Class<T> clazz) {
        Connector<T, ? extends DataBlock> connector = null;

        while (dataNode != null) {
            if (dataNode.getDataBlock().getClass().isAssignableFrom(clazz))
                break;
            if (dataNode.getParent() != null) {
                connector = dataNode.getParent();
                dataNode = dataNode.getParent().getParent();
            } else {
                connector = null;
                dataNode = null;
            }
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
        return new Pair<DataNode<T>, Connector<T, ? extends DataBlock>>(dataNode, connector);
    }

    /**
     * tries to match a path from data node using exactly the given types of data nodes and algorithms
     *
     * @param dataNode
     * @param path
     * @return first connector and last data node, if matched, else null
     */
    public Pair<Connector, DataNode> matchPath(DataNode dataNode, Pair<Class<? extends Algorithm>, Class<? extends DataBlock>>... path) {
        final Pair<Connector, DataNode> pair = new Pair<>();
        final Single<Connector> mustForceRecompute = new Single<>(null);
        if (matchPathRecursively(dataNode, path, 0, pair, mustForceRecompute)) {
            if (mustForceRecompute.get() != null)
                mustForceRecompute.get().forceRecompute();
            return pair;
        } else
            return null;
    }

    /**
     * recursively does the work
     *
     * @param dataNode
     * @param path
     * @param pos
     * @param result
     * @param mustForceRecompute
     */
    private boolean matchPathRecursively(DataNode dataNode, Pair<Class<? extends Algorithm>, Class<? extends DataBlock>>[] path, int pos, Pair<Connector, DataNode> result, Single<Connector> mustForceRecompute) {
        final Pair<Class<? extends Algorithm>, Class<? extends DataBlock>> pair = path[pos];

        for (Object obj : dataNode.getChildren()) {
            final Connector connector = (Connector) obj;
            final DataNode target = connector.getChild();

            if (target.getDataBlock().getClass().isAssignableFrom(pair.getSecond())) {
                if (!connector.getAlgorithm().getClass().isAssignableFrom(pair.getFirst())) {
                    try {
                        connector.setAlgorithm(pair.getFirst().newInstance());
                        if (connector.getAlgorithm().isApplicable(getWorkingTaxaBlock(), dataNode.getDataBlock()) && mustForceRecompute.get() == null)
                            mustForceRecompute.set(connector);
                    } catch (Exception e) {
                        Basic.caught(e);
                    }
                }
                if (pos == path.length - 1) { // reached the target node
                    result.setSecond(target);
                    return true;
                }
                if (matchPathRecursively(target, path, pos + 1, result, mustForceRecompute)) {
                    if (pos == 0)
                        result.setFirst(connector);
                    return true;
                }
            }
        }
        return false; // didn't match, return false
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

    /**
     * select all nodes  below
     *
     * @param nodes
     * @param strictlyBelow don't select the given nodes, only ones below
     * @return
     */
    public boolean selectAllBelow(Collection<WorkflowNode> nodes, boolean strictlyBelow) {
        final Set<WorkflowNode> nodesToSelect = new HashSet<>();

        if (!strictlyBelow)
            nodesToSelect.addAll(nodes);

        final Stack<WorkflowNode> stack = new Stack<>();
        stack.addAll(nodes);
        while (stack.size() > 0) {
            final WorkflowNode v = stack.pop();
            for (WorkflowNode w : v.getChildren()) {
                stack.push(w);
                nodesToSelect.add(w);
            }
        }
        nodesToSelect.removeAll(getNodeSelectionModel().getSelectedItems());
        getNodeSelectionModel().selectItems(nodesToSelect);
        return nodesToSelect.size() > 0;
    }

    public Document getDocument() {
        return document;
    }
}
