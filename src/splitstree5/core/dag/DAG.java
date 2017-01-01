/*
 *  Copyright (C) 2016 Daniel H. Huson
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
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import jloda.util.Basic;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.*;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.topfilters.ATopFilter;
import splitstree5.core.topfilters.DistancesTopFilter;
import splitstree5.core.topfilters.SplitsTopFilter;
import splitstree5.core.topfilters.TreesTopFilter;

import java.util.ArrayList;

/**
 * The document analysis graph
 * <p/>
 * Created by huson on 12/29/16.
 */
public class DAG {
    private final IntegerProperty size = new SimpleIntegerProperty();

    private final ObservableSet<AConnector> connectorNodes = FXCollections.observableSet();
    private final ObservableSet<ADataNode> dataNodes = FXCollections.observableSet();

    private ADataNode<TaxaBlock> topTaxaNode;
    private TaxaFilter taxaFilter;
    private ADataNode<TaxaBlock> workingTaxaNode;
    private ADataNode topDataNode;
    private ATopFilter<? extends ADataBlock> topFilter;
    private ADataNode workingDataNode;

    private final BooleanProperty updating = new SimpleBooleanProperty();
    private final ObservableSet<ANode> invalidNodes = FXCollections.observableSet();

    /**
     * constructor
     */
    public DAG() {
        invalidNodes.addListener(new InvalidationListener() {
            public void invalidated(javafx.beans.Observable observable) {
                updating.set(invalidNodes.size() > 0);
                System.err.println("DAG updating: " + updating.get());
            }
        });
        dataNodes.addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                size.set(connectorNodes.size() + dataNodes.size());
            }
        });
        connectorNodes.addListener(new InvalidationListener() {
            public void invalidated(Observable observable) {
                size.set(connectorNodes.size() + dataNodes.size());
            }
        });

        updatingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                System.err.println("UPDATING: " + newValue);
            }
        });
    }

    /**
     * setup the top nodes, taxa filter, top filter and working nodes
     *
     * @param topTaxaBlock
     * @param topDataBlock
     */
    public void setupTopAndWorkingNodes(TaxaBlock topTaxaBlock, ADataBlock topDataBlock) {
        topTaxaNode = createDataNode(topTaxaBlock);
        workingTaxaNode = createDataNode((TaxaBlock) topTaxaBlock.newInstance());
        taxaFilter = new TaxaFilter(topTaxaNode, workingTaxaNode);
        register(taxaFilter);
        topDataNode = createDataNode(topDataBlock);
        workingDataNode = createDataNode(topDataBlock.newInstance());

        if (topDataBlock instanceof DistancesBlock) {
            topFilter = new DistancesTopFilter(topTaxaNode, workingTaxaNode, topDataNode, workingDataNode);
        } else if (topDataBlock instanceof SplitsBlock) {
            topFilter = new SplitsTopFilter(topTaxaNode, workingTaxaNode, topDataNode, workingDataNode);
        } else if (topDataBlock instanceof TreesBlock) {
            topFilter = new TreesTopFilter(topTaxaNode, workingTaxaNode, topDataNode, workingDataNode);
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
    public <P extends ADataBlock, C extends ADataBlock> AConnector createConnector(ADataNode<P> parent, ADataNode<C> child, Algorithm<P, C> algorithm) {
        return addConnector(new AConnector<>(workingTaxaNode.getDataBlock(), parent, child, algorithm));
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
        return topTaxaNode;
    }

    public TaxaFilter getTaxaFilter() {
        return taxaFilter;
    }

    public ADataNode<TaxaBlock> getWorkingTaxaNode() {
        return workingTaxaNode;
    }

    public ADataNode getTopDataNode() {
        return topDataNode;
    }

    public ATopFilter<? extends ADataBlock> getTopFilter() {
        return topFilter;
    }

    public ADataNode getWorkingDataNode() {
        return workingDataNode;
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
            if (node instanceof ADataNode)
                dataNodes.remove(node);
            else
                connectorNodes.remove((AConnector) node);
            node.disconnect();

            if (invalidNodes.contains(node))
                invalidNodes.remove(node);
        }
    }

    /**
     * clear the whole DAG
     */
    public void clear() {
        topTaxaNode = null;
        topDataNode = null;
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
        node.stateProperty().addListener(new WeakChangeListener<>((observable, oldValue, newValue) -> {
            if (newValue != UpdateState.VALID && newValue != UpdateState.FAILED) {
                if (!invalidNodes.contains(node))
                    invalidNodes.add(node);
            } else {
                if (invalidNodes.contains(node))
                    invalidNodes.remove(node);
            }
        }));
    }

    public int getNumberOfDataNodes() {
        return dataNodes.size();
    }

    public ReadOnlyIntegerProperty size() {
        return ReadOnlyIntegerProperty.readOnlyIntegerProperty(size);
    }
}