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

package jloda.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import jloda.util.ProgramProperties;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.main.MainWindow;

import java.util.ArrayList;

/**
 * manages recent files
 * Daniel Huson, 2.2018
 */
public class RecentFilesManager {
    private static RecentFilesManager instance;

    private final int maxNumberRecentFiles;
    private final ObservableList<String> recentFiles;

    private RecentFilesManager() {
        recentFiles = FXCollections.observableArrayList();
        maxNumberRecentFiles = ProgramProperties.get("MaxNumberRecentFiles", 40);
        for (String fileName : ProgramProperties.get("RecentFiles", new String[0]))
            addRecentFile(fileName);
        recentFiles.addListener((InvalidationListener) (c) -> {
            synchronized (recentFiles) {
                ProgramProperties.put("RecentFiles", recentFiles.toArray(new String[recentFiles.size()]));
            }
        });
    }

    /**
     * get the instance
     *
     * @return instance
     */
    public static RecentFilesManager getInstance() {
        if (instance == null)
            instance = new RecentFilesManager();
        return instance;
    }

    /**
     * create the recent files menu
     *
     * @param mainWindow
     * @return recent files menuy
     */
    public void setupMenu(MainWindow mainWindow, final Menu menu) {
        final ListChangeListener<String> changeListener = (c) -> {
            Platform.runLater(() -> {
                while (c.next()) {
                    for (String fileName : c.getAddedSubList()) {
                        final MenuItem openMenuItem = new MenuItem(fileName);
                        openMenuItem.setOnAction((e) -> {
                                    FileOpener.open(mainWindow, fileName);
                                }
                        );
                        menu.getItems().add(0, openMenuItem);
                    }
                    if (c.getRemovedSize() > 0) {
                        final ArrayList<MenuItem> toDelete = new ArrayList<>();
                        for (MenuItem menuItem : menu.getItems()) {
                            if (c.getRemoved().contains(menuItem.getText())) {
                                toDelete.add(menuItem);
                            }
                        }
                        menu.getItems().removeAll(toDelete);

                    }
                }
            });
        };
        menu.setUserData(changeListener); // need to keep a reference
        recentFiles.addListener(new WeakListChangeListener<>(changeListener));
        for (String fileName : recentFiles) {
            final MenuItem openMenuItem = new MenuItem(fileName);
            openMenuItem.setOnAction((e) -> {
                        FileOpener.open(mainWindow, fileName);
                    }
            );
            menu.getItems().add(0, openMenuItem);
        }
    }

    public ObservableList<String> getRecentFiles() {
        return recentFiles;
    }

    public void addRecentFile(String fileName) {
        // remove if already present and then add, this will bring to top of list
        if (recentFiles.contains(fileName))
            removeRecentFile(fileName);
        recentFiles.add(fileName);
        if (recentFiles.size() >= maxNumberRecentFiles)
            recentFiles.remove(maxNumberRecentFiles - 1);
    }

    public boolean removeRecentFile(String fileName) {
        return recentFiles.remove(fileName);
    }
}
