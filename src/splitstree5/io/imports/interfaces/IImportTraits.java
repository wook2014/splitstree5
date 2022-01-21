/*
 * IImportTraits.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.imports.interfaces;

import jloda.util.CanceledException;
import jloda.util.progress.ProgressListener;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.TraitsBlock;

import java.io.IOException;

public interface IImportTraits extends IImporter {
    /**
     * import a file
     *
     * @param fileName
     * @param taxaBlock
     * @param dataBlock
     */
    void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, TraitsBlock dataBlock) throws CanceledException, IOException;
}
