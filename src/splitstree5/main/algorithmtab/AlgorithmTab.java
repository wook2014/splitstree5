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

package splitstree5.main.algorithmtab;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.algorithms.filters.TaxaFilter;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataBlock;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.connectorview.AlgorithmPane;
import splitstree5.main.ViewerTab;
import splitstree5.main.algorithmtab.taxafilterview.TaxaFilterPane;
import splitstree5.menu.MenuController;
import splitstree5.undo.UndoRedoManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * tab for configuring algorithms
 *
 * @param <P>
 * @param <C>
 */
public class AlgorithmTab<P extends ADataBlock, C extends ADataBlock> extends ViewerTab {
    private final Document document;
    private final AlgorithmTabController controller;
    private final UndoRedoManager undoManager;
    private AlgorithmPane algorithmPane;
    private Algorithm<P, C> currentAlgorithm;
    private final BooleanProperty algorithmIsApplicable = new SimpleBooleanProperty();
    private final BooleanProperty applicableChangeHasBeenMade = new SimpleBooleanProperty();

    private Map<Algorithm<P, C>, AlgorithmPane> algorithm2pane = new HashMap<>(); // we keep used panes around in case we want to undo back to one

    private final AConnector<P, C> connector;

    /**
     * constructor
     */
    public AlgorithmTab(Document document, AConnector<P, C> connector) throws IOException {
        this.document = document;
        this.connector = connector;
        this.currentAlgorithm = connector.getAlgorithm();
        setMainWindow(document.getMainWindow());

        {
            final ExtendedFXMLLoader<AlgorithmTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            controller = extendedFXMLLoader.getController();
            setContent(extendedFXMLLoader.getRoot());
        }

        undoManager = new UndoRedoManager();

        undoManager.undoStackSizeProperty().addListener((c, o, n) -> applicableChangeHasBeenMade.set(n.intValue() > 0));

        connector.stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == UpdateState.VALID) {
                algorithmPane.syncModel2Controller();
            }
            // undoManager.clear();
        });

        connector.getParent().stateProperty().addListener((observable, oldValue, newValue) -> {
            undoManager.clear(); // if parent changes, have to forget history...
        });

        controller.getBorderPane().setCenter(updateAlgorithmPane());

        final ComboBox<Algorithm> algorithmComboBox = controller.getAlgorithmComboBox();
        algorithmComboBox.setCellFactory(
                new Callback<ListView<Algorithm>, ListCell<Algorithm>>() {
                    @Override
                    public ListCell<Algorithm> call(ListView<Algorithm> param) {
                        return new ListCell<Algorithm>() {
                            @Override
                            public void updateItem(Algorithm item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null) {
                                    setText(item.getName());
                                    if (item.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock(), connector.getChildDataBlock()))
                                        setTextFill(Color.BLACK);
                                    else
                                        setTextFill(Color.LIGHTGRAY);
                                } else {
                                    setText(null);
                                }
                                //setPrefWidth(250);
                            }
                        };
                    }
                });

        final ArrayList<Algorithm<P, C>> algorithms = connector.getAllAlgorithms();
        if (currentAlgorithm != null) { // if algorithm already set, make sure we use the existing algorithm object
            for (int i = 0; i < algorithms.size(); i++) {
                final Algorithm<P, C> algorithm = algorithms.get(i);
                if (currentAlgorithm.getClass().equals(algorithm.getClass())) {
                    algorithms.set(i, currentAlgorithm);
                    break;
                }
            }
        }
        if (algorithms.size() <= 1 && currentAlgorithm != null) {
            algorithms.add(currentAlgorithm);
            algorithmComboBox.setDisable(true);
        }


        connector.applicableProperty().addListener((c, o, n) -> {
            System.err.println(currentAlgorithm.getName() + " is applicable: " + n);
        });

        algorithmComboBox.getItems().addAll(algorithms);
        algorithmComboBox.setValue(currentAlgorithm);
        algorithmComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            undoManager.add("Set Algorithm", algorithmComboBox.valueProperty(), oldValue, newValue);
            currentAlgorithm = ((Algorithm) newValue);
            controller.getBorderPane().setCenter(updateAlgorithmPane());
            algorithmIsApplicable.setValue(currentAlgorithm.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock(), connector.getChildDataBlock()));
        });


        algorithmPane.syncModel2Controller();

        final Label label = new Label();
        label.setRotate(-90);
        setText("");
        setGraphic(label);
        label.setText(connector.getName());

        controller.getApplyButton().setOnAction((e) -> {
            if (connector.getAlgorithm() != currentAlgorithm)
                connector.setAlgorithm(currentAlgorithm);
            algorithmPane.syncController2Model();
            undoManager.addUndoableApply(algorithmPane::syncController2Model);
            applicableChangeHasBeenMade.set(false);
            label.setText(controller.getAlgorithmComboBox().getSelectionModel().getSelectedItem().getName());
        });

        algorithmIsApplicable.setValue(currentAlgorithm.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock(), connector.getChildDataBlock()));
        controller.getApplyButton().disableProperty().bind((applicableChangeHasBeenMade.and(algorithmIsApplicable)).not());

    }

    /**
     * setup the algorithm pane
     */
    private AlgorithmPane updateAlgorithmPane() {
        algorithmPane = algorithm2pane.get(currentAlgorithm);
        if (algorithmPane == null) {
            if (currentAlgorithm instanceof TaxaFilter) {
                try {
                    algorithmPane = new TaxaFilterPane((TaxaFilter) currentAlgorithm);
                } catch (IOException e) {
                    Basic.caught(e);
                }
            } else algorithmPane = currentAlgorithm.getControl(); // todo: remove this whole mechanism?
            if (algorithmPane == null)
                algorithmPane = new GenericAlgorithmPane<>(connector, currentAlgorithm);
            algorithm2pane.put(currentAlgorithm, algorithmPane);
            algorithmPane.setDocument(document);
            algorithmPane.setUndoManager(undoManager);
            algorithmPane.setConnector(connector);
            /*
            algorithmPane.setPrefWidth(controller.getCenterPane().getWidth());
            algorithmPane.setPrefHeight(controller.getCenterPane().getHeight());
            algorithmPane.prefHeightProperty().bind(controller.getCenterPane().heightProperty());
            algorithmPane.prefWidthProperty().bind(controller.getCenterPane().widthProperty());
            */
            algorithmPane.setup();
        }
        return algorithmPane;
    }


    @Override
    public void updateMenus(MenuController controller) {
        System.err.println("Update menus");
    }
}
