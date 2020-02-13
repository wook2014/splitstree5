/*
 * Legend.java Copyright (C) 2020. Daniel H. Huson
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

/*
 * Legend.java Copyright (C) 2020. Algorithms in Bioinformatics, University of Tuebingen
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

/*
 *  Legend.java Copyright (C) 2019 This is third party code.
 */

package splitstree5.xtra;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;

import java.util.List;
import java.util.stream.Collectors;

/**
 * legend
 * This was copied from here: com.sun.javafx.charts.Legend (couldn't figure out how to enable this)
 */
public class Legend extends TilePane {
    private static final int GAP = 5;

    private ListChangeListener<Legend.LegendItem> itemsListener = (c) -> {
        List list = (List) this.getItems().stream().map((item) -> {
            return item.label;
        }).collect(Collectors.toList());
        this.getChildren().setAll(list);
        if (this.isVisible()) {
            this.requestLayout();
        }

    };
    private BooleanProperty vertical = new BooleanPropertyBase(false) {
        protected void invalidated() {
            Legend.this.setOrientation(this.get() ? Orientation.VERTICAL : Orientation.HORIZONTAL);
        }

        public Object getBean() {
            return Legend.this;
        }

        public String getName() {
            return "vertical";
        }
    };
    private ObjectProperty<ObservableList<Legend.LegendItem>> items = new ObjectPropertyBase<ObservableList<Legend.LegendItem>>() {
        ObservableList<Legend.LegendItem> oldItems = null;

        protected void invalidated() {
            if (this.oldItems != null) {
                this.oldItems.removeListener(Legend.this.itemsListener);
            }

            Legend.this.getChildren().clear();
            ObservableList items = (ObservableList) this.get();
            if (items != null) {
                items.addListener(Legend.this.itemsListener);
                List list = (List) items.stream().map((item) -> {
                    return ((LegendItem) item).label;
                }).collect(Collectors.toList());
                Legend.this.getChildren().addAll(list);
            }

            this.oldItems = items;
            Legend.this.requestLayout();
        }

        public Object getBean() {
            return Legend.this;
        }

        public String getName() {
            return "items";
        }
    };

    public final boolean isVertical() {
        return this.vertical.get();
    }

    public final void setVertical(boolean vertical) {
        this.vertical.set(vertical);
    }

    public final BooleanProperty verticalProperty() {
        return this.vertical;
    }

    public final void setItems(ObservableList<Legend.LegendItem> items) {
        this.itemsProperty().set(items);
    }

    public final ObservableList<Legend.LegendItem> getItems() {
        return (ObservableList) this.items.get();
    }

    public final ObjectProperty<ObservableList<Legend.LegendItem>> itemsProperty() {
        return this.items;
    }

    public Legend() {
        super(5.0D, 5.0D);
        this.setTileAlignment(Pos.CENTER_LEFT);
        this.setItems(FXCollections.observableArrayList());
        this.getStyleClass().setAll("chart-legend");
    }

    protected double computePrefWidth(double value) {
        return this.getItems().size() > 0 ? super.computePrefWidth(value) : 0.0D;
    }

    protected double computePrefHeight(double value) {
        return this.getItems().size() > 0 ? super.computePrefHeight(value) : 0.0D;
    }

    public static class LegendItem {
        private Label label;
        private StringProperty text;
        private ObjectProperty<Node> symbol;

        public final String getText() {
            return this.text.getValue();
        }

        public final void setText(String text) {
            this.text.setValue(text);
        }

        public final StringProperty textProperty() {
            return this.text;
        }

        public final Node getSymbol() {
            return (Node) this.symbol.getValue();
        }

        public final void setSymbol(Node symbol) {
            this.symbol.setValue(symbol);
        }

        public final ObjectProperty<Node> symbolProperty() {
            return this.symbol;
        }

        public LegendItem(String text) {
            this.label = new Label();
            this.text = new StringPropertyBase() {
                protected void invalidated() {
                    LegendItem.this.label.setText(this.get());
                }

                public Object getBean() {
                    return LegendItem.this;
                }

                public String getName() {
                    return "text";
                }
            };
            this.symbol = new ObjectPropertyBase<Node>(new Region()) {
                protected void invalidated() {
                    Node var1 = (Node) this.get();
                    if (var1 != null) {
                        var1.getStyleClass().setAll("chart-legend-item-symbol");
                    }

                    LegendItem.this.label.setGraphic(var1);
                }

                public Object getBean() {
                    return LegendItem.this;
                }

                public String getName() {
                    return "symbol";
                }
            };
            this.setText(text);
            this.label.getStyleClass().add("chart-legend-item");
            this.label.setAlignment(Pos.CENTER_LEFT);
            this.label.setContentDisplay(ContentDisplay.LEFT);
            this.label.setGraphic(this.getSymbol());
            this.getSymbol().getStyleClass().setAll("chart-legend-item-symbol");
        }

        public LegendItem(String text, Node symbol) {
            this(text);
            this.setSymbol(symbol);
        }
    }
}
