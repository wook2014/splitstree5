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
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.ReportConnector;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.*;
import splitstree5.core.topfilters.*;
import splitstree5.gui.IHasTab;

import java.util.*;

/**
 * The workflow graph
 * <p/>
 * Daniel Huson, 2016
 */
public class Workflow {
    private final IntegerProperty size = new SimpleIntegerProperty();

    private final ObservableSet<AConnector> connectorNodes = FXCollections.observableSet();
    private final ObservableSet<ADataNode> dataNodes = FXCollections.observableSet();

    private final ObjectProperty<ADataNode<TaxaBlock>> topTaxaNode = new SimpleObjectProperty<>();
    private final ObjectProperty<AConnector<TaxaBlock, TaxaBlock>> taxaFilter = new SimpleObjectProperty<>();

    private final ObjectProperty<ADataNode<TaxaBlock>> workingTaxaNode = new SimpleObjectProperty<>();
    private final ObjectProperty<ADataNode> topDataNode = new SimpleObjectProperty<>();
    private ObjectProperty<ATopFilter<? extends ADataBlock>> topFilter = new SimpleObjectProperty<>();
    private final ObjectProperty<ADataNode> workingDataNode = new SimpleObjectProperty<>();

    private final BooleanProperty updating = new SimpleBooleanProperty();
    private final ObservableSet<ANode> invalidNodes = FXCollections.observableSet();

    private final ASelectionModel<ANode> nodeSelectionModel = new ASelectionModel<>();

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

        connectorNodes.addListener((SetChangeListener<AConnector>) change -> {
            if (change.wasAdded() || change.wasRemoved()) {
                updateSelectionModel();
            }
        });
        dataNodes.addListener((SetChangeListener<ADataNode>) change -> {
            size.set(connectorNodes.size() + dataNodes.size());

            if (change.wasAdded() || change.wasRemoved()) {
                final Set<ANode> selected = new HashSet<>(nodeSelectionModel.getSelectedItems());
                nodeSelectionModel.selectItems(selected);
                updateSelectionModel();
            }
        });

        updatingProperty().addListener((observable, oldValue, newValue) -> System.err.println("UPDATING: " + newValue));

        topologyChanged.addListener(observable -> {
            ANode root = topDataNode.get();
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
    private void setPathIdsRec(ANode v, int currentId, BitSet pathIds) {
        if (v.getPathId() == 0) {
            v.setPathId(currentId);
            pathIds.set(currentId);
        } else
            currentId = v.getPathId();

        boolean first = true;
        for (Object obj : v.getChildren()) {
            final ANode w = (ANode) obj;
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
    public void setupTopAndWorkingNodes(TaxaBlock topTaxaBlock, ADataBlock topDataBlock) {
        setTopTaxaNode(createDataNode(topTaxaBlock));
        setWorkingTaxaNode(createDataNode((TaxaBlock) topTaxaBlock.newInstance()));
        taxaFilter.set(new AConnector<>(getTopTaxaNode().getDataBlock(), getTopTaxaNode(), getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter()));
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
    public <D extends ADataBlock> ADataNode<D> createDataNode(D dataBlock) {
        dataBlock.setDocument(document);
        return addDataNode(new ADataNode<>(dataBlock));
    }

    /**
     * Adds a dataNode created outside of the Workflow
     *
     * @param dataNode
     */
    public <D extends ADataBlock> ADataNode<D> addDataNode(ADataNode<D> dataNode) {
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
    public <P extends ADataBlock, C extends ADataBlock> AConnector<P, C> createConnector(ADataNode<P> parent, ADataNode<C> child, Algorithm<P, C> algorithm) {
        final AConnector<P, C> connector = addConnector(new AConnector<>(getWorkingTaxaNode().getDataBlock(), parent, child, algorithm));
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
    public <P extends ADataBlock> AConnector createReporter(ADataNode<P> parent) {
        return addConnector(new ReportConnector<P>(getWorkingTaxaNode().getDataBlock(), parent));
    }

    /**
     * Adds a connector created outside of the Workflow
     *
     * @param connector
     */
    public <P extends ADataBlock, C extends ADataBlock> AConnector<P, C> addConnector(AConnector<P, C> connector) {
        if (!connectorNodes.contains(connector))
            register(connector);
        if (connector.getAlgorithm() != null)
            connector.getAlgorithm().setConnector(connector);
        incrementTopologyChanged();
        return connector;
    }

    public ADataNode<TaxaBlock> getTopTaxaNode() {
        return topTaxaNode.get();
    }

    public ObjectProperty<ADataNode<TaxaBlock>> topTaxaNodeProperty() {
        return topTaxaNode;
    }

    public void setTopTaxaNode(ADataNode<TaxaBlock> topTaxaNode) {
        this.topTaxaNode.set(topTaxaNode);
    }

    public ADataNode<TaxaBlock> getWorkingTaxaNode() {
        return workingTaxaNode.get();
    }

    public TaxaBlock getWorkingTaxaBlock() {
        return getWorkingTaxaNode().getDataBlock();
    }

    public ObjectProperty<ADataNode<TaxaBlock>> workingTaxaNodeProperty() {
        return workingTaxaNode;
    }

    public void setWorkingTaxaNode(ADataNode<TaxaBlock> workingTaxaNode) {
        this.workingTaxaNode.set(workingTaxaNode);
    }

    public ADataNode getTopDataNode() {
        return topDataNode.get();
    }

    public ObjectProperty<ADataNode> topDataNodeProperty() {
        return topDataNode;
    }

    public void setTopDataNode(ADataNode topDataNode) {
        this.topDataNode.set(topDataNode);
    }

    public ADataNode getWorkingDataNode() {
        return workingDataNode.get();
    }

    public ObjectProperty<ADataNode> workingDataNodeProperty() {
        return workingDataNode;
    }

    public void setWorkingDataNode(ADataNode workingDataNode) {
        this.workingDataNode.set(workingDataNode);
    }

    public ObjectProperty<AConnector<TaxaBlock, TaxaBlock>> taxaFilterProperty() {
        return taxaFilter;
    }

    public AConnector<TaxaBlock, TaxaBlock> getTaxaFilter() {
        return taxaFilter.get();
    }

    public ATopFilter<? extends ADataBlock> getTopFilter() {
        return topFilter.get();
    }

    public ObjectProperty<ATopFilter<? extends ADataBlock>> topFilterProperty() {
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

    /**
     * delete the given node, or only all below, or both node and all below. Note that setting both delete and all below to false doesn't make sense
     *
     * @param node
     * @param deleteNode       delete the node
     * @param deleteNodesBelow delete all its dependents
     */
    public void delete(ANode node, boolean deleteNode, boolean deleteNodesBelow) {
        if (deleteNodesBelow) {
            for (Object obj : node.getChildren()) {
                delete((AConnector) obj, true, true);
            }
        }
        if (deleteNode) {
            node.disconnect();

            if (node instanceof ADataNode)
                dataNodes.remove(node);
            else if (node instanceof AConnector)
                connectorNodes.remove(node);

            if (node instanceof ADataNode && ((ADataNode) node).getDataBlock() instanceof IHasTab) {
                ((ADataNode) node).getDataBlock().getDocument().getMainWindow().remove(((IHasTab) ((ADataNode) node).getDataBlock()).getTab());
            }

            if (invalidNodes.contains(node))
                invalidNodes.remove(node);
        }
        topologyChanged.set(topologyChanged.get() + 1);
    }

    public void delete(Collection<ANode> nodes) {
        for (ANode node : nodes) {
            delete(node, true, false);
        }
    }

    /**
     * duplicates the given set of nodes
     *
     * @param nodes
     * @return new nodes
     */
    public Collection<ANode> duplicate(Collection<ANode> nodes) {
        final ADataNode root = getTopDataNode(); // only allow duplication below top data node
        final Set<ANode> newNodes = new HashSet<>();
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
    private void duplicateRec(ADataNode parent, ADataNode parentCopy, Collection<ANode> nodesToDuplicate, Set<ANode> newNodes) {
        ArrayList<AConnector> children = new ArrayList<>(parent.getChildren());
        for (AConnector connector : children) {
            if (!newNodes.contains(connector)) {
                if (nodesToDuplicate.contains(connector)) {
                    final ADataNode childCopy = createDataNode(connector.getChild().getDataBlock().newInstance());
                    newNodes.add(childCopy);
                    final AConnector connectorCopy = createConnector(parentCopy, childCopy, connector.getAlgorithm().newInstance());
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
    public void recomputeTop(Collection<ANode> nodes) {
        final ADataNode root = getTopDataNode(); // only allow duplication below top data node
        if (root != null) {
            final Stack<ANode> stack = new Stack<>();
            stack.push(root);
            while (stack.size() > 0) {
                ANode node = stack.pop();
                if (node instanceof AConnector && nodes.contains(node)) {
                    ((AConnector) node).forceRecompute();
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
        final ArrayList<ANode> toRemove = new ArrayList<>(dataNodes);
        toRemove.addAll(connectorNodes);
        for (ANode node : toRemove)
            delete(node, true, false);
        invalidNodes.clear();
    }

    /**
     * registers a newly created node
     *
     * @param node
     */
    private void register(final ANode node) {
        if (node instanceof ADataNode) {
            dataNodes.add((ADataNode) node);
            ((ADataNode) node).getDataBlock().setDocument(document);
        } else {
            connectorNodes.add((AConnector) node);
        }
        node.stateProperty().addListener((ObservableValue<? extends UpdateState> observable, UpdateState oldValue, UpdateState newValue) -> {
            if (newValue != UpdateState.VALID && newValue != UpdateState.FAILED) {
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

    public ASelectionModel<ANode> getNodeSelectionModel() {
        return nodeSelectionModel;
    }

    public void reconnect(ANode parent, ANode node, ObservableList<ANode> children) {
        if (parent != null) {
            if (parent instanceof AConnector)
                ((AConnector) parent).getChildren().add(node);
            else
                ((ADataNode) parent).getChildren().add(node);
        }
        for (ANode child : children) {
            if (node instanceof AConnector)
                ((AConnector) node).getChildren().add(child);
            else
                ((ADataNode) node).getChildren().add(child);
        }
        if (node instanceof ADataNode)
            dataNodes.add((ADataNode) node);
        else if (node instanceof AConnector)
            connectorNodes.add((AConnector) node);
    }

    public ANode findParent(ANode node) {
        if (node instanceof ADataNode) {
            for (ANode parent : connectorNodes) {
                if (parent.getChildren().contains(node))
                    return parent;
            }
        } else {
            return ((AConnector) node).getParent();
        }
        return null;
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

}
