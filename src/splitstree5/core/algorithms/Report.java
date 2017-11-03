/*
 *  Copyright (C) 2017 Daniel H. Huson
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
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.AnalysisResultBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.NexusFileWriter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * report on the contents of a block
 * Created by huson on 1/31/17.
 */
public class Report extends Algorithm<ADataBlock, AnalysisResultBlock> implements IFromAnalysisResults, IFromChararacters, IFromTrees, IFromDistances,
        IFromSplits, IFromTaxa, IToAnalysisResults, IFromNetwork, IToNetwork {
    @Override
    public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, ADataBlock parent, AnalysisResultBlock child) throws Exception {
        try (final StringWriter w = new StringWriter()) {
            w.write("### " + parent.getName() + (parent.getShortDescription() != null ? ", " + parent.getShortDescription() + "\n" : "\n"));
            NexusFileWriter.write(w, taxaBlock, parent);
            child.setShortDescription(w.toString());
        } catch (IOException e) {
            Basic.caught(e);
        }
    }
}
