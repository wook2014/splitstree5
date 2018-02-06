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

package splitstree5.dialogs.importer;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import jloda.util.CanceledException;
import splitstree5.core.workflow.TaskWithProgressListener;
import splitstree5.dialogs.ProgressPane;
import splitstree5.io.imports.interfaces.IImporter;
import splitstree5.main.MainWindow;

/**
 * performs import as service in separate thread
 * Daniel Huson, 1.2018
 */
public class ImportService extends Service<Boolean> {
    private MainWindow parentMainWindow;
    private IImporter importer;
    private String fileName;
    private String title;

    /**
     * setup a task
     *
     * @param parentMainWindow
     * @param importer
     * @param fileName
     * @param progressBarParent
     */
    public void setup(MainWindow parentMainWindow, IImporter importer, String fileName, String title, Pane progressBarParent) {
        this.parentMainWindow = parentMainWindow;
        this.importer = importer;
        this.fileName = fileName;
        this.title = title;

        final ProgressPane progressPane = new ProgressPane(titleProperty(), messageProperty(), progressProperty(), runningProperty(), this::cancel);
        if (progressBarParent != null)
            progressBarParent.getChildren().add(progressPane);
    }

    @Override
    protected Task<Boolean> createTask() {
        return new TaskWithProgressListener<Boolean>() {
            @Override
            public Boolean call() {
                updateTitle(title);
                try {
                    getProgressListener().setMaximum(0);
                    getProgressListener().setProgress(0);
                } catch (CanceledException e) {
                    cancel();
                }
                Importer.apply(getProgressListener(), parentMainWindow, importer, fileName);
                return true;
            }
        };
    }
}
