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

package splitstree5.core.misc;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import splitstree5.core.Document;

/**
 * A processing graph node
 * Created by huson on 12/21/16.
 */
abstract public class ANode extends Named {
    private static int created = 0;
    private static final Object lock = new Object();
    private final int uid;
    private final Document document;

    private final ObjectProperty<UpdateState> state = new SimpleObjectProperty<>(UpdateState.VALID);

    /**
     * create a node
     *
     * @param document
     */
    public ANode(Document document) {
        this.document = document;
        synchronized (lock) {
            this.uid = (++created);
        }
    }

    public UpdateState getState() {
        return state.get();
    }

    public void setState(UpdateState state) {
        if (state != UpdateState.VALID && state != UpdateState.FAILED) {
            if (!document.invalidNodes().contains(this))
                document.invalidNodes().add(this);
        } else {
            if (document.invalidNodes().contains(this))
                document.invalidNodes().remove(this);
        }

        this.state.set(state);
    }

    public ObjectProperty<UpdateState> stateProperty() {
        return state;
    }

    public Document getDocument() {
        return document;
    }

    /**
     * gets the unique id of this node
     *
     * @return unique id
     */
    public int getUid() {
        return uid;
    }
}
