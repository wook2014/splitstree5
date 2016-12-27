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

package splitstree5.io;

import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * interface for reader and writer
 * Created by huson on 12/22/16.
 */
public interface IBlockReaderWriter {
    /**
     * read
     *
     * @param r
     * @throws IOException
     */
    void read(Reader r, TaxaBlock taxaBlock) throws IOException;

    /**
     * write
     *
     * @param w
     * @throws IOException
     */
    void write(Writer w, TaxaBlock taxaBlock) throws IOException;
}