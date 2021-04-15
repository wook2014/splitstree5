/*
 * RootByOutGroupPane.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.algorithmtab.rootbyoutgroup;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.ListView;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableChangeListViews2;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Triplet;
import splitstree5.core.Document;
import splitstree5.core.algorithms.trees2trees.RootByOutGroupAlgorithm;
import splitstree5.core.misc.Taxon;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.algorithmtab.AlgorithmPane;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.utils.DragAndDropSupportListView2;

import java.util.*;

/**
 * tree rerooting pane
 * Daniel Huson, 5.2018
 */
public class RootByOutGroupPane extends AlgorithmPane {
    private final RootByOutGroupAlgorithm rootByOutGroup;
    private final RootByOutGroupPaneController controller;
    private Document document;
    private UndoManager undoManager;

    private ArrayList<Taxon> prevActiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify
    private ArrayList<Taxon> prevInactiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify

    final SimpleBooleanProperty applicableProperty = new SimpleBooleanProperty();

    private final Map<Graph2DTab, Triplet<ListChangeListener<Taxon>, InvalidationListener, Boolean>> graphTab2SelectionListeners = new HashMap<>();

    private Connector connector;

    private boolean inSync = false;

    private ListChangeListener<Taxon> paneTaxonSelectionChangeListener;
    private ListChangeListener<Taxon> documentTaxonSelectionChangeListener;
    private boolean inSelection = false;

    /**
     * constructor
     *
     * @param rootByOutGroup
     */
    public RootByOutGroupPane(RootByOutGroupAlgorithm rootByOutGroup) {
        this.rootByOutGroup = rootByOutGroup;
        final ExtendedFXMLLoader extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = (RootByOutGroupPaneController) extendedFXMLLoader.getController();
        this.getChildren().add(extendedFXMLLoader.getRoot());
        undoManager = new UndoManager();
    }

    @Override
    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public void setDocument(Document document) {
        this.document = document;

        // setup listeners that use document:
        paneTaxonSelectionChangeListener = c -> {
            try {
                inSelection = true;
                while (c.next()) {
                    if (c.getAddedSize() > 0) {
                        document.getTaxaSelectionModel().selectItems(c.getAddedSubList());
                    }
                    if (c.getRemovedSize() > 0) {
                        document.getTaxaSelectionModel().clearSelection(c.getRemoved());
                    }
                }
            } finally {
                inSelection = false;
            }
        };
        documentTaxonSelectionChangeListener = c -> {
            if (!inSelection) {
                inSelection = true;
                try {
                    while (c.next()) {
                        if (c.getAddedSize() > 0) {
                            select(c.getAddedSubList(), true);

                        }
                        if (c.getRemovedSize() > 0) {
                            select(c.getRemoved(), false);
                        }
                    }
                } finally {
                    inSelection = false;
                }
            }
        };
    }

    @Override
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    /**
     * setup controller
     */
    public void setup() {
        controller.getActiveList().getItems().addListener((ListChangeListener.Change<? extends Taxon> c) -> {
            if (!undoManager.isPerformingUndoOrRedo()) { // for performance reasons, check this here. Is also checked in addUndoableChange, but why make a change object if we don't need it...
                final UndoableChangeListViews2<Taxon> change = new UndoableChangeListViews2<>("Change Outgroup", controller.getActiveList(), prevActiveTaxa, controller.getInactiveList(), prevInactiveTaxa);
                prevActiveTaxa = change.getItemsA();
                prevInactiveTaxa = change.getItemsB();
                if (!inSync)
                    undoManager.add(change);
            }

            controller.getActiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));
            controller.getInactiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));

            controller.getActiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));
            controller.getInactiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));
            DragAndDropSupportListView2.setup(controller.getActiveList(), controller.getInactiveList(), undoManager, "Change Outgroup");
            //select(document.getTaxaSelectionModel().getSelectedItems(),true);
        });

        controller.getActiveList().getSelectionModel().getSelectedItems().addListener(new WeakListChangeListener<>(paneTaxonSelectionChangeListener));
        controller.getInactiveList().getSelectionModel().getSelectedItems().addListener(new WeakListChangeListener<>(paneTaxonSelectionChangeListener));

        controller.getActiveList().getItems().addListener((InvalidationListener) (c) -> select(document.getTaxaSelectionModel().getSelectedItems(), true));

        document.getTaxaSelectionModel().getSelectedItems().addListener(new WeakListChangeListener<>(documentTaxonSelectionChangeListener));

        controller.getInactivateAllButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getActiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getItems().addAll(controller.getActiveList().getItems());
            controller.getActiveList().getItems().clear();
            select(selected, true);
        });
        controller.getInactivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getActiveList().getItems()));

        controller.getInactivateSelectedButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getActiveList().getSelectionModel().getSelectedItems());
            controller.getActiveList().getSelectionModel().clearSelection();
            controller.getActiveList().getItems().removeAll(selected);
            controller.getInactiveList().getItems().addAll(selected);
            select(selected, true);
        });
        controller.getInactivateSelectedButton().disableProperty().bind(Bindings.isEmpty(controller.getActiveList().getSelectionModel().getSelectedIndices()));

        controller.getActivateAllButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getInactiveList().getSelectionModel().getSelectedItems());
            final ArrayList<Taxon> inactivated = new ArrayList<>(controller.getInactiveList().getItems());
            controller.getInactiveList().getSelectionModel().clearSelection();
            controller.getInactiveList().getItems().clear();
            controller.getActiveList().getItems().addAll(inactivated);
            select(selected, true);
        });
        controller.getActivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getItems()));

        controller.getActivateSelectedButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getInactiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getSelectionModel().clearSelection();
            controller.getInactiveList().getItems().removeAll(selected);
            controller.getActiveList().getItems().addAll(selected);
            select(selected, true);
        });
        controller.getActivateSelectedButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getSelectionModel().getSelectedIndices()));


        applicableProperty.bind(Bindings.isEmpty(controller.getActiveList().getItems()).not().and(undoManager.undoableProperty()));
    }

    /**
     * set or deselect taxa
     *
     * @param taxa
     * @param select
     */
    private void select(Collection<? extends Taxon> taxa, boolean select) {
        if (select) {
            boolean firstActive = true;
            boolean firstInactive = true;
            for (Taxon taxon : taxa) {
                if (controller.getActiveList().getItems().contains(taxon) && !controller.getActiveList().getSelectionModel().getSelectedItems().contains(taxon)) {
                    controller.getActiveList().getSelectionModel().select(taxon);
                    if (firstActive) {
                        controller.getActiveList().scrollTo(taxon);
                        firstActive = false;
                    }
                } else if (controller.getInactiveList().getItems().contains(taxon) && !controller.getInactiveList().getSelectionModel().getSelectedItems().contains(taxon)) {
                    controller.getInactiveList().getSelectionModel().select(taxon);
                    if (firstInactive) {
                        controller.getInactiveList().scrollTo(taxon);
                        firstActive = false;
                    }
                }
            }
        } else {
            {
                Set<Taxon> selected = new HashSet<>(controller.getActiveList().getSelectionModel().getSelectedItems());
                if (selected.size() > 0) {
                    selected.removeAll(taxa);
                    controller.getActiveList().getSelectionModel().clearSelection();
                    for (Taxon taxon : selected) {
                        if (controller.getActiveList().getItems().contains(taxon))
                            controller.getActiveList().getSelectionModel().select(taxon);
                    }
                }
            }
            {
                Set<Taxon> selected = new HashSet<>(controller.getInactiveList().getSelectionModel().getSelectedItems());
                if (selected.size() > 0) {
                    selected.removeAll(taxa);
                    controller.getInactiveList().getSelectionModel().clearSelection();
                    for (Taxon taxon : selected) {
                        if (controller.getInactiveList().getItems().contains(taxon))
                            controller.getInactiveList().getSelectionModel().select(taxon);
                    }
                }
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
            final ListView<Taxon> activeList = controller.getActiveList();
            final ListView<Taxon> inactiveList = controller.getInactiveList();

            activeList.getItems().clear();
            inactiveList.getItems().clear();

            if (rootByOutGroup.getOptionInGroupTaxa().size() == 0 && rootByOutGroup.getOptionOutGroupTaxa().size() == 0) {
                activeList.getItems().addAll(document.getWorkflow().getWorkingTaxaBlock().getTaxa());
            } else {
                activeList.getItems().addAll(rootByOutGroup.getOptionInGroupTaxa());
                inactiveList.getItems().addAll(rootByOutGroup.getOptionOutGroupTaxa());
            }

            select(document.getTaxaSelectionModel().getSelectedItems(), true);
        } finally {
            inSync = false;
        }
    }

    /**
     * sync controller to model
     */
    public void syncController2Model() {
        rootByOutGroup.getOptionInGroupTaxa().clear();
        rootByOutGroup.getOptionInGroupTaxa().addAll(controller.getActiveList().getItems());
        rootByOutGroup.getOptionOutGroupTaxa().clear();
        rootByOutGroup.getOptionOutGroupTaxa().addAll(controller.getInactiveList().getItems());
        connector.setState(UpdateState.INVALID);
    }

    public RootByOutGroupPaneController getController() {
        return controller;
    }
}
