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

package splitstree5.io.nexus;

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.util.List;

/**
 * nexus input interface for all datablocks except TaxaBlock
 * Daniel Huson, 2.2018
 *
 * @param <D>
 */
public interface INexusInput<D extends DataBlock> {
    /**
     * get syntax
     */
    public abstract String getSyntax();

    /**
     * parse a nexus block
     *
     * @param np
     * @param taxaBlock
     * @param dataBlock
     * @return taxon names, if found
     */
    List<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, D dataBlock) throws IOException;

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    boolean atBeginOfBlock(NexusStreamParser np);
}
