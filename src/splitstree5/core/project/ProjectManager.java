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

package splitstree5.core.project;

import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.stage.Stage;
import splitstree5.main.MainWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * manages projects
 * Daniel Huson, 1.2018
 */
public class ProjectManager {
    private final ArrayList<MainWindow> mainWindows;
    private final Map<MainWindow, ArrayList<Stage>> mainWindows2AdditionalWindows;

    private final LongProperty changed = new SimpleLongProperty(0);

    private final ObservableSet<String> previousSelection;

    private static ProjectManager instance;

    /**
     * constructor
     */
    private ProjectManager() {
        mainWindows = new ArrayList<>();
        mainWindows2AdditionalWindows = new HashMap<>();
        previousSelection = FXCollections.observableSet();
    }

    /**
     * get the instance
     *
     * @return instance
     */
    public static ProjectManager getInstance() {
        if (instance == null)
            instance = new ProjectManager();
        return instance;
    }

    public int size() {
        return mainWindows.size();
    }

    /**
     * get the previous selection list
     *
     * @return previous selection
     */
    public ObservableSet<String> getPreviousSelection() {
        return previousSelection;
    }

    public void addMainWindow(MainWindow mainWindow) {
        mainWindows.add(mainWindow);
        mainWindows2AdditionalWindows.put(mainWindow, new ArrayList<>());
        changed.set(changed.get() + 1);
    }

    public void removeMainWindow(MainWindow mainWindow) {
        mainWindows.remove(mainWindow);
        mainWindows2AdditionalWindows.remove(mainWindow);
        changed.set(changed.get() + 1);
        if (mainWindows.size() == 0)
            Platform.exit();
    }

    public MainWindow getMainWindow(int index) {
        return mainWindows.get(index);
    }

    public void addAuxiliaryWindow(MainWindow mainWindow, Stage stage) {
        mainWindows2AdditionalWindows.get(mainWindow).add(stage);
        changed.set(changed.get() + 1);
    }

    public void removeAuxiliaryWindow(MainWindow mainWindow, Stage stage) {
        if (mainWindows2AdditionalWindows.containsKey(mainWindow)) {
            mainWindows2AdditionalWindows.get(mainWindow).remove(stage);
            changed.set(changed.get() + 1);
        }
    }

    public ReadOnlyLongProperty changedProperty() {
        return changed;
    }

    public ArrayList<MainWindow> getMainWindows() {
        return mainWindows;
    }

    public ArrayList<Stage> getAuxiliaryWindows(MainWindow mainWindow) {
        return mainWindows2AdditionalWindows.get(mainWindow);
    }
}
