/*
 *  DuplicateNodesCommand.java Copyright (C) 2020 Daniel H. Huson
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

import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Node;
import splitstree5.tools.phyloedit.PhyloEditor;

import java.util.Collection;

public class DuplicateNodesCommand extends UndoableRedoableCommand {
    final Runnable undo;
    final Runnable redo;

    public DuplicateNodesCommand(Collection<Node> nodes, PhyloEditor view) {
        super("Duplicate");
        undo = () -> {

        };
        redo = () -> {

            for (Node v : nodes) {

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
}
