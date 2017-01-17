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
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * performs nexus input and output for an algorithm
 * Created by huson on 12/27/16.
 */
public class AlgorithmNexusIO<P extends ADataBlock, C extends ADataBlock> {
    public static final String NAME = "ST_ALGORITHM";
    /**
     * parse a description of the algorithm
     *
     * @param np
     * @throws IOException
     */
    public static <P extends ADataBlock, C extends ADataBlock> void parse(Algorithm<P, C> algorithm, NexusStreamParser np, boolean skipHeader) throws IOException {
        if (!skipHeader) {
            np.matchBeginBlock(NAME);
            np.matchIgnoreCase("NAME='" + algorithm.getName() + "';");
        }
        final List<Option> options = OptionsAccessor.getAllOptions(algorithm);

        Set<String> legalOptions = new HashSet<>();
        for (Option option : options) {
            legalOptions.add(option.getName());
        }

        while (!np.peekMatchEndBlock()) {
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
    }


    /**
     * write a description of the algorithm
     *
     * @param w
     * @throws IOException
     */
    public static <P extends ADataBlock, C extends ADataBlock> void write(Algorithm<P, C> algorithm, Writer w) throws IOException {
        w.write("BEGIN " + NAME + ";\n");
        w.write("NAME = '" + algorithm.getName() + "';\n");
        for (Option option : OptionsAccessor.getAllOptions(algorithm)) {
            w.write(option.getName() + " = " + option.getValue().toString() + ";\n");
        }
        w.write("END; [" + NAME + "]\n");
    }

    public static <P extends ADataBlock, C extends ADataBlock> String getUsage(Algorithm<P, C> algorithm) {
        final StringBuilder buf = new StringBuilder();
        buf.append("BEGIN " + NAME + ";\n");
        buf.append("NAME = '").append(algorithm.getName()).append("';\n");
        for (Option option : OptionsAccessor.getAllOptions(algorithm)) {
            final String[] choice = option.getLegalValues();
            final String possibleValues = (choice != null ? "{ " + Basic.toString(choice, " | ") + " }" : "<" + option.getType().toString() + ">");
            buf.append(option.getName()).append(" = ").append(possibleValues).append(";\n");
        }
        buf.append("END; [" + NAME + "]\n");
        return buf.toString();
    }
}
