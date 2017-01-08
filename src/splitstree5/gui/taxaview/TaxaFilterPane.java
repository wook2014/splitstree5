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

package splitstree5.gui.taxaview;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import splitstree5.core.dag.UpdateState;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.misc.Taxon;
import splitstree5.gui.connectorview.AlgorithmPane;
import splitstree5.undo.UndoManager;
import splitstree5.undo.UndoableChangeListViews2;
import splitstree5.utils.DragAndDropSupportListView2;
import splitstree5.utils.ExtendedFXMLLoader;

import java.io.IOException;
import java.util.ArrayList;

/**
 * taxon filter pane
 * Created by huson on 12/23/16.
 */
public class TaxaFilterPane extends AlgorithmPane {
    private final TaxaFilter taxaFilter;
    private final TaxaFilterPaneController controller;
    private UndoManager undoManager = new UndoManager();


    private ArrayList<Taxon> prevActiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify
    private ArrayList<Taxon> prevInactiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify

    final SimpleBooleanProperty applicableProperty = new SimpleBooleanProperty();

    /**
     * constructor
     *
     * @param taxaFilter
     */
    public TaxaFilterPane(TaxaFilter taxaFilter) throws IOException {
        this.taxaFilter = taxaFilter;
        final ExtendedFXMLLoader extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = (TaxaFilterPaneController) extendedFXMLLoader.getController();
        this.getChildren().add(extendedFXMLLoader.getRoot());
        undoManager = new UndoManager();
    }

    @Override
    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    /**
     * setup controller
     */
    public void setup() {
        controller.getActiveList().getItems().addListener((ListChangeListener<Taxon>) c -> {
            if (!undoManager.isPerformingUndoOrRedo()) { // for performance reasons, check this here. Is also checked in addUndoableChange, but why make a change object if we don't need it...
                final UndoableChangeListViews2<Taxon> change = new UndoableChangeListViews2<>("Change Active Taxa", controller.getActiveList(), prevActiveTaxa, controller.getInactiveList(), prevInactiveTaxa);
                final boolean isInitialLoad = (prevActiveTaxa.isEmpty() && prevInactiveTaxa.isEmpty()); // don't want user to undo original load of taxa
                prevActiveTaxa = change.getItemsA();
                prevInactiveTaxa = change.getItemsB();
                if (!isInitialLoad)
                    undoManager.addUndoableChange(change);
            }

            controller.getActiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));
            controller.getInactiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));

            controller.getActiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));
            controller.getInactiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));

            DragAndDropSupportListView2.setup(controller.getActiveList(), controller.getInactiveList(), undoManager, "Change Active Taxa");
        });

        controller.getInactivateAllButton().setOnAction((e) -> {
            controller.getInactiveList().getItems().addAll(controller.getActiveList().getItems());
            controller.getActiveList().getItems().clear();
        });
        controller.getInactivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getActiveList().getItems()));


        controller.getInactivateSelectedButton().setOnAction((e) -> {
            controller.getInactiveList().getItems().addAll(controller.getActiveList().getSelectionModel().getSelectedItems());
            controller.getActiveList().getItems().removeAll(controller.getActiveList().getSelectionModel().getSelectedItems());
        });
        controller.getInactivateSelectedButton().disableProperty().bind(Bindings.isEmpty(controller.getActiveList().getSelectionModel().getSelectedIndices()));

        controller.getActivateAllButton().setOnAction((e) -> {
            final ArrayList<Taxon> list = new ArrayList<>(controller.getInactiveList().getItems());
            controller.getInactiveList().getItems().clear();
            controller.getActiveList().getItems().addAll(list);
        });
        controller.getActivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getItems()));

        controller.getActivateSelectedButton().setOnAction((e) -> {
            final ArrayList<Taxon> list = new ArrayList<>(controller.getInactiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getItems().removeAll(list);
            controller.getActiveList().getItems().addAll(list);
        });
        controller.getActivateSelectedButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getSelectionModel().getSelectedIndices()));


        if (false) {
            // this would allow us to edit taxon labels, but we need to figure out how to write this back to the original taxa
            controller.getActiveList().setEditable(true);
            controller.getActiveList().setCellFactory(TaxonListCell.forListView());
            controller.getActiveList().setOnEditCommit((e) -> {
                // make sure new value is unique:
                for (int i = 0; i < controller.getActiveList().getItems().size(); i++) {
                    if (i != e.getIndex() && controller.getActiveList().getItems().get(i).equals(e.getNewValue())) {
                        System.err.println("Error: name exists");
                        //undoManager.undoAndForget();
                        return;
                    }
                    controller.getActiveList().getItems().set(e.getIndex(), e.getNewValue());
                }
            });
        }

        applicableProperty.bind(Bindings.isEmpty(controller.getActiveList().getItems()).not().and(undoManager.canUndoProperty()));
    }

    public BooleanProperty applicableProperty() {
        return applicableProperty;
    }

    /**
     * sync model to controller
     */
    public void syncModel2Controller() {
        final ListView<Taxon> activeList = controller.getActiveList();
        final ListView<Taxon> inactiveList = controller.getInactiveList();

        activeList.getItems().setAll(taxaFilter.getEnabledTaxa());
        inactiveList.getItems().setAll(taxaFilter.getDisabledTaxa());
    }

    /**
     * sync controller to model
     */
    public void syncController2Model() {
        final ListView<Taxon> activeList = controller.getActiveList();
        final ListView<Taxon> inactiveList = controller.getInactiveList();

        taxaFilter.getEnabledTaxa().clear();
        taxaFilter.getEnabledTaxa().addAll(activeList.getItems());
        taxaFilter.getDisabledTaxa().clear();
        taxaFilter.getDisabledTaxa().addAll(inactiveList.getItems());
        taxaFilter.setState(UpdateState.INVALID);
    }
}
