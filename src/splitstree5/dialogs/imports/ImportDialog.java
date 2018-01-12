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

package splitstree5.dialogs.imports;

import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import splitstree5.main.ImportManager;
import splitstree5.main.MainWindow;

import java.io.File;
import java.io.IOException;

/**
 * shows the import dialog
 * Daniel Huson, 1.2018
 */
public class ImportDialog {
    private final ImportDialogController controller;
    private final Stage stage;

    public ImportDialog(MainWindow parentMainWindow) throws IOException {
        final ExtendedFXMLLoader<ImportDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        stage = new Stage();
        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.sizeToScene();
        if (parentMainWindow != null) {
            stage.setX(parentMainWindow.getStage().getX() + 50);
            stage.setY(parentMainWindow.getStage().getY() + 50);
        }

        controller.getDataTypeComboBox().getItems().addAll(ImportManager.getInstance().getAllDataTypes());
        controller.getFileFormatComboBox().getItems().addAll(ImportManager.getInstance().getAllFileFormats());

        controller.getBrowseButton().setOnAction((e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Import File");
            fileChooser.getExtensionFilters().addAll(ImportManager.getInstance().getAllExtensionFilters());
            // show file browser
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null)
                controller.getFileTextField().setText(selectedFile.getPath());
        });

        controller.getDataTypeComboBox().getItems().addAll();

        controller.getFileTextField().textProperty().addListener((c, o, n) -> {
            String dataType = ImportManager.getInstance().getDataType(n);
            controller.getDataTypeComboBox().setValue(dataType);
            String dataFormat = ImportManager.getInstance().getFileFormat(n);
            controller.getFileFormatComboBox().setValue(dataFormat);

        });

        controller.getCancelButton().setOnAction((e) -> stage.close());

        controller.getImportButton().setOnAction((e) -> {
            stage.close();
            Importer.apply(parentMainWindow, ImportManager.getInstance().getImporterByDataTypeAndFileFormat(controller.getDataTypeComboBox().getSelectionModel().getSelectedItem(),
                    controller.getFileFormatComboBox().getSelectionModel().getSelectedItem()), controller.getFileTextField().getText());
        });
        controller.getImportButton().disableProperty().bind(Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), "Unknown")
                .or(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedItemProperty(), "Unknown")));
    }

    public void show() {
        stage.show();
    }

    /**
     * show the import dialog
     *
     * @param other
     */
    public static void show(MainWindow other) {
        try {
            ImportDialog importDialog = new ImportDialog(other);
            importDialog.show();
        } catch (IOException e) {
            Basic.caught(e);
        }
    }
}