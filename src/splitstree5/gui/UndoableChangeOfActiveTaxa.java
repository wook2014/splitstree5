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

package splitstree5.gui;

import javafx.scene.control.ListView;
import splitstree5.core.misc.Taxon;
import splitstree5.utils.UndoableChange;

import java.util.ArrayList;
import java.util.List;

/**
 * active taxa change
 * Created by huson on 12/25/16.
 */
public class UndoableChangeOfActiveTaxa extends UndoableChange {
    private final ArrayList<Taxon> activeTaxa;
    private final ArrayList<Taxon> inactiveTaxa;

    private final List<Taxon> prevActiveTaxa;
    private final List<Taxon> prevInactiveTaxa;

    private final ListView<Taxon> activeTaxaView;
    private final ListView<Taxon> inactiveTaxaView;

    /**
     * constructor
     *
     * @param activeTaxaView
     * @param prevActiveTaxa
     * @param inactiveTaxaView
     * @param prevInactiveTaxa
     */
    public UndoableChangeOfActiveTaxa(ListView<Taxon> activeTaxaView, List<Taxon> prevActiveTaxa, ListView<Taxon> inactiveTaxaView, List<Taxon> prevInactiveTaxa) {
        this.activeTaxaView = activeTaxaView;
        this.activeTaxa = new ArrayList<>(activeTaxaView.getItems());
        this.prevActiveTaxa = prevActiveTaxa;
        this.inactiveTaxaView = inactiveTaxaView;
        this.inactiveTaxa = new ArrayList<>(inactiveTaxaView.getItems());
        this.prevInactiveTaxa = prevInactiveTaxa;
    }

    /**
     * get list of active taxa, constructed in this class
     *
     * @return list of active taxa
     */
    public ArrayList<Taxon> getActiveTaxa() {
        return activeTaxa;
    }

    /**
     * get list of inactive taxa, constructed in this class
     *
     * @return list of inactive taxa
     */
    public ArrayList<Taxon> getInactiveTaxa() {
        return inactiveTaxa;
    }

    @Override
    public void undo() {
        activeTaxaView.getItems().setAll(prevActiveTaxa);
        inactiveTaxaView.getItems().setAll(prevInactiveTaxa);
    }

    @Override
    public void redo() {
        activeTaxaView.getItems().setAll(activeTaxa);
        inactiveTaxaView.getItems().setAll(inactiveTaxa);
    }
}
