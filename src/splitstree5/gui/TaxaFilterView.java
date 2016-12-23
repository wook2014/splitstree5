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

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import splitstree5.core.connectors.AConnectorNode;
import splitstree5.core.datablocks.TaxaBlock;

/**
 * dialog for filtering taxa
 * Created by huson on 12/23/16.
 */
public class TaxaFilterView {
    private final Pane root;
    private AConnectorNode<TaxaBlock, TaxaBlock> connector;

    /**
     * constructor
     *
     * @param connector
     */
    public TaxaFilterView(AConnectorNode<TaxaBlock, TaxaBlock> connector) {
        this.connector = connector;
        root = setup();
    }

    /**
     * show this view
     */
    public void show() {
        Stage stage = new Stage();
        stage.setTitle("My New Stage Title");
        stage.setScene(new Scene(root, 450, 450));
        stage.show();
    }


    private Pane setup() { // todo: use FXML
        final BorderPane pane = new BorderPane();


        return pane;
    }


}
