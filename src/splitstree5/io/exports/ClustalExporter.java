/*
 * ClustalExporter.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.exports;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportCharacters;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class ClustalExporter implements IExportCharacters {

    private int optionLineLength = 40;

    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters)
            throws IOException {

        final String description = "CLUSTAL multiple sequence alignment (Produced by SplitsTree 5)";
        w.write(description + "\n" + "\n" + "\n");
        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();

        int iterations;
        if (nchar % optionLineLength == 0)
            iterations = nchar / optionLineLength;
        else
            iterations = nchar / optionLineLength + 1;

        for (int i = 1; i <= iterations; i++) {
            int startIndex = optionLineLength * (i - 1) + 1;
            for (int t = 1; t <= ntax; t++) {
                StringBuilder sequence = new StringBuilder();
                int stopIndex = optionLineLength;
                for (int j = startIndex; j <= optionLineLength * i && j <= nchar; j++) {
                    sequence.append(characters.get(t, j));
                    stopIndex = j;
                }
                w.write(taxa.get(t) + " \t" + sequence.toString().toUpperCase() + " \t" + stopIndex + "\n");
            }
            w.write("\n");
        }
    }

    public int getOptionLineLength() {
        return this.optionLineLength;
    }

    public void setOptionLineLength(int lineLength) {
        this.optionLineLength = lineLength;
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("aln", "clustal");
    }
}