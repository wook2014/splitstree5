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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import splitstree5.gui.utils.Alert;
import splitstree5.io.imports.interfaces.IImporter;
import splitstree5.main.ImporterManager;
import splitstree5.main.MainWindow;

import java.io.File;
import java.io.IOException;

/**
 * shows the import dialog
 * Daniel Huson, 1.2018
 */
public class ImportDialog {
    private final ImporterService importerService;
    private final ImportDialogController controller;
    private final Stage stage;

    public ImportDialog(MainWindow parentMainWindow) throws IOException {
        final ExtendedFXMLLoader<ImportDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        importerService = new ImporterService();

        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIcons());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.sizeToScene();
        if (parentMainWindow != null) {
            stage.setX(parentMainWindow.getStage().getX() + 50);
            stage.setY(parentMainWindow.getStage().getY() + 50);
        }
        stage.setTitle("Import - " + ProgramProperties.getProgramName());

        controller.getProgressBar().setVisible(false);

        controller.getDataTypeComboBox().getItems().addAll(ImporterManager.getInstance().getAllDataTypes());
        controller.getDataTypeComboBox().disableProperty().bind(controller.getProgressBar().visibleProperty());

        controller.getFileFormatComboBox().getItems().addAll(ImporterManager.getInstance().getAllFileFormats());
        controller.getFileFormatComboBox().disableProperty().bind(controller.getProgressBar().visibleProperty());

        final ObjectProperty<FileChooser.ExtensionFilter> selectedExtensionFilter = new SimpleObjectProperty<>();
        controller.getBrowseButton().setOnAction((e) -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Import File");
            fileChooser.getExtensionFilters().addAll(ImporterManager.getInstance().getAllExtensionFilters());
            if (selectedExtensionFilter.get() != null)
                fileChooser.setSelectedExtensionFilter(selectedExtensionFilter.get());
            // show file browser
            final File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                controller.getFileTextField().setText(selectedFile.getPath());
                selectedExtensionFilter.set(fileChooser.getSelectedExtensionFilter());

            }
        });
        controller.getBrowseButton().disableProperty().bind(controller.getProgressBar().visibleProperty());


        controller.getFileTextField().textProperty().addListener((c, o, n) -> {
            String dataType = ImporterManager.getInstance().getDataType(n);
            controller.getDataTypeComboBox().setValue(dataType);
            String dataFormat = ImporterManager.getInstance().getFileFormat(n);
            controller.getFileFormatComboBox().setValue(dataFormat);
        });
        controller.getFileTextField().disableProperty().bind(controller.getProgressBar().visibleProperty());


        controller.getCancelButton().setOnAction((e) -> close());

        controller.getImportButton().setOnAction((e) -> {
            final IImporter importer = ImporterManager.getInstance().getImporterByDataTypeAndFileFormat(controller.getDataTypeComboBox().getSelectionModel().getSelectedItem(),
                    controller.getFileFormatComboBox().getSelectionModel().getSelectedItem());
            if (importer == null)
                new Alert("Can't import selected data type and file format");
            importerService.setup(parentMainWindow, importer, controller.getFileTextField().getText(), ImportDialog.this);
            importerService.restart();
        });
        controller.getImportButton().disableProperty().bind(controller.getProgressBar().visibleProperty().or(
                Bindings.isNull(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty()).or(Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), "Unknown"))
                        .or(Bindings.isNull(controller.getFileFormatComboBox().getSelectionModel().selectedItemProperty())).or(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedItemProperty(), "Unknown"))));
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

    public ImportDialogController getController() {
        return controller;
    }

    public void close() {
        stage.close();
    }
}
