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
import splitstree5.core.datablocks.TraitsBlock;

import java.io.IOException;
import java.io.Writer;

import static splitstree5.io.nexus.DistancesNexusOutput.pad;
import static splitstree5.io.nexus.TraitsNexusInput.NAME;


/**
 * traits nexus output
 * Daniel Huson, 2.2018
 */
public class TraitsNexusOutput implements INexusOutput<TraitsBlock> {
    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @param traitsBlock
     * @throws IOException
     */
    @Override
    public void write(Writer w, TaxaBlock taxaBlock, TraitsBlock traitsBlock) throws IOException {
        final TraitsNexusFormat format = (TraitsNexusFormat) traitsBlock.getFormat();

        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, traitsBlock);
        w.write("  DIMENSIONS [ntax=" + taxaBlock.getNtax() + "] ntraits=" + traitsBlock.getNTraits() + ";\n");
        w.write("  FORMAT");
        if (format.isOptionLabel())
            w.write(" labels=yes");
        else
            w.write(" labels=no");
        w.write(" missing=" + format.getOptionMissingCharacter());
        w.write(" separator=" + format.getOptionSeparator().toString());
        w.write(";\n");

        if (traitsBlock.isSetLatitudeLongitude()) {
            w.write("  TRAITLATITUDE");
            for (int i = 1; i <= traitsBlock.getNTraits(); i++) {
                w.write(" " + traitsBlock.getTraitLatitude(i));
            }
            w.write(";\n");
            w.write("  TRAITLONGITUDE");
            for (int i = 1; i <= traitsBlock.getNTraits(); i++) {
                w.write(" " + traitsBlock.getTraitLongitude(i));
            }
            w.write(";\n");
        }
        {
            w.write("  TRAITLABELS\n");
            for (int i = 1; i <= traitsBlock.getNTraits(); i++) {
                w.write("\t" + traitsBlock.getTraitLabel(i) + "\n");
            }
            w.write(";\n");
        }

        // write matrix:
        {
            w.write("MATRIX\n");

            for (int t = 1; t <= taxaBlock.getNtax(); t++) {
                if (format.isOptionLabel()) {
                    w.write("[" + t + "]");
                    w.write(" '" + taxaBlock.get(t).getName() + "'");
                    pad(w, taxaBlock, t);
                }
                for (int j = 1; j <= traitsBlock.getNTraits(); j++) {
                    if (j > 1)
                        w.write(format.getSeparatorString());
                    int value = traitsBlock.getTraitValue(t, j);
                    if (value == Integer.MAX_VALUE)
                        w.write(format.getOptionMissingCharacter());
                    else
                        w.write("" + value);
                }
                w.write("\n");
            }
            w.write(";\n");
        }
        w.write("END; [" + NAME + "]\n");
    }
}
