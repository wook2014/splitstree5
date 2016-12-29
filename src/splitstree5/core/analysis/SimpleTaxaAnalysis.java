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

package splitstree5.core.analysis;

import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.AnalysisResultBlock;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * Taxa analysis
 * Created by huson on 12/22/16.
 */
public class SimpleTaxaAnalysis extends AConnector<TaxaBlock, AnalysisResultBlock> {
    /**
     * constructor
     *
     * @param parent
     * @param child
     */
    public SimpleTaxaAnalysis(ADataNode<TaxaBlock> parent, ADataNode<AnalysisResultBlock> child) {
        super(null, parent, child);

        setAlgorithm(new Algorithm<TaxaBlock, AnalysisResultBlock>() {
            @Override
            public void compute(TaxaBlock ignored, TaxaBlock parent, AnalysisResultBlock child) throws InterruptedException {
                child.setInfo("Number of taxa: " + parent.getTaxa().size());
            }
        });
    }
}
