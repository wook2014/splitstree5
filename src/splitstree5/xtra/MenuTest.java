/*
 * MenuTest.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.xtra;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MenuTest extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MenuItem about = new MenuItem("About!");
        about.setOnAction((e) -> System.err.println(e.getSource()));

        MenuItem quit = new MenuItem("Quit!");
        about.setOnAction((e) -> System.err.println(e.getSource()));

        MenuItem preferences = new MenuItem("Preferences!");
        about.setOnAction((e) -> System.err.println(e.getSource()));


        final BorderPane root = new BorderPane();

        final Menu fileMenu = new Menu("File");

        fileMenu.getItems().addAll(new MenuItem("Open"), new MenuItem("Close"));

        MenuBar menuBar = new MenuBar(fileMenu, new Menu("Edit"));
        menuBar.useSystemMenuBarProperty().set(true);


        AppleSupportFX.apply(menuBar, about, quit, preferences);

        root.setTop(menuBar);

        root.setCenter(new TableView<>());

        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.sizeToScene();
        primaryStage.show();

        Platform.runLater(() -> AppleSupportFX.apply(menuBar, about, quit, preferences));


    }
}
