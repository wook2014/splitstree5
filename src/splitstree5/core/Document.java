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

package splitstree5.core;

import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ANode;

/**
 * a document
 * Created by huson on 12/25/16.
 */
public class Document {
    private final ObservableSet<ANode> invalidNodes = FXCollections.observableSet();
    private final BooleanProperty updating = new SimpleBooleanProperty();

    private ADataNode<TaxaBlock> topTaxaNode; // original input taxa
    private ADataNode topDataNode; // original input data

    private final StringProperty fileName = new SimpleStringProperty();

    /**
     * constructor
     */
    public Document() {
        invalidNodes.addListener(new InvalidationListener() {
            public void invalidated(javafx.beans.Observable observable) {
                updating.set(invalidNodes.size() > 0);
                System.err.println("Document updating: " + updating.get());
            }
        });
    }


    /**
     * gets the top taxa node
     *
     * @return top taxa node
     */
    public ADataNode<TaxaBlock> getTopTaxaNode() {
        return topTaxaNode;
    }

    /**
     * set the top taxa block
     *
     * @param topTaxaNode
     */
    public void setTopTaxaNode(ADataNode<TaxaBlock> topTaxaNode) {
        this.topTaxaNode = topTaxaNode;
    }

    /**
     * gets top data node
     *
     * @return top data node
     */
    public ADataNode getTopDataNode() {
        return topDataNode;
    }

    /**
     * set the top data node
     *
     * @param topDataNode
     */
    public void setTopDataNode(ADataNode topDataNode) {
        this.topDataNode = topDataNode;
    }

    public boolean getUpdating() {
        return updating.get();
    }

    public ReadOnlyBooleanProperty updatingProperty() {
        return ReadOnlyBooleanProperty.readOnlyBooleanProperty(updating);
    }

    public ObservableSet<ANode> invalidNodes() {
        return invalidNodes;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }
}
