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

package splitstree5.core;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import jloda.util.Basic;
import splitstree5.core.dag.DAG;
import splitstree5.core.misc.Taxon;
import splitstree5.utils.ASelectionModel;

import java.util.List;

/**
 * a document
 * Created by huson on 12/25/16.
 */
public class Document {
    private final DAG dag;
    private final StringProperty fileName = new SimpleStringProperty();
    private final ASelectionModel<Taxon> taxaSelectionModel = new ASelectionModel<>();

    /**
     * constructor
     */
    public Document() {
        dag = new DAG();
    }

    /**
     * setup the taxon selection model (after top and working taxa nodes have been set)
     */
    public void setupTaxonSelectionModel() {
        if (dag.getTopTaxaNode() != null) {
            dag.getTopTaxaNode().stateProperty().addListener((observable, oldValue, newValue) -> {
                final List<Taxon> taxa = dag.getTopTaxaNode().getDataBlock().getTaxa();
                taxaSelectionModel.setItems(taxa.toArray(new Taxon[taxa.size()]));
            });
            final List<Taxon> taxa = dag.getTopTaxaNode().getDataBlock().getTaxa();
            taxaSelectionModel.setItems(taxa.toArray(new Taxon[taxa.size()]));
        }

        // todo: for debugging:
        taxaSelectionModel.getSelectedItems().addListener((ListChangeListener<Taxon>) c -> System.err.println("Taxa selection changed: " +
                Basic.toString(taxaSelectionModel.getSelectedItems(), ",")));
    }


    public DAG getDag() {
        return dag;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public ReadOnlyBooleanProperty updatingProperty() {
        return dag.updatingProperty();
    }

    public ASelectionModel<Taxon> getTaxaSelectionModel() {
        return taxaSelectionModel;
    }
}
