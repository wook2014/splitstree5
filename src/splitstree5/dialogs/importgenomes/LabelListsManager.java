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
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.ListViewSearcher;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.util.Basic;
import jloda.util.FileLineIterator;
import jloda.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    /**
     * constructor
     *
     * @param controller
     */
    public LabelListsManager(ImportGenomesController controller) {
        this.controller = controller;
        displayLabels = controller.getDisplayLabelsListView().getItems();
        controller.getStatusFlowPane().getChildren().add(label);
        label.visibleProperty().bind(Bindings.isNotEmpty(controller.getDisplayLabelsListView().getItems()));

        final ListViewSearcher<String> listViewSearcher = new ListViewSearcher<>(controller.getDisplayLabelsListView());
        final FindToolBar findToolBar = new FindToolBar(listViewSearcher);
        controller.getDisplayLabelsVBox().getChildren().add(findToolBar);
        controller.getReplaceButton().setOnAction(c -> findToolBar.setShowReplaceToolBar(!findToolBar.isShowReplaceToolBar()));

        undoManager = new UndoManager();
        listViewSearcher.setLabelSetter((listView, which, newLabel) -> {
            final String oldLabel = listView.getItems().get(which).toString();
            undoManager.doAndAdd(new UndoableRedoableCommand("Replace") {
                @Override
                public void undo() {
                    ((ListView<String>) listView).getItems().set(which, oldLabel);
                }

                @Override
                public void redo() {
                    ((ListView<String>) listView).getItems().set(which, cleanOrKeep(newLabel, oldLabel));
                }
            });
        });

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

    /**
     * update the list of labels
     *
     * @param controller
     */
    public void update(ImportGenomesController controller) {
        final ArrayList<String> oldDisplayLabels = new ArrayList<>(displayLabels);
        displayLabels.clear();

        for (String label : getLabels(Arrays.asList(Basic.split(controller.getInputTextArea().getText(), ',')), controller.getTaxaChoiceBox().getValue())) {
            final Integer pos = line2PosInDisplayLabels.get(label);
            if (pos != null && pos < oldDisplayLabels.size())
                displayLabels.add(oldDisplayLabels.get(pos));
            else
                displayLabels.add(label.replaceAll("'", "_"));
            line2PosInDisplayLabels.put(label, displayLabels.size() - 1);
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
            map.put(line, Basic.getUniqueName(name, map.values()));
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
                        displayLabels.setAll(labels.stream().map(s -> cleanOrKeep(s.replaceAll(word, ""), s)).collect(Collectors.toList()));
                        updateFrequentWordButtons();
                    }
                });
            });
            controller.getAdditionalButtonsHBox().getChildren().add(button);
        }
    }

    private static String cleanOrKeep(String str, String original) {
        final String result = str.replaceAll(",\\s*,", ",").replaceAll(",\\s*$", "").replaceAll("\\s+", " ").
                replaceAll("[(:;,.]$", "").replaceAll(",\\s*[,.:;]", ",").replace("( ", "(").replace(" )", ")").trim();
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
                if (line != null)
                    labels.add(Basic.swallowLeadingGreaterSign(line));
                else
                    labels.add(fileName);
            }
            return labels;
        } else {
            labels = new ArrayList<>();

            for (String fileName : inputFiles) {
                try (FileLineIterator it = new FileLineIterator(fileName)) {
                    labels.addAll(StreamSupport.stream(it.lines().spliterator(), false).filter(a -> a.startsWith(">")).map(Basic::swallowLeadingGreaterSign).collect(Collectors.toList()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return labels;
    }
}
