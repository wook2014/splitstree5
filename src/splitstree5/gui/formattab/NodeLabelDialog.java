/*
 * NodeLabelDialog.java Copyright (C) 2020. Daniel H. Huson
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
 *
 */

package splitstree5.gui.formattab;

import jloda.fx.label.EditLabelDialog;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.main.MainWindow;

import java.util.Optional;

/**
 * presents the node label dialog
 * Daniel Huson, 6.2020
 */
public class NodeLabelDialog {

    public static void apply(MainWindow mainWindow, int taxonId, NodeViewBase nv) {
        final EditLabelDialog editLabelDialog = new EditLabelDialog(mainWindow.getStage(), nv.getLabel());
        final Optional<String> result = editLabelDialog.showAndWait();
        if (result.isPresent()) {
            final String newLabel = result.get();
            final TaxaBlock taxaBlock = mainWindow.getWorkflow().getTopTaxaNode().getDataBlock();
            taxaBlock.get(taxonId).setDisplayLabel(newLabel);
            nv.setLabel(newLabel);
        }
    }
}
