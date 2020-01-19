/*
 *  ControlBindings.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.treebased.editor;

import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import jloda.fx.undo.UndoManager;

public class ControlBindings {
    public static void setup(RootedNetworkEditor editor) {
        final RootedNetworkEditorController controller = editor.getController();
        final PhyloTreeView view = editor.getPhyloTreeView();
        final UndoManager undoManager = editor.getUndoManager();

        final Pane mainPane = controller.getMainPane();

        controller.getUndoMenuItem().setOnAction(e -> undoManager.undo());
        controller.getUndoMenuItem().disableProperty().bind(undoManager.undoableProperty().not());
        controller.getRedoMenuItem().setOnAction(e -> undoManager.redo());
        controller.getRedoMenuItem().disableProperty().bind(undoManager.redoableProperty().not());

        controller.getSelectAllMenuItem().setOnAction(e -> {
            view.getGraph().nodes().forEach((v) -> view.getNodeSelection().select(v));
        });
        controller.getSelectNoneMenuItem().setOnAction(e -> view.getNodeSelection().clearSelection());
        controller.getSelectNoneMenuItem().disableProperty().bind(Bindings.isEmpty(view.getNodeSelection().getSelectedItems()));

        mainPane.setOnMouseClicked((e) -> {
            if (e.getClickCount() == 2) {
                final Point2D location = mainPane.sceneToLocal(e.getSceneX(), e.getSceneY());
                undoManager.doAndAdd(view.createAddNodeCommand(mainPane, location.getX(), location.getY()));
            }
        });

    }
}
