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

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.DistancesBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Distance block nexus implementation
 * Created by huson on 12/22/16.
 */
public class DistancesNexusIO extends NexusBlock implements INexusIO {
    private static final String NAME = "DISTANCES";

    public static final String SYNTAX = "BEGIN TAXA;\n" +
            "\tDIMENSIONS NTAX=number-of-taxa;\n" +
            "\t[TAXLABELS taxon_1 taxon_2 ... taxon_ntax;]\n" +
            "\t[TAXINFO info_1 info_2 ... info_ntax;]\n" +
            "END;\n";


    private final DistancesBlock distancesBlock;
    private final DistancesNexusFormat format;


    /**
     * constructor
     *
     * @param distancesBlock
     */
    public DistancesNexusIO(DistancesBlock distancesBlock) {
        this.distancesBlock = distancesBlock;
        format = new DistancesNexusFormat();
    }

    @Override
    public void parse(NexusStreamParser np, TaxaBlock taxaBlock) throws IOException {
        getTaxonNamesFound().clear();

        if (np.peekMatchIgnoreCase("#nexus"))
            np.matchIgnoreCase("#nexus"); // skip header line if it is the first line

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
            List<String> format = np.getTokensLowerCase("format", ";");

            getFormat().setLabels(np.findIgnoreCase(format, "labels=left", true, getFormat().getLabels()));
            getFormat().setLabels(np.findIgnoreCase(format, "labels=no", false, getFormat().getLabels())); //DJB 14mar03

            // backward compatibility:
            getFormat().setLabels(np.findIgnoreCase(format, "no labels", false, getFormat().getLabels()));
            getFormat().setLabels(np.findIgnoreCase(format, "nolabels", false, getFormat().getLabels())); //DJB 14mar03
            getFormat().setLabels(np.findIgnoreCase(format, "labels", true, getFormat().getLabels()));

            getFormat().setDiagonal(np.findIgnoreCase(format, "no diagonal", false, getFormat().getDiagonal()));
            getFormat().setDiagonal(np.findIgnoreCase(format, "diagonal", true, getFormat().getDiagonal()));
            getFormat().setDiagonal(np.findIgnoreCase(format, "nodiagonal", false, getFormat().getDiagonal())); //DJB 14mar03

            getFormat().setTriangle(np.findIgnoreCase(format, "triangle=", "both upper lower", getFormat().getTriangle()));

            // for compatibilty with splitstree3, swallow missing=?
            np.findIgnoreCase(format, "missing=", null, '?');

            if (format.size() != 0)
                throw new IOException("line " + np.lineno() + ": '" + format + "' unexpected in FORMAT");
        }

        np.matchIgnoreCase("MATRIX");

        boolean both = getFormat().getTriangle().equals("both");
        boolean upper = getFormat().getTriangle().equals("upper");
        boolean lower = getFormat().getTriangle().equals("lower");

        int diag = getFormat().getDiagonal() ? 0 : 1;

        for (int t = 1; t <= distancesBlock.getNtax(); t++) {
            String label = np.getLabelRespectCase();
            if (taxaBlock.getNtax() > 0 && !taxaBlock.get(t).getName().equals(label))
                throw new IOException("line " + np.lineno() + ": expected '" + taxaBlock.get(t).getName() + "', found: '" + label + "'");
            getTaxonNamesFound().add(label);

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
            if (!isSymmetric()) {
                symmetrize();
                System.err.println("Warning: Distance matrix not symmetric: averaging between upper and lower parts");
            }
        }
    }

    /**
     * gets the value of a format switch
     *
     * @param name
     * @return value of format switch
     */
    public boolean getFormatSwitchValue(String name) {
        if (name.equalsIgnoreCase("diagonal"))
            return getFormat().getDiagonal();
        else if (name.equalsIgnoreCase("triangle=upper"))
            return getFormat().getTriangle().equalsIgnoreCase("upper");
        else if (name.equalsIgnoreCase("triangle=lower"))
            return getFormat().getTriangle().equalsIgnoreCase("lower");
        else if (name.equalsIgnoreCase("triangle=both"))
            return getFormat().getTriangle().equalsIgnoreCase("both");
        else
            return !name.equalsIgnoreCase("labels") || getFormat().getLabels();
    }

    @Override
    public void write(Writer w, TaxaBlock taxaBlock) throws IOException {
        w.write("\nBEGIN " + NAME + ";\n");
        w.write("DIMENSIONS ntax=" + distancesBlock.getNtax() + ";\n");
        w.write("FORMAT");
        if (getFormat().getLabels())
            w.write(" labels=left");
        else
            w.write(" labels=no");
        if (getFormat().getDiagonal())
            w.write(" diagonal");
        else
            w.write(" no diagonal");

        w.write(" triangle=" + getFormat().getTriangle());

        w.write(";\n");
        w.write("MATRIX\n");

        int diag = getFormat().getDiagonal() ? 0 : 1;

        for (int t = 1; t <= distancesBlock.getNtax(); t++) {
            if (getFormat().getLabels()) {
                w.write("[" + t + "]");
                w.write(" '" + taxaBlock.get(t).getName() + "'");
                pad(w, taxaBlock, t);

            }
            int left;
            int right;

            switch (getFormat().getTriangle()) {
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
    private boolean isSymmetric() {
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
    private void symmetrize() {
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
    private void pad(Writer w, TaxaBlock taxaBlock, int index) {
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
    private int maxLabelLength(TaxaBlock taxaBlock) {
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


    @Override
    public String getSyntax() {
        return SYNTAX;
    }

    public DistancesNexusFormat getFormat() {
        return format;
    }
}
