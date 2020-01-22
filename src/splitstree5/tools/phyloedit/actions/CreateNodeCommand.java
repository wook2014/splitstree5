/*
 *  CreateNodeCommand.java Copyright (C) 2020 Daniel H. Huson
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
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.tools.phyloedit.PhyloEditor;

/**
 * create a node
 * Daniel Huson, 1.2020
 */
public class CreateNodeCommand extends UndoableRedoableCommand {
    final private Runnable undo;
    final private Runnable redo;
    private int id;

    public CreateNodeCommand(Pane pane, PhyloEditor window, double x, double y) {
        super("New Node");
        final PhyloTree graph = window.getGraph();

        undo = () -> {
            if (id != 0) {
                final Node v = graph.searchNodeId(id);
                if (v != null) {
                    window.removeNode(v);
                    graph.deleteNode(v);
                }
            }
        };

        redo = () -> {
            final Node v;
            if (id == 0) {
                v = graph.newNode();
                id = v.getId();
            } else {
                v = graph.newNode(null, id);
            }
            window.addNode(pane, x, y, v);
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
}
