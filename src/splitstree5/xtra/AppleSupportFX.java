/*
 * AppleSupportFX.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.xtra;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import jloda.util.ProgramProperties;

import java.awt.*;

/**
 * setups up apple menus
 * todo: this does not work
 * Daniel Huson, 5.2019
 */
public class AppleSupportFX {
    public static void apply(MenuBar menuBar, MenuItem about, MenuItem quit, MenuItem preferences) {
        if (ProgramProperties.isMacOS()) {
            final Desktop desktop = Desktop.getDesktop();

            if (about != null)
                desktop.setAboutHandler(e -> Platform.runLater(() -> about.getOnAction().handle(null)));
            if (quit != null)
                desktop.setQuitHandler((e, r) -> Platform.runLater(() -> quit.getOnAction().handle(null)));
            if (preferences != null)
                desktop.setPreferencesHandler(e -> Platform.runLater(() -> preferences.getOnAction().handle(null)));

            for (Menu menu : menuBar.getMenus()) {
                if (quit != null)
                    removeFromMenu(menu, quit);
                if (about != null)
                    removeFromMenu(menu, about);
                if (preferences != null)
                    removeFromMenu(menu, preferences);
            }
        }
    }

    private static void removeFromMenu(Menu menu, MenuItem item) {
        int pos = menu.getItems().indexOf(item);
        if (pos != -1) {
            boolean separatorBefore = (pos > 0 && menu.getItems().get(pos - 1) instanceof SeparatorMenuItem);
            boolean separatorAfter = (pos < menu.getItems().size() - 1 && menu.getItems().get(pos + 1) instanceof SeparatorMenuItem);

            if ((pos == 0 || separatorBefore) && separatorAfter) {
                menu.getItems().remove(pos + 1);
                menu.getItems().remove(pos);
            } else if (separatorBefore && pos == menu.getItems().size() - 1) {
                menu.getItems().remove(pos);
                menu.getItems().remove(pos - 1);
            } else
                menu.getItems().remove(pos);
        }
    }
}
