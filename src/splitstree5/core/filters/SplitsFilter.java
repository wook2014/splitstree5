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

package splitstree5.core.filters;

import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;


/**
 * splits filter
 * Created by huson on 12/12/16.
 */
public class SplitsFilter extends AConnector<SplitsBlock, SplitsBlock> {
    private final SplitsFilterAlgorithm splitsFilterAlgorithm;
    /**
     * constructor
     *
     * @param parent
     * @param child
     */
    public SplitsFilter(TaxaBlock taxaBlock, ADataNode<SplitsBlock> parent, ADataNode<SplitsBlock> child) {
        super(taxaBlock, parent, child);
        splitsFilterAlgorithm = new SplitsFilterAlgorithm(parent.getDataBlock());
        setAlgorithm(splitsFilterAlgorithm);
    }

    @Override
    public String getShortDescription() {
        if (splitsFilterAlgorithm.getDisabledSplits().size() == 0)
            return "Enabled: " + splitsFilterAlgorithm.getEnabledSplits().size();
        else
            return "Enabled: " + splitsFilterAlgorithm.getEnabledSplits().size() + " (of " + (splitsFilterAlgorithm.getEnabledSplits().size() + splitsFilterAlgorithm.getDisabledSplits().size() + ")");
    }
}