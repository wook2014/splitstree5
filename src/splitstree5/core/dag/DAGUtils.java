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

package splitstree5.core.dag;

import jloda.util.Basic;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * some utilities for working with the DAG
 * Created by huson on 12/27/16.
 */
public class DAGUtils {
    public static void print(ADataNode<TaxaBlock> originalTaxonNode, ADataNode originalDataNode) {
        System.err.println("+++ Document update graph:");
        printRec(originalTaxonNode, true, 0);
        printRec(originalDataNode, false, 0);
        System.err.println("+++ end");
    }

    /**
     * recursively print all below
     *
     * @param node
     */
    private static void printRec(final ANode node, final boolean stopBeforeTopFilter, int level) {
        if (node instanceof ADataNode) {
            final ADataNode that = (ADataNode) node;

            for (int i = 0; i < level; i++)
                System.err.print("--");

            System.err.print("Data " + Basic.getShortName(((ADataNode) node).getDataBlock().getClass()) + "[" + node.getUid() + "]");
            if (that.getChildren().size() > 0) {
                System.err.print(": c=");
                for (Object b : that.getChildren()) {
                    System.err.print(" " + ((ANode) b).getUid());
                }
            }
            System.err.println();
            for (Object b : that.getChildren()) {
                printRec((ANode) b, stopBeforeTopFilter, level + 1);
            }
        } else if (node instanceof AConnector) {
            final AConnector that = (AConnector) node;

            if (stopBeforeTopFilter && that.getAlgorithm() != null && that.getAlgorithm().getName().equals("TopFilter"))
                return;

            for (int i = 0; i < level; i++)
                System.err.print("--");

            System.err.println("Algo " + that.getAlgorithm().getName() + " [" + node.getUid() + "]: p=" + that.getParent().getUid() + " c=" + that.getChild().getUid());
            printRec(that.getChild(), stopBeforeTopFilter, level + 1);

        }

    }
}
