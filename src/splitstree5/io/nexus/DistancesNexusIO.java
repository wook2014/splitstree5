/*
 *  Copyright (C) 2016 Daniel H. Huson
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

import com.sun.istack.internal.Nullable;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * input and output of a distance block in Nexus format
 * Created by huson on 12/28/16.
 */
public class DistancesNexusIO {
    public static final String NAME = "DISTANCES";

    public static final String SYNTAX = "BEGIN TAXA;\n" +
            "\tDIMENSIONS NTAX=number-of-taxa;\n" +
            "\t[TAXLABELS taxon_1 taxon_2 ... taxon_ntax;]\n" +
            "\t[TAXINFO info_1 info_2 ... info_ntax;]\n" +
            "END;\n";

    /**
     * report the syntax for this block
     *
     * @return syntax string
     */
    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a distances block
     * @param np
     * @param taxaBlock
     * @param distancesBlock
     * @param distancesNexusFormat
     * @return taxon names found in this block
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock, DistancesBlock distancesBlock, @Nullable DistancesNexusFormat distancesNexusFormat) throws IOException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();

        if (distancesNexusFormat == null)
            distancesNexusFormat = new DistancesNexusFormat();

        np.matchBeginBlock(NAME);

        if (taxaBlock.getNtax() == 0) {
            np.matchIgnoreCase("dimensions ntax=");
            distancesBlock.setNtax(np.getInt(1, Integer.MAX_VALUE));
            np.matchIgnoreCase(";");
        } else {
            np.matchIgnoreCase("dimensions ntax=" + taxaBlock.getNtax() + ";");
            distancesBlock.setNtax(taxaBlock.getNtax());
        }

        if (np.peekMatchIgnoreCase("FORMAT")) {
            final List<String> formatTokens = np.getTokensLowerCase("format", ";");

            distancesNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "labels=left", true, distancesNexusFormat.getLabels()));
            distancesNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "labels=no", false, distancesNexusFormat.getLabels())); //DJB 14mar03

            // backward compatibility:
            distancesNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "no labels", false, distancesNexusFormat.getLabels()));
            distancesNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "nolabels", false, distancesNexusFormat.getLabels())); //DJB 14mar03
            distancesNexusFormat.setLabels(np.findIgnoreCase(formatTokens, "labels", true, distancesNexusFormat.getLabels()));

            distancesNexusFormat.setDiagonal(np.findIgnoreCase(formatTokens, "no diagonal", false, distancesNexusFormat.getDiagonal()));
            distancesNexusFormat.setDiagonal(np.findIgnoreCase(formatTokens, "diagonal", true, distancesNexusFormat.getDiagonal()));
            distancesNexusFormat.setDiagonal(np.findIgnoreCase(formatTokens, "nodiagonal", false, distancesNexusFormat.getDiagonal())); //DJB 14mar03

            distancesNexusFormat.setTriangle(np.findIgnoreCase(formatTokens, "triangle=", "both upper lower", distancesNexusFormat.getTriangle()));

            // for compatibilty with splitstree3, swallow missing=?
            np.findIgnoreCase(formatTokens, "missing=", null, '?');

            if (formatTokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + formatTokens + "' unexpected in FORMAT");
        }

        np.matchIgnoreCase("MATRIX");

        boolean both = distancesNexusFormat.getTriangle().equals("both");
        boolean upper = distancesNexusFormat.getTriangle().equals("upper");
        boolean lower = distancesNexusFormat.getTriangle().equals("lower");

        int diag = distancesNexusFormat.getDiagonal() ? 0 : 1;

        for (int t = 1; t <= distancesBlock.getNtax(); t++) {
            String label = np.getLabelRespectCase();
            if (taxaBlock.getNtax() > 0 && !taxaBlock.get(t).getName().equals(label))
                throw new IOException("line " + np.lineno() + ": expected '" + taxaBlock.get(t).getName() + "', found: '" + label + "'");
            taxonNamesFound.add(label);

            distancesBlock.set(t, t, 0);

            int left;
            int right;

            if (lower) {
                left = 1;
                right = t - diag;
            } else if (upper) {
                left = t + diag;
                right = distancesBlock.getNtax();
            } else // both
            {
                left = 1;
                right = distancesBlock.getNtax();
            }

            for (int q = left; q <= right; q++) {
                double z = np.getDouble();

                if (both)
                    distancesBlock.set(t, q, z);
                else
                    distancesBlock.setBoth(t, q, z);

            }
        }
        np.matchIgnoreCase(";");
        np.matchEndBlock();

        if (both) {
            if (!isSymmetric(distancesBlock)) {
                symmetrize(distancesBlock);
                System.err.println("Warning: Distance matrix not symmetric: averaging between upper and lower parts");
            }
        }
        return taxonNamesFound;
    }

    /**
     * write a block in nexus format
     * @param w
     * @param taxaBlock
     * @param distancesBlock
     * @param distancesNexusFormat - if null
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock, DistancesBlock distancesBlock, @Nullable DistancesNexusFormat distancesNexusFormat) throws IOException {
        if (distancesNexusFormat == null)
            distancesNexusFormat = new DistancesNexusFormat();

        w.write("\nBEGIN " + NAME + ";\n");
        w.write("DIMENSIONS ntax=" + distancesBlock.getNtax() + ";\n");
        w.write("FORMAT");
        if (distancesNexusFormat.getLabels())
            w.write(" labels=left");
        else
            w.write(" labels=no");
        if (distancesNexusFormat.getDiagonal())
            w.write(" diagonal");
        else
            w.write(" no diagonal");

        w.write(" triangle=" + distancesNexusFormat.getTriangle());

        w.write(";\n");
        w.write("MATRIX\n");

        int diag = distancesNexusFormat.getDiagonal() ? 0 : 1;

        for (int t = 1; t <= distancesBlock.getNtax(); t++) {
            if (distancesNexusFormat.getLabels()) {
                w.write("[" + t + "]");
                w.write(" '" + taxaBlock.get(t).getName() + "'");
                pad(w, taxaBlock, t);

            }
            int left;
            int right;

            switch (distancesNexusFormat.getTriangle()) {
                case "lower":
                    left = 1;//1;

                    right = t - diag;//t-1+diag;

                    break;
                case "upper":
                    left = t + diag;//t-1+diag;

                    right = distancesBlock.getNtax();
                    for (int i = 1; i < t; i++)
                        w.write("      ");
                    break;
                default: // both
                    left = 1;
                    right = distancesBlock.getNtax();
                    break;
            }

            for (int q = left; q <= right; q++) {
                w.write(" " + (float) (distancesBlock.get(t, q)));
            }
            w.write("\n");
        }
        w.write(";\n");
        w.write("END; [" + NAME + "]\n");
    }


    /**
     * Check if the matrix is symmetric.
     *
     * @return boolean. True if it is symmetric.
     */
    private static boolean isSymmetric(DistancesBlock distancesBlock) {
        int ntax = distancesBlock.getNtax();
        for (int i = 1; i <= ntax; i++) {
            for (int j = 1; j < i; j++)
                if (distancesBlock.get(i, j) != distancesBlock.get(j, i))
                    return false;
        }
        return true;
    }

    /**
     * Symmetrize the matrix. Replace d_ij and d_ji with (d_ij+d_ji)/2
     */
    private static void symmetrize(DistancesBlock distancesBlock) {
        int ntax = distancesBlock.getNtax();
        for (int i = 1; i <= ntax; i++) {
            for (int j = 1; j < i; j++) {
                double d_ij = (distancesBlock.get(i, j) + distancesBlock.get(j, i)) / 2.0;
                distancesBlock.set(i, j, d_ij);
                distancesBlock.set(j, i, d_ij);
            }
        }
    }

    /**
     * pad with white space
     *
     * @param w         the writer
     * @param taxaBlock the Taxa
     * @param index     the index of label
     */
    private static void pad(Writer w, TaxaBlock taxaBlock, int index) {
        try {
            int len = taxaBlock.getLabel(index).length();
            int max = maxLabelLength(taxaBlock);

            for (int i = 1; i <= (max - len + 2); i++) {
                w.write(" ");
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Get the max length of all the labels.
     *
     * @param taxaBlock
     * @return longer the max length.
     */
    private static int maxLabelLength(TaxaBlock taxaBlock) {
        int len;
        int longer = 0;

        for (int i = 1; i <= taxaBlock.getNtax(); i++) {
            len = taxaBlock.getLabel(i).length();
            if (longer < len) {
                longer = len;
            }
        }
        return longer;
    }

}
