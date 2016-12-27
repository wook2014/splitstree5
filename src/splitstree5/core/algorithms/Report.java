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
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.*;
import splitstree5.io.nexus.DistancesNexusIO;
import splitstree5.io.nexus.TaxaNexusIO;

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
     * @param document
     * @param taxaBlock
     * @param parent
     */
    public Report(Document document, TaxaBlock taxaBlock, ADataNode<D> parent) {
        super(document, taxaBlock, parent, new ADataNode<AnalysisResultBlock>(document, new AnalysisResultBlock()));

        setAlgorithm(new Algorithm<D, AnalysisResultBlock>() {
            @Override
            public void compute(TaxaBlock taxaBlock, D parent, AnalysisResultBlock child) throws InterruptedException {
                if (parent instanceof DistancesBlock) {
                    try (final StringWriter w = new StringWriter()) {
                        final DistancesNexusIO io = new DistancesNexusIO((DistancesBlock) getParent());
                        io.write(w, taxaBlock);
                        child.setInfo(w.toString());
                        System.err.println(child.getInfo());
                    } catch (IOException e) {
                        Basic.caught(e);
                    }
                } else if (parent instanceof TaxaBlock) {
                    try (final StringWriter w = new StringWriter()) {
                        final TaxaNexusIO io = new TaxaNexusIO((TaxaBlock) getParent());
                        io.write(w, taxaBlock);
                        child.setInfo(w.toString());
                        System.err.println(child.getInfo());
                    } catch (IOException e) {
                        Basic.caught(e);
                    }
                }
            }
        });
    }
}
