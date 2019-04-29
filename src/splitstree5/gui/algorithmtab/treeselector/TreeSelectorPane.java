/*
 *  TreeSelectorPane.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.algorithmtab.treeselector;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import jloda.fx.undo.UndoManager;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.phylo.PhyloTree;
import jloda.util.Basic;
import splitstree5.core.Document;
import splitstree5.core.algorithms.filters.TreeSelector;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.workflow.Connector;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.algorithmtab.AlgorithmPane;

import java.io.IOException;

/**
 * tree selector
 * Daniel Huson, 3 2018
 */
public class TreeSelectorPane extends AlgorithmPane {
    private Connector<TreesBlock, TreesBlock> connector;
    private TreeSelector treeSelector;
    private final TreeSelectorPaneController controller;
    private Document document;
    private UndoManager undoManager;
    private final BooleanProperty isApplicable = new SimpleBooleanProperty(false);

    private final IntegerProperty numberOfTrees = new SimpleIntegerProperty(0);
    private final IntegerProperty currentTree = new SimpleIntegerProperty(1); // 1-based

    private boolean inSync = false;
    private boolean inUpdatingText = false;

    /**
     * constructor
     */
    public TreeSelectorPane(TreeSelector treeSelector) throws IOException {
        this.treeSelector = treeSelector;

        final ExtendedFXMLLoader extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = (TreeSelectorPaneController) extendedFXMLLoader.getController();
        this.getChildren().setAll(extendedFXMLLoader.getRoot());
        undoManager = new UndoManager();

        controller.getTreeIdTextField().setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));

        currentTree.addListener((c, o, n) -> {
            if (!inSync) {
                undoManager.add(new UndoableRedoableCommand("Select Tree") {
                    @Override
                    public void undo() {
                        currentTree.set(o.intValue());
                    }

                    @Override
                    public void redo() {
                        currentTree.set(n.intValue());
                    }
                });
            }
            if (!inUpdatingText) {
                inUpdatingText = true;
                try {
                    controller.getTreeIdTextField().setText("" + n.intValue());
                } finally {
                    inUpdatingText = false;
                }
            }
            controller.getTreeNameTextField().setText(connector.getParent().getDataBlock().getTree(n.intValue()).getName());
        });


        controller.getTreeIdTextField().textProperty().addListener((c, o, n) -> {
            if (!inUpdatingText)
                try {
                    inUpdatingText = true;
                    if (n.length() > 0) {
                        final int value = Math.max(1, Math.min(Basic.parseInt(n), numberOfTrees.get()));
                        currentTree.set(value);
                    }
                } finally {
                    inUpdatingText = false;
                }
        });
    }

    @Override
    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void setup() {
        numberOfTrees.set(connector.getParent().getDataBlock().getNTrees());
        currentTree.set(Math.min(treeSelector.getOptionWhich(), numberOfTrees.get()));
        controller.getTreeIdTextField().setText("" + currentTree.get());


        controller.getTreeIdTextField().setOnAction((e) -> {
            final String text = controller.getTreeIdTextField().getText();
            if (Basic.isInteger(text)) {
                int value = Basic.parseInt(text);
                if (value >= 1 && value <= numberOfTrees.get()) {
                    currentTree.set(value);
                    syncController2Model();
                }
            } else {
                for (int i = 1; i <= numberOfTrees.get(); i++) {
                    final PhyloTree tree = treeSelector.getConnector().getParent().getDataBlock().getTree(i);
                    if (tree.getName() != null && tree.getName().contains(text)) {
                        currentTree.set(i);
                        syncController2Model();
                        break;
                    }
                }
            }
        });
        controller.getGotoFirstButton().disableProperty().bind(currentTree.lessThanOrEqualTo(1).or(connector.stateProperty().isNotEqualTo(UpdateState.VALID)));


        controller.getGotoFirstButton().setOnAction((e) -> {
            currentTree.set(1);
            syncController2Model();
        });
        controller.getGotoFirstButton().disableProperty().bind(currentTree.lessThanOrEqualTo(1).or(connector.stateProperty().isNotEqualTo(UpdateState.VALID)));

        controller.getGotoPreviousButton().setOnAction((e) -> {
            currentTree.set(currentTree.get() - 1);
            syncController2Model();
        });
        controller.getGotoPreviousButton().disableProperty().bind(currentTree.lessThanOrEqualTo(1).or(connector.stateProperty().isNotEqualTo(UpdateState.VALID)));

        controller.getGotoNextButton().setOnAction((e) -> {
            currentTree.set(currentTree.get() + 1);
            syncController2Model();
        });
        controller.getGotoNextButton().disableProperty().bind(currentTree.greaterThanOrEqualTo(numberOfTrees).or(connector.stateProperty().isNotEqualTo(UpdateState.VALID)));

        controller.getGotoLastButton().setOnAction((e) -> {
            currentTree.set(numberOfTrees.get());
            syncController2Model();
        });
        controller.getGotoLastButton().disableProperty().bind(currentTree.greaterThanOrEqualTo(connector.getParent().getDataBlock().getNTrees()).or(connector.stateProperty().isNotEqualTo(UpdateState.VALID)));

        isApplicable.bind(currentTree.greaterThanOrEqualTo(1).and(currentTree.lessThanOrEqualTo(numberOfTrees)));

        controller.getNumberOfTreesLabel().setText("Trees: " + numberOfTrees.get());
        controller.getTreeNameTextField().setText(connector.getParent().getDataBlock().getTree(currentTree.get()).getName());

    }

    @Override
    public void syncModel2Controller() {
        inSync = true;
        try {
            setup();
        } finally {
            inSync = false;
        }
    }

    @Override
    public void syncController2Model() {
        treeSelector.setOptionWhich(currentTree.get());
        connector.setState(UpdateState.INVALID);
    }

    @Override
    public BooleanProperty applicableProperty() {
        return isApplicable;
    }
}
