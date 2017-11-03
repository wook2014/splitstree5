/*
 *  Copyright (C) 2017 Daniel H. Huson
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

package splitstree5.core.dag;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import splitstree5.core.misc.ANamed;

/**
 * A DAG node
 * Created by huson on 12/21/16.
 */
abstract public class ANode extends ANamed {
    private static int created = 0;
    private static final Object lock = new Object();
    private final int uid;

    private final ObjectProperty<UpdateState> state = new SimpleObjectProperty<>(UpdateState.VALID);

    /**
     * constructor
     */
    public ANode() {
        synchronized (lock) {
            this.uid = (++created);
        }
    }

    public UpdateState getState() {
        return state.get();
    }

    public void setState(UpdateState state) {
        this.state.set(state);
    }

    public ObjectProperty<UpdateState> stateProperty() {
        return state;
    }

    /**
     * gets the unique id of this node
     *
     * @return unique id
     */
    public int getUid() {
        return uid;
    }

    /**
     * disconnect this node
     */
    abstract public void disconnect();

    /**
     * get all children of this node
     *
     * @return children
     */
    abstract public ObservableList<? extends ANode> getChildren();
}
