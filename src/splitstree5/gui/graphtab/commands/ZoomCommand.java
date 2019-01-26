/*
 *  Copyright (C) 2019 Daniel H. Huson
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

import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.undo.UndoableRedoableCommand;

/**
 * rotate graph command
 */
public class ZoomCommand extends UndoableRedoableCommand {
    private final double zoomFactorX;
    private final double zoomFactorY;

    private final Graph2DTab graphTab;

    public ZoomCommand(double zoomFactorX, double zoomFactorY, Graph2DTab graphTab) {
        super("Zoom");
        this.zoomFactorX = zoomFactorX;
        this.zoomFactorY = zoomFactorY;
        this.graphTab = graphTab;
    }

    @Override
    public void undo() {
        graphTab.scale(1 / zoomFactorX, 1 / zoomFactorY);
    }

    @Override
    public void redo() {
        graphTab.scale(zoomFactorX, zoomFactorY);
    }
}
