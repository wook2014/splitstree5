/*
 *  Copyright (C) 2017 Daniel H. Huson
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
 *  Copyright (C) 2017 Daniel H. Huson
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
 *  Copyright (C) 2017 Daniel H. Huson
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
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.main.Version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * generates the methods text for a given DAG
 * Daniel Huson, June 2017
 */
public class MethodsTextGenerator {
    private static MethodsTextGenerator instance;

    public static String preambleTemplate = "Analysis was performed using SplitsTree5 %s (%s).\n";
    public static String inputDataTemplate = "The original input consisted of %s and %s.\n";
    public static String filteredInputTemplate = "After removal of %d taxa, the input consisted of %s and %s.\n";
    public static String methodWithOutputTemplate = "The %s method (%s) was used%s so as to obtain %s.\n";
    public static String methodTemplate = "The %s method %s was used%s.\n";

    private final Map<String, String> method2citation;
    private final Map<String, String> method2paperTitle;


    /**
     * constructor
     */
    private MethodsTextGenerator() {
        method2citation = new HashMap<>();
        method2paperTitle = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceManager.getFileAsStream("splitstree5.info", "citations.txt")))) {
            String aLine;
            while ((aLine = reader.readLine()) != null) {
                final String[] tokens = Basic.split(aLine, ';');
                if (tokens.length >= 2) {
                    method2citation.put(tokens[0], tokens[1]);
                    if (tokens.length >= 3) {
                        method2paperTitle.put(tokens[0], tokens[2]);
                    }
                }
            }

        } catch (IOException e) {
            Basic.caught(e);
        }
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

        buf.append("Methods:\n");

        buf.append(String.format(preambleTemplate, Version.VERSION, method2citation.get("SplitsTree4")));

        if (dag.getTopTaxaNode().getDataBlock() != null && dag.getTopDataNode() != null) {
            final ArrayList<String> papers = new ArrayList<>();

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
                                final String name = Basic.fromCamelCase(algorithm.getName());
                                String citation = method2citation.get(Basic.getShortName(algorithm.getClass()));
                                if (citation == null)
                                    citation = "citation???";
                                final String paper = method2paperTitle.get(Basic.getShortName(algorithm.getClass()));
                                if (paper != null && !papers.contains(paper))
                                    papers.add(paper);

                                final String parameters = (algorithm.getParameters().length() > 0 ? " (parameters: " + algorithm.getParameters() + ")" : "");
                                if (w != null) {
                                    buf.append(String.format(methodWithOutputTemplate, name, citation, parameters, w.getDataBlock().getInfo()));
                                } else {
                                    buf.append(String.format(methodTemplate, name, citation, parameters));
                                }
                            }
                            stack.push(w);
                        }
                    }
                }
            }
            buf.append("\n");
            buf.append("References:\n");

            if (method2paperTitle.containsKey("SplitsTree4"))
                buf.append(method2paperTitle.get("SplitsTree4")).append("\n");
//            if(method2paperTitle.containsKey("SplitsTree5"))
//                buf.append(method2paperTitle.get("SplitsTree5")).append("\n");

            if (papers.size() > 0) {
                buf.append(Basic.toString(papers, "\n"));
            }
        }
        if (false) // todo: turn back on?
            System.err.println(buf.toString());
        return buf.toString();
    }

    /**
     * gets the citation for a method
     *
     * @param algorithm
     * @return citation
     */
    public static String getCitation(Algorithm algorithm) {
        final String result = getInstance().method2citation.get(Basic.getShortName(algorithm.getClass()));
        return result != null ? result : "";
    }

    /**
     * gets the paper title for a method
     *
     * @param algorithm
     * @return paper
     */
    public static String getPaperTitle(Algorithm algorithm) {
        final String result = getInstance().method2paperTitle.get(Basic.getShortName(algorithm.getClass()));
        return result != null ? result : "";
    }


}
