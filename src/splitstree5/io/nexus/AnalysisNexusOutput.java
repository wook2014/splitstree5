/*
 * AnalysisNexusOutput.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.nexus;

import jloda.util.Basic;
import splitstree5.core.datablocks.AnalysisBlock;

import java.io.IOException;
import java.io.Writer;

/**
 * writes an analysi result in nexus format
 * Daniel Huson, 2.2018
 */
public class AnalysisNexusOutput extends NexusIOBase {
    /**
     * writes the analysis block in nexus format
     *
     * @param w
     * @param block
     * @throws IOException
     */
    public void write(Writer w, AnalysisBlock block) throws IOException {
        w.write("\nBEGIN " + AnalysisBlock.BLOCK_NAME + ";\n");
        writeTitleAndLink(w);
        w.write("DIMENSIONS nlines=" + Basic.countOccurrences(block.getShortDescription(), '\n') + ";\n");
        w.write("RESULT;\n");
        w.write(block.getShortDescription());
        if (!block.getShortDescription().endsWith("\n"))
            w.write("\n");
        w.write("END; [" + AnalysisBlock.BLOCK_NAME + "]\n");
    }
}
