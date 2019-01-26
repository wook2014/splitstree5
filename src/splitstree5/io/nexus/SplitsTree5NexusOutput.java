/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import splitstree5.core.datablocks.SplitsTree5Block;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

/**
 * writes the splitstree5 block in nexus format
 * Daniel Huson, 3.2018
 */
public class SplitsTree5NexusOutput {
    /**
     * writes the taxa block in nexus format
     *
     * @param w
     * @param splitsTree5Block
     * @throws IOException
     */
    public void write(Writer w, SplitsTree5Block splitsTree5Block) throws IOException {
        w.write("\nBEGIN " + SplitsTree5Block.BLOCK_NAME + ";\n");
        w.write("DIMENSIONS nDataNodes=" + splitsTree5Block.getOptionNumberOfDataNodes()
                + " nAlgorithms=" + splitsTree5Block.getOptionNumberOfAlgorithms() + ";\n");
        w.write("PROGRAM version='" + splitsTree5Block.getOptionVersion() + "';\n");
        w.write(String.format("WORKFLOW creationDate='%s'; [%s]\n", splitsTree5Block.getOptionCreationDate(),
                new Date(splitsTree5Block.getOptionCreationDate())));
        w.write("END; [" + SplitsTree5Block.BLOCK_NAME + "]\n");
    }
}
