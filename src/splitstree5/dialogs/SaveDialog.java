/*
 * SaveDialog.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.dialogs;

import javafx.stage.FileChooser;
import jloda.fx.util.RecentFilesManager;
import jloda.fx.window.NotificationManager;
import jloda.util.FileUtils;
import jloda.util.ProgramProperties;
import splitstree5.core.Document;
import splitstree5.io.nexus.workflow.WorkflowNexusOutput;
import splitstree5.main.MainWindow;

import java.io.File;
import java.io.IOException;

public class SaveDialog {
    /**
     * save dialog
     *
     * @return true if saved
     */
    public static boolean showSaveDialog(MainWindow mainWindow, boolean asWorkflowOnly) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(asWorkflowOnly ? "Export Workflow" : "Save SplitsTree5 file");

        final File previousDir = new File(ProgramProperties.get("SaveDir", ""));
        if (previousDir.isDirectory()) {
            fileChooser.setInitialDirectory(previousDir);
        } else
            fileChooser.setInitialDirectory((new File(mainWindow.getDocument().getFileName()).getParentFile()));

        if (!asWorkflowOnly) {
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Files", "*.stree5", "*.nxs", "*.nex"));
			fileChooser.setInitialFileName(FileUtils.getFileNameWithoutPath(FileUtils.replaceFileSuffix(mainWindow.getDocument().getFileName(), ".stree5")));
        } else {
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SplitsTree5 Workflow Files", "*.wflow5"));
			fileChooser.setInitialFileName(FileUtils.getFileNameWithoutPath(FileUtils.replaceFileSuffix(mainWindow.getDocument().getFileName(), ".wflow5")));
        }

        final File selectedFile = fileChooser.showSaveDialog(mainWindow.getStage());

        boolean result = false;
        if (selectedFile != null) {
            if (selectedFile.getParentFile().isDirectory())
                ProgramProperties.put("SaveDir", selectedFile.getParent());
            try {
                final Document document = mainWindow.getDocument();
                new WorkflowNexusOutput().save(mainWindow.getWorkflow(), selectedFile, asWorkflowOnly);
                if (!asWorkflowOnly) {
                    document.setFileName(selectedFile.getPath());
                    mainWindow.getDocument().setDirty(false);
                    document.setHasSplitsTree5File(true);
                }
                if (!document.getFileName().endsWith(".tmp"))
                    RecentFilesManager.getInstance().insertRecentFile(document.getFileName());
                result = true;
            } catch (IOException ex) {
                NotificationManager.showError("Save FAILED: " + ex);
            }
        }
        return result;
    }
}
