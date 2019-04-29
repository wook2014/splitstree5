/*
 *  MyNewickParser.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.xtra.crespo.util;


import jloda.graph.Node;
import jloda.phylo.PhyloTree;

import java.util.Hashtable;
import java.util.Vector;

public class MyNewickParser {

    private Hashtable<Integer, Integer> bracketToBracket;
    private Hashtable<Integer, Integer> sqBracketToSqBracket;
    private String newickString;

    public MyNewickParser() {
        bracketToBracket = new Hashtable<Integer, Integer>();
        sqBracketToSqBracket = new Hashtable<Integer, Integer>();
    }

    public MyTree run(String newickString) {

        this.newickString = newickString;

        mapBrackets(newickString);
        mapSqBrackets(newickString);

        int pos = newickString.length() - 1;
        String rootLabel = "";
        while (pos >= 0 && newickString.charAt(pos) != ')') {
            char c = newickString.charAt(pos);
            if (c != ';')
                rootLabel = c + rootLabel;
            pos--;
        }
        MyTree t = new MyTree(new MyNode());
        PhyloTree t1 = new PhyloTree();
        Node n = new Node(t1);
        t1.setRoot(n);
        if (pos > 0)
            parseChildren(t, t.getRoot(), pos);

        return t;

    }

    private void mapBrackets(String newickString) {
        Vector<Integer> openBrackets = new Vector<Integer>();
        for (int i = 0; i < newickString.length(); i++) {
            char c = newickString.charAt(i);
            if (c == '(')
                openBrackets.add(i);
            else if (c == ')') {
                int openBracket = openBrackets.lastElement();
                bracketToBracket.put(openBracket, i);
                bracketToBracket.put(i, openBracket);
                openBrackets.removeElement(openBracket);
            }
        }
    }

    private void mapSqBrackets(String newickString) {
        int openSqBrackets = -1;
        for (int i = 0; i < newickString.length(); i++) {
            char c = newickString.charAt(i);
            if (c == '[')
                openSqBrackets = i;
            else if (c == ']') {
                sqBracketToSqBracket.put(openSqBrackets, i);
                sqBracketToSqBracket.put(i, openSqBrackets);
            }
        }
    }

    private void parseChildren(MyTree t, MyNode v, int i) {
        Hashtable<String, Vector<Integer>> labelToStartpos = splitBracket(i);
        for (String s : labelToStartpos.keySet()) {
            for (int startPos : labelToStartpos.get(s)) {
                String[] a = parseLabel(s);
                MyNode w = new MyNode(a[0]);
                v.addChildren(w);
                if (a[2] != null && !a[2].isEmpty())
                    w.setBranchLength(Double.parseDouble(a[2]));
                if (startPos != -1)
                    parseChildren(t, w, startPos);
            }
        }
    }


    private Hashtable<String, Vector<Integer>> splitBracket(int pos) {
        Hashtable<String, Vector<Integer>> labelToStartpos = new Hashtable<String, Vector<Integer>>();
        int i = pos - 1;
        String s = "";
        while (i >= bracketToBracket.get(pos)) {
            char c = newickString.charAt(i);
            if (c == ',' || c == '(') {
                String label = s;
                if (!labelToStartpos.containsKey(label))
                    labelToStartpos.put(label, new Vector<Integer>());
                labelToStartpos.get(label).add(-1);
                s = "";
            } else if (c == ')') {
                String label = s;
                if (!labelToStartpos.containsKey(label))
                    labelToStartpos.put(label, new Vector<Integer>());
                labelToStartpos.get(label).add(i);
                i = bracketToBracket.get(i) - 1;
                s = "";
            } else if (c == ']') {
                s = newickString.substring(sqBracketToSqBracket.get(i), i + 1) + s;
                i = sqBracketToSqBracket.get(i);
            } else
                s = String.valueOf(c) + s;
            i--;
        }
        return labelToStartpos;
    }

    public String[] parseLabel(String l) {
        String[] content = new String[4];
        if (l.contains("[")) {
            int index = l.indexOf("[");
            String a[] = {l.substring(0, index), l.substring(index + 1)};
            content[3] = a.length == 2 ? a[1] : "";
            if (a.length > 1) {
                if (a[0].contains(":")) {
                    a = a[0].split(":");
                    content[2] = a.length == 2 ? a[1] : "";
                    if (a.length > 1) {
                        if (a[0].contains("#")) {
                            a = a[0].split("#");
                            content[0] = a.length > 0 ? a[0] : null;
                            content[1] = a.length == 2 ? a[1] : "";
                        } else
                            content[0] = a[0];
                    }
                } else if (a[0].contains("#")) {
                    a = a[0].split("#");
                    content[0] = a.length > 0 ? a[0] : null;
                    content[1] = a.length == 2 ? a[1] : "";
                } else
                    content[0] = a[0];
            }
        } else if (l.contains(":")) {
            String[] a = l.split(":");
            content[2] = a.length == 2 ? a[1] : "";
            if (a.length > 1) {
                if (a[0].contains("#")) {
                    a = a[0].split("#");
                    content[0] = a.length > 0 ? a[0] : null;
                    content[1] = a.length == 2 ? a[1] : "";
                } else
                    content[0] = a.length > 0 ? a[0] : null;
            }
        } else if (l.contains("#")) {
            String[] a = l.split("#");
            if (a.length > 1) {
                content[0] = a.length > 0 ? a[0] : null;
                content[1] = a.length == 2 ? a[1] : "";
            }
        } else
            content[0] = l;
        return content;
    }

}

