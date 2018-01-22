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

import javafx.beans.InvalidationListener;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import javafx.util.converter.IntegerStringConverter;
import jloda.fx.ExtendedFXMLLoader;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.graphtab.base.GraphTab;
import splitstree5.main.MainWindow;
import splitstree5.menu.MenuController;

import java.io.IOException;

/**
 * style tab for setting fonts etc
 * Daniel Huson, 1.2018
 */
public class FormatTab extends ViewerTab {
    private GraphTab graphTab;

    private final FormatTabController controller;

    private boolean isUpdating = false; // used during updating of comboboxes
    private InvalidationListener labelFontListener;

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

        mainWindow.getMainWindowController().getMainTabPane().getSelectionModel().selectedItemProperty().addListener((c, o, n) -> {
            updateControls(n);
        });

        updateControls(mainWindow.getMainWindowController().getMainTabPane().getSelectionModel().getSelectedItem());

        controller.getNodeShapeComboBox().getItems().addAll(NodeShape.values());
        controller.getNodeWidthComboBox().getItems().addAll(1, 3, 5, 10, 20, 40, 80);
        controller.getNodeHeightComboBox().getItems().addAll(1, 3, 5, 10, 20, 40, 80);
        controller.getEdgeWidthComboBox().getItems().addAll(1, 2, 3, 4, 5, 6, 8, 10, 20);

        controller.getLabelColorPicker().setValue(Color.BLACK);
        controller.getNodeWidthComboBox().setValue(1);
        controller.getNodeHeightComboBox().setValue(1);
        controller.getNodeColorPicker().setValue(Color.BLACK);

        controller.getEdgeWidthComboBox().setValue(1);
        controller.getEdgeColorPicker().setValue(Color.BLACK);
    }

    private void updateControls(Tab tab) {
        if (tab instanceof GraphTab) {
            graphTab = (GraphTab) tab;

            // label font:
            controller.getFontComboBox().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty().and(graphTab.getEdgeSelectionModel().emptyProperty()));
            if (labelFontListener != null)
                controller.getFontComboBox().fontValueProperty().removeListener(labelFontListener);
            labelFontListener = (x -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        FormatItem formatItem = new FormatItem();
                        formatItem.addFont(controller.getFontComboBox().getFontValue());
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(),
                                graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view()));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getFontComboBox().fontValueProperty().addListener(labelFontListener);
            controller.getFontComboBox().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(),
                                graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view());
                        if (formatItem.getFont() != null)
                            controller.getFontComboBox().setDefaultFont(formatItem.getFont());
                        else
                            controller.getFontComboBox().getSelectionModel().clearSelection();
                    } finally {
                        isUpdating = false;
                    }
                }
            });

            // label color:
            controller.getLabelColorPicker().disableProperty().bind(controller.getFontComboBox().disableProperty());
            controller.getLabelColorPicker().setOnAction((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        FormatItem formatItem = new FormatItem();
                        formatItem.addLabelColor(controller.getLabelColorPicker().getValue());
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(),
                                graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view()));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getLabelColorPicker().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(),
                                graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view());
                        if (formatItem.getLabelColor() != null)
                            controller.getLabelColorPicker().setValue(formatItem.getLabelColor());
                    } finally {
                        isUpdating = false;
                    }
                }
            });

            controller.getNodeShapeComboBox().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty());
            controller.getNodeShapeComboBox().setOnAction((e) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        FormatItem formatItem = new FormatItem();
                        formatItem.addNodeShape(controller.getNodeShapeComboBox().getValue());
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(), null, null));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getNodeShapeComboBox().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(), null, null);
                        if (formatItem.getNodeShape() != null)
                            controller.getNodeShapeComboBox().setValue(formatItem.getNodeShape());
                    } finally {
                        isUpdating = false;
                    }
                }
            });

            controller.getNodeWidthComboBox().setConverter(new IntegerStringConverter());
            controller.getNodeWidthComboBox().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty());
            controller.getNodeWidthComboBox().valueProperty().addListener((c, o, n) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;

                        FormatItem formatItem = new FormatItem();
                        formatItem.addNodeSize(n, n);
                        controller.getNodeHeightComboBox().setValue(n);
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(), null, null));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getNodeWidthComboBox().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(), null, null);
                        if (formatItem.getNodeWidth() != null) {
                            controller.getNodeWidthComboBox().setValue(formatItem.getNodeWidth());
                        }
                    } finally {
                        isUpdating = false;
                    }
                }
            });

            controller.getNodeHeightComboBox().setConverter(new IntegerStringConverter());
            controller.getNodeHeightComboBox().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty());
            controller.getNodeHeightComboBox().valueProperty().addListener((c, o, n) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        FormatItem formatItem = new FormatItem();
                        formatItem.addNodeSize(null, n);
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(), null, null));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getNodeHeightComboBox().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(), null, null);
                        if (formatItem.getNodeHeight() != null)
                            controller.getNodeHeightComboBox().setValue(formatItem.getNodeHeight());
                    } finally {
                        isUpdating = false;
                    }
                }
            });

            controller.getNodeColorPicker().disableProperty().bind(graphTab.getNodeSelectionModel().emptyProperty());
            controller.getNodeColorPicker().setOnAction((e) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        FormatItem formatItem = new FormatItem();
                        formatItem.addNodeColor(controller.getNodeColorPicker().getValue());
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(), null, null));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getNodeColorPicker().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(graphTab.getNodeSelectionModel().getSelectedItems(), graphTab.getNode2view(),
                                null, null);
                        if (formatItem.getNodeColor() != null)
                            controller.getNodeColorPicker().setValue(formatItem.getNodeColor());
                    } finally {
                        isUpdating = false;
                    }
                }
            });

            controller.getEdgeWidthComboBox().setConverter(new IntegerStringConverter());
            controller.getEdgeWidthComboBox().disableProperty().bind(graphTab.getEdgeSelectionModel().emptyProperty());
            controller.getEdgeWidthComboBox().valueProperty().addListener((c, o, n) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        FormatItem formatItem = new FormatItem();
                        formatItem.addEdgeWidth(n);
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, null, null, graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view()));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getEdgeWidthComboBox().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(null, null, graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view());
                        if (formatItem.getEdgeWidth() != null)
                            controller.getEdgeWidthComboBox().setValue(formatItem.getEdgeWidth());
                    } finally {
                        isUpdating = false;
                    }
                }
            });

            controller.getEdgeColorPicker().disableProperty().bind(graphTab.getEdgeSelectionModel().emptyProperty());
            controller.getEdgeColorPicker().setOnAction((e) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        FormatItem formatItem = new FormatItem();
                        formatItem.addEdgeColor(controller.getEdgeColorPicker().getValue());
                        getMainWindow().getUndoRedoManager().doAndAdd(new FormatChangeCommand(formatItem, null, null, graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view()));
                    } finally {
                        isUpdating = false;
                    }
                }
            });
            controller.getEdgeColorPicker().setOnShowing((x) -> {
                if (!isUpdating) {
                    try {
                        isUpdating = true;
                        final FormatItem formatItem = FormatItem.createFromSelection(null, null, graphTab.getEdgeSelectionModel().getSelectedItems(), graphTab.getEdge2view());
                        if (formatItem.getEdgeColor() != null)
                            controller.getEdgeColorPicker().setValue(formatItem.getEdgeColor());
                    } finally {
                        isUpdating = false;
                    }
                }
            });
        } else {
            graphTab = null;

            if (labelFontListener != null) {
                controller.getFontComboBox().fontValueProperty().removeListener(labelFontListener);
                labelFontListener = null;
            }

            controller.getFontComboBox().disableProperty().unbind();
            controller.getFontComboBox().setDisable(true);
            controller.getLabelColorPicker().disableProperty().unbind();
            controller.getLabelColorPicker().setDisable(true);

            controller.getNodeShapeComboBox().disableProperty().unbind();
            controller.getNodeShapeComboBox().setDisable(true);

            controller.getNodeWidthComboBox().disableProperty().unbind();
            controller.getNodeWidthComboBox().setDisable(true);

            controller.getNodeHeightComboBox().disableProperty().unbind();
            controller.getNodeHeightComboBox().setDisable(true);

            controller.getNodeColorPicker().disableProperty().unbind();
            controller.getNodeColorPicker().setDisable(true);

            controller.getEdgeWidthComboBox().disableProperty().unbind();
            controller.getEdgeWidthComboBox().setDisable(true);

            controller.getEdgeColorPicker().disableProperty().unbind();
            controller.getEdgeColorPicker().setDisable(true);
        }
    }

    public FormatTabController getController() {
        return controller;
    }

    @Override
    public void updateMenus(MenuController controller) {
    }
}
