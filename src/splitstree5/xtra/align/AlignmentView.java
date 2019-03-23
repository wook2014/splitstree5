/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.xtra.align;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.Pair;


/**
 * alignment view
 * Daniel Huson, 2.2018
 */
public class AlignmentView extends Pane {
    final private AlignmentViewController controller;

    final private ObservableList<Pair<String, String>> sequences = FXCollections.observableArrayList();

    public AlignmentView() {
        final ExtendedFXMLLoader<AlignmentViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();
        getChildren().add(extendedFXMLLoader.getRoot());

        final TableView<Pair<String, String>> tableView = controller.getAlignmentTableView();

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.getSelectionModel().setCellSelectionEnabled(true);

        getSequences().addListener((ListChangeListener<? super Pair<String, String>>) (c) -> {
            while (c.next()) {
                if (c.getRemovedSize() > 0 || c.getAddedSize() > 0) {
                    updateTable();
                }
            }
        });

        tableView.getSelectionModel().getSelectedCells().addListener(new ListChangeListener<TablePosition>() {
            @Override
            public void onChanged(Change<? extends TablePosition> c) {
                while (c.next()) {
                    for (TablePosition pos : c.getAddedSubList()) {
                        System.err.println("Added: " + pos);
                    }

                }
            }
        });
    }

    public ObservableList<Pair<String, String>> getSequences() {
        return sequences;
    }

    private void updateTable() {
        final TableView<Pair<String, String>> tableView = controller.getAlignmentTableView();
        tableView.getItems().clear();

        if (getSequences().size() > 0) {
            {
                final TableColumn<Pair<String, String>, String> column = new TableColumn<>("Taxon");
                column.setCellValueFactory(param -> new SimpleObjectProperty<>((param.getValue().getFirst())));
                column.setSortable(false);
                tableView.getColumns().add(column);
            }

            final int length = getSequences().get(0).getSecond().length();
            for (int i = 0; i < length; i++) {
                final int colNo = i;
                final TableColumn<Pair<String, String>, Character> column = new TableColumn<>("" + (i + 1));
                column.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSecond().charAt(colNo)));
                column.setCellFactory(col -> new TableCell<Pair<String, String>, Character>() {
                    @Override
                    protected void updateItem(Character item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText("" + item);
                            setStyle(// "-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );" +
                                    "-fx-alignment: center;-fx-font-family: Courier New;-fx-font-size: 12;");
                            switch (Character.toLowerCase(item)) {
                                case 'a':
                                    //setStyle("-fx-background-color: rgba(100%,0%,0%,0.2)");
                                    setBackground((new Background(new BackgroundFill(Color.RED.deriveColor(0, 1, 1, 0.2), CornerRadii.EMPTY, Insets.EMPTY))));
                                    break;
                                case 'c':
                                    setBackground((new Background(new BackgroundFill(Color.GREEN.deriveColor(0, 1, 1, 0.2), CornerRadii.EMPTY, Insets.EMPTY))));
                                    break;
                                case 'g':
                                    setBackground((new Background(new BackgroundFill(Color.BLUE.deriveColor(0, 1, 1, 0.2), CornerRadii.EMPTY, Insets.EMPTY))));
                                    break;
                                case 't':
                                case 'u':
                                    setBackground((new Background(new BackgroundFill(Color.ORANGE.deriveColor(0, 1, 1, 0.2), CornerRadii.EMPTY, Insets.EMPTY))));
                                    break;
                                default:
                                    setBackground((new Background(new BackgroundFill(Color.GRAY.deriveColor(0, 1, 1, 0.2), CornerRadii.EMPTY, Insets.EMPTY))));
                                    break;
                            }
                        }
                    }
                });

                column.setPrefWidth(24);
                column.setSortable(false);
                tableView.getColumns().add(column);
                {
                    final MenuItem selectColumn = new MenuItem("Select");
                    selectColumn.setOnAction((e) -> {
                        tableView.getSelectionModel().selectRange(0, column, getSequences().size(), column);
                    });
                    column.setContextMenu(new ContextMenu(selectColumn));
                }
            }
            tableView.setItems(getSequences());

            columnReorder(tableView, tableView.getColumns().toArray(new TableColumn[tableView.getColumns().size()]));
        }
    }

    public AlignmentViewController getController() {
        return controller;
    }

    /**
     * prevents reordering of columns
     *
     * @param table
     * @param columns
     * @param <S>
     * @param <T>
     */
    public static <S, T> void columnReorder(TableView table, TableColumn<S, T>[] columns) {
        table.getColumns().addListener(new ListChangeListener() {
            public boolean suspended;

            @Override
            public void onChanged(Change change) {
                change.next();
                if (change.wasReplaced() && !suspended) {
                    this.suspended = true;
                    table.getColumns().setAll((Object[]) columns);
                    this.suspended = false;
                }
            }
        });
    }
}
