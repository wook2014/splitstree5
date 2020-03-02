/*
 *  CompareGenomesDialog.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.dialogs.genome;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jloda.fx.util.AllFileFilter;
import jloda.fx.util.BasicFX;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.FastAFileFilter;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import splitstree5.main.Version;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * genome analysis dialog
 * Daniel Huson, 2.2020
 */
public class CompareGenomesDialog {
    public enum Sequence {DNA, Protein}

    public enum TaxonIdentification {PerFastARecord, PerFile, PerFileUsingFileName}

    private final Stage stage;

    /**
     * constructor
     *
     * @param initialParent
     */
    public CompareGenomesDialog(Stage initialParent) {
        final ExtendedFXMLLoader<CompareGenomesController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        final CompareGenomesController controller = extendedFXMLLoader.getController();

        stage = new Stage();
        stage.setTitle("Compare Genomes - " + Version.NAME);
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));

        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setX(initialParent.getX() + 100);
        stage.setY(initialParent.getY() + 100);

        controlBindings(stage, controller);
    }

    public void show() {
        stage.show();
    }

    private void controlBindings(Stage stage, CompareGenomesController controller) {
        final BooleanProperty isRunning = new SimpleBooleanProperty(false);

        controller.getInputBrowseButton().setOnAction(c -> {
            final List<File> files = getFiles(stage);
            if (files != null) {
                if (controller.getInputTextArea().getText().trim().length() > 0 && !controller.getInputTextArea().getText().trim().endsWith("'")) {
                    controller.getInputTextArea().setText(controller.getInputTextArea().getText().trim() + ",\n" + Basic.toString(files, ",\n"));
                } else
                    controller.getInputTextArea().setText(Basic.toString(files, ",\n"));
            }
        });
        controller.getInputTextArea().textProperty().addListener((c, o, n) -> {
            final String firstLine = Basic.getFirstLine(n);
            if (firstLine.length() > 0) {
                final File inputFile = new File(firstLine);
                if (inputFile.getParentFile().exists()) {
                    controller.getOutputFileTextField().setText(createOutputName(inputFile.getParentFile()));
                }
            }
        });

        controller.getOutputBrowseButton().setOnAction(c -> {
            final File outputFile = getOutputFile(stage, controller.getOutputFileTextField().getText());
            if (outputFile != null) {
                final String outputFileName;
                if (Basic.getFileSuffix(outputFile.getName()).length() == 0)
                    outputFileName = outputFile.getPath() + ".stree5";
                else
                    outputFileName = outputFile.getPath();

                controller.getOutputFileTextField().setText(outputFileName);
            }
        });

        controller.getOutputFileTextField().textProperty().addListener((c, o, n) -> ProgramProperties.put("GenomesOutputFile", n));


        controller.getSequenceTypeChoiceBox().getItems().addAll(Sequence.values());
        controller.getSequenceTypeChoiceBox().setValue(Sequence.DNA);

        controller.getMinLengthTextField().setText(ProgramProperties.get("MinLength", "10000"));
        controller.getMinLengthTextField().textProperty().addListener((c, o, n) -> ProgramProperties.put("MinLength", n));
        BasicFX.ensureAcceptsIntegersOnly(controller.getMinLengthTextField());

        controller.getTaxaChoiceBox().getItems().addAll(TaxonIdentification.values());
        controller.getTaxaChoiceBox().setValue(TaxonIdentification.PerFastARecord);

        controller.getCancelButton().setOnAction(c -> stage.close());
        controller.getCancelButton().disableProperty().bind(isRunning);

        controller.getDisplayLabelsListView().setItems(FXCollections.observableArrayList());
        final LabelListsManager labelListsManager = new LabelListsManager(controller);
        controller.getTaxonLabelsTab().selectedProperty().addListener((c, o, n) -> {
            if (n)
                labelListsManager.update(controller);
        });


        controller.getApplyButton().setOnAction(c -> {
            final GenomeInputManager genomeInputManager = new GenomeInputManager(Arrays.asList(Basic.split(controller.getInputTextArea().getText(), ',')),
                    controller.getTaxaChoiceBox().getValue(), labelListsManager.computeLine2Label(), Basic.parseInt(controller.getMinLengthTextField().getText()));

            try {
                genomeInputManager.saveData(controller.getOutputFileTextField().getText(), controller.getStatusFlowPane());
            } catch (IOException e) {
                NotificationManager.showError("Save failed: " + e);
            }

        });
        controller.getApplyButton().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());
    }

    /**
     * create a default output file name
     *
     * @param parentFile
     * @return name
     */
    private String createOutputName(File parentFile) {
        File file = new File(parentFile, "genomes.stree5");
        int count = 0;
        while (file.exists()) {
            file = new File(parentFile, "genomes-" + (++count) + ".stree5");
        }
        return file.getPath();
    }

    public List<File> getFiles(Stage owner) {
        final File previousDir = new File(ProgramProperties.get("GenomesDir", ""));
        final FileChooser fileChooser = new FileChooser();
        if (previousDir.isDirectory())
            fileChooser.setInitialDirectory(previousDir);
        fileChooser.setTitle("Genome Files");
        fileChooser.getExtensionFilters().addAll(FastAFileFilter.getInstance(), AllFileFilter.getInstance());

        return fileChooser.showOpenMultipleDialog(owner);
    }

    public File getOutputFile(Stage owner, String defaultName) {
        final FileChooser fileChooser = new FileChooser();
        if (defaultName.length() > 0) {
            final File previousDir = new File(defaultName);
            if (previousDir.isDirectory())
                fileChooser.setInitialDirectory(previousDir);
            fileChooser.setInitialFileName(Basic.getFileNameWithoutPath(defaultName));
        }
        fileChooser.setTitle("Output File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Files", "*.stree5", "*.nxs", "*.nex"));

        return fileChooser.showSaveDialog(owner);
    }

    public Stage getStage() {
        return stage;
    }
}
