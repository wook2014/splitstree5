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

package splitstree5.gui.datatab;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jloda.util.ResourceManager;
import splitstree5.core.Document;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.texttab.TextViewTab;
import splitstree5.io.nexus.NexusFileWriter;

/**
 * a data block viewer tab
 * Daniel Huson, 1.2018
 */
public class DataViewTab extends TextViewTab {
    /**
     * constructor
     *
     * @param document
     * @param aNode
     */
    public DataViewTab(Document document, ADataNode aNode) {
        super(aNode.nameProperty());
        final StringProperty textProperty = new SimpleStringProperty(NexusFileWriter.toString(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), ((ADataNode) aNode).getDataBlock()));
        aNode.stateProperty().addListener((c, o, n) -> {
            if (n == UpdateState.VALID) {
                textProperty.set(NexusFileWriter.toString(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), ((ADataNode) aNode).getDataBlock()));
            }
        });
        getTextArea().textProperty().bind(textProperty);
        setMainWindow(document.getMainWindow());
        if (getGraphic() instanceof Labeled) {
            Image icon = ResourceManager.getIcon(aNode.getName().replaceAll("^Orig", "").replaceAll(".*]", "") + "16.gif");
            if (icon != null) {
                ((Labeled) getGraphic()).setGraphic(new ImageView(icon));
            }
        }
    }
}
