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

package splitstree5.core;

import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.main.Version;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * generates the methods text for a given DAG
 * Daniel Huson, June 2017
 */
public class MethodsTextGenerator {
    private static MethodsTextGenerator instance;

    public static String preambleTemplate = "Analysis was performed using SplitsTree5 %s (Huson and Bryant, in preparation).\n";
    public static String inputDataTemplate = "The original input consisted of %s and %s.\n";
    public static String filteredInputTemplate = "After removal of %d taxa, the input consisted of %s and %s.\n";
    public static String methodWithOutputTemplate = "The %s method (%s) was used (parameters: %s) to obtain %s.\n";
    public static String methodTemplate = "The %s method %s was used (parameters: %s).\n";

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
        final DAG dag = document.getDag();

        final StringBuilder buf = new StringBuilder();

        buf.append(String.format(preambleTemplate, Version.VERSION));

        if (dag.getTopTaxaNode().getDataBlock() != null && dag.getTopDataNode() != null) {
            buf.append(String.format(inputDataTemplate, dag.getTopTaxaNode().getDataBlock().getInfo(), dag.getTopDataNode().getDataBlock().getInfo()));
            if (dag.getWorkingTaxaBlock() != null && dag.getWorkingTaxaBlock().getNtax() < dag.getTopTaxaNode().getDataBlock().getNtax()) {
                int removed = (dag.getTopTaxaNode().getDataBlock().getNtax() - dag.getWorkingTaxaBlock().getNtax());
                buf.append(String.format(filteredInputTemplate, removed, dag.getWorkingTaxaBlock().getInfo(), dag.getWorkingDataNode().getDataBlock().getInfo()));
            }
            final ADataNode root = dag.getWorkingDataNode();
            final Set<ADataNode> visited = new HashSet<>();
            final Stack<ADataNode> stack = new Stack<>();
            stack.push(root); // should only contain data nodes
            while (stack.size() > 0) {
                final ADataNode v = stack.pop();
                if (!visited.contains(v)) {
                    visited.add(v);
                    for (Object obj : v.getChildren()) {
                        if (obj instanceof AConnector) {
                            final AConnector connector = (AConnector) obj;
                            final Algorithm algorithm = connector.getAlgorithm();
                            final ADataNode w = connector.getChild();
                            if (algorithm != null) {
                                if (w != null) {
                                    buf.append(String.format(methodWithOutputTemplate, algorithm.getName(), algorithm.getCitation(), algorithm.getParameters(), w.getDataBlock().getInfo()));
                                } else
                                    buf.append(String.format(methodTemplate, algorithm.getName(), algorithm.getCitation(), algorithm.getParameters()));
                            } else
                                System.err.println("algorithm=null");
                            stack.push(w);
                        }
                    }
                }
            }
        }
        System.err.println(buf.toString());
        return buf.toString();
    }
}
