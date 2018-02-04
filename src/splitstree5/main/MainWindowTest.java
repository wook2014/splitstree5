/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.main;

import javafx.application.Application;
import javafx.stage.Stage;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;
import splitstree5.dialogs.importer.FileOpener;

public class MainWindowTest extends Application {
    @Override
    public void init() {
        ProgramProperties.getProgramIcons().setAll(ResourceManager.getIcon("SplitsTree5-16.png"), ResourceManager.getIcon("SplitsTree5-32.png"),
                ResourceManager.getIcon("SplitsTree5-64.png"), ResourceManager.getIcon("SplitsTree5-128.png"));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final MainWindow mainWindow = new MainWindow();

        FileOpener.open(mainWindow, "test/nexus/characters-simple.nex");

        mainWindow.show(primaryStage, 100, 100);
    }
}

