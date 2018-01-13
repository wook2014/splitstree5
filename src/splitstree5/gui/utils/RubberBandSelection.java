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

/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.gui.utils;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Shows a rubber band and calls a handler
 * Daniel Huson, 1.2018
 */
public class RubberBandSelection {
    @FunctionalInterface
    public interface Handler {
        /**
         * handle a rubber band selection
         *
         * @param rectangle       in scene coordinates
         * @param extendSelection true if shift key down
         */
        void handle(Rectangle2D rectangle, boolean extendSelection);
    }

    private final Rectangle rectangle;
    private Point2D start;
    private Point2D end;
    private Handler handler;


    /**
     * constructor
     *
     * @param pane    node on which mouse can be clicked and dragged to show rubber band
     * @param group   group into which rubber band should be temporarily added so that it appears in the scene
     * @param handler this is called when rubber band is released
     */
    public RubberBandSelection(final Pane pane, final Group group, final Handler handler) {
        this.handler = handler;
        rectangle = new Rectangle();

        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.GOLDENROD);

        pane.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            start = group.screenToLocal(e.getScreenX(), e.getScreenY());
            if (start != null) {
                end = null;
                rectangle.setX(start.getX());
                rectangle.setY(start.getY());
                rectangle.setWidth(0);
                rectangle.setHeight(0);
            }
            e.consume();
        });

        pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, (e) -> {
            if (start != null) {
                if (end == null) {
                    group.getChildren().add(rectangle);
                }

                end = group.screenToLocal(e.getScreenX(), e.getScreenY());
                rectangle.setX(Math.min(start.getX(), end.getX()));
                rectangle.setY(Math.min(start.getY(), end.getY()));
                rectangle.setWidth(Math.abs(end.getX() - start.getX()));
                rectangle.setHeight(Math.abs(end.getY() - start.getY()));
                e.consume();
            }
        });

        pane.addEventHandler(MouseEvent.MOUSE_RELEASED, (e) -> {
            if (start != null) {
                group.getChildren().remove(rectangle);
                start = null;
                e.consume();
                if (this.handler != null && rectangle.getWidth() > 0 && rectangle.getHeight() > 0) {
                    Point2D min = group.localToScene(rectangle.getX(), rectangle.getY());
                    Point2D max = group.localToScene(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());
                    this.handler.handle(new Rectangle2D(min.getX(), min.getY(), max.getX() - min.getX(), max.getY() - min.getY()), e.isShiftDown());
                }
            }
        });
    }

    public Handler getHandler() {
        return handler;
    }

    /**
     * set the handler
     *
     * @param handler handles selections
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
