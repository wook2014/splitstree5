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

import splitstree5.gui.graphtab.base.GraphTab2D;
import splitstree5.undo.UndoableRedoableCommand;

/**
 * rotate graph command
 * Daniel Huson, 1.2018
 */
public class RotateCommand extends UndoableRedoableCommand {
    private final double angle;

    private final GraphTab2D graphTab2D;

    public RotateCommand(double angle, GraphTab2D graphTab2D) {
        super("Rotate");
        this.angle = angle;
        this.graphTab2D = graphTab2D;
    }

    @Override
    public void undo() {
        graphTab2D.rotate(-angle);
    }

    @Override
    public void redo() {
        graphTab2D.rotate(angle);
    }
}
