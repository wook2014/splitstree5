/*
 * PhylipDistancesExporter.java Copyright (C) 2020. Daniel H. Huson
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

import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportDistances;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * simple output format
 * Daniel Huson, 2.2020
 */
public class SimpleDistancesExporter implements IFromDistances, IExportDistances {

    public void export(Writer w, TaxaBlock taxa, DistancesBlock distances) throws IOException {

        int ntax = taxa.getNtax();

        final String labelFormat;
        {
            int maxLabelLength = taxa.getLabel(1).length();
            for (int i = 2; i <= ntax; i++) {
                if (taxa.getLabel(i).length() > maxLabelLength)
                    maxLabelLength = taxa.getLabel(i).length();
            }
            labelFormat = "%-" + maxLabelLength + "s";
        }


        w.write(ntax + "\n");

        for (int i = 1; i <= ntax; i++) {
            w.write(String.format(labelFormat, taxa.getLabel(i)));
            for (int j = 1; j <= ntax; j++)
                w.write(String.format(" %.5f ", distances.get(i, j)));
            w.write("\n");
        }
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("dist", "dst", "txt");
    }
}
