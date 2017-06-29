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

package splitstree5.core.datablocks;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.ANode;
import splitstree5.core.dag.UpdateState;

import java.util.ArrayList;

/**
 * a data node
 * Created by huson on 12/21/16.
 */
public class ADataNode<D extends ADataBlock> extends ANode {
    private final D dataBlock;
    private final ObservableList<AConnector> children;

    /**
     * constructor
     *
     * @param dataBlock
     */
    public ADataNode(D dataBlock) {
        this.dataBlock = dataBlock;
        this.children = FXCollections.observableArrayList();
    }

    public D getDataBlock() {
        return dataBlock;
    }

    public void setState(UpdateState state) {
        System.err.println(getDataBlock().getName() + " " + getState() + " -> " + state);
        super.setState(state);
        for (ANode child : getChildren()) {
            child.setState(UpdateState.INVALID);
        }
    }

    public ObservableList<AConnector> getChildren() {
        return children;
    }

    public void disconnect() {
        final ArrayList<ANode> children = new ArrayList<>(getChildren());
        for (ANode connector : children) {
            if (getChildren().contains(connector))
                connector.disconnect();
        }
    }

    @Override
    public String getName() {
        return dataBlock.getName();
    }

    @Override
    public void setName(String name) {
        if (dataBlock != null)
            dataBlock.setName(name);
    }

    public StringProperty nameProperty() {
        return dataBlock.nameProperty();
    }

    public void clear() {
    }
}
