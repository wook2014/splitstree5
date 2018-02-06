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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import jloda.util.ProgramProperties;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

/**
 * manages recent files
 * Daniel Huson, 2.2018
 */
public class RecentFilesManager {
    private static RecentFilesManager instance;

    private final int maxNumberRecentFiles;
    private final ObservableList<String> recentFiles;
    private final ArrayList<WeakReference<Menu>> menuReferences = new ArrayList<>();
    private final Map<WeakReference<Menu>, Consumer<String>> menuRef2FileOpener = new HashMap<>();

    private RecentFilesManager() {

        recentFiles = FXCollections.observableArrayList();

        maxNumberRecentFiles = ProgramProperties.get("MaxNumberRecentFiles", 40);
        for (String fileName : ProgramProperties.get("RecentFiles", new String[0]))
            addRecentFile(fileName);


        recentFiles.addListener((ListChangeListener<String>) (c) -> {
            Platform.runLater(() -> {
                final Set<WeakReference<Menu>> deadRefs = new HashSet<>();

                while (c.next()) {
                    if (c.wasRemoved()) {
                        for (WeakReference<Menu> ref : menuReferences) {
                            final Menu menu = ref.get();
                            if (menu != null) {
                                final ArrayList<MenuItem> toDelete = new ArrayList<>();
                                for (MenuItem menuItem : menu.getItems()) {
                                    if (c.getRemoved().contains(menuItem.getText())) {
                                        toDelete.add(menuItem);

                                    }
                                }
                                menu.getItems().removeAll(toDelete);
                            } else
                                deadRefs.add(ref);
                        }
                    }
                    if (c.wasAdded()) {
                        for (WeakReference<Menu> ref : menuReferences) {
                            final Menu menu = ref.get();
                            if (menu != null) {
                                final Consumer<String> fileOpen = menuRef2FileOpener.get(ref);
                                for (String fileName : c.getAddedSubList()) {
                                    final MenuItem openMenuItem = new MenuItem(fileName);
                                    openMenuItem.setOnAction((e) -> {
                                                fileOpen.accept(fileName);
                                            }
                                    );
                                    menu.getItems().add(0, openMenuItem);
                                }
                            } else
                                deadRefs.add(ref);
                        }
                    }
                }

                if (deadRefs.size() > 0) {
                    menuRef2FileOpener.keySet().removeAll(deadRefs);
                    menuReferences.removeAll(deadRefs); // purge anything that has been garbage collected
                }
                ProgramProperties.put("RecentFiles", recentFiles.toArray(new String[recentFiles.size()]));
            });
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
     * @return recent files menuy
     */
    public void setupMenu(final Menu menu, Consumer<String> fileOpener) {
        final WeakReference<Menu> ref = new WeakReference<>(menu);
        menuRef2FileOpener.put(ref, fileOpener);
        menuReferences.add(ref);

        for (String fileName : recentFiles) {
            final MenuItem openMenuItem = new MenuItem(fileName);
            openMenuItem.setOnAction((e) -> {
                fileOpener.accept(fileName);
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
