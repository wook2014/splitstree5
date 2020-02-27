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

package splitstree5.dialogs.genome;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import jloda.fx.find.FindToolBar;
import jloda.fx.find.ListViewSearcher;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.util.Basic;
import jloda.util.FileLineIterator;

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
    private final Label label = new Label();

    private final ObservableList<String> displayLabels;
    private final Map<String, Integer> line2PosInDisplayLabels = new HashMap<>();

    public LabelListsManager(CompareGenomesController controller) {
        displayLabels = controller.getDisplayLabelsListView().getItems();
        controller.getStatusFlowPane().getChildren().add(label);
        label.visibleProperty().bind(Bindings.isNotEmpty(controller.getDisplayLabelsListView().getItems()));

        final ListViewSearcher<String> listViewSearcher = new ListViewSearcher<>(controller.getDisplayLabelsListView());
        final FindToolBar findToolBar = new FindToolBar(listViewSearcher);
        controller.getDisplayLabelsVBox().getChildren().add(findToolBar);
        findToolBar.setShowReplaceToolBar(true);

        final UndoManager undoManager = new UndoManager();
        listViewSearcher.setLabelSetter((listView, which, newLabel) -> {
            final String oldLabel = listView.getItems().get(which).toString();
            undoManager.doAndAdd(new UndoableRedoableCommand("Replace") {
                @Override
                public void undo() {
                    ((ListView<String>) listView).getItems().set(which, oldLabel);
                }

                @Override
                public void redo() {
                    ((ListView<String>) listView).getItems().set(which, newLabel);
                }
            });
        });

        controller.getLabelsUndoButton().setOnAction(c -> undoManager.undo());
        controller.getLabelsUndoButton().disableProperty().bind(undoManager.undoableProperty().not());

        controller.getLabelsRedoButton().setOnAction(c -> undoManager.redo());
        controller.getLabelsRedoButton().disableProperty().bind(undoManager.redoableProperty().not());

        controller.getDisplayLabelsListView().setCellFactory(TextFieldListCell.forListView());
        controller.getDisplayLabelsListView().setOnEditCommit(t -> {
            controller.getDisplayLabelsListView().getItems().set(t.getIndex(), t.getNewValue());
        });

        controller.getDisplayLabelsVBox().setOnMouseClicked(c -> findToolBar.setShowReplaceToolBar(true));
    }

    /**
     * update the list of labels
     *
     * @param controller
     */
    public void update(CompareGenomesController controller) {
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
    }

    public Map<String, String> computeLine2Label() {
        final Map<String, String> map = new HashMap<>();
        for (String line : line2PosInDisplayLabels.keySet()) {
            final Integer pos = line2PosInDisplayLabels.get(line);
            if (pos != null && pos < displayLabels.size())
                map.put(line, displayLabels.get(pos));
            else
                map.put(line, line.replaceAll("'", "_"));
        }
        return map;
    }

    private static List<String> getLabels(List<String> inputFiles, CompareGenomesDialog.TaxonIdentification taxonIdentification) {
        final List<String> labels;
        if (taxonIdentification == CompareGenomesDialog.TaxonIdentification.PerFileUsingFileName) {
            labels = inputFiles.stream().map(s -> Basic.replaceFileSuffix(Basic.getFileNameWithoutPath(s), "")).collect(Collectors.toList());
        } else if (taxonIdentification == CompareGenomesDialog.TaxonIdentification.PerFile) {
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
