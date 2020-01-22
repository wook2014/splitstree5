/*
 *  DeleteNodesCommand.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.tools.phyloedit.actions;

import javafx.scene.layout.Pane;
import jloda.fx.control.ItemSelectionModel;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.graph.NodeEdge;
import jloda.phylo.PhyloTree;
import jloda.util.Pair;
import splitstree5.tools.phyloedit.PhyloEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * delete nodes command
 * Daniel Huson, 1.2020
 */
public class DeleteNodesCommand extends UndoableRedoableCommand {
    private final Runnable undo;
    private final Runnable redo;

    private final Map<Integer, OldNodeData> node2oldNodeData = new HashMap<>();

    public DeleteNodesCommand(Pane pane, PhyloEditor editor) {
        super("Delete");

        final PhyloTree graph = editor.getGraph();
        final ItemSelectionModel<Node> selectionModel = editor.getNodeSelection();

        final Collection<Integer> nodeIds = selectionModel.getSelectedItems().stream().map(NodeEdge::getId).collect(Collectors.toList());

        undo = () -> {
            for (OldNodeData data : node2oldNodeData.values()) {
                Node v = graph.newNode(null, data.id);
                editor.addNode(pane, data.x, data.y, v);
                selectionModel.clearSelection(v);
            }
            final Map<Integer, Node> id2node = new HashMap<>();
            for (Node v : graph.nodes()) {
                id2node.put(v.getId(), v);
            }
            for (Integer id : node2oldNodeData.keySet()) {
                final OldNodeData data = node2oldNodeData.get(id);
                final Node v = id2node.get(id);
                for (Pair<Integer, Integer> pair : data.parentNodeEdges) {
                    final Node u = id2node.get(pair.getFirst());
                    final Edge e = graph.newEdge(u, v, null, pair.getSecond());
                    editor.addEdge(e);
                }
                for (Pair<Integer, Integer> pair : data.childNodeEdges) {
                    final Node w = id2node.get(pair.getFirst());
                    final Edge e = graph.newEdge(v, w, null, pair.getSecond());
                    editor.addEdge(e);
                }
            }
        };

        redo = () -> {
            node2oldNodeData.clear();
            for (int id : nodeIds) {
                final Node v = graph.searchNodeId(id);
                selectionModel.clearSelection(v);
                final OldNodeData data = new OldNodeData();
                data.id = v.getId();
                data.label = graph.getLabel(v);
                data.x = editor.getNode2shapeAndLabel().get(v).getFirst().getTranslateX();
                data.y = editor.getNode2shapeAndLabel().get(v).getFirst().getTranslateY();

                data.parentNodeEdges = new ArrayList<>();
                for (Edge w : v.inEdges()) {
                    data.parentNodeEdges.add(new Pair<>(w.getSource().getId(), w.getId()));
                }
                data.childNodeEdges = new ArrayList<>();
                for (Edge w : v.outEdges()) {
                    data.parentNodeEdges.add(new Pair<>(w.getTarget().getId(), w.getId()));
                }
                node2oldNodeData.put(v.getId(), data);
                editor.removeNode(v);
                graph.deleteNode(v);
            }
        };
    }

    @Override
    public void undo() {
        undo.run();

    }

    @Override
    public void redo() {
        redo.run();
    }

    static class OldNodeData {
        int id;
        String label;
        double x;
        double y;

        Collection<Pair<Integer, Integer>> parentNodeEdges;
        Collection<Pair<Integer, Integer>> childNodeEdges;
    }
}
