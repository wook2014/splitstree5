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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.ANode;

/**
 * a document
 * Created by huson on 12/25/16.
 */
public class Document {
    private final ObservableSet<ANode> invalidNodes = FXCollections.observableSet();
    private final BooleanProperty updating = new SimpleBooleanProperty();

    private TaxaBlock topTaxaBlock; // original input taxa
    private DataBlock topDataBlock; // original input data

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
     * get the top taxa block
     *
     * @return original taxa
     */
    public TaxaBlock getTopTaxaBlock() {
        return topTaxaBlock;
    }

    /**
     * set the top taxa block
     *
     * @param topTaxaBlock
     */
    public void setTopTaxaBlock(TaxaBlock topTaxaBlock) {
        this.topTaxaBlock = topTaxaBlock;
    }

    /**
     * get the top datablock
     *
     * @return top datablock
     */
    public DataBlock getTopDataBlock() {
        return topDataBlock;
    }

    /**
     * set the top data block
     *
     * @param topDataBlock
     */
    public void setTopDataBlock(DataBlock topDataBlock) {
        this.topDataBlock = topDataBlock;
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

}
