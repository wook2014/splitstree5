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

package splitstree5.dialogs;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * A progress pane  with stop button
 * Daniel Huson, 1.2018
 */
public class ProgressPane extends StackPane {
    private final Label label;
    private final ProgressBar progressBar;
    private final Button stopButton;
    private final Tooltip tooltip;

    public ProgressPane(ReadOnlyStringProperty titleProperty, ReadOnlyStringProperty messageProperty, ReadOnlyDoubleProperty progressProperty, ReadOnlyBooleanProperty isRunning, Runnable cancelRunnable) {
        setPadding(new Insets(1, 10, 1, 50));
        label = new Label();
        label.textProperty().bind(titleProperty.concat(": "));
        progressBar = new ProgressBar();
        progressBar.progressProperty().bind(progressProperty);
        progressBar.setPrefHeight(label.getPrefHeight());
        stopButton = new Button("Cancel");
        stopButton.setMaxHeight(label.getPrefHeight());
        stopButton.disableProperty().bind(isRunning.not());
        stopButton.setOnAction((e) -> cancelRunnable.run());
        final HBox hBox = new HBox(label, progressBar, stopButton);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(5);

        tooltip = new Tooltip();
        tooltip.textProperty().bind(titleProperty.concat(": ").concat(messageProperty));
        Tooltip.install(label, tooltip);
        Tooltip.install(progressBar, tooltip);
        stopButton.setTooltip(new Tooltip("Cancel this computation"));

        getChildren().add(hBox);
    }
}
