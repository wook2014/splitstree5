/*
 * SaveBeforeClosingDialog.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import splitstree5.core.Document;
import splitstree5.main.MainWindow;

import java.util.Optional;

public class SaveBeforeClosingDialog {
    public enum Result {save, close, cancel}

    /**
     * ask whether to save before closing
     *
     * @return true if doesn't need saving or saved, false else
     */
    public static Result apply(MainWindow mainWindow) {
        final Document document = mainWindow.getDocument();
        if (!document.isDirty()) {
            return Result.close;
        } else {
            mainWindow.getStage().toFront();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(mainWindow.getStage());
            alert.setTitle("Save File Dialog");
            alert.setHeaderText("This document has unsaved changes");
            alert.setContentText("Save changes?");
            ButtonType buttonTypeYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
            ButtonType buttonTypeNo = new ButtonType("No", ButtonBar.ButtonData.NO);
            ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeCancel);

            final Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == buttonTypeYes) {
                    return SaveChangesDialog.apply(mainWindow) ? Result.save : Result.close;
                } else return result.get() == buttonTypeNo ? Result.close : Result.cancel;
            }
            return Result.cancel; // canceled
        }
    }
}
