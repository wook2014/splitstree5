/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.gui.treefilterview;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import jloda.fx.ExtendedFXMLLoader;
import jloda.phylo.PhyloTree;
import splitstree5.core.Document;
import splitstree5.core.algorithms.filters.TreeFilter;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.connectorview.AlgorithmPane;
import splitstree5.undo.UndoRedoManager;
import splitstree5.undo.UndoableChangeListViews2;
import splitstree5.utils.DragAndDropSupportListView2;

import java.io.IOException;
import java.util.ArrayList;

/**
 * taxon filter pane
 * Created by huson on 12/23/16.
 */
public class TreeFilterPane extends AlgorithmPane {
    private final TreeFilter treeFilter;
    private final TreeFilterPaneController controller;
    private Document document = null;
    private UndoRedoManager undoManager = new UndoRedoManager();

    private ArrayList<String> prevActiveTrees = new ArrayList<>(); // used to facilitate undo/redo, do not modify
    private ArrayList<String> prevInactiveTrees = new ArrayList<>(); // used to facilitate undo/redo, do not modify

    final SimpleBooleanProperty applicableProperty = new SimpleBooleanProperty();

    private AConnector connector;

    private boolean inSync = false;

    /**
     * constructor
     */
    public TreeFilterPane(TreeFilter treeFilter) throws IOException {
        this.treeFilter = treeFilter;
        final ExtendedFXMLLoader extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = (TreeFilterPaneController) extendedFXMLLoader.getController();
        this.getChildren().add(extendedFXMLLoader.getRoot());
        undoManager = new UndoRedoManager();
    }

    @Override
    public void setUndoManager(UndoRedoManager undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public void setConnector(AConnector connector) {
        this.connector = connector;
    }

    private boolean inUpdateSelection = false;

    /**
     * setup controller
     */
    public void setup() {
        controller.getActiveList().getItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            if (!undoManager.isPerformingUndoOrRedo()) { // for performance reasons, check this here. Is also checked in addUndoableChange, but why make a change object if we don't need it...
                final UndoableChangeListViews2<String> change = new UndoableChangeListViews2<String>("Change Active Trees", controller.getActiveList(), prevActiveTrees, controller.getInactiveList(), prevInactiveTrees);
                prevActiveTrees = change.getItemsA();
                prevInactiveTrees = change.getItemsB();
                if (!inSync)
                    undoManager.add(change);
            }

            controller.getActiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));
            controller.getInactiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));

            controller.getActiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));
            controller.getInactiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));
            DragAndDropSupportListView2.setup(controller.getActiveList(), controller.getInactiveList(), undoManager, "Change Active Trees");
            updateSelection();
        });

        controller.getInactiveList().getItems().addListener((ListChangeListener.Change<? extends String> c) -> updateSelection());

        controller.getActiveList().getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> c) -> updateSelection());

        controller.getInactiveList().getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends String> c) -> updateSelection());

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
            final ArrayList<String> list = new ArrayList<>(controller.getInactiveList().getItems());
            controller.getInactiveList().getItems().clear();
            controller.getActiveList().getItems().addAll(list);
        });
        controller.getActivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getItems()));

        controller.getActivateSelectedButton().setOnAction((e) -> {
            final ArrayList<String> list = new ArrayList<>(controller.getInactiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getItems().removeAll(list);
            controller.getActiveList().getItems().addAll(list);
        });
        controller.getActivateSelectedButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getSelectionModel().getSelectedIndices()));


        applicableProperty.bind(Bindings.isEmpty(controller.getActiveList().getItems()).not().and(undoManager.canUndoProperty()));
    }

    /**
     * updates selection
     */
    private void updateSelection() {
        if (!inUpdateSelection) {
            inUpdateSelection = true;
            try {
            } finally {
                inUpdateSelection = false;
            }
        }
    }

    public BooleanProperty applicableProperty() {
        return applicableProperty;
    }

    /**
     * sync model to controller
     */
    public void syncModel2Controller() {
        inSync = true;
        try {
            final ListView<String> activeList = controller.getActiveList();
            final ListView<String> inactiveList = controller.getInactiveList();

            activeList.getItems().clear();
            inactiveList.getItems().clear();

            if (treeFilter.getEnabledTrees().size() == 0 && treeFilter.getDisabledTrees().size() == 0) {
                for (PhyloTree phyloTree : ((TreesBlock) connector.getParent().getDataBlock()).getTrees()) {
                    activeList.getItems().add(phyloTree.getName());
                }
            } else {
                for (String name : treeFilter.getEnabledTrees()) {
                    activeList.getItems().add(name);
                }
                for (String name : treeFilter.getDisabledTrees()) {
                    inactiveList.getItems().add(name);
                }
            }
        } finally {
            inSync = false;
        }
    }

    /**
     * sync controller to model
     */
    public void syncController2Model() {
        treeFilter.getEnabledTrees().clear();
        treeFilter.getEnabledTrees().addAll(controller.getActiveList().getItems());
        treeFilter.getDisabledTrees().clear();
        treeFilter.getDisabledTrees().addAll(controller.getInactiveList().getItems());
        connector.setState(UpdateState.INVALID);
    }
}
