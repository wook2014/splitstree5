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

import jloda.util.Basic;
import splitstree5.core.datablocks.CharactersBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import static splitstree5.io.nexus.CharactersNexusInput.NAME;

/**
 * characters nexus output
 * Daniel Huson, 2.2018
 */
public class CharactersNexusOutput implements INexusOutput<CharactersBlock, CharactersNexusFormat> {
    /**
     * write a block in nexus format
     *
     * @param w
     * @param taxa
     * @param characters
     * @param format     - if null
     * @throws IOException
     */
    @Override
    public void write(Writer w, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {
        if (format == null)
            format = new CharactersNexusFormat();

        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, characters);
        w.write("DIMENSIONS ntax=" + characters.getNtax() + " nchar=" + characters.getNchar() + ";\n");
        w.write("FORMAT\n");
        w.write("\tdatatype='" + characters.getDataType().toString() + "'");

        if (characters.isRespectCase())
            w.write(" respectcase");

        if (characters.getMissingCharacter() != 0)
            w.write(" missing=" + characters.getMissingCharacter());
        if (format.getMatchChar() != 0)
            w.write(" matchChar=" + format.getMatchChar());
        if (characters.getGapCharacter() != 0)
            w.write(" gap=" + characters.getGapCharacter());
        if (characters.isDiploid())
            w.write(" diploid = yes");
        if (!characters.getSymbols().equals("") && !format.isTokens()) {
            w.write(" symbols=\"");
            for (int i = 0; i < characters.getSymbols().length(); i++) {
                //if (i > 0)
                //w.write(" ");
                w.write(characters.getSymbols().charAt(i));
            }
            w.write("\"");
        }

        if (format.isLabels())
            w.write(" labels=left");
        else
            w.write(" labels=no");

        if (format.isTranspose())
            w.write(" transpose=yes");
        else
            w.write(" transpose=no");

        if (format.isTokens())
            w.write(" tokens=yes");

        if (format.isInterleave())
            w.write(" interleave=yes");
        else
            w.write(" interleave=no");

        w.write(";\n");
        if (characters.getCharacterWeights() != null) {
            w.write("CHARWEIGHTS");
            double[] charWeights = characters.getCharacterWeights();
            for (int i = 1; i < charWeights.length; i++)
                w.write(" " + charWeights[i]);
            w.write(";\n");
        }

        // Writes the CharStateLabels only if set
        if (characters.getStateLabeler() != null && characters.getCharLabeler() != null) {
            w.write("CHARSTATELABELS\n");
            boolean first = true;
            for (int i = 1; i <= characters.getNchar(); i++) {
                if (characters.getCharLabeler().containsKey(i)) {
                    if (!first)
                        w.write(",\n");
                    w.write("\t" + i + " ");
                    String label = characters.getCharLabeler().get(i);
                    if (label != null) {
                        w.write("" + Basic.quoteIfNecessary(label) + "");
                        if (first)
                            first = false;
                    }
                    if (characters.getStateLabeler().hasStates(i)) {
                        w.write("/");
                        for (String str : characters.getStateLabeler().getStates(i)) {
                            w.write(" " + Basic.quoteIfNecessary(str));
                        }
                        if (first)
                            first = false;
                    }
                }
            }
            w.write(";\n");
        }

        w.write("MATRIX\n");
        if (!format.isIgnoreMatrix()) {
            if (format.isTranspose() && !format.isInterleave())
                writeMatrixTranposed(w, taxa, characters, format);
            else if (!format.isTranspose() && format.isInterleave())
                writeMatrixInterleaved(w, taxa, characters, format);
            else
                writeMatrix(w, taxa, characters, format);
            w.write(";\n");
        }
        w.write("END; [" + NAME + "]\n");
    }

    /**
     * write the character matrix
     *
     * @param w
     * @param taxa
     * @param characters
     * @param format
     * @throws IOException
     */
    private void writeMatrix(Writer w, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {
        //Determine width of matrix columns (if appropriate) and taxa column (if appropriate)
        int columnWidth = 0;
        if (format.isTokens())
            columnWidth = characters.getStateLabeler().getMaximumLabelLength() + 1;
        int taxaWidth = 0;
        if (format.isLabels()) {
            taxaWidth = maxLabelLength(taxa) + 1;
        }

        for (int t = 1; t <= characters.getNtax(); t++) {
            //Print taxon label
            if (format.isLabels()) {
                w.write(padLabel(taxa.getLabel(t), taxaWidth));
            }

            if (!format.isTokens() || characters.getStateLabeler() == null) { //Write sequence without tokens
                for (int c = 1; c <= characters.getNchar(); c++) {
                    if (format.getMatchChar() == 0 || t == 1 || characters.get(t, c) != characters.get(1, c))
                        w.write(characters.get(t, c)); // get original?
                    else
                        w.write(format.getMatchChar());
                }
            } else {  //Write with tokens
                for (int c = 1; c <= characters.getNchar(); c++) {
                    if (format.getMatchChar() == 0 || c == 1 || characters.get(t, c) != characters.get(1, c))
                        w.write(padLabel(characters.getStateLabeler().char2token(c, characters.get(t, c)), columnWidth));
                    else
                        w.write(padLabel("" + format.getMatchChar(), columnWidth));
                }
            }
            w.write("\n");
        }
    }

    /**
     * write character matrix in transposed format
     *
     * @param w
     * @param taxa
     * @param characters
     * @param format
     * @throws IOException
     */
    private void writeMatrixTranposed(Writer w, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {
        //Get the max width of a column, given taxa and token labels

        //Determine width of matrix columns (if appropriate) and taxa column (if appropriate)
        int columnWidth = 0;
        if (format.isTokens())
            columnWidth = characters.getStateLabeler().getMaximumLabelLength() + 1;
        int taxaWidth = 0;
        if (format.isLabels()) {
            taxaWidth = maxLabelLength(taxa) + 1;
        }
        columnWidth = Math.max(taxaWidth, columnWidth); //Taxa printed above columns

        //Print taxa first
        if (format.isLabels()) {
            for (int t = 1; t <= characters.getNtax(); t++) {
                w.write(padLabel(taxa.getLabel(t), columnWidth));
            }
            w.write("\n");
        }

        if (!format.isTokens()) {  //No tokens
            String padString = padLabel("", columnWidth - 1); //String of (columnWidth-1) spaces.
            for (int c = 1; c <= characters.getNchar(); c++) {
                for (int t = 1; t <= characters.getNtax(); t++) {
                    if (format.getMatchChar() == 0 || t == 1 || characters.get(t, c) != characters.get(1, c))
                        w.write(characters.get(t, c)); // todo: get original
                    else
                        w.write(format.getMatchChar());
                    w.write(padString);
                }
                w.write("\n");
            }
        } else {
            for (int c = 1; c <= characters.getNchar(); c++) {
                for (int t = 1; t <= characters.getNtax(); t++) {
                    if (format.getMatchChar() == 0 || t == 1 || characters.get(t, c) != characters.get(1, c))
                        w.write(padLabel(characters.getStateLabeler().char2token(c, characters.get(t, c)), columnWidth));
                    else
                        w.write(padLabel("" + format.getMatchChar(), columnWidth));
                }
                w.write("\n");
            }
        }
    }

    /**
     * write matrix in interleaved format
     *
     * @param w
     * @param taxa
     * @param characters
     * @param format
     * @throws IOException
     */
    private void writeMatrixInterleaved(Writer w, TaxaBlock taxa, CharactersBlock characters, CharactersNexusFormat format) throws IOException {
        //Determine width of matrix columns (if appropriate) and taxa column (if appropriate)
        int columnWidth = 1;
        if (format.isTokens())
            columnWidth = characters.getStateLabeler().getMaximumLabelLength() + 1;
        int taxaWidth = 0;
        if (format.isLabels()) {
            taxaWidth = maxLabelLength(taxa) + 1;
        }

        int maxColumns;
        if (format.getColumnsPerBlock() == 0)
            // will use 60 columns per block
            maxColumns = Math.max(1, 60 / columnWidth); //Maximum number of sites to print on one line.
        else
            maxColumns = Math.max(1, format.getColumnsPerBlock() / columnWidth); //Maximum number of sites to print on one line.

        for (int c0 = 1; c0 <= characters.getNchar(); c0 += maxColumns) {
            final int cMax = Math.min(c0 + maxColumns - 1, characters.getNchar());
            for (int t = 1; t <= taxa.getNtax(); t++) {
                if (format.isLabels()) {
                    w.write(padLabel(taxa.getLabel(t), taxaWidth));
                }
                if (!format.isTokens()) {
                    for (int c = c0; c <= cMax; c++) {
                        if (format.getMatchChar() == 0 || t == 1 || characters.get(t, c) != characters.get(1, c))
                            w.write(characters.get(t, c));
                        else
                            w.write(format.getMatchChar());
                    }

                } else {
                    for (int c = c0; c <= cMax; c++) {
                        if (format.getMatchChar() == 0 || t == 1 || characters.get(t, c) != characters.get(1, c))
                            w.write(padLabel(characters.getStateLabeler().char2token(c, characters.get(t, c)), columnWidth));
                        else
                            w.write(padLabel("" + format.getMatchChar(), columnWidth));
                    }
                }
                w.write("\n");
            }
            if (c0 < characters.getNchar())
                w.write("\n");
        }
    }

    /**
     * Formats a label. Adds single quotes if addQuotes set to true.
     * Appends spaces until the length of the resulting string is at least length.
     *
     * @param label  String
     * @param length add spaces to achieve this length
     * @return String of given length, or longer if the label + quotes exceed the length.
     */
    public static String padLabel(String label, int length) {
        label = Basic.quoteIfNecessary(label);
        if (label.length() >= length)
            return label;
        char[] padding = new char[length - label.length()];
        Arrays.fill(padding, ' ');
        String paddingString = new String(padding);
        return label + paddingString;
    }

    /**
     * Get the max length of all the labels.
     *
     * @param taxa
     * @return longer the max length.
     */
    public static int maxLabelLength(TaxaBlock taxa) {
        int maxLength = 0;
        for (int i = 1; i <= taxa.getNtax(); i++) {
            maxLength = Math.max(maxLength, Basic.quoteIfNecessary(taxa.getLabel(i)).length());
        }
        return maxLength;
    }
}