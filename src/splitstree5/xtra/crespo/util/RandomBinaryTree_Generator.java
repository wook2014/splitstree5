/*
 *  RandomBinaryTree_Generator.java Copyright (C) 2019 Daniel H. Huson
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

import java.util.ArrayList;

public class RandomBinaryTree_Generator {

    public static Object[] run(int numberOfTaxa) {
        ArrayList<MyNode> nodes = new ArrayList<MyNode>();
        ArrayList<String> taxaOrdering = new ArrayList<String>();
        int idCounter = 0;
        for (int i = 0; i < numberOfTaxa; i++) {
            String taxon = String.valueOf(idCounter++);
            nodes.add(new MyNode(taxon));
            taxaOrdering.add(taxon);
        }
        createInnerNodes(nodes, idCounter);
        Object[] res = {new MyTree(nodes.get(0)), taxaOrdering};
        return res;
    }

    private static void createInnerNodes(ArrayList<MyNode> nodes, int idCounter) {
        while (nodes.size() != 1) {
            MyNode v1 = getRandomNode(nodes);
            nodes.remove(v1);
            MyNode v2 = getRandomNode(nodes);
            nodes.remove(v2);

            MyNode v = new MyNode("");
            v.addChildren(v1);
            v.addChildren(v2);
            nodes.add(v);
        }
    }

    private static MyNode getRandomNode(ArrayList<MyNode> nodes) {
        double d = Math.random() * (nodes.size() - 1);
        int index = (int) Math.round(d);
        return nodes.get(index);
    }

}