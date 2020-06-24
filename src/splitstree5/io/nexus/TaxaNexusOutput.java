/*
 * TaxaNexusOutput.java Copyright (C) 2020. Daniel H. Huson
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
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;

public class TaxaNexusOutput extends NexusIOBase {
    /**
     * writes the taxa block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @throws IOException
     */
    public void write(Writer w, TaxaBlock taxaBlock) throws IOException {
        w.write("\nBEGIN " + TaxaBlock.BLOCK_NAME + ";\n");
        writeTitleAndLink(w);
        w.write("DIMENSIONS ntax=" + taxaBlock.getNtax() + ";\n");
        w.write("TAXLABELS\n");
        for (int i = 1; i <= taxaBlock.getNtax(); i++)
            w.write("\t[" + i + "] '" + taxaBlock.get(i).getName() + "'\n");
        w.write(";\n");
        if (TaxaBlock.hasDisplayLabels(taxaBlock)) {
            w.write("DISPLAYLABELS\n");
            for (int i = 1; i <= taxaBlock.getNtax(); i++)
                w.write("\t[" + i + "] '" + Basic.protectBackSlashes(taxaBlock.get(i).getDisplayLabelOrName()) + "'\n");
            w.write(";\n");
        }
        if (TaxaBlock.hasInfos(taxaBlock)) {
            w.write("TAXINFO\n");
            for (int i = 1; i <= taxaBlock.getNtax(); i++)
                w.write("\t[" + i + "] '" + taxaBlock.get(i).getInfo() + "'\n");
            w.write(";\n");
        }
        w.write("END; [" + TaxaBlock.BLOCK_NAME + "]\n");
    }

}
