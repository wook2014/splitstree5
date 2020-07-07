/*
 *  LabelListsManager.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.dialogs.importgenomes;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldListCell;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.ListViewSearcher;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.window.NotificationManager;
import jloda.util.Basic;
import jloda.util.FastAFileIterator;
import jloda.util.IFastAIterator;
import jloda.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * manages the list of labels
 * Daniel Huson, 2.2020
 */
public class LabelListsManager {
    private final ImportGenomesController controller;
    private final Label label = new Label();

    private final UndoManager undoManager;

    private final ObservableList<String> displayLabels;
    private final Map<String, Integer> line2PosInDisplayLabels = new HashMap<>();

    private final BooleanProperty applicable = new SimpleBooleanProperty();

    /**
     * constructor
     *
     * @param controller
     */
    public LabelListsManager(ImportGenomesController controller) {
        this.controller = controller;
        displayLabels = controller.getDisplayLabelsListView().getItems();

        applicable.bind(Bindings.isNotEmpty(displayLabels));

        controller.getStatusFlowPane().getChildren().add(label);
        label.visibleProperty().bind(Bindings.isNotEmpty(controller.getDisplayLabelsListView().getItems()));

        final ListViewSearcher<String> listViewSearcher = new ListViewSearcher<>(controller.getDisplayLabelsListView());
        final FindToolBar findToolBar = new FindToolBar(null, listViewSearcher);

        controller.getDisplayLabelsVBox().getChildren().add(findToolBar);
        controller.getReplaceButton().setOnAction(c -> findToolBar.setShowReplaceToolBar(!findToolBar.isShowReplaceToolBar()));

        undoManager = new UndoManager();

        // override replace buttons to implement undo/redo
        {
            final EventHandler<ActionEvent> replaceAllAction = findToolBar.getController().getReplaceAllButton().getOnAction();
            findToolBar.getController().getReplaceAllButton().setOnAction(c -> {
                final ArrayList<String> original = new ArrayList<>(displayLabels);
                replaceAllAction.handle(null);
                final ArrayList<String> replaced = new ArrayList<>(displayLabels);
                undoManager.add(UndoableRedoableCommand.create("replace all", () -> displayLabels.setAll(original), () -> displayLabels.setAll(replaced)));
            });
        }
        {
            final EventHandler<ActionEvent> replaceAction = findToolBar.getController().getReplaceButton().getOnAction();
            findToolBar.getController().getReplaceButton().setOnAction(c -> {
                final ArrayList<String> original = new ArrayList<>(displayLabels);
                replaceAction.handle(null);
                final ArrayList<String> replaced = new ArrayList<>(displayLabels);
                undoManager.add(UndoableRedoableCommand.create("replace", () -> displayLabels.setAll(original), () -> displayLabels.setAll(replaced)));
            });
        }


        Button removeFirstButton = new Button("- first word");
        removeFirstButton.setOnAction(c -> {
            undoManager.doAndAdd(new UndoableRedoableCommand("Remove first word") {
                final List<String> labels = new ArrayList<>(controller.getDisplayLabelsListView().getItems());

                @Override
                public void undo() {
                    displayLabels.setAll(labels);
                    updateFrequentWordButtons();
                }

                @Override
                public void redo() {
                    displayLabels.setAll(labels.stream().map(s -> cleanOrKeep(s.replaceAll("^\\S+\\s*", ""), s)).collect(Collectors.toList()));
                    updateFrequentWordButtons();
                }
            });
        });
        removeFirstButton.setTooltip(new Tooltip("Remove the first word from each item"));
        removeFirstButton.disableProperty().bind(Bindings.isEmpty(displayLabels));

        Button removeLastButton = new Button("- last word");
        removeLastButton.setOnAction(c -> {
            undoManager.doAndAdd(new UndoableRedoableCommand("Remove last word") {
                final List<String> labels = new ArrayList<>(displayLabels);

                @Override
                public void undo() {
                    displayLabels.setAll(labels);
                    updateFrequentWordButtons();
                }

                @Override
                public void redo() {
                    displayLabels.setAll(labels.stream().map(s -> cleanOrKeep(s.replaceAll("\\s*\\S+$", ""), s)).collect(Collectors.toList()));
                    updateFrequentWordButtons();
                }
            });
        });
        removeLastButton.setTooltip(new Tooltip("Remove the last word from each item"));
        removeLastButton.disableProperty().bind(Bindings.isEmpty(displayLabels));

        controller.getAdditionalButtonsHBox().getChildren().addAll(new Separator(Orientation.VERTICAL), removeFirstButton, removeLastButton, new Separator(Orientation.VERTICAL));

        controller.getLabelsUndoButton().setOnAction(c -> undoManager.undo());
        controller.getLabelsUndoButton().disableProperty().bind(undoManager.undoableProperty().not());

        controller.getLabelsRedoButton().setOnAction(c -> undoManager.redo());
        controller.getLabelsRedoButton().disableProperty().bind(undoManager.redoableProperty().not());

        controller.getDisplayLabelsListView().setCellFactory(TextFieldListCell.forListView());
        controller.getDisplayLabelsListView().setOnEditCommit(t -> {
            controller.getDisplayLabelsListView().getItems().set(t.getIndex(), t.getNewValue());
        });
    }

    public void clear() {
        displayLabels.clear();
        line2PosInDisplayLabels.clear();
        undoManager.clear();
    }

    /**
     * update the list of labels
     *
     * @param controller
     */
    public void update(ImportGenomesController controller) {
        final ArrayList<String> oldDisplayLabels = new ArrayList<>(displayLabels);
        displayLabels.clear();

        final List<String> labels = getLabels(Arrays.asList(Basic.split(controller.getInputTextArea().getText(), ',')), controller.getTaxaChoiceBox().getValue());
        if (labels != null) {
            for (String label : labels) {
                final Integer pos = line2PosInDisplayLabels.get(label);
                if (pos != null && pos < oldDisplayLabels.size())
                    displayLabels.add(oldDisplayLabels.get(pos));
                else
                    displayLabels.add(label.replaceAll("'", "_"));
                line2PosInDisplayLabels.put(label, displayLabels.size() - 1);
            }
        }
        label.setText("Taxa: " + displayLabels.size());
        updateFrequentWordButtons();
    }

    public Map<String, String> computeLine2Label() {
        final Map<String, String> map = new HashMap<>();
        for (String line : line2PosInDisplayLabels.keySet()) {
            final Integer pos = line2PosInDisplayLabels.get(line);
            final String name;
            if (pos != null && pos < displayLabels.size())
                name = displayLabels.get(pos);
            else
                name = line.replaceAll("'", "_");
            map.put(line, name);
        }
        return map;
    }

    private void updateFrequentWordButtons() {
        controller.getAdditionalButtonsHBox().getChildren().removeIf(node -> (node.getUserData() != null && node.getUserData().equals("frequent")));

        final Map<String, Integer> word2count = new HashMap<>();
        displayLabels.forEach(s -> {
            for (String a : s.split("\\s+")) {
                if (a.length() > 0)
                    word2count.put(a, word2count.computeIfAbsent(a, z -> 0) + 1);
            }
        });

        final List<Pair<String, Integer>> list = Basic.reverse(word2count.keySet().stream().map(a -> new Pair<>(a, word2count.get(a))).sorted(Comparator.comparingInt(Pair::getSecond)).collect(Collectors.toList()));

        for (int i = 0; i < Math.min(7, list.size()); i++) {
            if (list.get(i).getSecond() < 5)
                break;
            final String word = list.get(i).getFirst();
            final Button button = new Button("- " + word);
            button.setUserData("frequent");
            button.setTooltip(new Tooltip("Remove all occurrences of the word '" + word + "'"));
            button.setOnAction(c -> {
                undoManager.doAndAdd(new UndoableRedoableCommand("Delete word") {
                    final List<String> labels = new ArrayList<>(displayLabels);

                    @Override
                    public void undo() {
                        displayLabels.setAll(labels);
                        updateFrequentWordButtons();
                    }

                    @Override
                    public void redo() {
                        displayLabels.setAll(labels.stream().map(s -> cleanOrKeep(s.replace(word, ""), s)).collect(Collectors.toList()));
                        updateFrequentWordButtons();
                    }
                });
            });
            controller.getAdditionalButtonsHBox().getChildren().add(button);
        }
    }

    private static String cleanOrKeep(String str, String original) {
        final String result = str.replaceAll("(,;:)\\s*[,:;]", "$1").replaceAll("[,;:(]\\s*$", "").replaceAll("[|\\s]+", " ")
                .replaceAll("(\\(\\[\\{) ", "$1").replaceAll("(\\)]}) ", "$1").trim();
        return result.length() > 0 ? result : original;
    }

    private static List<String> getLabels(List<String> inputFiles, ImportGenomesDialog.TaxonIdentification taxonIdentification) {
        final List<String> labels;
        if (taxonIdentification == ImportGenomesDialog.TaxonIdentification.PerFileUsingFileName) {
            labels = inputFiles.stream().map(s -> Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(s), "")).collect(Collectors.toList());
        } else if (taxonIdentification == ImportGenomesDialog.TaxonIdentification.PerFile) {
            labels = new ArrayList<>();

            for (String fileName : inputFiles) {
                final String line = Basic.getFirstLineFromFile(new File(fileName));
                if (line != null) {
                    labels.add(line.startsWith(">") || line.startsWith("@") ? line.substring(1).trim() : line);
                } else
                    labels.add(fileName);
            }
            return labels;
        } else { // perFastARecord
            labels = new ArrayList<>();

            for (String fileName : inputFiles) {
                try (IFastAIterator it = FastAFileIterator.getFastAOrFastQAsFastAIterator(fileName)) {
                    while (it.hasNext()) {
                        final String line = it.next().getFirst();
                        labels.add(line.startsWith(">") || line.startsWith("@") ? line.substring(1).trim() : line);
                        if (labels.size() > 1000)
                            throw new IOException("Too many taxon labels (>1000)");

                    }
                } catch (IOException e) {
                    NotificationManager.showError("Processing failed: " + e);
                    return null;
                }
            }
        }
        return labels;
    }

    public boolean isApplicable() {
        return applicable.get();
    }

    public BooleanProperty applicableProperty() {
        return applicable;
    }
}
