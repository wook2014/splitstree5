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

package splitstree5.core.topfilters;

import splitstree5.core.Document;
import splitstree5.core.connectors.AConnectorNode;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.UpdateState;

/**
 * top filter
 * Created by huson on 12/21/16.
 */
public class TopFilter<D extends DataBlock> extends AConnectorNode<D, D> {

    public TopFilter(Document document, ADataNode<TaxaBlock> originalTaxaNode, ADataNode<TaxaBlock> modifiedTaxaNode, ADataNode<D> parent, ADataNode<D> child) {
        super(document, originalTaxaNode.getDataBlock(), parent, child);

        modifiedTaxaNode.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != UpdateState.VALID && newValue == UpdateState.VALID) {
                forceRecompute();
            }
        });
    }
}
