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

import jloda.util.Pair;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.misc.ANamed;

import java.io.IOException;
import java.io.Writer;

/**
 * code for reading and writing links
 * Daniel Huson, 2/24/17.
 */
public class UtilitiesNexusIO {
    /**
     * read name, title and links
     *
     * @param np
     * @param block
     * @throws IOException
     */
    public static void readTitleLinks(NexusStreamParser np, ANamed block) throws IOException {
        if (np.peekMatchIgnoreCase("TITLE")) {
            np.matchIgnoreCase("TITLE");
            block.setTitle(np.getLabelRespectCase());
            np.matchIgnoreCase(";");
        }
        while (np.peekMatchIgnoreCase("LINK")) {
            String name = np.getLabelRespectCase();
            np.matchIgnoreCase("=");
            String title = np.getLabelRespectCase();
            np.matchIgnoreCase(";");
            block.getLinks().add(new Pair<String, String>(name, title));
        }
    }

    /**
     * write name, title and links
     *
     * @param w
     * @param block
     * @throws IOException
     */
    public static void writeTitleLinks(Writer w, ANamed block) throws IOException {
        if (block.getTitle() != null) {
            w.write("\tTITLE " + block.getTitle() + ";\n");
        }
        if (block.hasLinks()) {
            for (Pair<String, String> link : block.getLinks()) {
                w.write("\tLINK " + link.getFirst() + " = " + link.getSecond() + ";\n");
            }
        }
    }
}
