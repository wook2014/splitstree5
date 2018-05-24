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

package splitstree5.gui.algorithmtab;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import jloda.fx.ExtendedFXMLLoader;
import splitstree5.core.Document;
import splitstree5.core.algorithms.Algorithm;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.ViewerTab;
import splitstree5.menu.MenuController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * tab for configuring algorithms
 * Daniel Huson, 1.2018
 *
 * @param <P>
 * @param <C>
 */
public class AlgorithmTab<P extends DataBlock, C extends DataBlock> extends ViewerTab {
    private final Document document;
    private final AlgorithmTabController controller;
    private AlgorithmPane algorithmPane;
    private Algorithm<P, C> currentAlgorithm;
    private final BooleanProperty algorithmIsApplicable = new SimpleBooleanProperty(); // is the algorithm generally applicable to the given type of input data?
    private final BooleanProperty algorithmSettingsIsApplicable = new SimpleBooleanProperty(); // are the current settings applicable?
    private final BooleanProperty applicableChangeHasBeenMade = new SimpleBooleanProperty();

    private Map<Algorithm<P, C>, AlgorithmPane> algorithm2pane = new HashMap<>(); // we keep used panes around in case we want to undo back to one

    private final Connector<P, C> connector;

    private final ChangeListener<UpdateState> connectorStateChangeListener;
    private final ChangeListener<UpdateState> parentStateChangeListener;

    /**
     * constructor
     */
    public AlgorithmTab(Document document, Connector<P, C> connector) {
        this.document = document;
        this.connector = connector;
        this.currentAlgorithm = connector.getAlgorithm();
        setMainWindow(document.getMainWindow());

        final Label label = new Label();
        setText("");
        setGraphic(label);
        label.textProperty().bind(connector.nameProperty());

        {
            final ExtendedFXMLLoader<AlgorithmTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            controller = extendedFXMLLoader.getController();
            setContent(extendedFXMLLoader.getRoot());
        }

        getUndoManager().undoStackSizeProperty().addListener((c, o, n) -> applicableChangeHasBeenMade.set(n.intValue() > 0));

        connectorStateChangeListener = (c, o, n) -> {
            if (n == UpdateState.VALID) {
                this.currentAlgorithm = connector.getAlgorithm();
                controller.getAlgorithmComboBox().setValue(currentAlgorithm);
                algorithmPane.syncModel2Controller();
            } else if (n == UpdateState.FAILED)
                applicableChangeHasBeenMade.set(true);
            // undoManager.clear();
        };
        connector.stateProperty().addListener(new WeakChangeListener<>(connectorStateChangeListener));

        parentStateChangeListener = (c, o, n) -> getUndoManager().clear();

        connector.getParent().stateProperty().addListener(new WeakChangeListener<>(parentStateChangeListener));

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
                                    if (item.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock()))
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
            // System.err.println(currentAlgorithm.getName() + " is applicable: " + n);
        });

        algorithmComboBox.getItems().addAll(algorithms);
        algorithmComboBox.setValue(currentAlgorithm);
        algorithmComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            getUndoManager().add("Set Algorithm", algorithmComboBox.valueProperty(), oldValue, newValue);
            currentAlgorithm = ((Algorithm) newValue);
            currentAlgorithm.setConnector(oldValue.getConnector());
            controller.getBorderPane().setCenter(updateAlgorithmPane());
            algorithmIsApplicable.setValue(currentAlgorithm.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock()));
            algorithmPane.syncModel2Controller();
        });

        algorithmPane.syncModel2Controller();

        controller.getApplyButton().setOnAction((e) -> {
            if (connector.getAlgorithm() != currentAlgorithm)
                connector.setAlgorithm(currentAlgorithm);
            algorithmPane.syncController2Model();
            getUndoManager().addUndoableApply(algorithmPane::syncController2Model);

            getUndoManager().clear(); // clear undo

            applicableChangeHasBeenMade.set(false);
        });

        algorithmIsApplicable.setValue(currentAlgorithm.isApplicable(connector.getTaxaBlock(), connector.getParentDataBlock()));
        controller.getApplyButton().disableProperty().bind((applicableChangeHasBeenMade.and(algorithmIsApplicable).and(algorithmSettingsIsApplicable)).not());
    }

    /**
     * setup the algorithm pane
     */
    private AlgorithmPane updateAlgorithmPane() {
        algorithmPane = algorithm2pane.get(currentAlgorithm);
        if (algorithmPane == null) {
            algorithmPane = currentAlgorithm.getAlgorithmPane(); // some algorithms have their own control pane
            if (algorithmPane == null)
                algorithmPane = new GenericAlgorithmPane<>(connector, currentAlgorithm);
            algorithm2pane.put(currentAlgorithm, algorithmPane);
            algorithmPane.setDocument(document);
            algorithmPane.setUndoManager(getUndoManager());
            algorithmPane.setConnector(connector);
            /*
            algorithmPane.setPrefWidth(controller.getCenterPane().getWidth());
            algorithmPane.setPrefHeight(controller.getCenterPane().getHeight());
            algorithmPane.prefHeightProperty().bind(controller.getCenterPane().heightProperty());
            algorithmPane.prefWidthProperty().bind(controller.getCenterPane().widthProperty());
            */
            algorithmPane.setup();
        }
        algorithmSettingsIsApplicable.bind(algorithmPane.applicableProperty());
        return algorithmPane;
    }


    @Override
    public void updateMenus(MenuController controller) {
        System.err.println("Update menus");
    }
}
