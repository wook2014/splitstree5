/*
 * INexusOutput.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.io.nexus;

import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;

/**
 * nexus output interface for all datablocks except TaxaBlock
 * Daniel Huson, 2.2018
 *
 * @param <D>
 * @param <F>
 */
public interface INexusOutput<D extends DataBlock> {
    /**
     * write a datablock
     *
     * @param w
     * @param taxaBlock
     * @param dataBlock
     * @throws IOException
     */
    void write(Writer w, TaxaBlock taxaBlock, D dataBlock) throws IOException;
}
