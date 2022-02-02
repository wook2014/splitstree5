/*
 * MSFExporter.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.io.exports;

import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.datablocks.characters.CharactersType;
import splitstree5.io.exports.interfaces.IExportCharacters;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MSFExporter implements IExportCharacters {

    private int optionLineLength = 40;

    @Override
    public void export(Writer w, TaxaBlock taxa, CharactersBlock characters) throws IOException {

        int ntax = taxa.getNtax();
        int nchar = characters.getNchar();

        // data type
        if (characters.getDataType().equals(CharactersType.Protein))
            w.write("!!AA_MULTIPLE_ALIGNMENT 1.0" + "\n");
        else if (characters.getDataType().equals(CharactersType.DNA)
                || characters.getDataType().equals(CharactersType.RNA))
            w.write("!!NA_MULTIPLE_ALIGNMENT 1.0" + "\n");
        else
            w.write("!!??_MULTIPLE_ALIGNMENT 1.0" + "\n");

        // info
        w.write("\n" + "Generated by SplitsTree5 ");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        w.write(dateFormat.format(date) + " \n..\n\n");

        // taxa
        int maxTaxaLength = 0;
        for (int i = 1; i <= ntax; i++) {

            w.write("Name: " + taxa.get(i) + "\t" +
                    " Len: " + nchar +
                    " Check: 0000" +
                    " Weight: 1.0" + "\n");

            if (maxTaxaLength < taxa.get(i).toString().length())
                maxTaxaLength = taxa.get(i).toString().length();
        }

        w.write("\n//\n\n");

        int iterations;
        if (nchar % optionLineLength == 0)
            iterations = nchar / optionLineLength;
        else
            iterations = nchar / optionLineLength + 1;

        int stopIndex = optionLineLength;

        // text blocks
        for (int i = 1; i <= iterations; i++) {
            int startIndex = optionLineLength * (i - 1) + 1;

            writeSpaces(w, maxTaxaLength);
            w.write("\t" + startIndex);

            //space between numbers at block beginning
            var last_line_offset = Math.min((nchar - i * optionLineLength), 0);
            writeSpaces(w, optionLineLength - (startIndex + "").length() - (stopIndex + "").length() + last_line_offset);
            w.write(stopIndex + "\n");

            // taxa names
            for (int t = 1; t <= ntax; t++) {
                StringBuilder sequence = new StringBuilder();
                //chars
                for (int j = startIndex; j <= optionLineLength * i && j <= nchar; j++) {
                    sequence.append(characters.get(t, j));
                }
                String taxaString = taxa.get(t).toString();
                w.write(taxaString);
                writeSpaces(w, maxTaxaLength - taxaString.length());
                w.write("\t" + sequence.toString().toUpperCase() + "\n");
            }

            stopIndex += optionLineLength;
            if (stopIndex > nchar)
                stopIndex = nchar;

            w.write("\n");
        }
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList("msf");
    }

    public int getOptionLineLength() {
        return this.optionLineLength;
    }

    public void setOptionLineLength(int lineLength) {
        this.optionLineLength = lineLength;
    }

    private void writeSpaces(Writer w, int n) throws IOException {
        for (int i = 0; i < n; i++) {
            w.write(" ");
        }
    }
}
