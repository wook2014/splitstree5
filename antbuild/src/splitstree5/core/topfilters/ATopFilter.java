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

package splitstree5.core.topfilters;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;

/**
 * top filter
 * Daniel Huson, 12/21/16.
 */
public class ATopFilter<D extends DataBlock> extends Connector<D, D> {
    private final TaxaBlock originalTaxaBlock;
    private final ChangeListener<UpdateState> stateChangeListener;

    public ATopFilter(TaxaBlock originalTaxaBlock, DataNode<TaxaBlock> modifiedTaxaNode, DataNode<D> parent, DataNode<D> child) {
        super(modifiedTaxaNode.getDataBlock(), parent, child);
        this.originalTaxaBlock = originalTaxaBlock;

        stateChangeListener = (c, o, n) -> {
            if (o != UpdateState.VALID && n == UpdateState.VALID) {
                forceRecompute();
            }
        };
        modifiedTaxaNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
    }

    public TaxaBlock getOriginalTaxaBlock() {
        return originalTaxaBlock;
    }
}