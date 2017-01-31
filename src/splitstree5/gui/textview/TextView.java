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

package splitstree5.gui.textview;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.utils.ExtendedFXMLLoader;

import java.io.IOException;

/**
 * text view
 * Created by huson on 1/27/17.
 */
public class TextView {
    private final Parent root;
    private final TextViewController controller;
    private Stage stage;

    public TextView(String text) throws IOException {
        final ExtendedFXMLLoader<TextViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        controller.getTextArea().setText(text);
        controller.getTextArea().setEditable(false);
        controller.getTextArea().setFont(new Font("Courier", 12));
    }

    /**
     * show this view
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("Text Viewer - SplitsTree5");
        stage.setScene(new Scene(root, 600, 400));

        stage.setX(100 + ConnectorView.windowCount * 40);
        stage.setY(200 + ConnectorView.windowCount * 40);
        ConnectorView.windowCount++;

        stage.show();

        controller.getCloseButton().setOnAction((e) -> stage.close());
    }
}
