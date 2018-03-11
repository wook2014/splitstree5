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
import splitstree5.io.imports.IOExceptionWithLineNumber;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

/**
 * base class for nexus input and output
 * Daniel Huson, 3.2018
 */
public class NexusIOBase {
    private String title;
    private final ArrayList<Pair<String, String>> links = new ArrayList<>();

    /**
     * write the block title and links, if set
     *
     * @param w
     * @throws IOException
     */
    public void writeTitleAndLinks(Writer w) throws IOException {
        if (getTitle() != null && getTitle().length() > 0) {
            w.write("\tTITLE " + getTitle() + ";\n");
            for (Pair<String, String> pair : links)
                w.write("\tLINK " + pair.getFirst() + " = " + pair.getSecond() + ";\n");
        }
    }

    /**
     * parse the title and links, if present
     *
     * @param np
     * @throws IOExceptionWithLineNumber
     */
    public void parseTitleAndLinks(NexusStreamParser np) throws IOExceptionWithLineNumber {
        setTitle(null);
        links.clear();

        if (np.peekMatchIgnoreCase("title")) {
            np.matchIgnoreCase("title");
            setTitle(np.getWordRespectCase());
            np.matchIgnoreCase(";");
            if (np.peekMatchIgnoreCase("link")) {
                np.matchIgnoreCase("link");
                final String parentType = np.getWordRespectCase();
                np.matchIgnoreCase("=");
                final String parentTitle = np.getWordRespectCase();
                links.add(new Pair<>(parentType, parentTitle));
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Pair<String, String>> getLinks() {
        return links;
    }

    /**
     * set all the links
     *
     * @param links
     */
    public void setAllLinks(Collection<Pair<String, String>> links) {
        this.links.clear();
        this.links.addAll(links);
    }
}
