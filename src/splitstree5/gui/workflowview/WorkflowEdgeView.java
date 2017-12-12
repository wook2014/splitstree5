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

package splitstree5.gui.workflowview;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;

/**
 * workflow edge view
 * Daniel Huson, 1.2017
 */
public class WorkflowEdgeView extends Group {
    private final Line line;

    /**
     * constructor
     *
     * @param source
     * @param target
     */
    public WorkflowEdgeView(WorkflowNodeView source, WorkflowNodeView target) {
        line = new Line();

        line.startXProperty().bind(source.xProperty().add(source.widthProperty().divide(2)));
        line.startYProperty().bind(source.yProperty().add(source.heightProperty().divide(2)));
        line.endXProperty().bind(target.xProperty().add(target.widthProperty().divide(2)));
        line.endYProperty().bind(target.yProperty().add(target.heightProperty().divide(2)));

        final ArrowHead arrowHead = new ArrowHead();
        arrowHead.update(line);

        line.startXProperty().addListener((observable, oldValue, newValue) -> arrowHead.update(line));
        line.startYProperty().addListener((observable, oldValue, newValue) -> arrowHead.update(line));
        line.endXProperty().addListener((observable, oldValue, newValue) -> arrowHead.update(line));
        line.endYProperty().addListener((observable, oldValue, newValue) -> arrowHead.update(line));

        this.getChildren().addAll(line, arrowHead);
    }


    /**
     * compute angle of vector in radian
     *
     * @param p
     * @return angle of vector in radian
     */
    public static double computeAngle(Point2D p) {
        if (p.getX() != 0) {
            double x = Math.abs(p.getX());
            double y = Math.abs(p.getY());
            double a = Math.atan(y / x);

            if (p.getX() > 0) {
                if (p.getY() > 0)
                    return a;
                else
                    return 2.0 * Math.PI - a;
            } else // p.getX()<0
            {
                if (p.getY() > 0)
                    return Math.PI - a;
                else
                    return Math.PI + a;
            }
        } else if (p.getY() > 0)
            return 0.5 * Math.PI;
        else // p.y<0
            return -0.5 * Math.PI;
    }

    private class ArrowHead extends Group {
        private Line part1 = new Line();
        private Line part2 = new Line();

        public ArrowHead() {
            getChildren().add(part1);
            getChildren().add(part2);
        }

        public void update(Line line) {
            Point2D start = new Point2D(line.getStartX(), line.getStartY());
            Point2D end = new Point2D(line.getEndX(), line.getEndY());
            double radian = computeAngle(end.subtract(start));

            (new Point2D(line.getStartX(), line.getStartY())).angle(line.getEndX(), line.getEndY());

            double dx = 10 * Math.cos(radian);
            double dy = 10 * Math.sin(radian);

            Point2D mid = start.midpoint(end);
            Point2D head = mid.add(dx, dy);
            Point2D one = mid.add(-dy, dx);
            Point2D two = mid.add(dy, -dx);

            part1.setStartX(one.getX());
            part1.setStartY(one.getY());
            part1.setEndX(head.getX());
            part1.setEndY(head.getY());

            part2.setStartX(two.getX());
            part2.setStartY(two.getY());
            part2.setEndX(head.getX());
            part2.setEndY(head.getY());
        }
    }
}
