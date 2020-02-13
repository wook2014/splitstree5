/*
 * DataNode.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.core.workflow;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import splitstree5.core.datablocks.DataBlock;

import java.util.ArrayList;

/**
 * A data node
 * Daniel Huson, 12.2016
 */
public class DataNode<D extends DataBlock> extends WorkflowNode {
    private boolean verbose = false;

    private final D dataBlock;
    private final ObservableList<Connector<D, ? extends DataBlock>> children;
    private Connector<? extends DataBlock, D> parent;

    /**
     * constructor
     *
     * @param dataBlock
     */
    public DataNode(D dataBlock) {
        this.dataBlock = dataBlock;
        this.dataBlock.setDataNode(this);
        this.children = FXCollections.observableArrayList();
        this.shortDescriptionProperty().bind(dataBlock.shortDescriptionProperty());
        nameProperty().bindBidirectional(dataBlock.nameProperty());
        titleProperty().bindBidirectional(dataBlock.titleProperty());
    }

    public D getDataBlock() {
        return dataBlock;
    }

    public void setState(UpdateState state) {
        if (verbose)
            System.err.println(getDataBlock().getName() + " " + getState() + " -> " + state);
        super.setState(state);
        if (this == dataBlock.getDocument().getWorkflow().getWorkingTaxaNode())
            dataBlock.getDocument().getWorkflow().getWorkingDataNode().setState(UpdateState.INVALID);
        for (WorkflowNode child : getChildren()) {
            child.setState(UpdateState.INVALID);
            for (Object connector : getChildren()) {
                WorkflowNode node = ((Connector) connector).getChild();
                if (node != null)
                    node.setState(UpdateState.INVALID);
            }
        }
    }

    public void clear() {
    }

    public ObservableList<Connector<D, ? extends DataBlock>> getChildren() {
        return children;
    }

    public void disconnect() {
        setParent(null);
        for (WorkflowNode connector : new ArrayList<>(getChildren())) {
            connector.disconnect();
        }
    }

    public Connector<? extends DataBlock, D> getParent() {
        return parent;
    }

    public void setParent(Connector<? extends DataBlock, D> parent) {
        this.parent = parent;
    }
}
