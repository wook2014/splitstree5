/*
 *  FastaExporter.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.io.exports;

import splitstree5.core.algorithms.interfaces.IFromChararacters;
import splitstree5.core.algorithms.interfaces.IFromSplits;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.io.exports.interfaces.IExportCharacters;
import splitstree5.io.exports.interfaces.IExportSplits;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class FastaExporter implements IFromChararacters, IExportCharacters, IFromSplits, IExportSplits {

    @Override
    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        jloda.util.FastA fasta = new jloda.util.FastA();

        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();

        for (int i = 1; i <= ntax; i++) {
            StringBuilder sequence = new StringBuilder("");
            for (int j = 1; j <= nchar; j++) {
                sequence.append(characters.get(i, j));
            }
            fasta.add(taxa.getLabel(i), sequence.toString().toUpperCase());
        }
        fasta.write(w);
    }

    @Override
    public void export(Writer w, TaxaBlock taxa, SplitsBlock splits) throws IOException {

        jloda.util.FastA fasta = new jloda.util.FastA();
        for (int t = 1; t <= taxa.getNtax(); t++) {
            char[] seq = new char[splits.getNsplits()];
            for (int s = 1; s <= splits.getNsplits(); s++) {
                if (splits.get(s).getA().get(t))
                    seq[s - 1] = '1';
                else
                    seq[s - 1] = '0';
            }
            fasta.add(taxa.getLabel(t), String.valueOf(seq));
        }
        fasta.write(w);
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("fasta", "fas", "fa", "seq", "fsa", "fna");
    }
}
