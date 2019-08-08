/*
 *  ImportDialog.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.dialogs.importer;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.window.NotificationManager;
import jloda.util.ProgramProperties;
import splitstree5.io.imports.interfaces.IImportCharacters;
import splitstree5.io.imports.interfaces.IImportDistances;
import splitstree5.io.imports.interfaces.IImporter;
import splitstree5.io.imports.utils.DistanceSimilarityCalculator;
import splitstree5.main.MainWindow;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

/**
 * shows the import dialog
 * Daniel Huson, 1.2018
 */
public class ImportDialog {
    private final ImportService importService;
    private final ImportDialogController controller;
    private final Stage stage;

    /**
     * constructor
     *
     * @param parentMainWindow
     * @throws IOException
     */
    public ImportDialog(MainWindow parentMainWindow, String fileName) {
        final ExtendedFXMLLoader<ImportDialogController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        importService = new ImportService();

        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.sizeToScene();

        stage.setX(parentMainWindow.getStage().getX() + 50);
        stage.setY(parentMainWindow.getStage().getY() + 50);

        stage.setTitle("Import - " + ProgramProperties.getProgramName());

        controller.getDataTypeComboBox().getItems().addAll(ImporterManager.getInstance().getAllDataTypes());
        controller.getDataTypeComboBox().disableProperty().bind(importService.runningProperty());

        controller.getFileFormatComboBox().getItems().addAll(ImporterManager.getInstance().getAllFileFormats());
        controller.getFileFormatComboBox().disableProperty().bind(importService.runningProperty());

        if (fileName != null)
            controller.getFileTextField().setText(fileName);

        final ObjectProperty<FileChooser.ExtensionFilter> selectedExtensionFilter = new SimpleObjectProperty<>();
        controller.getBrowseButton().setOnAction((e) -> {
            final File previousDir = new File(ProgramProperties.get("ImportDir", ""));
            final FileChooser fileChooser = new FileChooser();
            if (previousDir.isDirectory())
                fileChooser.setInitialDirectory(previousDir);
            fileChooser.setTitle("Open Import File");
            fileChooser.getExtensionFilters().addAll(ImporterManager.getInstance().getAllExtensionFilters());
            if (selectedExtensionFilter.get() != null)
                fileChooser.setSelectedExtensionFilter(selectedExtensionFilter.get());
            // show file browser
            final File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                if (selectedFile.getParentFile().isDirectory())
                    ProgramProperties.put("ImportDir", selectedFile.getParent());
                controller.getFileTextField().setText(selectedFile.getPath());
                selectedExtensionFilter.set(fileChooser.getSelectedExtensionFilter());

            }
        });
        controller.getBrowseButton().disableProperty().bind(importService.runningProperty());


        controller.getFileTextField().textProperty().addListener((c, o, n) -> {
            final ImporterManager.DataType dataType = ImporterManager.getInstance().getDataType(n);
            controller.getDataTypeComboBox().setValue(dataType);
            String dataFormat = ImporterManager.getInstance().getFileFormat(n);
            controller.getFileFormatComboBox().setValue(dataFormat);
        });
        controller.getFileTextField().disableProperty().bind(importService.runningProperty());


        controller.getCancelButton().setOnAction((e) -> {
            if (importService.isRunning())
                importService.cancel();
            close();
        });

        controller.getImportButton().setOnAction((e) -> {
            final IImporter importer = ImporterManager.getInstance().getImporterByDataTypeAndFileFormat(controller.getDataTypeComboBox().getSelectionModel().getSelectedItem(),
                    controller.getFileFormatComboBox().getSelectionModel().getSelectedItem());
            if (importer == null)
                NotificationManager.showWarning("Can't import selected data type and file format");
            else {
                setupImporter(importer);
                importService.setup(false, parentMainWindow, importer, controller.getFileTextField().getText(), "Loading file", controller.getProgressBarPane());
                importService.restart();
            }
        });
        controller.getImportButton().disableProperty().bind(importService.runningProperty().or(
                Bindings.isNull(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty()).or(Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), "Unknown"))
                        .or(Bindings.isNull(controller.getFileFormatComboBox().getSelectionModel().selectedItemProperty())).or(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedItemProperty(), "Unknown"))));

        /*
        IMPORT SETTINGS
        */
        // characters
        controller.getCharactersLabel().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Characters)
                        .and(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedIndexProperty(), 1).not())
        );
        controller.getDistanceLabel().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Distances)
        );
        controller.getTreesLabel().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Trees)
        );
        controller.getGapChar().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Characters)
                        .and(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedIndexProperty(), 1).not())
        );
        controller.getGapInput().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Characters)
                        .and(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedIndexProperty(), 1).not())
        );
        controller.getMissingChar().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Characters)
                        .and(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedIndexProperty(), 1).not())
        );
        controller.getMissingInput().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Characters)
                        .and(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedIndexProperty(), 1).not())
        );
        controller.getMatchChar().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Characters)
                        .and(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedIndexProperty(), 1).not())
        );
        controller.getMatchInput().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Characters)
                        .and(Bindings.equal(controller.getFileFormatComboBox().getSelectionModel().selectedIndexProperty(), 1).not())
        );
        // distances
        controller.getSimilarityValues().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Distances)
        );
        controller.getSimilarityCalculation().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Distances)
        );
        controller.getSimilarityCalculation().disableProperty().bind(
                Bindings.not(controller.getSimilarityValues().selectedProperty())
        );
        EnumSet<DistanceSimilarityCalculator> distanceSimilarityCalculators =
                EnumSet.allOf(DistanceSimilarityCalculator.class);
        for (DistanceSimilarityCalculator d : distanceSimilarityCalculators)
            controller.getSimilarityCalculation().getItems().add(d.getLabel());

        // trees
        controller.getInnerNodesLabeling().visibleProperty().bind(
                Bindings.equal(controller.getDataTypeComboBox().getSelectionModel().selectedItemProperty(), ImporterManager.DataType.Trees)
        );
    }

    private Thread thread;

    public void show() {
        stage.show();
        stage.focusedProperty().addListener((c, o, n) -> {
            if (n) {
                if (thread != null)
                    thread = null;
            } else {
                thread = new Thread(() -> { // wait three seconds before showing the progress pane
                    try {
                        Thread.sleep(120000);
                        if (thread != null)
                            Platform.runLater(stage::close);
                    } catch (InterruptedException e) {
                    }
                });
                thread.setDaemon(true);
                thread.start();
            }

        });
    }

    public static void show(MainWindow other) {
        show(other, null);
    }

    /**
     * show the import dialog
     *
     * @param other
     */
    public static void show(MainWindow other, String file) {
            ImportDialog importDialog = new ImportDialog(other, file);
            importDialog.show();
    }

    public ImportDialogController getController() {
        return controller;
    }

    public void close() {
        stage.close();
    }


    /*
    add user defined setting to the importer
     */

    private void setupImporter(IImporter importer){
        if (importer instanceof IImportCharacters){
            if (controller.getMissingInput().getText().length() > 0)
                ((IImportCharacters) importer).setMissing(controller.getMissingInput().getText().charAt(0));
            if (controller.getGapInput().getText().length() > 0)
                ((IImportCharacters) importer).setGap(controller.getGapInput().getText().charAt(0));
        } else if (importer instanceof IImportDistances) {
            ((IImportDistances) importer).setSimilarities(controller.getSimilarityValues().isSelected());
            ((IImportDistances) importer).setSimilaritiesCalculation(controller.getSimilarityCalculation().getValue());
        }
    }
}
