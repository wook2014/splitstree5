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

package splitstree5.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * FX utilities
 * Created by huson on 12/23/16.
 */
public class FXUtils {
    /**
     * load the FXML from the fxml file associated with a class
     * For example, if the path for clazz is splitstree5.gui.TaxaFilterView, parses the file splitstree5.gui.TaxaFilterView.fxml
     *
     * @param clazz
     * @return parent node
     */
    public static Parent loadAssociatedFXML(Class clazz) throws IOException {
        final String path = clazz.getCanonicalName().replaceAll("\\.", File.separator) + ".fxml";
        final URL url = clazz.getClassLoader().getResource(path);
        if (url == null)
            throw new IOException("Failed to load resource: " + path);
        return FXMLLoader.load(url);
    }
}
