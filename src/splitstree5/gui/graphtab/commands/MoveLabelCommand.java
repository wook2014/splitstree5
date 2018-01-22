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

package splitstree5.gui.graphtab.commands;

import javafx.geometry.Point2D;
import javafx.scene.control.Labeled;
import splitstree5.gui.graphtab.base.ANodeView;
import splitstree5.undo.UndoableRedoableCommand;

/**
 * move node label command
 */
public class MoveLabelCommand extends UndoableRedoableCommand {
    private final Labeled label;
    private final Point2D oldLocation;
    private final Point2D newLocation;
    private final ANodeView nv;

    public MoveLabelCommand(Labeled label, Point2D oldLocation, Point2D newLocation, ANodeView nv) {
        super("Move Label");
        this.label = label;
        this.oldLocation = oldLocation;
        this.newLocation = newLocation;
        this.nv = nv;
    }

    @Override
    public void undo() {
        label.setLayoutX(oldLocation.getX());
        label.setLayoutY(oldLocation.getY());
    }

    @Override
    public void redo() {
        label.setLayoutX(newLocation.getX());
        label.setLayoutY(newLocation.getY());
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
