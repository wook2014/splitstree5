/*
 * SplitsTree5NexusInput.java Copyright (C) 2020. Daniel H. Huson
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

import jloda.util.parse.NexusStreamParser;
import splitstree5.core.datablocks.SplitsTree5Block;

import java.io.IOException;
import java.util.ArrayList;

/**
 * SplitsTree5 nexus input
 * Daniel Huson, 3.2018
 */
public class SplitsTree5NexusInput {
    public static final String SYNTAX = "BEGIN " + SplitsTree5Block.BLOCK_NAME + ";\n" +
            "\tDIMENSIONS nDataNodes=number nAlgorithms=number;\n" +
            "\tPROGRAM version=version-string;\n" +
            "\tWORKFLOW creationDate=long;\n" +
            "END;\n";

    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse a SplitsTree5 block
     *
     * @param np
     * @param splitsTree5Block
     * @throws IOException
     */
    public void parse(NexusStreamParser np, SplitsTree5Block splitsTree5Block) throws IOException {
        final ArrayList<String> taxonNamesFound = new ArrayList<>();
        splitsTree5Block.clear();

        np.matchBeginBlock(SplitsTree5Block.BLOCK_NAME);

        np.matchIgnoreCase("DIMENSIONS nDataNodes=");
        splitsTree5Block.setOptionNumberOfDataNodes(np.getInt());
        np.matchIgnoreCase("nAlgorithms=");
        splitsTree5Block.setOptionNumberOfAlgorithms(np.getInt());
        np.matchIgnoreCase(";");

        np.matchIgnoreCase("PROGRAM version=");
        splitsTree5Block.setOptionVersion(np.getWordRespectCase());
        np.matchIgnoreCase(";");

        np.matchIgnoreCase("WORKFLOW creationDate=");
        splitsTree5Block.setOptionCreationDate(np.getLong());
        np.matchIgnoreCase(";");

        np.matchEndBlock();
    }

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + SplitsTree5Block.BLOCK_NAME + ";");
    }
}
