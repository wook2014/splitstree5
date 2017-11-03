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
package splitstree5.xtra;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import jloda.phylo.PhyloTree;
import splitstree5.xtra.phylotreeview.PhylogeneticTreeView;
import splitstree5.xtra.phylotreeview.TreeNode;

public class TestPhyloTreeView extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        //String treeString="((a,b),(c,(d,(e,f))),(g,h,i,j,(k,l)));";

        final String treeString = "(((((((((((40:1.0,12:1.0):1.0,(29:1.0,16:1.0):1.0):1.0,((97:1.0,15:1.0):1.0,91:1.0):1.0):1.0,76:1.0):1.0,73:1.0):1.0,(38:1.0,9:1.0):1.0):1.0,((46:1.0,(67:1.0,((74:1.0,39:1.0):1.0,51:1.0):1.0):1.0):1.0,(43:1.0,32:1.0):1.0):1.0):1.0,((1:1.0,79:1.0):1.0,(((37:1.0,98:1.0):1.0,(33:1.0,(69:1.0,86:1.0):1.0):1.0):1.0,100:1.0):1.0):1.0):1.0,28:1.0):1.0,((63:1.0,((24:1.0,(55:1.0,(48:1.0,90:1.0):1.0):1.0):1.0,(19:1.0,50:1.0):1.0):1.0):1.0,((89:1.0,25:1.0):1.0,((42:1.0,66:1.0):1.0,60:1.0):1.0):1.0):1.0):1.0,(((14:1.0,(((((57:1.0,(((61:1.0,45:1.0):1.0,11:1.0):1.0,(94:1.0,(82:1.0,22:1.0):1.0):1.0):1.0):1.0,((70:1.0,53:1.0):1.0,(68:1.0,52:1.0):1.0):1.0):1.0,(85:1.0,((54:1.0,93:1.0):1.0,75:1.0):1.0):1.0):1.0,(2:1.0,(((20:1.0,44:1.0):1.0,(6:1.0,88:1.0):1.0):1.0,26:1.0):1.0):1.0):1.0,(95:1.0,(10:1.0,87:1.0):1.0):1.0):1.0):1.0,((71:1.0,49:1.0):1.0,(99:1.0,65:1.0):1.0):1.0):1.0,(((31:1.0,((64:1.0,23:1.0):1.0,72:1.0):1.0):1.0,((((35:1.0,3:1.0):1.0,17:1.0):1.0,80:1.0):1.0,(77:1.0,(5:1.0,(78:1.0,7:1.0):1.0):1.0):1.0):1.0):1.0,((((21:1.0,(((47:1.0,27:1.0):1.0,(30:1.0,84:1.0):1.0):1.0,36:1.0):1.0):1.0,((18:1.0,(41:1.0,((92:1.0,(34:1.0,13:1.0):1.0):1.0,4:1.0):1.0):1.0):1.0,((96:1.0,62:1.0):1.0,(83:1.0,(58:1.0,81:1.0):1.0):1.0):1.0):1.0):1.0,(8:1.0,56:1.0):1.0):1.0,59:1.0):1.0):1.0):1.0);";

        final PhyloTree tree = new PhyloTree();
        tree.parseBracketNotation(treeString, true);

        System.err.println(tree.toBracketString(true) + ";");

        BorderPane borderPane = new BorderPane();
        Group world = new Group();
        Rectangle rect = new Rectangle(-10, -10, 0, 0);
        rect.setFill(Color.TRANSPARENT);
        world.getChildren().add(rect);
        borderPane.setCenter(new ScrollPane(world));

        Scene scene = new Scene(borderPane, 500, 500);

        primaryStage.setScene(scene);
        primaryStage.show();

        TreeNode root = new PhyloTreeNode(tree.getRoot());

        PhylogeneticTreeView treeView = new PhylogeneticTreeView(root);

        treeView.update();

        world.getChildren().add(treeView);

        setMouseEventHandlers(world);
    }

    public void setMouseEventHandlers(Group world) {
        final DoubleProperty mouseDownX = new SimpleDoubleProperty();
        final DoubleProperty mouseDownY = new SimpleDoubleProperty();

        world.setOnMousePressed((e) -> {
            mouseDownX.set(e.getScreenX());
            mouseDownY.set(e.getScreenY());
        });

        world.setOnMouseDragged((e) -> {
            double x = mouseDownX.get();
            double y = mouseDownY.get();
            double deltaX = e.getScreenX() - x;
            double deltaY = e.getScreenY() - y;

            if (deltaX > 0) {
                if (1.1 * world.getScaleX() < 100) {
                    world.setScaleX(1.1 * world.getScaleX());
                    world.setScaleY(1.1 * world.getScaleY());
                }
            } else if (deltaX < 0) {
                if (1 / 1.1 * world.getScaleX() > 0.5) {
                    world.setScaleX(1 / 1.1 * world.getScaleX());
                    world.setScaleY(1 / 1.1 * world.getScaleY());
                }
            }
        });
    }
}
