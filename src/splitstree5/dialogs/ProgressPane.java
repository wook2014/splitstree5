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

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Service;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

/**
 * A progress pane with cancel button
 * Daniel Huson, 1.2018
 */
public class ProgressPane extends StackPane {
    private final Label label;
    private final ProgressBar progressBar;
    private final Button stopButton;
    private final Tooltip tooltip;
    private boolean removed;

    /**
     * a progress pane with cancel button
     *
     * @param service
     */
    public ProgressPane(Service service) {
        this(service.titleProperty(), service.messageProperty(), service.progressProperty(), service.runningProperty(), service::cancel);
    }

    /**
     * a progress pane with cancel button
     *
     * @param titleProperty
     * @param messageProperty
     * @param progressProperty
     * @param isRunning
     * @param cancelRunnable
     */
    public ProgressPane(ReadOnlyStringProperty titleProperty, ReadOnlyStringProperty messageProperty, ReadOnlyDoubleProperty progressProperty, ReadOnlyBooleanProperty isRunning, Runnable cancelRunnable) {
        setPadding(new Insets(5, 10, 3, 40));
        setVisible(false);
        label = new Label();
        label.textProperty().bind(titleProperty.concat(": "));
        label.setFont(Font.font("System",10));
        progressBar = new ProgressBar();
        progressBar.progressProperty().bind(progressProperty);
        progressBar.setPrefHeight(label.getPrefHeight());
        stopButton = new Button("Cancel");
        stopButton.setFont(Font.font("System",10));
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

        // remove's itself once no longer running
        isRunning.addListener((c, o, n) -> {
            if (!n) {
                final Parent parent = getParent();
                if (parent != null && parent.getChildrenUnmodifiable().contains(this)) {
                    if (parent instanceof Group)
                        ((Group) getParent()).getChildren().remove(this);
                    else if (parent instanceof Pane)
                        ((Pane) getParent()).getChildren().remove(this);
                    removed = true;
                }
            }
        });


        (new Thread(() -> { // wait three seconds before showing the progress pane
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            Platform.runLater(() -> {
                if (!removed && isRunning.getValue()) {
                    setVisible(true);
                }
            });
        })).start();

    }
}
