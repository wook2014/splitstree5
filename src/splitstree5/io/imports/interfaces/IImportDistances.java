/*
 * IImportDistances.java Copyright (C) 2022 Daniel H. Huson
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
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;

public interface IImportDistances extends IImporter {

    /**
     * parse a file
     *
	 */
    void parse(ProgressListener progressListener, String fileName, TaxaBlock taxaBlock, DistancesBlock dataBlock) throws CanceledException, IOException;

    /**
     * set if the values in the matrix should be considered as similarities
     */
    void setSimilarities(boolean similarities);

    void setSimilaritiesCalculation(String similaritiesCalculation);
}
