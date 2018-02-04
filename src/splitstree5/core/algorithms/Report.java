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

package splitstree5.core.algorithms;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.algorithms.interfaces.*;
import splitstree5.core.datablocks.AnalysisBlock;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.NexusOut;

import java.io.IOException;
import java.io.StringWriter;

/**
 * report on the contents of a block
 * Daniel Huson, 1/31/17.
 */
public class Report extends Algorithm<DataBlock, AnalysisBlock> implements IFromAnalysis, IFromChararacters, IFromTrees, IFromDistances, IFromSplits, IFromTaxa, IFromSplitsNetworkView, IFromTreeView, IToAnalysis {
    @Override
    public void compute(ProgressListener progress, TaxaBlock taxaBlock, DataBlock parent, AnalysisBlock child) throws Exception {
        try (final StringWriter w = new StringWriter()) {
            w.write("### " + parent.getName() + (parent.getShortDescription() != null ? ", " + parent.getShortDescription() + "\n" : "\n"));

            if (parent instanceof TaxaBlock)
                new NexusOut().export(w, (TaxaBlock) parent);
            else if (parent instanceof AnalysisBlock)
                new NexusOut().export(w, (AnalysisBlock) parent);
            else
                new NexusOut().export(w, taxaBlock, parent);
            child.setShortDescription(w.toString());
        } catch (IOException e) {
            Basic.caught(e);
        }
    }
}
