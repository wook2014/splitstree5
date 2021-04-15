/*
 * MoveEdgeLabelCommand.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.gui.graphtab.commands;

import javafx.geometry.Point2D;
import javafx.scene.control.Labeled;
import jloda.fx.undo.UndoableRedoableCommand;
import splitstree5.gui.graphtab.base.EdgeView2D;

/**
 * move edge label command
 */
public class MoveEdgeLabelCommand extends UndoableRedoableCommand {
    private final Labeled label;
    private final Point2D oldLocation;
    private final Point2D newLocation;
    private final EdgeView2D ev;

    public MoveEdgeLabelCommand(Labeled label, Point2D oldLocation, Point2D newLocation, EdgeView2D ev) {
        super("Move Label");
        this.label = label;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        this.ev = ev;
    }

    @Override
    public void undo() {
        label.setTranslateX(oldLocation.getX());
        label.setTranslateX(oldLocation.getY());
    }

    @Override
    public void redo() {
        label.setTranslateX(newLocation.getX());
        label.setTranslateY(newLocation.getY());
    }

    @Override
    public boolean isUndoable() {
        return ev.getEdge().getOwner() != null; // still contained in graph
    }

    @Override
    public boolean isRedoable() {
        return ev.getEdge().getOwner() != null; // still contained in graph
    }

}
