/*
 *  NodeViewContextMenu.java Copyright (C) 2020 Daniel H. Huson
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

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableChangeProperty;
import jloda.graph.Node;

import java.util.Optional;


/**
 * the node view context menu
 * Daniel Huson, 1.2020
 */
public class NodeViewContextMenu extends ContextMenu {

    public NodeViewContextMenu(final Stage owner, UndoManager undoManager, Node v, Shape shape, Label label) {
        final MenuItem setLabel = new MenuItem("Set Label");
        setLabel.setOnAction((e) -> {
            final String oldLabel = label.getText();

            // ask for label
            TextInputDialog dialog = new TextInputDialog(label.getText());
            dialog.initOwner(owner);
            dialog.setTitle("Node Label Input");
            dialog.setHeaderText("Set node label");
            dialog.setContentText("Please enter node label:");

            Optional<String> result = dialog.showAndWait();
            final String newLabel;
            if (result.isPresent()) {
                newLabel = result.get();
                undoManager.doAndAdd(new UndoableChangeProperty<>(label.textProperty(), oldLabel, newLabel));
            }
        });
        getItems().add(setLabel);
    }
}
