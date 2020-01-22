/*
 *  TranslateCommand.java Copyright (C) 2020 Daniel H. Huson
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

import javafx.geometry.Point2D;
import javafx.scene.Node;
import jloda.fx.undo.UndoableRedoableCommand;

/**
 * translate a node command
 * Daniel Huson, 1.2020
 */
public class TranslateCommand extends UndoableRedoableCommand {
    private final Runnable undo;
    private final Runnable redo;

    public TranslateCommand(String label, Node node, Point2D delta) {
        super(label);
        undo = () -> {
            node.setTranslateX(node.getTranslateX() - delta.getX());
            node.setTranslateY(node.getTranslateY() - delta.getY());
        };
        redo = () -> {
            node.setTranslateX(node.getTranslateX() + delta.getX());
            node.setTranslateY(node.getTranslateY() + delta.getY());
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
