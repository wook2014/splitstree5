/*
 * FileOpener.java Copyright (C) 2022 Daniel H. Huson
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

package splitstree5.dialogs.importer;

import javafx.scene.layout.Pane;
import jloda.fx.window.NotificationManager;
import splitstree5.io.imports.interfaces.IImporter;
import splitstree5.main.MainWindow;

import java.io.File;
import java.util.function.Consumer;

import static splitstree5.dialogs.importer.ImporterManager.UNKNOWN_FORMAT;

/**
 * opens a file by importing it without showing the import dialog
 */
public class FileOpener {
    /**
     * can this file be opened?
     *
	 */
    public static boolean isOpenable(String fileName) {
        if (!(new File(fileName)).canRead())
            return false;
        final ImporterManager.DataType dataType = ImporterManager.getInstance().getDataType(fileName);
        final String fileFormat = ImporterManager.getInstance().getFileFormat(fileName);
        return !dataType.equals(ImporterManager.DataType.Unknown) && !fileFormat.equals(UNKNOWN_FORMAT);
    }

    /**
     * open the named file
     *
	 */
    public static void open(boolean reload, MainWindow parentMainWindow, Pane statusPane, String fileName, Consumer<Throwable> exceptionHandler) {
        if (!(new File(fileName)).canRead())
            NotificationManager.showError("File not found or unreadable: " + fileName);
        else {
            final ImporterManager.DataType dataType = ImporterManager.getInstance().getDataType(fileName);
            final String fileFormat = ImporterManager.getInstance().getFileFormat(fileName);
            if (!dataType.equals(ImporterManager.DataType.Unknown) && !fileFormat.equals(UNKNOWN_FORMAT)) {
                final IImporter importer = ImporterManager.getInstance().getImporterByDataTypeAndFileFormat(dataType, fileFormat);
                if (importer == null)
                    NotificationManager.showError("Can't open file '" + fileName + "'\nUnknown data type or file format");
                else {
                    final ImportService importService = new ImportService();
                    importService.setup(reload, parentMainWindow, importer, fileName, "Loading file", statusPane);
                    importService.setOnCancelled((e) -> NotificationManager.showWarning("User canceled"));
                    importService.setOnFailed((e) -> {
                        NotificationManager.showError("Import of " + fileName + " (" + dataType + "/" + fileFormat + ") failed:\n" + (importService.getException().getCause() != null ? importService.getException().getCause().getMessage() : importService.getException().getMessage()));
                        if (exceptionHandler != null)
                            exceptionHandler.accept(importService.getException());
                    });
                    importService.start();
                }
            } else {
                ImportDialog.show(parentMainWindow, fileName);
            }
        }
    }
}
