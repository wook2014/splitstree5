/*
 *  CompareGenomesDialog.java Copyright (C) 2021 Daniel H. Huson
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

package splitstree5.dialogs.analyzegenomes;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.control.RichTextLabel;
import jloda.fx.util.*;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.FileUtils;
import jloda.util.ProgramProperties;
import jloda.util.StringUtils;
import splitstree5.main.Version;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * import genomes dialog
 * Daniel Huson, 2.2020
 */
public class AnalyzeGenomesDialog {
    public enum Sequence {DNA, Protein}

    public enum TaxonIdentification {PerFastARecord, PerFastARecordUsingFileName, PerFile, PerFileUsingFileName}

    private final Stage stage;

    private final ObjectProperty<AccessReferenceDatabase> referencesDatabase = new SimpleObjectProperty<>(null);
    private final ObservableList<Integer> referenceIds = FXCollections.observableArrayList();
    private final ObservableList<Map.Entry<Integer, Double>> references = FXCollections.observableArrayList();

    private final BooleanProperty running = new SimpleBooleanProperty(false);

    /**
     * constructor
     *
     * @param initialParent
     */
    public AnalyzeGenomesDialog(Stage initialParent) {
        final ExtendedFXMLLoader<AnalyzeGenomesController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        final AnalyzeGenomesController controller = extendedFXMLLoader.getController();

        stage = new Stage();
        stage.setTitle("Analyze Genomes - " + Version.NAME);
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.setX(initialParent.getX() + 100);
        stage.setY(initialParent.getY() + 100);

        controlBindings(stage, controller);
    }

    public void show() {
        stage.show();
    }

    private void controlBindings(Stage stage, AnalyzeGenomesController controller) {
        stage.setOnCloseRequest(c -> {
            if (isRunning())
                c.consume();
        });

        controller.getInputBrowseButton().setOnAction(c -> {
            final List<File> files = getFiles(stage);
            if (files != null) {
                if (controller.getInputTextArea().getText().trim().length() > 0 && !controller.getInputTextArea().getText().trim().endsWith("'")) {
					controller.getInputTextArea().setText(controller.getInputTextArea().getText().trim() + ",\n" + StringUtils.toString(files, ",\n"));
                } else
					controller.getInputTextArea().setText(StringUtils.toString(files, ",\n"));
            }
        });
        controller.getInputTextArea().textProperty().addListener((c, o, n) -> {
			final String firstLine = StringUtils.getFirstLine(n);
            if (firstLine.length() > 0) {
                final File inputFile = new File(firstLine);
                if (inputFile.getParentFile().exists()) {
                    controller.getOutputFileTextField().setText(createOutputName(inputFile));
                }
            }
            clearReferences(controller);
        });

        controller.getOutputBrowseButton().setOnAction(c -> {
            final File outputFile = getOutputFile(stage, controller.getOutputFileTextField().getText());
            if (outputFile != null) {
                final String outputFileName;
				if (FileUtils.getFileSuffix(outputFile.getName()).length() == 0)
					outputFileName = outputFile.getPath() + ".stree5";
				else
					outputFileName = outputFile.getPath();

                controller.getOutputFileTextField().setText(outputFileName);
            }
        });

        controller.getClearInputButton().setOnAction(c -> controller.getInputTextArea().setText(""));
        controller.getClearInputButton().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty());

        controller.getOutputFileTextField().textProperty().addListener((c, o, n) -> ProgramProperties.put("GenomesOutputFile", n));

        controller.getSequenceTypeChoiceBox().getItems().addAll(Sequence.values());
        controller.getSequenceTypeChoiceBox().setValue(Sequence.DNA);

        controller.getMinLengthTextField().setText(ProgramProperties.get("MinLength", "10000"));
        controller.getMinLengthTextField().textProperty().addListener((c, o, n) -> ProgramProperties.put("MinLength", n));
        BasicFX.ensureAcceptsIntegersOnly(controller.getMinLengthTextField());

        controller.getTaxaChoiceBox().getItems().addAll(TaxonIdentification.values());
        controller.getTaxaChoiceBox().setValue(TaxonIdentification.PerFastARecord);

        controller.getStoreOnlyReferencesCheckBox().setSelected(ProgramProperties.get("StoreOnlyReferences", true));
        controller.getStoreOnlyReferencesCheckBox().selectedProperty().addListener((c, o, n) -> ProgramProperties.put("StoreOnlyReferences", n));

        controller.getCancelButton().setOnAction(c -> stage.close());
        controller.getCancelButton().disableProperty().bind(running);

        controller.getSupportedHTMLTextArea().setText("Supported HTML tags:\n" + RichTextLabel.getSupportedHTMLTags());
        controller.getSupportedHTMLTextArea().visibleProperty().bind(controller.getHtmlInfoButton().selectedProperty());
        controller.getSupportedHTMLTextArea().prefRowCountProperty().bind(new When(controller.getHtmlInfoButton().selectedProperty()).then(4).otherwise(0));
        controller.getSupportedHTMLTextArea().prefHeightProperty().bind(new When(controller.getHtmlInfoButton().selectedProperty()).then(Region.USE_COMPUTED_SIZE).otherwise(0));

        controller.getDisplayLabelsListView().setItems(FXCollections.observableArrayList());
        final LabelListsManager labelListsManager = new LabelListsManager(controller);
        controller.getTaxonLabelsTab().selectedProperty().addListener((c, o, n) -> {
            if (n)
                labelListsManager.update(controller);
        });
        controller.getTaxonLabelsTab().disableProperty().bind(running);

        controller.getInputTextArea().textProperty().addListener((c, o, n) -> {
            if (n.length() == 0) {
                labelListsManager.clear();
            }
        });

        controller.getFilesTab().disableProperty().bind(running);

        final RichTextLabel richTextLabel = new RichTextLabel();
        controller.getStatusFlowPane().getChildren().add(richTextLabel);

        controller.getDisplayLabelsListView().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            richTextLabel.setText(n != null ? n : "");
        });

        controller.getApplyButton().setOnAction(c -> {
			final GenomesAnalyzer genomesAnalyzer = new GenomesAnalyzer(Arrays.asList(StringUtils.split(controller.getInputTextArea().getText(), ',')),
					controller.getTaxaChoiceBox().getValue(), labelListsManager.computeLine2Label(), Basic.parseInt(controller.getMinLengthTextField().getText()),
					controller.getStoreOnlyReferencesCheckBox().isSelected());

            if (referencesDatabase.get() != null && referenceIds.size() > 0) {
                String fileCacheDirectory = ProgramProperties.get("fileCacheDirectory", "");

				if (fileCacheDirectory.equals("") || !FileUtils.isDirectory(fileCacheDirectory)) {
					Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
					alert.setTitle("Confirmation Dialog - " + ProgramProperties.getProgramName());
					alert.setHeaderText("SplitsTree5 will download and cache reference genomes");
					alert.setContentText("Do you want to proceed and choose a cache directory?");

					final Optional<ButtonType> result = alert.showAndWait();
					if (result.isPresent() && result.get() != ButtonType.OK) {
						NotificationManager.showWarning("User canceled");
						return;
					}

                    final File dir = chooseCacheDirectory(stage, referencesDatabase.get().getDbFile().getParentFile());
                    if (dir == null || !dir.canWrite())
                        return;
                    else
                        ProgramProperties.put("fileCacheDirectory", dir.getPath());
                }
            }

            genomesAnalyzer.saveData(getReferencesDatabase(), referenceIds, controller.getOutputFileTextField().getText(), controller.getStatusFlowPane(), running::set);
        });
        controller.getApplyButton().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty().or(running).or(labelListsManager.applicableProperty().not())
                .or(controller.getMainTabPane().getSelectionModel().selectedItemProperty().isEqualTo(controller.getRelatedTab()).and(Bindings.isEmpty(referenceIds))));

        setupReferenceDatabaseTab(stage, controller, labelListsManager, referencesDatabase, referenceIds, runningProperty());
    }

    private void setupReferenceDatabaseTab(Stage stage, AnalyzeGenomesController controller, LabelListsManager labelListsManager, ObjectProperty<AccessReferenceDatabase> database, ObservableList<Integer> referenceIds, BooleanProperty running) {
        final DoubleProperty threshold = new SimpleDoubleProperty(0.1);
        final IntegerProperty maxCount = new SimpleIntegerProperty(0);

        final Label floatingLabel = new Label();
        controller.getAddedReferencesLabel().setText(String.valueOf(referenceIds.size()));
        referenceIds.addListener((InvalidationListener) e -> {
            controller.getAddedReferencesLabel().setText(String.valueOf(referenceIds.size()));
            floatingLabel.setText("Added: " + referenceIds.size());
            if (referenceIds.size() == 0)
                controller.getStatusFlowPane().getChildren().remove(floatingLabel);
            else if (!controller.getStatusFlowPane().getChildren().contains(floatingLabel))
                controller.getStatusFlowPane().getChildren().add(floatingLabel);
        });

        controller.getFoundReferencesLabel().setText("");
        references.addListener((InvalidationListener) e -> controller.getFoundReferencesLabel().setText("Found: " + references.size()));

        controller.getReferencesDatabaseButton().setOnAction(e -> {
            final File file = getReferenceDatabaseFile(stage);
            if (file != null)
                controller.getReferencesDatabaseTextField().setText(file.getPath());
        });
        controller.getReferencesDatabaseButton().disableProperty().bind(running);

        controller.getReferencesDatabaseTextField().textProperty().addListener(e -> {
            final String fileName = controller.getReferencesDatabaseTextField().getText();
            if (AccessReferenceDatabase.isDatabaseFile(fileName)) {
                database.set(null);
                try {
                    database.set(new AccessReferenceDatabase(fileName, 2 * ProgramExecutorService.getNumberOfCoresToUse()));
                    ProgramProperties.put("ReferenceDatabaseFile", fileName);
                } catch (IOException | SQLException ex) {
                    NotificationManager.showError("Open reference database failed: " + ex.getMessage());
                }
            }
        });
        controller.getReferencesDatabaseTextField().setText(ProgramProperties.get("ReferenceDatabaseFile", ""));
        controller.getReferencesDatabaseTextField().disableProperty().bind(running);

        final XYChart.Series<Double, Integer> thresholdLine = createThresholdLine(threshold, references);

        controller.getFindReferencesButton().setOnAction(e -> {
            final AService<Collection<Map.Entry<Integer, Double>>> service = new AService<>(controller.getStatusFlowPane());
            service.setCallable(new TaskWithProgressListener<>() {
                @Override
                public Collection<Map.Entry<Integer, Double>> call() throws Exception {
					final GenomesAnalyzer genomesAnalyzer = new GenomesAnalyzer(Arrays.asList(StringUtils.split(controller.getInputTextArea().getText(), ',')),
							controller.getTaxaChoiceBox().getValue(), labelListsManager.computeLine2Label(), Basic.parseInt(controller.getMinLengthTextField().getText()),
							controller.getStoreOnlyReferencesCheckBox().isSelected());

                    final ArrayList<byte[]> queries = new ArrayList<>();
                    for (GenomesAnalyzer.InputRecord record : genomesAnalyzer.iterable(getProgressListener())) {
                        queries.add(record.getSequence());
                    }
                    return database.get().findSimilar(service.getProgressListener(), Basic.parseDouble(controller.getMaxDistToSearchTextField().getText()), controller.getIncludeStrainsCB().isSelected(), queries, true);
                }
            });
            service.runningProperty().addListener((c, o, n) -> running.set(n));
            clearReferences(controller);
            service.setOnSucceeded(z -> {
                references.setAll(service.getValue());
                final ObservableList<XYChart.Data<Double, Integer>> data = FXCollections.observableArrayList();
                int runningSum = 0;
                for (Map.Entry<Integer, Double> pair : references) {
                    data.add(new XYChart.Data<>(pair.getValue(), runningSum++));
                }
                controller.getMashDistancesChart().getData().add(new XYChart.Series<>(data));
                controller.getMashDistancesChart().getData().add(thresholdLine);
                final double dist = controller.getMaxDistanceSlider().getValue();
                controller.getMaxDistanceSlider().setValue(0);
                Platform.runLater(() -> controller.getMaxDistanceSlider().setValue(dist));
            });
            service.start();
        });

        controller.getFindReferencesButton().disableProperty().bind(controller.getInputTextArea().textProperty().isEmpty().or(running).or(labelListsManager.applicableProperty().not()).or(Bindings.isNull(referencesDatabaseProperty())));

        controller.getMaxDistanceSlider().disableProperty().bind(controller.getFindReferencesButton().disabledProperty().or(running));
        threshold.bindBidirectional(controller.getMaxDistanceSlider().valueProperty());

        final BooleanProperty inThesholdUpdate = new SimpleBooleanProperty(false);
        threshold.addListener((c, o, n) -> {
            if (!inThesholdUpdate.get()) {
                inThesholdUpdate.set(true);
                try {
                    controller.getMaxToAddTextField().setText(String.valueOf(references.stream().filter(p -> p.getValue() <= n.doubleValue()).count()));
                } finally {
                    inThesholdUpdate.set(false);
                }
            }
        });

        controller.getMaxToAddTextField().textProperty().addListener((c, o, n) -> {
            final int max = Math.min(references.size(), Math.max(0, Basic.parseInt(n)));
            maxCount.set(max);
        });

        maxCount.addListener((c, o, n) -> {
            if (!inThesholdUpdate.get()) {
                inThesholdUpdate.set(true);
                try {
                    OptionalDouble thresholdValue = references.stream().limit(n.intValue()).mapToDouble(Map.Entry::getValue).max();
                    if (thresholdValue.isPresent())
                        threshold.setValue(thresholdValue.getAsDouble());
                } finally {
                    inThesholdUpdate.set(false);
                }
            }
        });

        controller.getRemoveAllReferencesButton().setOnAction(e -> referenceIds.clear());
        controller.getRemoveAllReferencesButton().disableProperty().bind(Bindings.isEmpty(referenceIds).or(running));

        controller.getMaxToAddTextField().disableProperty().bind(controller.getFindReferencesButton().disabledProperty().or(running));

        controller.getAddReferencesButton().setOnAction(e -> {
            referenceIds.setAll(references.stream().limit(maxCount.intValue()).filter(p -> p.getValue() <= threshold.get()).map(Map.Entry::getKey).collect(Collectors.toList()));
        });
        controller.getAddReferencesButton().disableProperty().bind(Bindings.isEmpty(references).or(database.isNull()).or(running));
        controller.getMashDistancesChart().disableProperty().bind(controller.getAddReferencesButton().disabledProperty());

        BasicFX.ensureAcceptsDoubleOnly(controller.getMaxDistToSearchTextField());
        controller.getMaxDistToSearchTextField().textProperty().addListener((c, o, n) -> {
            if (Basic.isDouble(n) && Basic.parseDouble(n) > 0.0 && Basic.parseDouble(n) < 1)
                controller.getMaxDistanceSlider().setMax(Basic.parseDouble(n) + 0.01);
        });
        controller.getMaxDistToSearchTextField().disableProperty().bind(controller.getFindReferencesButton().disableProperty());

        controller.getCacheButton().setOnAction(e -> {
            final File dir = chooseCacheDirectory(stage, new File(ProgramProperties.get("fileCacheDirectory", database.get().getDbFile().getParent())));
            if (dir != null && dir.canRead())
                ProgramProperties.put("fileCacheDirectory", dir.getPath());
        });
        controller.getCacheButton().disableProperty().bind(controller.getFindReferencesButton().disabledProperty());
    }

    public void clearReferences(AnalyzeGenomesController controller) {
        references.clear();
        referenceIds.clear();
        controller.getMashDistancesChart().getData().clear();
    }

    private static XYChart.Series<Double, Integer> createThresholdLine(DoubleProperty threshold, ObservableList<Map.Entry<Integer, Double>> references) {
        final ObservableList<XYChart.Data<Double, Integer>> data = FXCollections.observableArrayList();
        final XYChart.Series<Double, Integer> series = new XYChart.Series<>(data);
        final InvalidationListener listener = e -> {
            data.clear();
            data.add(new XYChart.Data<>(threshold.get(), 0));
            data.add(new XYChart.Data<>(threshold.get(), references.size()));
        };
        threshold.addListener(listener);
        references.addListener(listener);
        return series;
    }

    /**
     * create a default output file name
     *
     * @param inputFile
     * @return name
     */
    private static String createOutputName(File inputFile) {
		File file = FileUtils.replaceFileSuffix(inputFile, ".stree5");
		int count = 0;
        while (file.exists()) {
			file = FileUtils.replaceFileSuffix(inputFile, "-" + (++count) + ".stree5");
        }
        return file.getPath();
    }

    private static List<File> getFiles(Stage owner) {
        final File previousDir = new File(ProgramProperties.get("GenomesDir", ""));
        final FileChooser fileChooser = new FileChooser();
        if (previousDir.isDirectory())
            fileChooser.setInitialDirectory(previousDir);
        fileChooser.setTitle("Genome Files");
        fileChooser.getExtensionFilters().addAll(FastAFileFilter.getInstance(), FastQFileFilter.getInstance(), AllFileFilter.getInstance());
        List<File> result = fileChooser.showOpenMultipleDialog(owner);
        if (result != null && result.size() > 0)
            ProgramProperties.put("GenomesDir", result.get(0).getParent());
        return result;
    }

    private static File getOutputFile(Stage owner, String defaultName) {
        final FileChooser fileChooser = new FileChooser();
        if (defaultName.length() > 0) {
            final File previousDir = new File(defaultName);
            if (previousDir.isDirectory())
                fileChooser.setInitialDirectory(previousDir);
			fileChooser.setInitialFileName(FileUtils.getFileNameWithoutPath(defaultName));
        }
        fileChooser.setTitle("Output File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Files", "*.stree5", "*.nxs", "*.nex"));

        return fileChooser.showSaveDialog(owner);
    }

    private static File getReferenceDatabaseFile(Stage owner) {
        final String previous = ProgramProperties.get("ReferencesDatabase", "");
        final FileChooser fileChooser = new FileChooser();
        if (previous.length() > 0) {
			fileChooser.setInitialDirectory((new File(previous)).getParentFile());
			fileChooser.setInitialFileName(FileUtils.getFileNameWithoutPath(previous));
        }
        fileChooser.setTitle("SplitsTree5 References Database");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 References Database", "*.db", "*.st5db"));

        final File result = fileChooser.showOpenDialog(owner);
        if (result != null)
            ProgramProperties.put("ReferencesDatabase", result.getPath());
        return result;
    }

    public static File chooseCacheDirectory(Stage stage, File defaultDir) {
        final DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Choose file cache directory");
        fileChooser.setInitialDirectory(defaultDir);

        final File dir = fileChooser.showDialog(stage);
        if (dir == null || !dir.canWrite())
            return null;
        else
            return dir;
    }

    public Stage getStage() {
        return stage;
    }

    public AccessReferenceDatabase getReferencesDatabase() {
        return referencesDatabase.get();
    }

    public ObjectProperty<AccessReferenceDatabase> referencesDatabaseProperty() {
        return referencesDatabase;
    }

    public ObservableList<Integer> getReferenceIds() {
        return referenceIds;
    }

    public boolean isRunning() {
        return running.get();
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }
}
