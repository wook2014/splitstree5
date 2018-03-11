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

import jloda.util.Basic;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.io.IOException;
import java.io.Writer;

import static splitstree5.io.nexus.AlgorithmNexusInput.NAME;

/**
 * algorithm nexus output
 * Daniel Huson, 2.2018
 */
public class AlgorithmNexusOutput {
    /**
     * write a description of the algorithm
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w, Algorithm algorithm) throws IOException {
        w.write("\nBEGIN " + NAME + ";\n");
        UtilitiesNexusIO.writeTitleLinks(w, algorithm);
        w.write("\tALGORITHM = " + algorithm.getName() + ";\n");
        for (Option option : OptionsAccessor.getAllOptions(algorithm)) {
            w.write("\t\tOPTION " + option.getName() + " = " + option.getValue().toString() + ";\n");
        }
        w.write("END; [" + NAME + "]\n");
    }

    /**
     * get the usage string for this particular algorithm
     *
     * @param algorithm
     * @return usage for this algorithm
     */
    public String getUsage(Algorithm algorithm) {
        final StringBuilder buf = new StringBuilder();
        buf.append("BEGIN " + NAME + ";\n");
        buf.append("\t[TITLE title;]\n");
        buf.append("\t[LINK name = title;]\n");
        buf.append("ALGORITHM = ").append(algorithm.getName()).append(";\n");
        for (Option option : OptionsAccessor.getAllOptions(algorithm)) {
            final String[] choice = option.getLegalValues();
            final String possibleValues = (choice != null ? "{ " + Basic.toString(choice, " | ") + " }" : "<" + option.getType().toString() + ">");
            buf.append("\t\tOPTION ").append(option.getName()).append(" = ").append(possibleValues).append(";\n");
        }
        buf.append("END; [" + NAME + "]\n");
        return buf.toString();
    }
}
