/*
 *  IImporter.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.io.imports.interfaces;


import java.io.IOException;
import java.util.List;

/**
 * importer interface
 * Daniel Huson, 1.2018
 */
public interface IImporter {
    /**
     * gets allowed file extensions
     *
     * @return extensions
     */
    List<String> getExtensions();

    /**
     * determines whether importer is applicable to data in given file
     *
     * @param fileName
     * @return true if applicable
     */
    boolean isApplicable(String fileName) throws IOException;
}
