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
import splitstree5.core.datablocks.AnalysisBlock;

import java.io.IOException;
import java.util.ArrayList;

/**
 * analysis result nexus input
 * Daniel Huson, 2.2018
 */
public class AnalysisNexusInput extends NexusIOBase {
    public static final String NAME = "ST_ANALYSIS_RESULT";

    public static final String SYNTAX = "BEGIN ST_ANALYSIS;\n" +
            "\t[TITLE title;]\n" +
            "\t[LINK name = title;]\n" +
            "\t[DIMENSIONS [NLINES=number-of-lines];]\n" +
            "\tRESULT\n" +
            "\t\tresults...\n" +
            "END;\n";

    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse an analysis result block
     *
     * @param np
     * @param block
     * @return list of taxon names found
     * @throws IOException
     */
    public ArrayList<String> parse(NexusStreamParser np, AnalysisBlock block) throws IOException {
        block.clear();
        final ArrayList<String> taxonNamesFound = new ArrayList<>();

        block.setShortDescription(null);

        if (np.peekMatchIgnoreCase("#nexus"))
            np.matchIgnoreCase("#nexus"); // skip header line if it is the first line

        np.matchBeginBlock(NAME);
        parseTitleAndLinks(np);

        np.matchIgnoreCase("DIMENSIONS nlines=");
        final int nLines = np.getInt();
        np.matchIgnoreCase(";");
        np.matchIgnoreCase("RESULT");
        {
            StringBuilder buf = new StringBuilder();
            np.setEolIsSignificant(true);
            int found = 0;
            while (found < nLines) {
                String word = np.getWordRespectCase();
                if (word.equals("\n"))
                    found++;
                buf.append(word);
            }
            block.setShortDescription(buf.toString());
        }
        np.matchIgnoreCase("END;");

        return taxonNamesFound;
    }

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + NAME + ";");
    }
}
