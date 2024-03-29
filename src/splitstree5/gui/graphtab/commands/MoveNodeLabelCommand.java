/*
 * MoveNodeLabelCommand.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.graphtab.commands;

import javafx.geometry.Point2D;
import jloda.fx.control.RichTextLabel;
import jloda.fx.undo.UndoableRedoableCommand;
import splitstree5.gui.graphtab.base.NodeView2D;

/**
 * move node label command
 */
public class MoveNodeLabelCommand extends UndoableRedoableCommand {
    private final RichTextLabel label;
    private final Point2D oldLocation;
    private final Point2D newLocation;
    private final NodeView2D nv;

    public MoveNodeLabelCommand(RichTextLabel label, Point2D oldLocation, Point2D newLocation, NodeView2D nv) {
        super("Move Label");
        this.label = label;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        this.nv = nv;
    }

    @Override
    public void undo() {
        label.setTranslateY(oldLocation.getX());
        label.setTranslateY(oldLocation.getY());
    }

    @Override
    public void redo() {
        label.setTranslateX(newLocation.getX());
        label.setTranslateY(newLocation.getY());
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
