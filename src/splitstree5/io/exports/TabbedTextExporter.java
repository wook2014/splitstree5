/*
 * TabbedTextExporter.java Copyright (C) 2020. Daniel H. Huson
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

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IFromDistances;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.algorithms.interfaces.IFromTaxa;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportCharacters;
import splitstree5.io.exports.interfaces.IExportDistances;
import splitstree5.io.exports.interfaces.IExportSplits;
import splitstree5.io.exports.interfaces.IExportTaxa;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class TabbedTextExporter implements
        IFromTaxa, IFromChararacters, IFromDistances, IFromSplits,
        IExportTaxa, IExportCharacters, IExportDistances, IExportSplits {

    public void export(Writer w, TaxaBlock taxa) throws IOException {

        w.write("Taxa\n");
        for (int i = 1; i <= taxa.getNtax(); i++) {
            w.write(i + "\t" + taxa.getLabel(i) + "\n");
        }
        w.write("\n");
        w.flush();
    }

    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        w.write("Characters\n");
        for (int i = 1; i <= taxa.getNtax(); i++)
            w.write(taxa.getLabel(i) + "\t");
        w.write("\n");

        for (int j = 1; j <= characters.getNchar(); j++) {
            w.write(j + "");
            for (int i = 1; i <= taxa.getNtax(); i++) {
                w.write("\t" + characters.get(i, j));
            }
            w.write("\n");
        }

        w.write("\n");
        w.flush();
    }

    public void export(Writer w, TaxaBlock taxa, DistancesBlock distances) throws IOException {

        int ntax = distances.getNtax();

        w.write("Distance matrix\n");
        for (int i = 1; i <= ntax; i++) {
            for (int j = 1; j <= ntax; j++)
                //w.write(dec.format(distances.get(i, j)) + "\t");
                w.write((float) distances.get(i, j) + "\t");
            w.write("\n");
        }
        w.write("\n");

        //Export the distances as a matrix then as a column vector.
        w.write("Distance matrix as column vector. (1,2),(1,3),..,(1,n),(2,3),...\n");
        for (int i = 1; i <= ntax; i++) {
            for (int j = i + 1; j <= ntax; j++)
                //w.write(dec.format(distances.get(i, j)) + "\n");
                w.write((float) distances.get(i, j) + "\n");
        }
        w.write("\n");
        w.flush();
    }

    public void export(Writer w, TaxaBlock taxa, SplitsBlock splits) throws IOException {

        w.write("Splits\n");
        w.write("\tWeights");

        for (int i = 1; i <= taxa.getNtax(); i++)
            w.write("\t" + taxa.getLabel(i));
        w.write("\n");

        //Now we loop through the splits, one split per row.
        int nsplits = splits.getNsplits();
        int ntax = taxa.getNtax();
        for (int s = 1; s <= nsplits; s++) {

            //Split number
            w.write(Integer.toString(s));
            w.write("\t" + splits.get(s).getWeight());
            BitSet A = splits.get(s).getA();
            for (int j = 1; j <= ntax; j++) {
                char ch = A.get(j) ? '1' : '0';
                w.write("\t" + ch);
            }

            w.write("\n");
        }
        w.write("\n");
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("tab", "txt");
    }
}
