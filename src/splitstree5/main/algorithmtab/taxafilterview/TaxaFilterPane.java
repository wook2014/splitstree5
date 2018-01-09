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

package splitstree5.main.algorithmtab.taxafilterview;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import jloda.fx.ExtendedFXMLLoader;
import jloda.graph.Node;
import jloda.util.Basic;
import jloda.util.Triplet;
import splitstree5.core.Document;
import splitstree5.core.algorithms.filters.TaxaFilter;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.misc.Taxon;
import splitstree5.core.project.ProjectManager;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.connectorview.AlgorithmPane;
import splitstree5.main.ISavesPreviousSelection;
import splitstree5.main.graphtab.base.GraphTab;
import splitstree5.undo.UndoRedoManager;
import splitstree5.undo.UndoableChangeListViews2;
import splitstree5.utils.DragAndDropSupportListView2;

import java.io.IOException;
import java.util.*;

/**
 * taxon filter pane
 * Daniel Huson, 12/2016
 */
public class TaxaFilterPane extends AlgorithmPane implements ISavesPreviousSelection {
    private final TaxaFilter taxaFilter;
    private final TaxaFilterPaneController controller;
    private Document document = null;
    private UndoRedoManager undoManager;

    private ArrayList<Taxon> prevActiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify
    private ArrayList<Taxon> prevInactiveTaxa = new ArrayList<>(); // used to facilitate undo/redo, do not modify

    final SimpleBooleanProperty applicableProperty = new SimpleBooleanProperty();

    private final Map<GraphTab, Triplet<ListChangeListener<Taxon>, InvalidationListener, Boolean>> graphTab2SelectionListeners = new HashMap<>();

    private AConnector connector;

    private boolean inSync = false;

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
        undoManager = new UndoRedoManager();
    }

    @Override
    public void setUndoManager(UndoRedoManager undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public void setDocument(Document document) {
        this.document = document;
        document.getMainWindow().getMainWindowController().getMainTabPane().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            if (n instanceof GraphTab) {
                connectSelectionModels(getController().getActiveList(), getController().getInactiveList(), (GraphTab) n);
            }
        });

        final Tab tab = document.getMainWindow().getMainWindowController().getMainTabPane().getSelectionModel().getSelectedItem();
        if (tab != null && tab instanceof GraphTab)
            connectSelectionModels(getController().getActiveList(), getController().getInactiveList(), (GraphTab) tab);
    }

    /**
     * todo: this is nonsense, should do this via taxaSelection model
     * <p>
     * connects taxon filter selection to graph node selection
     *
     * @param activeList
     * @param inactiveList
     * @param graphTab
     */
    private void connectSelectionModels(ListView<Taxon> activeList, ListView<Taxon> inactiveList, GraphTab graphTab) {
        if (!graphTab2SelectionListeners.containsKey(graphTab)) {
            final Triplet<ListChangeListener<Taxon>, InvalidationListener, Boolean> triplet = new Triplet<>(null, null, false);
            graphTab2SelectionListeners.put(graphTab, triplet);
            triplet.set1((c) -> {
                if (!triplet.get3()) {
                    try {
                        triplet.set3(true);
                        while (c.next()) {
                            try {
                                final Set<String> labels = new HashSet<>();
                                for (Taxon t : c.getAddedSubList()) {
                                    labels.add(t.getName());
                                }
                                if (labels.size() > 0)
                                    graphTab.selectNodesByLabel(labels, true);
                            } catch (IndexOutOfBoundsException ex) {
                                Basic.caught(ex);
                            }
                            {
                                final Set<String> labels = new HashSet<>();
                                for (Taxon t : c.getRemoved()) {
                                    labels.add(t.getName());
                                }
                                if (labels.size() > 0)
                                    graphTab.selectNodesByLabel(labels, false);
                            }
                        }
                    } finally {
                        triplet.set3(false);
                    }
                }
            });
            triplet.set2((c) -> {
                if (!triplet.get3()) {
                    try {
                        triplet.set3(true);
                        final Set<String> labels = new HashSet<>();
                        for (Node v : graphTab.getNodeSelectionModel().getSelectedItems()) {
                            String label = graphTab.getPhyloGraph().getLabel(v);
                            if (label != null)
                                labels.add(label);
                        }

                        activeList.getSelectionModel().clearSelection();
                        inactiveList.getSelectionModel().clearSelection();
                        if (labels.size() > 0) {
                            boolean first = true;
                            for (Taxon taxon : activeList.getItems()) {
                                if (labels.contains(taxon.getName())) {
                                    activeList.getSelectionModel().select(taxon);
                                    if (first) {
                                        activeList.scrollTo(taxon);
                                        first = false;
                                    }
                                }
                            }
                            for (Taxon taxon : inactiveList.getItems()) {
                                if (labels.contains(taxon.getName())) {
                                    inactiveList.getSelectionModel().select(taxon);
                                }
                            }
                        }
                    } finally {
                        triplet.set3(false);
                    }
                }
            });

            activeList.getSelectionModel().getSelectedItems().addListener(new WeakListChangeListener<>(triplet.get1()));
            inactiveList.getSelectionModel().getSelectedItems().addListener(new WeakListChangeListener<>(triplet.get1()));
            graphTab.getNodeSelectionModel().getSelectedItems().addListener(new WeakInvalidationListener(triplet.get2()));
        }
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
        controller.getActiveList().getItems().addListener((ListChangeListener.Change<? extends Taxon> c) -> {
            if (!undoManager.isPerformingUndoOrRedo()) { // for performance reasons, check this here. Is also checked in addUndoableChange, but why make a change object if we don't need it...
                final UndoableChangeListViews2<Taxon> change = new UndoableChangeListViews2<>("Change Active Taxa", controller.getActiveList(), prevActiveTaxa, controller.getInactiveList(), prevInactiveTaxa);
                prevActiveTaxa = change.getItemsA();
                prevInactiveTaxa = change.getItemsB();
                if (!inSync)
                    undoManager.add(change);
            }

            controller.getActiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));
            controller.getInactiveList().prefHeightProperty().bind(this.prefHeightProperty().subtract(50));

            controller.getActiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));
            controller.getInactiveList().prefWidthProperty().bind(this.prefWidthProperty().subtract(180).divide(2));
            DragAndDropSupportListView2.setup(controller.getActiveList(), controller.getInactiveList(), undoManager, "Change Active Taxa");
            updateSelection();
        });

        controller.getInactiveList().getItems().addListener((ListChangeListener.Change<? extends Taxon> c) -> updateSelection());

        controller.getActiveList().getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Taxon> c) -> updateSelection());

        controller.getInactiveList().getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Taxon> c) -> updateSelection());

        document.getTaxaSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Taxon> c) -> {
            if (!inUpdateSelection) {
                inUpdateSelection = true;
                try {
                    controller.getActiveList().getSelectionModel().clearSelection();
                    for (Taxon taxon : controller.getActiveList().getItems()) {
                        if (document.getTaxaSelectionModel().getSelectedItems().contains(taxon)) {
                            controller.getActiveList().getSelectionModel().select(taxon);
                        }
                    }
                    controller.getInactiveList().getSelectionModel().clearSelection();
                    for (Taxon taxon : controller.getInactiveList().getItems()) {
                        if (document.getTaxaSelectionModel().getSelectedItems().contains(taxon)) {
                            controller.getInactiveList().getSelectionModel().select(taxon);
                        }
                    }
                } finally {
                    inUpdateSelection = false;
                }
            }
        });

        controller.getInactivateAllButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getActiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getItems().addAll(controller.getActiveList().getItems());
            controller.getActiveList().getItems().clear();
            for (Taxon t : selected)
                controller.getInactiveList().getSelectionModel().select(t);
        });
        controller.getInactivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getActiveList().getItems()));


        controller.getInactivateSelectedButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getActiveList().getSelectionModel().getSelectedItems());
            controller.getActiveList().getSelectionModel().clearSelection();
            controller.getActiveList().getItems().removeAll(selected);
            controller.getInactiveList().getItems().addAll(selected);
            for (Taxon t : selected)
                controller.getInactiveList().getSelectionModel().select(t);
        });
        controller.getInactivateSelectedButton().disableProperty().bind(Bindings.isEmpty(controller.getActiveList().getSelectionModel().getSelectedIndices()));

        controller.getActivateAllButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getInactiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getSelectionModel().clearSelection();
            controller.getInactiveList().getItems().clear();
            controller.getActiveList().getItems().addAll(selected);
            for (Taxon t : selected)
                controller.getActiveList().getSelectionModel().select(t);
        });
        controller.getActivateAllButton().disableProperty().bind(Bindings.isEmpty(controller.getInactiveList().getItems()));

        controller.getActivateSelectedButton().setOnAction((e) -> {
            final ArrayList<Taxon> selected = new ArrayList<>(controller.getInactiveList().getSelectionModel().getSelectedItems());
            controller.getInactiveList().getSelectionModel().clearSelection();
            controller.getInactiveList().getItems().removeAll(selected);
            controller.getActiveList().getItems().addAll(selected);
            for (Taxon t : selected)
                controller.getActiveList().getSelectionModel().select(t);
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

    /**
     * updates selection
     */
    private void updateSelection() {
        if (!inUpdateSelection) {
            inUpdateSelection = true;
            try {
                for (Taxon taxon : controller.getActiveList().getItems()) {
                    if (controller.getActiveList().getSelectionModel().getSelectedItems().contains(taxon))
                        document.getTaxaSelectionModel().select(taxon);
                    else
                        document.getTaxaSelectionModel().clearSelection(taxon);
                }
                for (Taxon taxon : controller.getInactiveList().getItems()) {
                    if (controller.getInactiveList().getSelectionModel().getSelectedItems().contains(taxon))
                        document.getTaxaSelectionModel().select(taxon);
                    else
                        document.getTaxaSelectionModel().clearSelection(taxon);
                }
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
            final ListView<Taxon> activeList = controller.getActiveList();
            final ListView<Taxon> inactiveList = controller.getInactiveList();

            activeList.getItems().clear();
            inactiveList.getItems().clear();

            if (taxaFilter.getEnabledTaxa().size() == 0 && taxaFilter.getDisabledTaxa().size() == 0) {
                activeList.getItems().addAll(((TaxaBlock) connector.getParent().getDataBlock()).getTaxa());
            } else {
                activeList.getItems().addAll(taxaFilter.getEnabledTaxa());
                inactiveList.getItems().addAll(taxaFilter.getDisabledTaxa());
            }
        } finally {
            inSync = false;
        }
    }

    /**
     * sync controller to model
     */
    public void syncController2Model() {
        taxaFilter.getEnabledTaxa().clear();
        taxaFilter.getEnabledTaxa().addAll(controller.getActiveList().getItems());
        taxaFilter.getDisabledTaxa().clear();
        taxaFilter.getDisabledTaxa().addAll(controller.getInactiveList().getItems());
        connector.setState(UpdateState.INVALID);
    }

    @Override
    public void saveAsPreviousSelection() {
        ProjectManager.getInstance().getPreviousSelection().clear();
        for (Taxon taxon : controller.getActiveList().getSelectionModel().getSelectedItems()) {
            ProjectManager.getInstance().getPreviousSelection().add(taxon.getName());
        }
        for (Taxon taxon : controller.getInactiveList().getSelectionModel().getSelectedItems()) {
            ProjectManager.getInstance().getPreviousSelection().add(taxon.getName());
        }
    }

    public TaxaFilterPaneController getController() {
        return controller;
    }
}
