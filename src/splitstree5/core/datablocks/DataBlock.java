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

package splitstree5.core.datablocks;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import splitstree5.core.misc.Named;
import splitstree5.io.IBlockReaderWriter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A data block
 * Created by huson on 12/21/16.
 */
public class DataBlock extends Named implements IBlockReaderWriter {
    private ObjectProperty<IBlockReaderWriter> readerWriter = new SimpleObjectProperty<>();

    public IBlockReaderWriter getReaderWriter() {
        return readerWriter.get();
    }

    public ObjectProperty<IBlockReaderWriter> readerWriterProperty() {
        return readerWriter;
    }

    public void setReaderWriter(IBlockReaderWriter readerWriter) {
        this.readerWriter.set(readerWriter);
    }


    /**
     * write
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w, TaxaBlock taxaBlock) throws IOException {
        if (readerWriter.get() != null)
            readerWriter.get().write(w, taxaBlock);
    }

    /**
     * read
     *
     * @param r
     * @throws IOException
     */
    public void read(Reader r, TaxaBlock taxaBlock) throws IOException {
        if (readerWriter.get() != null)
            readerWriter.get().read(r, taxaBlock);
    }

}
