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

package splitstree5.gui.connectorview;

import javafx.application.Application;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TaxaBlock;
import splitstree5.core.filters.SplitsFilter;

/**
 * test the connector view
 * Created by huson on 12/31/16.
 */
public class ConnectorViewTest extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Document document = new Document();
        TaxaBlock taxaBlock = new TaxaBlock();
        SplitsBlock splitsBlock1 = new SplitsBlock();
        SplitsBlock splitsBlock2 = new SplitsBlock();

        SplitsFilter splitsFilter = new SplitsFilter(taxaBlock, new ADataNode<>(splitsBlock1), new ADataNode<>(splitsBlock2));

        ConnectorView<SplitsBlock, SplitsBlock> connectorView = new ConnectorView<>(document, splitsFilter);

        connectorView.show();
    }
}