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

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.IBlockReaderWriter;

import java.io.IOException;
import java.util.List;

/**
 * nexus block i/o interface
 * Created by huson on 12/27/16.
 */
abstract public interface INexusIO extends IBlockReaderWriter {
    /**
     * parse a file in nexus format
     *
     * @param np
     * @param taxaBlock
     */
    public void parse(NexusStreamParser np, TaxaBlock taxaBlock) throws IOException;

    /**
     * get the list of taxon names found while parsing the block
     *
     * @return list of taxon names found
     */
    public List<String> getTaxonNamesFound();

}
