/*
 * TaxaNexusInput.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.nexus;

import jloda.fx.window.NotificationManager;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;

import java.io.IOException;
import java.util.ArrayList;

/**
 * taxa nexus input
 * Daniel Huson, 2.2018
 */
public class TaxaNexusInput extends NexusIOBase {
    public static final String SYNTAX = "BEGIN " + TaxaBlock.BLOCK_NAME + ";\n" +
            "\t[TITLE title;]\n" +
            "\tDIMENSIONS NTAX=number-of-taxa;\n" +
            "\t[TAXLABELS\n" +
            "\t\tlist-of-labels\n" +
            "\t;]\n" +
            "\t[TAXINFO\n" +
            "\t\tlist-of-info-items (use 'null' for missing item)\n" +
            "\t;]\n" +
            "\t[DISPLAYLABELS\n" +
            "\t\tlist-of-html-strings (use 'null' for missing item)\n" +
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

        np.setCollectAllComments(true);

        if (np.peekMatchIgnoreCase("#nexus"))
            np.matchIgnoreCase("#nexus"); // skip header line if it is the first line

        final String comment = np.getComment();
        if (comment != null && comment.startsWith("!")) {
            NotificationManager.showInformation(comment);
        }
        np.setCollectAllComments(false);

        np.matchBeginBlock(TaxaBlock.BLOCK_NAME);
        parseTitleAndLink(np);

        np.matchIgnoreCase("DIMENSIONS ntax=");
        final int ntax = np.getInt();
        taxaBlock.setNtax(ntax);
        np.matchIgnoreCase(";");

        boolean labelsDetected = false;

        if (np.peekMatchIgnoreCase("taxLabels")) // grab labels now
        {
            np.matchIgnoreCase("taxLabels");
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

        if (labelsDetected && np.peekMatchIgnoreCase("displayLabels")) // get display labels
        {
            np.matchIgnoreCase("displayLabels");

            for (int t = 1; t <= ntax; t++) {
                final String displayLabel = np.getLabelRespectCase();
                if (!displayLabel.equals("null")) //  explicitly the word "null"
                    taxaBlock.get(t).setDisplayLabel(displayLabel);
            }
            np.matchIgnoreCase(";");
        }

        if (labelsDetected && np.peekMatchIgnoreCase("taxInfo")) // get info for labels
        {
            np.matchIgnoreCase("taxInfo");

            for (int t = 1; t <= ntax; t++) {
                final String info = np.getLabelRespectCase();
                if (!info.equals("null")) //  explicitly the word "null"
                    taxaBlock.get(t).setInfo(info);
            }
            np.matchIgnoreCase(";");
        }

        np.matchEndBlock();
        return taxonNamesFound;
    }

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + TaxaBlock.BLOCK_NAME + ";");
    }
}
