/*
 * ExportDialog.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.dialogs.exporter;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.FileUtils;
import jloda.util.ProgramProperties;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;

import java.io.File;

/**
 * shows the save dialog
 * Daniel Huson, 1.2018
 */
public class ExportDialog {
    private final ExportService exportService;
    private final ExportDialogController controller;
    private final Stage stage;
    private final BooleanProperty fileDialogShowing = new SimpleBooleanProperty();

    /**
     * constructor
     *
	 */
    public ExportDialog(Stage owner, TaxaBlock workingTaxa, DataBlock dataBlock) {
        final ExtendedFXMLLoader<ExportDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        exportService = new ExportService();

        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.sizeToScene();
        if (owner != null) {
            stage.setX(owner.getX() + 50);
            stage.setY(owner.getY() + 50);
        }
        stage.setTitle("Export - " + ProgramProperties.getProgramName());

        controller.getProgressBar().setVisible(false);

        controller.getDataTypeComboBox().getItems().setAll(Basic.getShortName(dataBlock.getClass()));
        controller.getDataTypeComboBox().setValue(Basic.getShortName(dataBlock.getClass()));

        controller.getFileFormatComboBox().getItems().addAll(ExportManager.getInstance().getExporterNames(dataBlock));

        {
            final String propertyTag = "ExportFileFormat" + dataBlock.getBlockName();
            final String defaultFileFormat = ProgramProperties.get(propertyTag, "");
            if (!defaultFileFormat.isBlank() && controller.getFileFormatComboBox().getItems().contains(defaultFileFormat))
                controller.getFileFormatComboBox().setValue(defaultFileFormat);
            else if (controller.getFileFormatComboBox().getItems().size() > 0)
                controller.getFileFormatComboBox().setValue(controller.getFileFormatComboBox().getItems().get(0));

            controller.getFileFormatComboBox().disableProperty().bind(controller.getProgressBar().visibleProperty());
            controller.getFileFormatComboBox().valueProperty().addListener((c, o, n) -> ProgramProperties.put(propertyTag, n));
        }

        controller.getCancelButton().setOnAction((e) -> close());

        controller.getExportButton().setOnAction((e) -> {
            fileDialogShowing.set(true);
            try {
                final File previousDir = new File(ProgramProperties.get("ExportDir", ""));
                final FileChooser fileChooser = new FileChooser();
                if (previousDir.isDirectory())
                    fileChooser.setInitialDirectory(previousDir);
                if (!workingTaxa.getDocument().getFileName().isBlank())
					fileChooser.setInitialFileName(FileUtils.replaceFileSuffix(FileUtils.getFileNameWithoutPath(workingTaxa.getDocument().getFileName()), ""));
                fileChooser.setTitle("Export File");
                File selectedFile = fileChooser.showSaveDialog(stage);
                if (selectedFile != null) {
                    selectedFile = ExportManager.getInstance().ensureFileSuffix(selectedFile, controller.getFileFormatComboBox().getValue());
                    if (selectedFile.getParentFile().isDirectory())
                        ProgramProperties.put("ExportDir", selectedFile.getParent());
                    exportService.setup(selectedFile.getPath(), workingTaxa, dataBlock, controller.getFileFormatComboBox().getValue(), this);
                    exportService.restart();
                    exportService.runningProperty().addListener((c, o, n) -> {
                        if (o && !n)
                            stage.close();
                    });
                }
            } finally {
                fileDialogShowing.set(false);
            }
        });
        controller.getExportButton().disableProperty().bind(controller.getProgressBar().visibleProperty().or(
                Bindings.isNull(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty()).or(Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), "Unknown"))
                        .or(Bindings.isNull(controller.getFileFormatComboBox().getSelectionModel().selectedItemProperty())).or(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedItemProperty(), "Unknown"))));


        stage.focusedProperty().addListener((c, o, n) -> {
            if (!n && !exportService.isRunning() && !fileDialogShowing.get())
                stage.close();
        });


    }

    public void show() {
        stage.show();
    }

    /**
     * show the import dialog
     *
	 */
    public static void show(Stage other, TaxaBlock taxonBlock, DataBlock dataBlock) {
        ExportDialog exportDialog = new ExportDialog(other, taxonBlock, dataBlock);
        exportDialog.show();
    }

    public ExportDialogController getController() {
        return controller;
    }

    public void close() {
        stage.close();
    }
}
