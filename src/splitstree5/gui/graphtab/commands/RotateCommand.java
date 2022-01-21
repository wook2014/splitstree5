/*
 * RotateCommand.java Copyright (C) 2022 Daniel H. Huson
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

import jloda.fx.undo.UndoableRedoableCommand;
import splitstree5.gui.graphtab.base.Graph2DTab;

/**
 * rotate graph command
 * Daniel Huson, 1.2018
 */
public class RotateCommand extends UndoableRedoableCommand {
    private final double angle;

    private final Graph2DTab graph2DTab;

    public RotateCommand(double angle, Graph2DTab graph2DTab) {
        super("Rotate");
        this.angle = angle;
        this.graph2DTab = graph2DTab;
    }

    @Override
    public void undo() {
        graph2DTab.rotate(-angle);
    }

    @Override
    public void redo() {
        graph2DTab.rotate(angle);
    }
}
