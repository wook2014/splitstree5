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

package splitstree5.core.connectors;


import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ANode;
import splitstree5.core.misc.UpdateState;

/**
 * an algorithm node
 * Created by huson on 12/21/16.
 */
public class AConnectorNode<P extends ADataBlock, C extends ADataBlock> extends ANode {
    private final ConnectorService<P, C> service;

    private final TaxaBlock taxaBlock;
    private final ADataNode<P> parent;
    private final ADataNode<C> child;

    private Algorithm<P, C> algorithm;

    private final BooleanProperty disable = new SimpleBooleanProperty(true);

    public AConnectorNode(Document document, TaxaBlock taxaBlock, ADataNode<P> parent, ADataNode<C> child) {
        super(document);
        this.taxaBlock = taxaBlock;
        this.parent = parent;
        parent.getChildren().add(this);
        this.child = child;
        disable.bind(stateProperty().isEqualTo(UpdateState.VALID).not());
        service = new ConnectorService<>(this);
    }

    public void disconnect() {
        parent.getChildren().remove(this);
    }

    public TaxaBlock getTaxaBlock() {
        return taxaBlock;
    }

    public ADataNode<P> getParent() {
        return parent;
    }

    public ADataNode<C> getChild() {
        return child;
    }

    public Algorithm<P, C> getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm<P, C> algorithm) {
        if (this.algorithm != null)
            this.algorithm.disabledProperty().unbind();
        this.algorithm = algorithm;
        if (algorithm != null) {
            algorithm.setTaxa(getTaxaBlock());
            algorithm.setParent(getParent().getDataBlock());
            algorithm.setChild(getChild().getDataBlock());
            algorithm.disabledProperty().bind(disable);
        }
    }

    public boolean getDisable() {
        return disable.get();
    }

    public BooleanProperty disableProperty() {
        return disable;
    }

    @Override
    public void setState(UpdateState state) {
        final UpdateState oldState = getState();

        switch (state) {
            case INVALID:
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

    /**
     * force a recompute
     */
    public void forceRecompute() {
        setState(UpdateState.INVALID);
        setState(UpdateState.VALID);
    }
}
