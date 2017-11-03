/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.core.dag;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import jloda.fx.ASelectionModel;
import jloda.util.Basic;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.ReportNode;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.*;
import splitstree5.core.topfilters.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The document update graph
 * <p/>
 * Daniel Huson, 2016
 */
public class DAG {
    private final IntegerProperty size = new SimpleIntegerProperty();

    private final ObservableSet<AConnector> connectorNodes = FXCollections.observableSet();
    private final ObservableSet<ADataNode> dataNodes = FXCollections.observableSet();

    private final ObjectProperty<ADataNode<TaxaBlock>> topTaxaNode = new SimpleObjectProperty<>();
    private AConnector<TaxaBlock, TaxaBlock> taxaFilter;
    private final ObjectProperty<ADataNode<TaxaBlock>> workingTaxaNode = new SimpleObjectProperty<>();
    private final ObjectProperty<ADataNode> topDataNode = new SimpleObjectProperty<>();
    private ATopFilter<? extends ADataBlock> topFilter;
    private final ObjectProperty<ADataNode> workingDataNode = new SimpleObjectProperty<>();

    private final BooleanProperty updating = new SimpleBooleanProperty();
    private final ObservableSet<ANode> invalidNodes = FXCollections.observableSet();

    private final ASelectionModel<ANode> nodeSelectionModel = new ASelectionModel<>();

    /**
     * constructor
     */
    public DAG() {
        invalidNodes.addListener((InvalidationListener) observable -> {
            updating.set(invalidNodes.size() > 0);
            System.err.println("DAG updating: " + updating.get());
        });

        dataNodes.addListener((InvalidationListener) observable -> {
            size.set(connectorNodes.size() + dataNodes.size());
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
            if (change.wasAdded() || change.wasRemoved()) {
                final Set<ANode> selected = new HashSet<>(nodeSelectionModel.getSelectedItems());
                nodeSelectionModel.selectItems(selected);
                updateSelectionModel();
            }
        });

        updatingProperty().addListener((observable, oldValue, newValue) -> System.err.println("UPDATING: " + newValue));
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
        taxaFilter = new AConnector<TaxaBlock, TaxaBlock>(getTopTaxaNode().getDataBlock(), getTopTaxaNode(), getWorkingTaxaNode(), new splitstree5.core.algorithms.filters.TaxaFilter());
        register(taxaFilter);
        setTopDataNode(createDataNode(topDataBlock));
        getTopDataNode().getDataBlock().setName("Orig" + getTopDataNode().getDataBlock().getName());
        setWorkingDataNode(createDataNode(topDataBlock.newInstance()));

        if (topDataBlock instanceof CharactersBlock) {
            topFilter = new CharactersTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode());
        } else if (topDataBlock instanceof DistancesBlock) {
            topFilter = new DistancesTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode());
        } else if (topDataBlock instanceof SplitsBlock) {
            topFilter = new SplitsTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode());
        } else if (topDataBlock instanceof TreesBlock) {
            topFilter = new TreesTopFilter(getTopTaxaNode(), getWorkingTaxaNode(), getTopDataNode(), getWorkingDataNode());
        } else
            throw new RuntimeException("No top filter for block of type: " + Basic.getShortName(topDataBlock.getClass()));
        register(topFilter);
    }

    /**
     * creates a new data node
     *
     * @param dataBlock
     * @param <D>
     * @return data node
     */
    public <D extends ADataBlock> ADataNode<D> createDataNode(D dataBlock) {
        return addDataNode(new ADataNode<D>(dataBlock));
    }

    /**
     * Adds a dataNode created outside of the DAG
     *
     * @param dataNode
     */
    public <D extends ADataBlock> ADataNode<D> addDataNode(ADataNode<D> dataNode) {
        if (!dataNodes.contains(dataNode))
            register(dataNode);
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
        return addConnector(new AConnector<>(getWorkingTaxaNode().getDataBlock(), parent, child, algorithm));
    }

    /**
     * creates a reporter node
     *
     * @param parent
     * @param <P>
     * @return connector node
     */
    public <P extends ADataBlock> AConnector createReporter(ADataNode<P> parent) {
        return addConnector(new ReportNode<P>(getWorkingTaxaNode().getDataBlock(), parent));
    }

    /**
     * Adds a connector created outside of the DAG
     *
     * @param connector
     */
    public <P extends ADataBlock, C extends ADataBlock> AConnector<P, C> addConnector(AConnector<P, C> connector) {
        if (!connectorNodes.contains(connector))
            register(connector);
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

    public AConnector<TaxaBlock, TaxaBlock> getTaxaFilter() {
        return taxaFilter;
    }

    public ATopFilter<? extends ADataBlock> getTopFilter() {
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

            if (invalidNodes.contains(node))
                invalidNodes.remove(node);
        }
    }

    /**
     * clear the whole DAG
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
        if (node instanceof ADataNode)
            dataNodes.add((ADataNode) node);
        else
            connectorNodes.add((AConnector) node);
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

    public void updateSelectionModel() {
        getNodeSelectionModel().setItems(dataNodes, connectorNodes);
    }
}
