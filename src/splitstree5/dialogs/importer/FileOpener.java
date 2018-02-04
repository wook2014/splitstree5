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

package splitstree5.dialogs.importer;

import splitstree5.gui.utils.Alert;
import splitstree5.io.imports.interfaces.IImporter;
import splitstree5.main.MainWindow;

import static splitstree5.dialogs.importer.ImporterManager.UNKNOWN;

/**
 * opens a file by importing it without showing the import dialog
 */
public class FileOpener {
    /**
     * can this file be opened?
     *
     * @param fileName
     * @return
     */
    public static boolean isOpenable(String fileName) {
        final String dataType = ImporterManager.getInstance().getDataType(fileName);
        final String fileFormat = ImporterManager.getInstance().getFileFormat(fileName);
        return !dataType.equals(UNKNOWN) && !fileFormat.equals(UNKNOWN);
    }

    /**
     * open the named file
     *
     * @param parentMainWindow
     * @param fileName
     */
    public static void open(MainWindow parentMainWindow, String fileName) {

        final String dataType = ImporterManager.getInstance().getDataType(fileName);
        final String fileFormat = ImporterManager.getInstance().getFileFormat(fileName);
        final IImporter importer = ImporterManager.getInstance().getImporterByDataTypeAndFileFormat(dataType, fileFormat);
        if (importer == null)
            new Alert("Unknown data type or fileFormat");
        else {
            final ImportService importService = new ImportService();
            importService.setup(parentMainWindow, importer, fileName, null);
            importService.start();
        }
    }
}
