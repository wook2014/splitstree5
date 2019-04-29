/*
 *  ISplitsViewTab.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.graphtab;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ToolBar;
import jloda.fx.control.AMultipleSelectionModel;
import jloda.graph.Edge;
import jloda.graph.EdgeArray;
import jloda.graph.Node;
import jloda.graph.NodeArray;
import jloda.phylo.PhyloSplitsGraph;
import splitstree5.core.Document;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.gui.graphtab.base.EdgeViewBase;
import splitstree5.gui.graphtab.base.GraphLayout;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.menu.MenuController;

public interface ISplitsViewTab {
    void init(PhyloSplitsGraph graph);

    Dimension2D getTargetDimensions();

    NodeArray<NodeViewBase> getNode2view();

    EdgeArray<EdgeViewBase> getEdge2view();

    Group getNodesGroup();

    Group getNodeLabelsGroup();

    Group getEdgesGroup();

    Group getEdgeLabelsGroup();

    NodeViewBase createNodeView(final Node v, Point2D location, String label);

    void setupNodeView(NodeViewBase nv);

    EdgeViewBase createEdgeView(final Edge e, final Point2D start, final Point2D end, String text);

    PhyloSplitsGraph getGraph();

    AMultipleSelectionModel<Node> getNodeSelectionModel();

    AMultipleSelectionModel<Edge> getEdgeSelectionModel();

    void show();

    ToolBar getToolBar();

    void setToolBar(ToolBar toolBar);

    void updateSelectionModels(PhyloSplitsGraph graph, TaxaBlock taxa, Document document);

    int size();

    void setLayout(GraphLayout graphLayout);

    void setText(String name);

    void updateMenus(MenuController controller);

    DataNode getDataNode();

    void setDataNode(DataNode dataNode);
}
