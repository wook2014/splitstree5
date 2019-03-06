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

package splitstree5.gui.methodstab;

import javafx.beans.property.ReadOnlyStringWrapper;
import jloda.fx.ResourceManagerFX;
import jloda.util.ProgramProperties;
import splitstree5.core.Document;
import splitstree5.gui.texttab.TextViewTab;

/**
 * the methods view tab
 */
public class MethodsViewTab extends TextViewTab {
    /**
     * constructor
     *
     * @param document
     */
    public MethodsViewTab(Document document) {
        super(new ReadOnlyStringWrapper("Methods"), document.methodsTextProperty());
        setIcon(ResourceManagerFX.getIcon("sun/toolbarButtonGraphics/general/History16.gif"));
        getTextArea().setFont(ProgramProperties.getDefaultFont());
        getTextArea().setWrapText(true);
        setClosable(false);
    }
}
