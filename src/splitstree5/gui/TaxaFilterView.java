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

package splitstree5.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.filters.TaxaFilter;
import splitstree5.core.misc.Taxon;
import splitstree5.core.misc.UpdateState;
import splitstree5.utils.ExtendedFXMLLoader;
import splitstree5.utils.UndoManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * dialog for filtering taxa
 * Created by huson on 12/23/16.
 */
public class TaxaFilterView {
    private final Document document;
    private final Parent root;
    private final TaxaFilterController controller;
    private final UndoManager undoManager;
    private Stage stage;

    private TaxaFilter taxaFilter;

    private ArrayList<Taxon> prevActiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify
    private ArrayList<Taxon> prevInactiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify

    /**
     * constructor
     *
     * @param taxaFilter
     */
    public TaxaFilterView(Document document, TaxaFilter taxaFilter) throws IOException {
        this.document = document;
        this.taxaFilter = taxaFilter;
        final ExtendedFXMLLoader<TaxaFilterController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        undoManager = new UndoManager();
        setupController();

        // if parent state changes, need to updat this:
        taxaFilter.getParent().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != UpdateState.VALID && newValue == UpdateState.VALID)
                syncModel2Controller();
            undoManager.clear();
        });
    }

    /**
     * setup controller
     */
    private void setupController() {

        controller.getActiveList().getItems().addListener((ListChangeListener<Taxon>) c -> {
            if (!undoManager.isPerformingUndoOrRedo()) { // avoid bounce
                final UndoableChangeOfActiveTaxa change = new UndoableChangeOfActiveTaxa(controller.getActiveList(), prevActiveTaxa, controller.getInactiveList(), prevInactiveTaxa);
                prevActiveTaxa = change.getActiveTaxa();
                prevInactiveTaxa = change.getInactiveTaxa();
                undoManager.addChangeable(change);
            }
        });

        controller.getUndoMenuItem().setOnAction((e) -> {
            undoManager.undo();
        });
        controller.getUndoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canUndoProperty()));
        controller.getRedoMenuItem().setOnAction((e) -> {
            undoManager.redo();
        });
        controller.getRedoMenuItem().disableProperty().bind(new SimpleBooleanProperty(false).isEqualTo(undoManager.canRedoProperty()));

        controller.getActiveList().disableProperty().bind(taxaFilter.disableProperty());
        controller.getInactiveList().disableProperty().bind(taxaFilter.disableProperty());


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
            controller.getActiveList().getItems().addAll(controller.getInactiveList().getItems());
            controller.getInactiveList().getItems().clear();
        });
        controller.getActivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getItems()));


        controller.getActivateSelectedButton().setOnAction((e) -> {
            controller.getActiveList().getItems().addAll(controller.getInactiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getItems().removeAll(controller.getInactiveList().getSelectionModel().getSelectedItems());
        });
        controller.getActivateSelectedButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getSelectionModel().getSelectedIndices()));

        controller.getCloseMenuItem().setOnAction((e) -> TaxaFilterView.this.stage.close());

        controller.getCancelButton().setOnAction((e) -> {
            syncModel2Controller();
            undoManager.clear();
        });

        controller.getApplyButton().setOnAction((e) -> {
            System.err.println("APPLY!");
            syncController2Model();
        });
        controller.getApplyButton().disableProperty().bind(Bindings.isEmpty(controller.getActiveList().getItems()).or(document.updatingProperty()).or(undoManager.canUndoProperty().not()));
    }

    /**
     * show this view
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Taxa Filter - SplitsTree5");
        stage.setScene(new Scene(root, 450, 450));
        stage.show();
        syncModel2Controller();
    }

    /**
     * sync model to controller
     */
    private void syncModel2Controller() {
        final ListView<Taxon> activeList = controller.getActiveList();
        final ListView<Taxon> inactiveList = controller.getInactiveList();

        activeList.getItems().setAll(taxaFilter.getEnabledData());
        inactiveList.getItems().setAll(taxaFilter.getDisabledData());
    }

    /**
     * sync controller to model
     */
    private void syncController2Model() {
        final ListView<Taxon> activeList = controller.getActiveList();
        final ListView<Taxon> inactiveList = controller.getInactiveList();

        taxaFilter.getEnabledData().setAll(activeList.getItems());
        taxaFilter.getDisabledData().setAll(inactiveList.getItems());
        taxaFilter.setState(UpdateState.INVALID);
    }
}
