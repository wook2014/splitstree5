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

package splitstree5.info;

import jloda.util.Basic;
import jloda.util.Pair;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.IFilter;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.Workflow;
import splitstree5.main.Version;

import java.util.*;

/**
 * generates the methods text for a given Workflow
 * Daniel Huson, June 2017
 */
public class MethodsTextGenerator {
    private static MethodsTextGenerator instance;

    public static String preambleTemplate = "Analysis was performed using SplitsTree5 %s%s.\n";
    public static String inputDataTemplate = "The original input consisted of %s and %s.\n";
    public static String taxonFilterTemplate = "After removal of %d taxa, the input consisted of %s and %s.\n";
    public static String methodWithOutputTemplate = "The %s method%s was used%s so as to obtain %s.\n";
    public static String methodTemplate = "The %s method%s was used%s.\n";

    public static String filterTemplate = "A %s%s was applied so as to be %s.\n";

    /**
     * constructor
     */
    private MethodsTextGenerator() {
    }

    /**
     * gets the single instance
     *
     * @return instance
     */
    public static MethodsTextGenerator getInstance() {
        if (instance == null)
            instance = new MethodsTextGenerator();
        return instance;
    }

    /**
     * generate the current methods text
     *
     * @param document
     * @return method text
     */
    public String apply(Document document) {
        final Workflow dag = document.getWorkflow();

        if (dag.getTopTaxaNode() == null || dag.getTopDataNode() == null)
            return "";

        final StringBuilder buf = new StringBuilder();
        buf.append("Methods:\n");

        buf.append(String.format(preambleTemplate, Version.VERSION, getSplitsTreeKeysString()));

        final Set<Pair<String, String>> allKeysAndPapers = new TreeSet<>(getSplitsTreeKeysAndPapers());

        final Set<String> set = new HashSet<>(); // use this to avoid duplicate lines

        buf.append(String.format(inputDataTemplate, dag.getTopTaxaNode().getDataBlock().getInfo(), dag.getTopDataNode().getDataBlock().getInfo()));
        if (dag.getWorkingTaxaBlock() != null && dag.getWorkingTaxaBlock().getNtax() < dag.getTopTaxaNode().getDataBlock().getNtax()) {
            int removed = (dag.getTopTaxaNode().getDataBlock().getNtax() - dag.getWorkingTaxaBlock().getNtax());
            buf.append(String.format(taxonFilterTemplate, removed, dag.getWorkingTaxaBlock().getInfo(), dag.getWorkingDataNode().getDataBlock().getInfo()));
        }
        final DataNode root = dag.getWorkingDataNode();
        final Set<DataNode> visited = new HashSet<>();
        final Stack<DataNode> stack = new Stack<>();
        stack.push(root); // should only contain data nodes
        while (stack.size() > 0) {
            final DataNode v = stack.pop();
            if (!visited.contains(v)) {
                visited.add(v);
                for (Object obj : v.getChildren()) {
                    if (obj instanceof Connector) {
                        final Connector connector = (Connector) obj;
                        final Algorithm algorithm = connector.getAlgorithm();
                        final DataNode w = connector.getChild();

                        if (algorithm instanceof IFilter) {
                            if (((IFilter) algorithm).isActive()) {
                                final String name = Basic.fromCamelCase(algorithm.getName());
                                final String parameters = (algorithm.getParameters().length() > 0 ? " (parameters: " + algorithm.getParameters() + ")" : "");
                                final String line = String.format(filterTemplate, name, parameters, algorithm.getShortDescription());
                                if (!set.contains(line)) {
                                    buf.append(line);
                                    set.add(line);
                                }
                            }
                        } else {
                            if (algorithm != null) {
                                final String keys = getKeysString(algorithm);
                                final Collection<Pair<String, String>> keysAndPapers = getKeysAndPapers(algorithm);
                                if (keysAndPapers != null)
                                    allKeysAndPapers.addAll(keysAndPapers);
                                final String name = Basic.fromCamelCase(algorithm.getName());

                                final String parameters = (algorithm.getParameters().length() > 0 ? " (parameters: " + algorithm.getParameters() + ")" : "");
                                final String line;
                                if (w != null) {
                                    line = String.format(methodWithOutputTemplate, name, keys, parameters, w.getDataBlock().getInfo());
                                } else {
                                    line = String.format(methodTemplate, name, keys, parameters);
                                }
                                if (!set.contains(line)) {
                                    buf.append(line);
                                    set.add(line);
                                }
                            }
                        }
                        stack.push(w);
                    }
                }
            }
        }
        buf.append("\n");
        if (allKeysAndPapers.size() > 0) {
            buf.append("References:\n");

            for (Pair<String, String> pair : allKeysAndPapers) {
                buf.append(String.format("%s: %s\n", pair.get1(), pair.get2()));
            }
        }

        return buf.toString();
    }

    /**
     * gets the citation keys for an algorithm
     *
     * @param algorithm
     * @return citation
     */
    public static String getKeysString(Algorithm algorithm) {
        if (algorithm.getCitation() == null || algorithm.getCitation().length() < 2)
            return "";
        else {
            final String[] tokens = Basic.split(algorithm.getCitation(), ';');
            final StringBuilder buf = new StringBuilder();
            buf.append(" (");
            for (int i = 0; i < tokens.length; i += 2) {
                if (i > 0)
                    buf.append(", ");
                else
                    buf.append(tokens[i]);
            }
            buf.append(")");
            return buf.toString();
        }
    }

    /**
     * get all the key - paper pairs for an algorithm
     *
     * @param algorithm
     * @return pairs
     */
    public static Collection<Pair<String, String>> getKeysAndPapers(Algorithm algorithm) {
        if (algorithm.getCitation() == null || algorithm.getCitation().length() < 2)
            return null;
        else {
            Set<Pair<String, String>> set = new TreeSet<>();
            final String[] tokens = Basic.split(algorithm.getCitation(), ';');
            if (tokens.length % 2 == 1)
                System.err.println("Internal error: Citation string has odd number of tokens: " + algorithm.getCitation());
            for (int i = 0; i < tokens.length - 1; i += 2) {
                set.add(new Pair<>(tokens[i], tokens[i + 1]));
            }
            return set;
        }
    }

    public static String getSplitsTreeKeysString() {
        return " (Huson 1998, Huson and Bryant 2006)";
    }

    public static Collection<Pair<String, String>> getSplitsTreeKeysAndPapers() {
        return Arrays.asList(new Pair<>("Huson and Bryant 2006",
                "D.H. Huson and D. Bryant. Application of phylogenetic networks in evolutionary studies. Molecular Biology and Evolution, 23:254–267, 2006."));
    }


}
