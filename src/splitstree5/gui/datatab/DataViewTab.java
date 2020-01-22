/*
 *  DataViewTab.java Copyright (C) 2020 Daniel H. Huson
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jloda.fx.util.ResourceManagerFX;
import splitstree5.core.Document;
import splitstree5.core.datablocks.DataBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.workflow.DataNode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.editinputtab.EditTextViewTab;

/**
 * a data block viewer tab
 * Daniel Huson, 1.2018
 */
public class DataViewTab extends EditTextViewTab {

    private final ChangeListener<UpdateState> stateChangeListener;

    /**
     * constructor
     *
     * @param document
     * @param dataNode
     */
    public DataViewTab(Document document, DataNode dataNode) {
        super(dataNode.nameProperty());

        setDataNode(dataNode);

        final DataBlock dataBlock = dataNode.getDataBlock();
        final StringProperty textProperty;
        if (dataBlock.getFormat() != null) {
            final TaxaBlock taxaBlock;
            if (document.getWorkflow().getTopNodes().contains(dataNode))
                taxaBlock = document.getWorkflow().getTopTaxaNode().getDataBlock();
            else
                taxaBlock = document.getWorkflow().getWorkingTaxaBlock();
            final GenericDatablockFormatToolBar toolBar = new GenericDatablockFormatToolBar(taxaBlock, dataBlock);
            textProperty = toolBar.textProperty();
            toolBar.setMinHeight(30);
            setToolBar(toolBar);
        } else {
            textProperty = new SimpleStringProperty(dataNode.getDataBlock().getDisplayText());
        }

        stateChangeListener = (c, o, n) -> {
            if (n == UpdateState.VALID) {
                textProperty.set(dataNode.getDataBlock().getDisplayText());
            }
        };
        dataNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
        //getTextArea().textProperty().bind(textProperty);
        bindToCodeArea(textProperty);

        setMainWindow(document.getMainWindow());
        if (getGraphic() instanceof Labeled) {
            Image icon = ResourceManagerFX.getIcon(dataNode.getName().replaceAll("^Input", "").replaceAll(".*]", "") + "16.gif");
            if (icon != null) {
                ((Labeled) getGraphic()).setGraphic(new ImageView(icon));
            }
        }
    }
}
