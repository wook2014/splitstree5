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

package splitstree5.gui.dagview;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import splitstree5.core.datablocks.ADataNode;

/**
 * installs a mouse handler
 * Created by huson on 1/31/17.
 */
public class DagNodeViewMouseHandler {
    private double mouseDownX;
    private double mouseDownY;
    private boolean shiftDown;
    private final Line line = new Line();

    /**
     * setup the mouse handler
     *
     * @param nodeView
     */
    private DagNodeViewMouseHandler(Pane world, DagNodeView nodeView) {
        line.setStroke(Color.DARKGRAY);

        nodeView.setOnMousePressed((e) -> {
            mouseDownX = e.getSceneX();
            mouseDownY = e.getSceneY();
            shiftDown = e.isShiftDown();
            if (shiftDown) {
                line.setStartX(mouseDownX - world.localToScene(0, 0).getX());
                line.setStartY(mouseDownY - world.localToScene(0, 0).getY());
                line.setEndX(mouseDownX);
                line.setEndY(mouseDownY);
                world.getChildren().add(line);
            }
        });

        nodeView.setOnMouseDragged((e) -> {
            if (!shiftDown) {
                nodeView.xProperty().set(nodeView.xProperty().get() + (e.getSceneX() - mouseDownX));
                nodeView.yProperty().set(nodeView.yProperty().get() + (e.getSceneY() - mouseDownY));
                mouseDownX = e.getSceneX();
                mouseDownY = e.getSceneY();
            }
            if (shiftDown && nodeView.getANode() instanceof ADataNode) {
                line.setEndX(e.getSceneX() - world.localToScene(0, 0).getX());
                line.setEndY(e.getSceneY() - world.localToScene(0, 0).getY());

            }
        });

        nodeView.setOnMouseReleased((e) -> world.getChildren().remove(line));
    }

    /**
     * install a mouse handler
     *
     * @param nodeView
     */
    public static void install(Pane world, DagNodeView nodeView) {
        new DagNodeViewMouseHandler(world, nodeView);
    }
}
