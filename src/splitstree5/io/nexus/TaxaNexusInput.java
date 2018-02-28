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

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.io.imports.IOExceptionWithLineNumber;

import java.io.IOException;
import java.util.ArrayList;

/**
 * taxa nexus input
 * Daniel Huson, 2.2018
 */
public class TaxaNexusInput {
    public static final String NAME = "TAXA";

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + NAME + ";");
    }

    public static final String SYNTAX = "BEGIN " + NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\tDIMENSIONS NTAX=number-of-taxa;\n" +
            "\t[TAXLABELS\n" +
            "\t\tlist-of-labels\n" +
            "\t;]\n" +
            "\t[TAXINFO\n" +
            "\t\tlist-of-info-items\n" +
            "\t;]\n" +
            "END;\n";

    public String getSyntax() {
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
    public ArrayList<String> parse(NexusStreamParser np, TaxaBlock taxaBlock) throws IOException {
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
                    if (taxonName.equals(";"))
                        throw new IOExceptionWithLineNumber("expected " + ntax + " taxon names, found: " + (t - 1), np.lineno());
                    final Taxon taxon = new Taxon(taxonName);

                    if (taxaBlock.indexOf(taxon) != -1) {
                        throw new IOExceptionWithLineNumber("taxon name '" + taxonName + "' appears multiple times, at " + taxaBlock.indexOf(taxon) + " and " + t, np.lineno());
                    }
                    taxaBlock.add(taxon);
                    taxonNamesFound.add(taxon.getName());
                }
                labelsDetected = true;
            }
            if (!np.peekMatchIgnoreCase(";")) {
                int count = ntax;
                while (!np.peekMatchIgnoreCase(";")) {
                    np.getWordRespectCase();
                    count++;
                }
                throw new IOExceptionWithLineNumber(np.lineno(), "expected " + ntax + " taxon names, found: " + count);

            }
            np.matchIgnoreCase(";");
        }

        if (labelsDetected && np.peekMatchIgnoreCase("taxinfo")) // get info for labels
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
}
