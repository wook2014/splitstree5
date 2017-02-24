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
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Taxa block block nexus implementation
 * Created by huson on 12/22/16.
 */
public class TaxaNexusIO {
    private static final String NAME = "TAXA";

    public static final String SYNTAX = "BEGIN DISTANCES;\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "\t[DIMENSIONS [NTAX=number-of-taxaBlock];]\n" +
            "\t[FORMAT\n" +
            "\t    [TRIANGLE={LOWER|UPPER|BOTH}]\n" +
            "\t    [[NO] DIAGONAL]\n" +
            "\t    [LABELS={LEFT|NO}]\n" +
            "\t;]\n" +
            "\tMATRIX\n" +
            "\t    distance data in specified format\n" +
            "\t;\n" +
            "END;\n";


    /**
     * gets syntax
     *
     * @return syntax message
     */
    public static String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a taxa block
     *
     * @param np
     * @param taxaBlock
     * @return list of taxon names found
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock) throws IOException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();

        taxaBlock.getTaxa().clear();

        if (np.peekMatchIgnoreCase("#nexus"))
            np.matchIgnoreCase("#nexus"); // skip header line if it is the first line

        np.matchBeginBlock(NAME);
        UtilitiesNexusIO.readTitleLinks(np, taxaBlock);

        np.matchIgnoreCase("DIMENSIONS ntax=");
        final int ntax = np.getInt();
        np.matchIgnoreCase(";");

        boolean labelsDetected = false;

        if (np.peekMatchIgnoreCase("taxlabels")) // grab labels now
        {
            np.matchIgnoreCase("taxlabels");
            if (np.peekMatchIgnoreCase("_detect_")) // for compatibility with SplitsTree3:
            {
                np.matchIgnoreCase("_detect_");
            } else {
                for (int t = 1; t <= ntax; t++) {
                    final String taxonName = np.getLabelRespectCase();
                    final Taxon taxon = new Taxon(taxonName);

                    if (taxaBlock.indexOf(taxon) != -1) {
                        throw new IOException((np.lineno() > 1 ? "Line " + np.lineno() + ":" : "") + " taxon name '" + taxonName + "' appears multiple times, at " + taxaBlock.indexOf(taxon) + " and " + t);
                    }
                    taxaBlock.add(taxon);
                    taxonNamesFound.add(taxon.getName());
                }
                labelsDetected = true;
            }
            np.matchIgnoreCase(";");
        }

        if (labelsDetected && np.peekMatchIgnoreCase("taxinfo")) // grab labels now
        {
            np.matchIgnoreCase("taxinfo");

            for (int t = 1; t <= ntax; t++) {
                final String info = np.getLabelRespectCase();
                if (!info.equals("null")) // not explicitly the word "null"
                    taxaBlock.get(t).setInfo(info);
            }
            np.matchIgnoreCase(";");
        }
        np.matchEndBlock();
        return taxonNamesFound;
    }


    /**
     * writes the taxa block in nexus format
     *
     * @param w
     * @param taxaBlock
     * @throws IOException
     */
    public static void write(Writer w, TaxaBlock taxaBlock) throws IOException {
        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, taxaBlock);
        w.write("\tDIMENSIONS ntax=" + taxaBlock.getNtax() + ";\n");
        w.write("\tTAXLABELS\n");
        for (int i = 1; i <= taxaBlock.getNtax(); i++)
            w.write("\t\t[" + i + "] '" + taxaBlock.get(i).getName() + "'\n");
        w.write(";\n");
        if (hasInfos(taxaBlock)) {
            w.write("\tTAXINFO\n");
            for (int i = 1; i <= taxaBlock.getNtax(); i++)
                w.write("\t\t[" + i + "] '" + taxaBlock.get(i).getInfo() + "'\n");
            w.write(";\n");
        }
        w.write("END; [" + NAME + "]\n");
    }

    /**
     * returns true, if any taxon has an info string associated with it
     *
     * @return true, if some taxon has info
     */
    private static boolean hasInfos(TaxaBlock taxaBlock) {
        for (int t = 1; t <= taxaBlock.getNtax(); t++)
            if (taxaBlock.get(t).getInfo() != null && taxaBlock.get(t).getInfo().length() > 0)
                return true;
        return false;
    }

}
