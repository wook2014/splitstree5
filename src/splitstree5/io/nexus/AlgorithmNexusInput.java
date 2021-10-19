/*
 * AlgorithmNexusInput.java Copyright (C) 2021. Daniel H. Huson
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

import jloda.util.IOExceptionWithLineNumber;
import jloda.util.PluginClassLoader;
import jloda.util.parse.NexusStreamParser;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.gui.algorithmtab.next.OptionNext;
import splitstree5.gui.algorithmtab.next.OptionValueType;
import splitstree5.utils.Option;
import splitstree5.utils.OptionsAccessor;

import java.io.IOException;
import java.util.*;

/**
 * algorithm nexus input
 * Daniel Huson, 2.2018
 */
public class AlgorithmNexusInput extends NexusIOBase {
    public static final String SYNTAX = "BEGIN " + Algorithm.BLOCK_NAME + ";\n" +
            "\t[TITLE <title>;]\n" +
            "\t[LINK <parent-block-type> = <parent-title>;]\n" +
            "\tALGORITHM <name>;\n" +
            "\t\t[OPTIONS <name>=<value>," +
            "\t\t ..." +
            "\t\t<name>=<value>\n" +
            "\t;\n" +
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
        np.matchBeginBlock(Algorithm.BLOCK_NAME);
        parseTitleAndLink(np);

        np.matchIgnoreCase("ALGORITHM");
        final String algorithmName = np.getWordRespectCase();
        np.matchIgnoreCase(";");

        final Algorithm algorithm = createAlgorithmFromName(algorithmName);
        if (algorithm == null)
            throw new IOExceptionWithLineNumber("Unknown algorithm: " + algorithmName, np.lineno());

        if (np.peekMatchIgnoreCase("OPTIONS")) {
            np.matchIgnoreCase("OPTIONS");

            if (!np.peekMatchIgnoreCase(";")) {
                final ArrayList<OptionNext> optionsNext = new ArrayList<>(OptionNext.getAllOptions(algorithm));
                if (optionsNext.size() > 0) {
                    final Map<String, OptionNext> legalOptions = new HashMap<>();
                    for (OptionNext option : optionsNext) {
                        legalOptions.put(option.getName(), option);
                    }
                    while (true) {
                        final String name = np.getWordRespectCase();
                        np.matchIgnoreCase("=");
                        final OptionNext option = legalOptions.get(name);
                        if (option != null) {
                            final OptionValueType type = option.getOptionValueType();
                            switch (type) {
                                case doubleArray: {
                                    final double[] array = (double[]) option.getProperty().getValue();
                                    for (int i = 0; i < array.length; i++) {
                                        array[i] = np.getDouble();
                                    }
                                    break;
                                }
                                case doubleSquareMatrix: {
                                    final double[][] matrix = (double[][]) option.getProperty().getValue();
                                    for (int i = 0; i < matrix.length; i++) {
                                        for (int j = 0; j < matrix.length; j++)
                                            matrix[i][j] = np.getDouble();
                                    }
                                    break;
                                }
                                case Enum: {
                                    option.getProperty().setValue(option.getEnumValueForName(np.getWordRespectCase()));
                                    break;
                                }
                                case stringArray: {
                                    final ArrayList<String> list = new ArrayList<>();
                                    while (!np.peekMatchAnyTokenIgnoreCase(", ;"))
                                        list.add(np.getWordRespectCase());
                                    option.getProperty().setValue(list.toArray(new String[0]));
                                    break;
                                }
                                default: {
                                    option.getProperty().setValue(OptionValueType.parseType(option.getOptionValueType(), np.getWordRespectCase()));
                                    break;
                                }
                            }

                        } else {

                            final StringBuilder buf = new StringBuilder();
                            while (!np.peekMatchIgnoreCase(",") && !np.peekMatchIgnoreCase(";"))
                                buf.append(" ").append(np.getWordRespectCase());
                            System.err.println("WARNING: skipped unknown option for algorithm '" + algorithmName + "': '" + name + "=" + buf.toString() + "' in line " + np.lineno());

                        }
                        if (np.peekMatchIgnoreCase(";"))
                            break; // finished reading options
                        else
                            np.matchIgnoreCase(",");
                    }
                } else {
                    // todo: remove this
                    final List<Option> options = OptionsAccessor.getAllOptions(algorithm);
                    if (options.size() > 0)
                        System.err.println("Reading using old-style options"); // todo: this shouldn't happen

                    Set<String> legalOptions = new HashSet<>();
                    for (Option option : options) {
                        legalOptions.add(option.getName());
                    }

                    while (true) {
                        final String name = np.getWordRespectCase();
                        np.matchIgnoreCase("=");
                        final String value = np.getWordRespectCase();

                        if (legalOptions.contains(name))
                            OptionsAccessor.setOptionValue(options, name, value);
                        else
                            System.err.println("WARNING: skipped unknown option for algorithm '" + algorithmName + "': '" + name + "' in line " + np.lineno());

                        if (np.peekMatchIgnoreCase(";"))
                            break;
                        else
                            np.matchIgnoreCase(",");
                    }
                }
            }
            np.matchIgnoreCase(";");
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

    /**
     * is the parser at the beginning of a block that this class can parse?
     *
     * @param np
     * @return true, if can parse from here
     */
    public boolean atBeginOfBlock(NexusStreamParser np) {
        return np.peekMatchIgnoreCase("begin " + Algorithm.BLOCK_NAME + ";");
    }
}
