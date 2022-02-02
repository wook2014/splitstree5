/*
 * ReportConnector.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.core.algorithms;

import splitstree5.core.datablocks.AnalysisBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;

/**
 * report the content of a block
 * Daniel Huson, 12/26/16.
 */
public class ReportConnector<D extends DataBlock> extends Connector<D, AnalysisBlock> {
    /**
     * report the block
     *
	 */
    public ReportConnector(TaxaBlock taxaBlock, DataNode<D> parent) {
        super(taxaBlock, parent, new DataNode<>(new AnalysisBlock()));
        setAlgorithm((Algorithm) new Report());
    }
}
