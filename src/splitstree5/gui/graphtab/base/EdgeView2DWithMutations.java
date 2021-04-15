/*
 * EdgeView2DWithMutations.java Copyright (C) 2021. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.graphtab.base;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import jloda.fx.util.GeometryUtilsFX;
import jloda.graph.Edge;
import jloda.util.Basic;
import splitstree5.core.algorithms.views.NetworkEmbedder;

/**
 * edge view that can show mutatuons
 * Daniel Huson, 2.2018
 */
public class EdgeView2DWithMutations extends EdgeView2D {
    private NetworkEmbedder.MutationView mutationView;

    private final int[] mutations;

    /**
     * create an edge view
     *
     * @param e
     * @param weight
     * @param start
     * @param end
     * @return edge view
     */
    public EdgeView2DWithMutations(Edge e, Double weight, final Point2D start, final Point2D end, int[] mutations, NetworkEmbedder.MutationView mutationView) {
        super(e, start, end, null);
        this.mutations = mutations;
        this.mutationView = mutationView;
        setCoordinates(start, end);
    }

    @Override
    public void rotateCoordinates(double angle) {
        for (Node node : shapeGroup.getChildren()) {
            if (node instanceof Line) {
                final Line line = (Line) node;
                final Point2D start = GeometryUtilsFX.rotate(line.getStartX(), (line).getStartY(), angle);
                final Point2D end = GeometryUtilsFX.rotate((line).getEndX(), (line).getEndY(), angle);
                line.setStartX(start.getX());
                line.setStartY(start.getY());
                line.setEndX(end.getX());
                line.setEndY(end.getY());
            }
        }
        if (label != null) {
            Point2D p = GeometryUtilsFX.rotate(label.getTranslateX(), label.getTranslateY(), angle);
            label.setTranslateX(p.getX());
            label.setTranslateY(p.getY());
        }
    }

    @Override
    public void scaleCoordinates(double factorX, double factorY) {
        if (getShape() instanceof Line) {
            final Line line = (Line) getShape();
            Point2D start = new Point2D(factorX * line.getStartX(), factorY * line.getStartY());
            Point2D end = new Point2D(factorX * line.getEndX(), factorY * line.getEndY());
            setCoordinates(start, end);
        }
    }

    @Override
    public void setCoordinates(Point2D start, Point2D end) {
        final Line line = (Line) getShape();

        final Point2D oldMid = new Point2D(line.getStartX(), line.getStartY()).add(line.getEndX(), line.getEndY()).multiply(0.5);
        final Point2D mid = start.add(end).multiply(0.5);

        line.setStartX(start.getX());
        line.setStartY(start.getY());
        line.setEndX(end.getX());
        line.setEndY(end.getY());

        final boolean labelAlreadyPresent;

        if (label != null) {
            label.setText("");
            labelAlreadyPresent = true;
        } else
            labelAlreadyPresent = false;

        shapeGroup.getChildren().clear();
        if (getShape() != null)
            shapeGroup.getChildren().add(getShape());

        if (mutations != null && mutations.length > 0) {
            switch (mutationView) {
                case None:
                    break; // do nothing
                case Labels:
                    if (label == null) {
                        label = new Label();
                    }
                    setLabel(Basic.toString(mutations, ", "));
                    break;
                case Count:
                    if (label == null) {
                        label = new Label();
                    }
                    setLabel("" + mutations.length);
                    label.setTextFill(Color.BLUE);
                    break;
                case Hatches: {
                    final double dist = start.distance(mid);
                    final double angle = GeometryUtilsFX.computeAngle(end.subtract(start));

                    final int gapBetweenHatches = 3;

                    Point2D point = GeometryUtilsFX.translateByAngle(mid, angle, -0.5 * gapBetweenHatches * mutations.length);
                    for (int mutation : mutations) {
                        point = GeometryUtilsFX.translateByAngle(point, angle, gapBetweenHatches);
                        if (point.distance(mid) < dist) {
                            Point2D left = GeometryUtilsFX.translateByAngle(point, angle + 90, 5);
                            Point2D right = GeometryUtilsFX.translateByAngle(point, angle + 270, 5);
                            Line hatch = new Line(left.getX(), left.getY(), right.getX(), right.getY());
                            hatch.setStroke(Color.BLACK);
                            getShapeGroup().getChildren().add(hatch);
                        }
                    }

                }
            }
        }
        if (label != null) {
            if (labelAlreadyPresent) { // already present, move relative to preserve any user changes
                final Point2D diff = mid.subtract(oldMid);
                label.setTranslateX(label.getTranslateX() + diff.getX());
                label.setTranslateY(label.getTranslateY() + diff.getY());
            } else {
                label.setTranslateX(mid.getX() + 4);
                label.setTranslateY(mid.getY() - 0.5 * label.getHeight());
            }
        }
    }

    public NetworkEmbedder.MutationView getMutationView() {
        return mutationView;
    }

    public void setMutationView(NetworkEmbedder.MutationView mutationView) {
        this.mutationView = mutationView;
    }
}
