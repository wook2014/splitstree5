/*
 *  Copyright (C) 2018 Daniel H. Huson
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
package splitstree5.gui.graphtab.commands;

import javafx.scene.control.Label;
import jloda.util.ProgramProperties;
import splitstree5.gui.graphtab.base.ANodeView;
import splitstree5.undo.UndoableRedoableCommand;

/**
 * Change node label
 * Daniel Huson, 1.2018
 */
public class ChangeNodeLabelCommand extends UndoableRedoableCommand {
    private final String oldText;
    private final String newText;
    private final ANodeView nv;

    public ChangeNodeLabelCommand(ANodeView nv, String newText) {
        super("Change Label");
        this.nv = nv;
        this.oldText = (nv.getLabel() != null ? nv.getLabel().getText() : null);
        this.newText = newText;
    }

    @Override
    public void undo() {
        if (oldText == null)
            nv.setLabel(null);
        else
            nv.getLabel().setText(oldText);
    }

    @Override
    public void redo() {
        if (oldText == null) {
            Label label = new Label(newText);
            label.setFont(ProgramProperties.getDefaultFont());
            nv.setLabel(label);
        } else
            nv.getLabel().setText(newText);
    }

    @Override
    public boolean isUndoable() {
        return nv.getNode().getOwner() != null; // still contained in graph
    }

    @Override
    public boolean isRedoable() {
        return nv.getNode().getOwner() != null; // still contained in graph
    }

}
