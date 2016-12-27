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

import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.*;
import splitstree5.core.misc.UpdateState;

/**
 * setup a top filter
 * Created by huson on 12/27/16.
 */
public class ATopFilterConnector<D extends ADataBlock> extends AConnector<D, D> {
    /**
     * constructor
     *
     * @param document
     * @param originalTaxaNode
     * @param modifiedTaxaNode
     * @param parent
     * @param child
     */
    public ATopFilterConnector(Document document, ADataNode<TaxaBlock> originalTaxaNode, ADataNode<TaxaBlock> modifiedTaxaNode,
                               ADataNode<D> parent, ADataNode<D> child) {
        super(document, originalTaxaNode.getDataBlock(), parent, child);
        modifiedTaxaNode.stateProperty().addListener((c, o, n) -> {
            if (n == UpdateState.VALID)
                setState(UpdateState.INVALID); // trigger recomputation
        });

        Algorithm algorithm = null;
        final ADataBlock parentBlock = parent.getDataBlock();
        if (parentBlock instanceof DistancesBlock) {
            algorithm = new DistancesTopFilter(modifiedTaxaNode.getDataBlock());
        } else if (parentBlock instanceof TreesBlock) {
            algorithm = new TreesTopFilter(modifiedTaxaNode.getDataBlock());
        } else if (parentBlock instanceof SplitsBlock) {
            algorithm = new SplitsTopFilter(modifiedTaxaNode.getDataBlock());
        } else throw new RuntimeException("TopFilter: not defined for: " + Basic.getShortName(parentBlock.getClass()));
        setAlgorithm(algorithm);
    }
}
