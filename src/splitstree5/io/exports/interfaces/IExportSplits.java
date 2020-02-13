/*
 * IExportSplits.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.io.exports.interfaces;

import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;

/**
 * save splits
 * Daniel Huson, 1.2018
 */
public interface IExportSplits extends IExporter, IFromSplits {
    /**
     * save splits
     *
     * @param w
     * @param taxa
     * @param splitsBlock
     */
    void export(Writer w, TaxaBlock taxa, SplitsBlock splitsBlock) throws IOException;
}
