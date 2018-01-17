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

package splitstree5.gui.formattab;

import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.ExtendedFXMLLoader;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.graphtab.base.AEdgeView;
import splitstree5.gui.graphtab.base.ANodeView;
import splitstree5.gui.graphtab.base.GraphTab;
import splitstree5.main.MainWindow;
import splitstree5.menu.MenuController;
import splitstree5.undo.UndoRedoManager;

import java.io.IOException;

/**
 * style tab for setting fonts etc
 * Daniel Huson, 1.2018
 */
public class FormatTab extends ViewerTab {
    private GraphTab graphTab;

    private final FormatTabController controller;
    private final UndoRedoManager undoManager;


    /**
     * constructor
     *
     * @param mainWindow
     * @throws IOException
     */
    public FormatTab(MainWindow mainWindow) {
        setMainWindow(mainWindow);
        {
            final ExtendedFXMLLoader<FormatTabController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            controller = extendedFXMLLoader.getController();
            setContent(extendedFXMLLoader.getRoot());
        }

        final Label label = new Label();
        setText("");
        setGraphic(label);
        label.setText("Format");

        undoManager = new UndoRedoManager();

        mainWindow.getMainWindowController().getMainTabPane().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            updateControls(n);
        });

        updateControls(mainWindow.getMainWindowController().getMainTabPane().getSelectionModel().getSelectedItem());
    }

    private void updateControls(Tab tab) {
        if (tab instanceof GraphTab) {
            graphTab = (GraphTab) tab;

            controller.getFontComboBox().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty().and(graphTab.getEdgeSelectionModel().emptyProperty()));
            controller.getFontComboBox().valueProperty().addListener((x) -> {
                final Font font = controller.getFontComboBox().getValue();
                for (jloda.graph.Node v : graphTab.getNodeSelectionModel().getSelectedItems()) {
                    final ANodeView nv = graphTab.getNode2view().get(v);
                    if (nv.getLabel() != null && nv.getLabel() instanceof Labeled) {
                        ((Labeled) nv.getLabel()).setFont(font);
                    }
                }
                for (jloda.graph.Edge e : graphTab.getEdgeSelectionModel().getSelectedItems()) {
                    final AEdgeView ev = graphTab.getEdge2view().get(e);
                    if (ev.getLabel() != null && ev.getLabel() instanceof Labeled) {
                        ((Labeled) ev.getLabel()).setFont(font);
                    }
                }
            });

            controller.getTextColorChooser().disableProperty().bind(controller.getFontComboBox().disableProperty());
            controller.getTextColorChooser().setOnAction((x) -> {
                final Color color = controller.getTextColorChooser().getValue();
                for (jloda.graph.Node v : graphTab.getNodeSelectionModel().getSelectedItems()) {
                    final ANodeView nv = graphTab.getNode2view().get(v);
                    if (nv.getLabel() != null && nv.getLabel() instanceof Labeled) {
                        ((Labeled) nv.getLabel()).setTextFill(color);
                    }
                }
                for (jloda.graph.Edge v : graphTab.getEdgeSelectionModel().getSelectedItems()) {
                    final AEdgeView nv = graphTab.getEdge2view().get(v);
                    if (nv.getLabel() != null && nv.getLabel() instanceof Labeled) {
                        ((Labeled) nv.getLabel()).setTextFill(color);
                    }
                }
            });
            controller.getLineColorChooser().disableProperty().bind(controller.getFontComboBox().disableProperty());
            controller.getLineColorChooser().setOnAction((e) -> {
                final Color color = controller.getLineColorChooser().getValue();
                for (jloda.graph.Node v : graphTab.getNodeSelectionModel().getSelectedItems()) {
                    final ANodeView nv = graphTab.getNode2view().get(v);
                    if (nv.getShape() != null && nv.getShape() instanceof Shape) {
                        ((Shape) nv.getShape()).setStroke(color);
                    }
                }
                for (jloda.graph.Edge v : graphTab.getEdgeSelectionModel().getSelectedItems()) {
                    final AEdgeView nv = graphTab.getEdge2view().get(v);
                    if (nv.getShape() != null && nv.getShape() instanceof Shape) {
                        ((Shape) nv.getShape()).setStroke(color);
                    }
                }
            });
            controller.getLineWidthField().disableProperty().bind(controller.getFontComboBox().disableProperty());

            controller.getNodeColorPicker().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty());
            controller.getNodeColorPicker().setOnAction((e) -> {
                final Color color = controller.getNodeColorPicker().getValue();
                for (jloda.graph.Node v : graphTab.getNodeSelectionModel().getSelectedItems()) {
                    final ANodeView nv = graphTab.getNode2view().get(v);
                    if (nv.getShape() != null && nv.getShape() instanceof Shape) {
                        ((Shape) nv.getShape()).setFill(color);
                    }
                }
            });
            controller.getNodeShapeComboBox().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty());
        } else {
            graphTab = null;
            controller.getFontComboBox().disableProperty().unbind();
            controller.getFontComboBox().setDisable(true);
            controller.getTextColorChooser().disableProperty().unbind();
            controller.getTextColorChooser().setDisable(true);
            controller.getLineColorChooser().disableProperty().unbind();
            controller.getLineColorChooser().setDisable(true);
            controller.getLineWidthField().disableProperty().unbind();
            controller.getLineWidthField().setDisable(true);

            controller.getNodeColorPicker().disableProperty().unbind();
            controller.getNodeColorPicker().setDisable(true);
            controller.getNodeShapeComboBox().disableProperty().unbind();
            controller.getNodeShapeComboBox().setDisable(true);

            controller.getNodeWidthTextField().disableProperty().unbind();
            controller.getNodeWidthTextField().setDisable(true);

            controller.getNodeWidthTextField().disableProperty().unbind();
            controller.getNodeWidthTextField().setDisable(true);

            controller.getNodeHeightTextField().disableProperty().unbind();
            controller.getNodeHeightTextField().setDisable(true);
        }
    }

    @Override
    public void updateMenus(MenuController controller) {
    }
}
