/*
 *  AlgorithmNexusOutput.java Copyright (C) 2020 Daniel H. Huson
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

import splitstree5.core.algorithms.Algorithm;
import splitstree5.gui.algorithmtab.next.OptionNext;
import splitstree5.gui.algorithmtab.next.OptionValueType;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * algorithm nexus output
 * Daniel Huson, 2.2018
 */
public class AlgorithmNexusOutput extends NexusIOBase {
    /**
     * write a description of the algorithm
     *
     * @param w
     * @throws IOException
     */
    public void write(Writer w, Algorithm algorithm) throws IOException {
        w.write("\nBEGIN " + Algorithm.BLOCK_NAME + ";\n");
        writeTitleAndLink(w);
        w.write("ALGORITHM " + algorithm.getName() + ";\n");

        final ArrayList<OptionNext> optionsNext = new ArrayList<>(OptionNext.getAllOptions(algorithm));
        if (optionsNext.size() > 0) {
            w.write("OPTIONS\n");
            boolean first = true;
            for (OptionNext option : optionsNext) {
                if (first)
                    first = false;
                else
                    w.write(",\n");
                final Object value = option.getProperty().getValue();

                w.write("\t" + option.getName() + " = " + OptionValueType.toStringType(option.getOptionValueType(), value));
            }
            w.write(";\n");
        } else {

            final ArrayList<Option> options = OptionsAccessor.getAllOptions(algorithm);
            if (options.size() > 0) {
                System.err.println("Writing using old-style options"); // todo: this shouldn't happen
                w.write("OPTIONS\n");
                boolean first = true;
                for (Option option : options) {
                    if (first)
                        first = false;
                    else
                        w.write(",\n");
                    w.write("\t" + option.getName() + " = " + option.getValue().toString());
                }
                w.write(";\n");
            }
        }
        w.write("END; [" + Algorithm.BLOCK_NAME + "]\n");
    }
}
