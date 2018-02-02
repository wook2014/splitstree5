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

package splitstree5.core.workflow;


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.util.PluginClassLoader;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.util.ArrayList;

/**
 * A connector between data nodes. This is where algorithms are run
 * Daniel Huson, 12/21/16.
 */
public class Connector<P extends DataBlock, C extends DataBlock> extends WorkflowNode {
    private final ConnectorService<P, C> service;

    private final TaxaBlock taxaBlock;
    private final DataNode<P> parent;
    private final DataNode<C> child;
    private final ObservableList<DataNode> children;

    private Algorithm<P, C> algorithm;

    private BooleanProperty applicable = new SimpleBooleanProperty(false); // algorithm is set and applicable?

    private final ChangeListener<UpdateState> parentStateChangeListener = new ChangeListener<UpdateState>() {
        @Override
        public void changed(ObservableValue<? extends UpdateState> observable, UpdateState oldValue, UpdateState newValue) {
            if (Connector.this.getAlgorithm() != null) {
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
    public Connector(TaxaBlock taxaBlock, DataNode<P> parent, DataNode<C> child) {
        this(taxaBlock, parent, child, true);
    }

    /**
     * constructor
     *
     * @param taxaBlock
     * @param parent
     * @param child
     */
    public Connector(TaxaBlock taxaBlock, DataNode<P> parent, DataNode<C> child, boolean connectToGraph) {
        this.taxaBlock = taxaBlock;
        this.parent = parent;
        if (connectToGraph)
            parent.getChildren().add(this);
        this.child = child;
        this.children = FXCollections.observableArrayList(child);
        service = new ConnectorService<>(this);
        if (connectToGraph)
            parent.stateProperty().addListener(new WeakChangeListener<>(parentStateChangeListener));
        child.setParent(this);
    }

    /**
     * constructor
     *
     * @param taxaBlock
     * @param parent
     * @param child
     * @param algorithm
     */
    public Connector(TaxaBlock taxaBlock, DataNode<P> parent, DataNode<C> child, Algorithm<P, C> algorithm) {
        this(taxaBlock, parent, child);
        setAlgorithm(algorithm);
    }

    public void disconnect() {
        for (DataNode child : getChildren())
            child.setParent(null);
        parent.getChildren().remove(this);
        parent.stateProperty().removeListener(parentStateChangeListener);
    }

    public TaxaBlock getTaxaBlock() {
        return taxaBlock;
    }

    public DataNode<P> getParent() {
        return parent;
    }

    public P getParentDataBlock() {
        return parent.getDataBlock();
    }

    public DataNode<C> getChild() {
        return child;
    }

    public C getChildDataBlock() {
        return child.getDataBlock();
    }

    @Override
    public ObservableList<DataNode> getChildren() {
        return children;
    }

    public Algorithm<P, C> getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm<P, C> algorithm) {
        if (this.algorithm != null) {
            this.algorithm.disabledProperty().unbind();
            shortDescriptionProperty().unbind();
        }
        this.algorithm = algorithm;

        if (algorithm != null)
            shortDescriptionProperty().bind(algorithm.shortDescriptionProperty());

        applicable.set(algorithm != null && algorithm.isApplicable(taxaBlock, parent.getDataBlock(), child.getDataBlock()));
        if (algorithm != null)
            setName(algorithm.getName());
        else
            setName("Connector");
        setPathId(getPathId());
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
                    super.setState(UpdateState.FAILED);
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
    public StringProperty shortDescriptionProperty() {
        return super.shortDescriptionProperty();
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
