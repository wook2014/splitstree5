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

package splitstree5.core.connectors;


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.util.PluginClassLoader;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.UpdateState;

import java.util.ArrayList;

/**
 * A connector between data nodes. This is where algorithms are run
 * Created by huson on 12/21/16.
 */
public class AConnector<P extends ADataBlock, C extends ADataBlock> extends ANode {
    private final ConnectorService<P, C> service;

    private final TaxaBlock taxaBlock;
    private final ADataNode<P> parent;
    private final ADataNode<C> child;
    private final ObservableList<ADataNode> children;

    private Algorithm<P, C> algorithm;

    private BooleanProperty applicable = new SimpleBooleanProperty(false); // algorithm is set and applicable?

    private final ChangeListener<UpdateState> parentStateChangeListener = new ChangeListener<UpdateState>() {
        @Override
        public void changed(ObservableValue<? extends UpdateState> observable, UpdateState oldValue, UpdateState newValue) {
            if (AConnector.this.getAlgorithm() != null) {
                if (newValue == UpdateState.VALID) {
                    applicable.set(algorithm != null && algorithm.isApplicable(taxaBlock, parent.getDataBlock(), child.getDataBlock()));
                } else {
                    applicable.set(false);
                }
            }
        }
    };

    /**
     * constructor
     *
     * @param taxaBlock
     * @param parent
     * @param child
     */
    public AConnector(TaxaBlock taxaBlock, ADataNode<P> parent, ADataNode<C> child) {
        this.taxaBlock = taxaBlock;
        this.parent = parent;
        parent.getChildren().add(this);
        this.child = child;
        this.children = FXCollections.observableArrayList(child);
        service = new ConnectorService<>(this);

        parent.stateProperty().addListener(new WeakChangeListener<UpdateState>(parentStateChangeListener));
    }

    /**
     * constructor
     *
     * @param taxaBlock
     * @param parent
     * @param child
     * @param algorithm
     */
    public AConnector(TaxaBlock taxaBlock, ADataNode<P> parent, ADataNode<C> child, Algorithm<P, C> algorithm) {
        this(taxaBlock, parent, child);
        setAlgorithm(algorithm);
    }

    public void disconnect() {
        parent.getChildren().remove(this);
        parent.stateProperty().removeListener(parentStateChangeListener);
    }

    public TaxaBlock getTaxaBlock() {
        return taxaBlock;
    }

    public ADataNode<P> getParent() {
        return parent;
    }

    public P getParentDataBlock() {
        return parent.getDataBlock();
    }

    public ADataNode<C> getChild() {
        return child;
    }

    public C getChildDataBlock() {
        return child.getDataBlock();
    }

    @Override
    public ObservableList<ADataNode> getChildren() {
        return children;
    }

    public Algorithm<P, C> getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm<P, C> algorithm) {
        if (this.algorithm != null) {
            this.algorithm.disabledProperty().unbind();
        }
        this.algorithm = algorithm;
        applicable.set(algorithm != null && algorithm.isApplicable(taxaBlock, parent.getDataBlock(), child.getDataBlock()));
        if (algorithm != null)
            setName(algorithm.getName());
        else
            setName("AConnector");
    }

    @Override
    public void setState(UpdateState state) {
        synchronized (this) {
            final UpdateState oldState = getState();

            switch (state) {
                case INVALID:
                    if (!isApplicable()) {
                        super.setState(UpdateState.NOT_APPLICABLE);
                        break;
                    }
                    if (Platform.isFxApplicationThread())
                        service.cancel();
                    else
                        Platform.runLater(service::cancel);

                    child.setState(UpdateState.INVALID);
                    if (getParent().getState() == UpdateState.VALID) {
                        System.err.println(getAlgorithm().getName() + " " + oldState + " -> " + UpdateState.COMPUTING);
                        super.setState(UpdateState.COMPUTING);
                        if (Platform.isFxApplicationThread())
                            service.restart();
                        else
                            Platform.runLater(service::restart);
                    } else
                        super.setState(UpdateState.INVALID);
                    break;
                case VALID:
                    super.setState(UpdateState.VALID);
                    break;
                case COMPUTING:
                    throw new RuntimeException("Should never happen");
                case FAILED:
                    System.err.println(getName() + ": " + state);
                    break;
            }
        }
    }

    /**
     * force a recompute
     */
    public void forceRecompute() {
        setState(UpdateState.INVALID);
    }

    /**
     * is an algorithm set and applicable?
     *
     * @return true if set and applicable
     */
    public boolean isApplicable() {
        return applicable.get();
    }

    public ReadOnlyBooleanProperty applicableProperty() {
        return ReadOnlyBooleanProperty.readOnlyBooleanProperty(applicable);
    }

    @Override
    public String getName() {
        if (algorithm != null && algorithm.getName() != null)
            return algorithm.getName();
        else
            return super.getName();
    }

    @Override
    public String getShortDescription() {
        if (algorithm == null)
            return super.getShortDescription();
        else
            return algorithm.getShortDescription();
    }

    public ConnectorService<P, C> getService() {
        return service;
    }

    /**
     * gets all algorithms that can be associated with this connector
     *
     * @return instances of all algorithms
     */
    public ArrayList<Algorithm<P, C>> getAllAlgorithms() {
        final ArrayList<Algorithm<P, C>> list = new ArrayList<>();
        for (Object object : PluginClassLoader.getInstances(getParent().getDataBlock().getFromInterface(), getChild().getDataBlock().getToInterface(), "splitstree5.core.algorithms")) {
            list.add((Algorithm) object);
        }
        return list;
    }
}
