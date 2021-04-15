/*
 * PolygonView2D.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.gui.graphtab.base;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import jloda.graph.Node;
import jloda.graph.NodeArray;

import java.util.ArrayList;

/**
 * polygon view used by outline algorithm
 * Daniel Huson, 1.2020
 */
public class PolygonView2D {
    private final Polygon polygon = new Polygon();
    private final ArrayList<Node> nodes;
    private final NodeArray<NodeViewBase> node2view;

    public PolygonView2D(ArrayList<Node> nodes, NodeArray<NodeViewBase> node2view) {
        this.nodes = nodes;
        this.node2view = node2view;
        polygon.setFill(Color.WHITESMOKE);
        update();
    }

    public void update() {
        polygon.getPoints().clear();
        for (Node v : nodes) {
            final NodeViewBase nodeViewBase = node2view.get(v);
            nodeViewBase.setFill(Color.WHITESMOKE);
            polygon.getPoints().addAll(nodeViewBase.getShapeGroup().getTranslateX(), nodeViewBase.getShapeGroup().getTranslateY());
        }
    }

    public Polygon getShape() {
        return polygon;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }
}
