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

package splitstree5.core.algorithms;

import jloda.util.Basic;
import jloda.util.ProgressListener;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.AnalysisResultBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.nexus.NexusFileWriter;

import java.io.IOException;
import java.io.StringWriter;

/**
 * report the content of a block
 * Created by huson on 12/26/16.
 */
public class Report<D extends ADataBlock> extends AConnector<D, AnalysisResultBlock> {
    /**
     * report the block
     *
     * @param taxaBlock
     * @param parent
     */
    public Report(TaxaBlock taxaBlock, ADataNode<D> parent) {
        super(taxaBlock, parent, new ADataNode<>(new AnalysisResultBlock()));

        setAlgorithm(new Algorithm<D, AnalysisResultBlock>("Report") {
            @Override
            public void compute(ProgressListener progressListener, TaxaBlock taxaBlock, D parent, AnalysisResultBlock child) throws InterruptedException {
                try (final StringWriter w = new StringWriter()) {
                    w.write("### " + parent.getName() + (parent.getShortDescription() != null ? ", " + parent.getShortDescription() + "\n" : "\n"));
                    NexusFileWriter.write(w, taxaBlock, parent);
                    child.setShortDescription(w.toString());
                    System.err.println(child.getShortDescription());
                } catch (IOException e) {
                        Basic.caught(e);
                    }
            }
        });
    }
}
