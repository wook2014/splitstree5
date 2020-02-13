/*
 * SimpleNewickParser.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.io.imports.utils;

import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NotOwnerException;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;

import java.io.IOException;
import java.util.Iterator;


/**
 * simple Newick parser that can't handle rooted networks or edge labels
 * Daniel Huson, 1.2018
 */
public class SimpleNewickParser {
    private static final String punctuationCharacters = "),;:";
    private static final String startOfNumber = "-.0123456789";
    private boolean enforceLeafLabelsStartWithLetter = false;

    private PhyloTree tree;
    private boolean hasWeights;

    /**
     * parse the tree
     *
     * @param line
     * @throws IOException
     */
    public PhyloTree parse(String line) throws IOException {
        this.tree = new PhyloTree();
        hasWeights = false;

        parseBracketNotationRecursively(0, null, 0, line);
        if (tree.getNumberOfNodes() > 0)
            tree.setRoot(tree.getFirstNode());

        if (!isHasWeights()) { // set all weights to 1
            for (Edge e : tree.edges()) {
                if (e.getSource().getInDegree() == 0 && e.getSource().getOutDegree() == 2 && !tree.getTaxa(e.getSource()).iterator().hasNext()) {
                    tree.setWeight(e, 0.5);
                } else
                    tree.setWeight(e, 1);
            }
        }

        return tree;
    }

    public PhyloTree getTree() {
        return tree;
    }

    public boolean isHasWeights() {
        return hasWeights;
    }

    /**
     * recursively do the work
     *
     * @param depth distance from root
     * @param v     parent node
     * @param i     current position in string
     * @param str   string
     * @return new current position
     * @throws IOException
     */
    private int parseBracketNotationRecursively(int depth, Node v, int i, String str) throws IOException {
        try {
            for (i = Basic.skipSpaces(str, i); i < str.length(); i = Basic.skipSpaces(str, i + 1)) {
                final Node w = tree.newNode();

                if (str.charAt(i) == '(') {
                    i = parseBracketNotationRecursively(depth + 1, w, i + 1, str);
                    if (str.charAt(i) != ')')
                        throw new IOException("Expected ')' at position " + i);
                    i = Basic.skipSpaces(str, i + 1);
                    while (i < str.length() && punctuationCharacters.indexOf(str.charAt(i)) == -1) {
                        int i0 = i;
                        StringBuilder buf = new StringBuilder();
                        boolean inQuotes = false;
                        while (i < str.length() && (inQuotes || punctuationCharacters.indexOf(str.charAt(i)) == -1)) {
                            if (str.charAt(i) == '\'')
                                inQuotes = !inQuotes;
                            else
                                buf.append(str.charAt(i));
                            i++;
                        }

                        String label = buf.toString().trim();

                        if (label.length() == 0)
                            throw new IOException("Expected label at position " + i0);

                        if (enforceLeafLabelsStartWithLetter && w.getOutDegree() == 0 && !Character.isLetter(label.charAt(0))) {
                            label = "T" + label;
                        }
                        tree.setLabel(w, label);
                    }
                } else // everything to next ) : or , is considered a label:
                {
                    if (tree.getNumberOfNodes() == 1)
                        throw new IOException("Expected '(' at position " + i);
                    int i0 = i;
                    final StringBuilder buf = new StringBuilder();
                    boolean inQuotes = false;
                    while (i < str.length() && (inQuotes || punctuationCharacters.indexOf(str.charAt(i)) == -1)) {
                        if (str.charAt(i) == '\'')
                            inQuotes = !inQuotes;
                        else
                            buf.append(str.charAt(i));
                        i++;
                    }

                    String label = buf.toString().trim();
                    if (label.startsWith("'") && label.endsWith("'") && label.length() > 1)
                        label = label.substring(1, label.length() - 1).trim(); // strip quotes

                    if (label.length() == 0)
                        throw new IOException("Expected label at position " + i0);

                    if (enforceLeafLabelsStartWithLetter && w.getOutDegree() == 0 && !Character.isLetter(label.charAt(0))) {
                        label = "T" + label;
                    }

                    tree.setLabel(w, label);
                }
                Edge e = null;
                if (v != null) {
                    e = tree.newEdge(v, w);
                }

                // detect and read embedded bootstrap values:
                i = Basic.skipSpaces(str, i);

                // read edge weights

                if (i < str.length() && str.charAt(i) == ':') // edge weight is following
                {
                    i = Basic.skipSpaces(str, i + 1);
                    int i0 = i;
                    final StringBuilder buf = new StringBuilder();
                    while (i < str.length() && (punctuationCharacters.indexOf(str.charAt(i)) == -1 && str.charAt(i) != '['))
                        buf.append(str.charAt(i++));
                    String number = buf.toString().trim();
                    try {
                        double weight = Math.max(0, Double.parseDouble(number));
                        if (e != null)
                            tree.setWeight(e, weight);
                        if (!hasWeights)
                            hasWeights = true;
                    } catch (Exception ex) {
                        throw new IOException("Expected number at position " + i0 + " (got: '" + number + "')");
                    }
                }

                // now i should be pointing to a ',' or a ')'
                if (i >= str.length()) {
                    if (depth == 0)
                        return i; // finished parsing tree
                    else
                        throw new IOException("Unexpected end of line");
                }
                if (str.charAt(i) == ';' && depth == 0)
                    return i; // finished parsing tree
                else if (str.charAt(i) == ')')
                    return i;
                else if (str.charAt(i) != ',')
                    throw new IOException("Unexpected '" + str.charAt(i) + "' at position " + i);
            }
        } catch (NotOwnerException ex) {
            throw new IOException(ex);
        }
        return -1;
    }

    public Iterable<String> leafLabels() {
        return () -> new Iterator<>() {
            private Node v = tree.getFirstNode();

            {
                while (v != null && (v.getOutDegree() > 0 || tree.getLabel(v) == null))
                    v = v.getNext();
            }

            @Override
            public boolean hasNext() {
                return v != null;
            }

            @Override
            public String next() {
                final String result = (v != null ? tree.getLabel(v) : null);
                if (v != null)
                    v = v.getNext();
                while (v != null && (v.getOutDegree() > 0 || tree.getLabel(v) == null))
                    v = v.getNext();
                return result;
            }
        };
    }


    public boolean isEnforceLeafLabelsStartWithLetter() {
        return enforceLeafLabelsStartWithLetter;
    }

    public void setEnforceLeafLabelsStartWithLetter(boolean enforceLeafLabelsStartWithLetter) {
        this.enforceLeafLabelsStartWithLetter = enforceLeafLabelsStartWithLetter;
    }
}
