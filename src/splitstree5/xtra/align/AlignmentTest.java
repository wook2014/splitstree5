/*
 *  AlignmentTest.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.xtra.align;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jloda.util.Pair;

public class AlignmentTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AlignmentView alignmentView = new AlignmentView();

        alignmentView.getSequences().addAll(
                new Pair<>("first", "agctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnag"),
                new Pair<>("second", "cgctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnag"),
                new Pair<>("third", "cgctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnag"),
                new Pair<>("fourth", "agctnagagctnagagctn----ctnagagctnagagctnagagctnagagctnagagctnagagctnag"),
                new Pair<>("fifth", "agctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnag"),
                new Pair<>("sixth", "agctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnagagctnag")
        );

        BorderPane borderPane = new BorderPane(alignmentView.getController().getAlignmentTableView());
        borderPane.setTop(alignmentView.getController().getToolBar());

        primaryStage.setScene(new Scene(borderPane));
        primaryStage.sizeToScene();
        primaryStage.show();
    }
}
