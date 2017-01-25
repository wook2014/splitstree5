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

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[DIMENSIONS [NTAX=number-of-taxa];]\n" +
            "\t[FORMAT\n" +
            "\t\t[TRIANGLE={LOWER|UPPER|BOTH}]\n" +
            "\t\t[[NO] DIAGONAL]\n" +
            "\t\t[LABELS={LEFT|NO}]\n" +
            "\t;]\n" +
            "\tMATRIX\n" +
            "\t\tdistance data in specified format\n" +
            "\t;\n" +
            "\t[VARMATRIX\n" +
            "\t\tvariance data in same specified format\n" +
            "\t;]\n" +
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
        distancesBlock.clear();

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
            final List<String> tokens = np.getTokensLowerCase("format", ";");

            distancesNexusFormat.setLabels(np.findIgnoreCase(tokens, "labels=left", true, distancesNexusFormat.getLabels()));
            distancesNexusFormat.setLabels(np.findIgnoreCase(tokens, "labels=no", false, distancesNexusFormat.getLabels())); //DJB 14mar03


            distancesNexusFormat.setDiagonal(np.findIgnoreCase(tokens, "diagonal=no", false, distancesNexusFormat.getDiagonal()));
            distancesNexusFormat.setDiagonal(np.findIgnoreCase(tokens, "diagonal=yes", true, distancesNexusFormat.getDiagonal()));

            distancesNexusFormat.setTriangle(np.findIgnoreCase(tokens, "triangle=", "both upper lower", distancesNexusFormat.getTriangle()));

            // backward compatibility:
            distancesNexusFormat.setLabels(np.findIgnoreCase(tokens, "no labels", false, distancesNexusFormat.getLabels()));
            distancesNexusFormat.setLabels(np.findIgnoreCase(tokens, "nolabels", false, distancesNexusFormat.getLabels())); //DJB 14mar03
            distancesNexusFormat.setLabels(np.findIgnoreCase(tokens, "labels", true, distancesNexusFormat.getLabels()));

            distancesNexusFormat.setDiagonal(np.findIgnoreCase(tokens, "no diagonal", false, distancesNexusFormat.getDiagonal()));
            distancesNexusFormat.setDiagonal(np.findIgnoreCase(tokens, "diagonal", true, distancesNexusFormat.getDiagonal()));
            distancesNexusFormat.setDiagonal(np.findIgnoreCase(tokens, "noDiagonal", false, distancesNexusFormat.getDiagonal())); //DJB 14mar03

            // for compatibilty with splitstree3, swallow missing=?
            np.findIgnoreCase(tokens, "missing=", null, '?');

            if (tokens.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + tokens + "' unexpected in FORMAT");
        }

        final boolean both = distancesNexusFormat.getTriangle().equals("both");
        final boolean upper = distancesNexusFormat.getTriangle().equals("upper");
        final boolean lower = distancesNexusFormat.getTriangle().equals("lower");
        final int diag = distancesNexusFormat.getDiagonal() ? 0 : 1;

        final ArrayList<String> taxonNamesFound = new ArrayList<>(distancesBlock.getNtax());

        {
            np.matchIgnoreCase("MATRIX");
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
        }

        if (np.peekMatchIgnoreCase("VARMATRIX")) {
            np.matchIgnoreCase("VARMATRIX");
            for (int t = 1; t <= distancesBlock.getNtax(); t++) {
                String label = np.getLabelRespectCase();
                if (taxaBlock.getNtax() > 0 && !taxaBlock.get(t).getName().equals(label))
                    throw new IOException("line " + np.lineno() + ": expected '" + taxaBlock.get(t).getName() + "', found: '" + label + "'");

                if (distancesNexusFormat.isVariancesIO())
                    distancesBlock.setVariance(t, t, 0);

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

                    if (distancesNexusFormat.isVariancesIO()) {
                        if (both)
                            distancesBlock.setVariance(t, q, z);
                        else
                            distancesBlock.setVariance(t, q, z);
                    }
                }
            }
            np.matchIgnoreCase(";");
        }

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

        final int diag = distancesNexusFormat.getDiagonal() ? 0 : 1;

        // write matrix:
        {
            w.write("MATRIX\n");

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
        }

        // write variances, if present
        if (distancesNexusFormat.isVariancesIO() && distancesBlock.isVariances()) {
            w.write("VARMATRIX\n");

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
                    w.write(" " + (float) (distancesBlock.getVariance(t, q)));
                }
                w.write("\n");
            }
            w.write(";\n");
        }
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
    public static int maxLabelLength(TaxaBlock taxaBlock) {
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
