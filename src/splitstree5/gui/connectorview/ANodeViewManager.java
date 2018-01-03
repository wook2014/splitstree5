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

package splitstree5.gui.connectorview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import splitstree5.core.Document;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.workflow.ANode;
import splitstree5.core.workflow.UpdateState;
import splitstree5.gui.textview.TextView;
import splitstree5.io.nexus.NexusFileWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * manages node viewers
 * Daniel Huson, 1.2018
 */
public class ANodeViewManager {
    private final Map<ANode, IShowable> map;

    private static ANodeViewManager instance;

    public static ANodeViewManager getInstance() {
        if (instance == null)
            instance = new ANodeViewManager();
        return instance;
    }

    private ANodeViewManager() {
        map = new HashMap<>();
    }

    public IShowable show(Document document, ANode aNode, double x, double y) {
        if (!map.containsKey(aNode)) {
            try {
                if (aNode instanceof AConnector)
                    map.put(aNode, new ConnectorView(document, (AConnector) aNode));
                else if (aNode instanceof ADataNode) {
                    final StringProperty textProperty = new SimpleStringProperty(NexusFileWriter.toString(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), ((ADataNode) aNode).getDataBlock()));
                    final ChangeListener<UpdateState> stateChangeListener = (observable, oldValue, newValue) -> textProperty.set(NexusFileWriter.toString(document.getWorkflow().getWorkingTaxaNode().getDataBlock(), ((ADataNode) aNode).getDataBlock()));
                    aNode.stateProperty().addListener(new WeakChangeListener<>(stateChangeListener));
                    map.put(aNode, new TextView(aNode.nameProperty(), textProperty));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        map.get(aNode).show(x, y);
        return map.get(aNode);
    }
}
