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

import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;

import static splitstree5.io.nexus.TaxaNexusInput.NAME;

public class TaxaNexusOutput {
    /**
     * writes the taxa block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @throws IOException
     */
    public void write(Writer w, TaxaBlock taxaBlock) throws IOException {
        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, taxaBlock);
        w.write("\tDIMENSIONS ntax=" + taxaBlock.getNtax() + ";\n");
        w.write("\tTAXLABELS\n");
        for (int i = 1; i <= taxaBlock.getNtax(); i++)
            w.write("\t\t[" + i + "] '" + taxaBlock.get(i).getName() + "'\n");
        w.write("\t;\n");
        if (hasInfos(taxaBlock)) {
            w.write("\tTAXINFO\n");
            for (int i = 1; i <= taxaBlock.getNtax(); i++)
                w.write("\t\t[" + i + "] '" + taxaBlock.get(i).getInfo() + "'\n");
            w.write(";\n");
        }
        w.write("END; [" + NAME + "]\n");
    }

    /**
     * returns true, if any taxon has an info string associated with it
     *
     * @return true, if some taxon has info
     */
    private boolean hasInfos(TaxaBlock taxaBlock) {
        for (int t = 1; t <= taxaBlock.getNtax(); t++)
            if (taxaBlock.get(t).getInfo() != null && taxaBlock.get(t).getInfo().length() > 0)
                return true;
        return false;
    }
}