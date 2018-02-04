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
import jloda.util.PluginClassLoader;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.io.imports.IOExceptionWithLineNumber;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * algorithm nexus input
 * Daniel Huson, 2.2018
 */
public class AlgorithmNexusInput {
    public static final String NAME = "ALGORITHM";

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
            "\t[LINK name = title;]\n" +
            "\t[ALGORITHM=name]\n" +
            "\t[OPTION name=value]\n" +
            "\t[OPTION name=value]\n" +
            "\t...\n" +
            "END;\n";

    /**
     * get syntax
     */
    public String getSyntax() {
        return SYNTAX;
    }

    /**
     * parse and create an algorithm
     *
     * @param np
     * @throws IOException
     */
    public Algorithm parse(NexusStreamParser np) throws IOException {
        np.matchBeginBlock(NAME);

        final String title;
        if (np.peekMatchIgnoreCase("TITLE")) {
            np.matchIgnoreCase("TITLE");
            title = np.getLabelRespectCase();
            np.matchIgnoreCase(";");
        } else
            title = null;
        final ArrayList<Pair<String, String>> links = new ArrayList<>();
        while (np.peekMatchIgnoreCase("LINK")) {
            String key = np.getLabelRespectCase();
            np.matchIgnoreCase("=");
            String value = np.getLabelRespectCase();
            np.matchIgnoreCase(";");
            links.add(new Pair<>(key, value));
        }

        np.matchIgnoreCase("ALGORITHM=");
        String algorithmName = np.getWordRespectCase();
        np.matchIgnoreCase(";");

        final Algorithm algorithm = createAlgorithmFromName(algorithmName);
        if (algorithm == null)
            throw new IOExceptionWithLineNumber("Unknown algorithmm: " + algorithmName, np.lineno());
        algorithm.setTitle(title);
        algorithm.getLinks().addAll(links);

        final List<Option> options = OptionsAccessor.getAllOptions(algorithm);

        Set<String> legalOptions = new HashSet<>();
        for (Option option : options) {
            legalOptions.add(option.getName());
        }

        while (np.peekMatchIgnoreCase("OPTION")) {
            np.matchIgnoreCase("OPTION");
            final String name = np.getWordRespectCase();
            np.matchIgnoreCase("=");
            final String value = np.getWordFileNamePunctuation();
            np.matchIgnoreCase(";");

            if (legalOptions.contains(name))
                OptionsAccessor.setOptionValue(options, name, value);
            else
                System.err.println("WARNING: skipped unknown option for '" + algorithm.getName() + "': '" + name + "'");
        }
        np.matchEndBlock();
        return algorithm;
    }

    /**
     * creates an instance of the named algorithm
     *
     * @param algorithmName
     * @return instance or null
     */
    public static Algorithm createAlgorithmFromName(String algorithmName) {
        List<Algorithm> algorithms = PluginClassLoader.getInstances(algorithmName, Algorithm.class, null, "splitstree5.core.algorithms");
        if (algorithms.size() == 1)
            return algorithms.get(0);
        else
            return null;
    }
}
