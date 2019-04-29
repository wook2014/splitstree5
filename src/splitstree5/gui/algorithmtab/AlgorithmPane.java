/*
 *  AlgorithmPane.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.algorithmtab;

import javafx.beans.property.BooleanProperty;
import javafx.scene.layout.StackPane;
import jloda.fx.undo.UndoManager;
import splitstree5.core.Document;
import splitstree5.core.workflow.Connector;

/**
 * An algorithm controller pane
 * Daniel Huson 1/2017
 */
public abstract class AlgorithmPane extends StackPane {
    /**
     * sets the document
     *
     * @param document
     */
    public void setDocument(Document document) {
    }

    /**
     * sets the undo manager
     *
     * @param undoManager
     */
    public void setUndoManager(UndoManager undoManager) {
    }

    /**
     * sets the connector
     *
     * @param connector
     */
    public void setConnector(Connector connector) {
    }

    /**
     * setup
     */
    abstract public void setup();

    /**
     * syncs the model to the view
     */
    abstract public void syncModel2Controller();

    /**
     * syncs the view to the model
     */
    abstract public void syncController2Model();

    /**
     * is algorithm applicable with current setings?
     *
     * @return is applicable
     */
    abstract public BooleanProperty applicableProperty();
}
