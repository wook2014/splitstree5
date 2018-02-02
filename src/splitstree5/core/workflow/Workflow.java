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

    private final ObjectProperty<DataNode<TaxaBlock>> topTaxaNode = new SimpleObjectProperty<>();
    private final ObjectProperty<Connector<TaxaBlock, TaxaBlock>> taxaFilter = new SimpleObjectProperty<>();

    private final ObjectProperty<DataNode<TaxaBlock>> workingTaxaNode = new SimpleObjectProperty<>();
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
            System.err.println("Workflow updating: " + updating.get());
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

        updatingProperty().addListener((observable, oldValue, newValue) -> System.err.println("UPDATING: " + newValue));

        topologyChanged.addListener(observable -> {
            WorkflowNode root = topDataNode.get();
            if (root != null) {
                setPathIdsRec(root, pathIds.nextClearBit(1), pathIds);
            }
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
        getTopDataNode().getDataBlock().setName("Orig" + getTopDataNode().getDataBlock().getName());
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

    public ObjectProperty<DataNode<TaxaBlock>> topTaxaNodeProperty() {
        return topTaxaNode;
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

    public ObjectProperty<DataNode<TaxaBlock>> workingTaxaNodeProperty() {
        return workingTaxaNode;
    }

    public void setWorkingTaxaNode(DataNode<TaxaBlock> workingTaxaNode) {
        this.workingTaxaNode.set(workingTaxaNode);
    }

    public DataNode getTopDataNode() {
        return topDataNode.get();
    }

    public ObjectProperty<DataNode> topDataNodeProperty() {
        return topDataNode;
    }

    public void setTopDataNode(DataNode topDataNode) {
        this.topDataNode.set(topDataNode);
    }

    public DataNode getWorkingDataNode() {
        return workingDataNode.get();
    }

    public ObjectProperty<DataNode> workingDataNodeProperty() {
        return workingDataNode;
    }

    public void setWorkingDataNode(DataNode workingDataNode) {
        this.workingDataNode.set(workingDataNode);
    }

    public ObjectProperty<Connector<TaxaBlock, TaxaBlock>> taxaFilterProperty() {
        return taxaFilter;
    }

    public Connector<TaxaBlock, TaxaBlock> getTaxaFilter() {
        return taxaFilter.get();
    }

    public ATopFilter<? extends DataBlock> getTopFilter() {
        return topFilter.get();
    }

    public ObjectProperty<ATopFilter<? extends DataBlock>> topFilterProperty() {
        return topFilter;
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

    /**
     * delete the given node, or only all below, or both node and all below. Note that setting both delete and all below to false doesn't make sense
     *
     * @param node
     * @param deleteNode       delete the node
     * @param deleteNodesBelow delete all its dependents
     */
    public void delete(WorkflowNode node, boolean deleteNode, boolean deleteNodesBelow) {
        if (deleteNodesBelow) {
            for (Object obj : node.getChildren()) {
                delete((Connector) obj, true, true);
            }
        }
        if (deleteNode) {
            node.disconnect();

            if (node instanceof DataNode)
                dataNodes.remove(node);
            else if (node instanceof Connector)
                connectorNodes.remove(node);
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
        setTopTaxaNode(null);
        setTopDataNode(null);
        final ArrayList<WorkflowNode> toRemove = new ArrayList<>(dataNodes);
        toRemove.addAll(connectorNodes);
        for (WorkflowNode node : toRemove)
            delete(node, true, false);
        invalidNodes.clear();
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
    private Set<WorkflowNode> getAllDecendants(DataNode parent) {
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
        topologyChanged.set(topologyChanged.get() + 1);
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
     * finds the lowest ancestor whose datablock is of the given class
     *
     * @param dataNode
     * @param clazz
     * @return lowest ancestor whose datablock is of the given class or null
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
        return dataNode;
    }

    /**
     * creates a short chain of nodes leading to a view. If the type of view is already present, returns the node
     *
     * @param parent
     * @param algorithmClass
     * @param childClass
     * @param viewAlgorithmClass
     * @param viewerClass
     * @return view node
     * @throws Exception
     */
    public Pair<Connector, DataNode> createView(DataNode parent, Class<? extends Algorithm> algorithmClass, Class<? extends DataBlock> childClass,
                                                Class<? extends Algorithm> viewAlgorithmClass, Class<? extends ViewDataBlock> viewerClass) throws Exception {
        Set<WorkflowNode> allBelow = getAllDecendants(parent);
        for (WorkflowNode node : allBelow) {
            if (pathMatches(parent, node, algorithmClass, childClass, viewAlgorithmClass, viewerClass))
                return new Pair<>((Connector) node.getParent().getParent().getParent(), (DataNode) node);
        }


        DataNode child = createDataNode(childClass.newInstance());
        Connector connector1 = createConnector(parent, child, (Algorithm) algorithmClass.newInstance());
        DataNode viewerNode = createDataNode(viewerClass.newInstance());
        createConnector(child, viewerNode, (Algorithm) viewAlgorithmClass.newInstance());
        connector1.forceRecompute();
        return new Pair<>(connector1, viewerNode);
    }

    /**
     * attempts to match the path in the existing graph and if can, it sets the algorithm
     *
     * @param source
     * @param target
     * @param algorithmClass
     * @param childClass
     * @param viewAlgorithmClass
     * @param viewerClass
     * @return true, if matched
     * @throws Exception
     */
    private boolean pathMatches(DataNode source, WorkflowNode target, Class<? extends Algorithm> algorithmClass, Class<? extends DataBlock> childClass,
                                Class<? extends Algorithm> viewAlgorithmClass, Class<? extends ViewDataBlock> viewerClass) throws Exception {
        if (target instanceof DataNode && ((DataNode) target).getDataBlock().getClass().isAssignableFrom(viewerClass)) { // is correct view
            target = target.getParent(); // view algorithm
            if (target != null && ((Connector) target).getAlgorithm().getClass().isAssignableFrom(viewAlgorithmClass)) { // is correct view algorithm
                target = target.getParent(); // data
                if (target != null && ((DataNode) target).getDataBlock().getClass().isAssignableFrom(childClass)) // is correct input data
                    target = target.getParent();
                if (target != null) {
                    Connector connector = (Connector) target;
                    if (connector.getParent() != null && connector.getParent().getClass().isAssignableFrom(source.getClass())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
