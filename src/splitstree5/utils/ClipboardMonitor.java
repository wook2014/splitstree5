/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.utils;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.input.Clipboard;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * monitors the clipboard for availability of a string
 * Daniel Huson, 2.2018
 */
public class ClipboardMonitor {
    private static ClipboardMonitor instance;

    private final Clipboard clipboard;
    private final BooleanProperty stringAvailable = new SimpleBooleanProperty();

    /**
     * get the instance
     *
     * @return instance
     */
    public static ClipboardMonitor getInstance() {
        if (instance == null)
            instance = new ClipboardMonitor();
        return instance;
    }

    private ClipboardMonitor() {
        clipboard = Clipboard.getSystemClipboard();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> Platform.runLater(() -> stringAvailable.set(clipboard.hasString()))
                , 0, 2, TimeUnit.SECONDS);
    }

    public boolean isStringAvailable() {
        return stringAvailable.get();
    }

    public BooleanProperty stringAvailableProperty() {
        return stringAvailable;
    }
}
