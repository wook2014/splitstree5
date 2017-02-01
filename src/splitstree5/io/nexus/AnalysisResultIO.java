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

import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.AnalysisResultBlock;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * analysis results block nexus implementation
 * Created by huson on 12/22/16.
 */
public class AnalysisResultIO {
    private static final String NAME = "ST_ANALYSIS_RESULT";

    public static final String SYNTAX = "BEGIN ST_ANALYSIS_RESULT;\n" +
            "\t[DIMENSIONS [NLINES=number-of-lines];]\n" +
            "\tRESULT\n" +
            "\tresults...\n" +
            "\t\n" +
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
     * parse an analysis block
     *
     * @param np
     * @param aBlock
     * @return list of taxon names found
     * @throws IOException
     */
    public static ArrayList<String> parse(NexusStreamParser np, AnalysisResultBlock aBlock) throws IOException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();

        aBlock.setShortDescription(null);

        if (np.peekMatchIgnoreCase("#nexus"))
            np.matchIgnoreCase("#nexus"); // skip header line if it is the first line

        np.matchBeginBlock(NAME);

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
            aBlock.setShortDescription(buf.toString());
        }
        np.matchIgnoreCase("END;");

        return taxonNamesFound;
    }


    /**
     * writes the block in nexus format
     *
     * @param w
     * @param aBlock
     * @throws IOException
     */
    public static void write(Writer w, AnalysisResultBlock aBlock) throws IOException {
        w.write("\nBEGIN " + NAME + ";\n");
        w.write("DIMENSIONS nlines=" + Basic.countOccurrences(aBlock.getShortDescription(), '\n') + ";\n");
        w.write("RESULT;\n");
        w.write(aBlock.getShortDescription());
        if (!aBlock.getShortDescription().endsWith("\n"))
            w.write("\n");
        w.write("END; [" + NAME + "]\n");
    }
}
