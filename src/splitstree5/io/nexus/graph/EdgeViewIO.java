/*
 *  EdgeViewIO.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.io.nexus.graph;

import javafx.geometry.Point2D;
import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import jloda.fx.util.FontUtils;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.Basic;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.parse.NexusStreamParser;
import splitstree5.gui.graphtab.base.EdgeView2D;
import splitstree5.gui.graphtab.base.Graph2DTab;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * edge view input and output
 * Daniel Huson, 3.2018
 */
public class EdgeViewIO {
    /**
     * write an edge view
     *
     * @param ev
     * @return string representation of edge view
     */
    public static String toString(EdgeView2D ev) {
        final StringBuilder buf = new StringBuilder();

        buf.append(String.format("E: %d %d", ev.getEdge().getSource().getId(), ev.getEdge().getTarget().getId()));
        if (ev.getShape() instanceof Line) {
            final Line line = (Line) ev.getShape();

            buf.append(String.format(" S: 'M %s %s L %s %s'",
                    Basic.toString(line.getStartX(), 4),
                    Basic.toString(line.getStartY(), 4),
                    Basic.toString(line.getEndX(), 4),
                    Basic.toString(line.getEndY(), 4)));
        } else if (ev.getShape() instanceof Path) {
            final Path path = (Path) ev.getShape();

            buf.append(" S: ");
            boolean first = true;
            for (PathElement element : path.getElements()) {
                if (first) {
                    first = false;
                    buf.append("'");
                } else
                    buf.append(" ");
                if (element instanceof MoveTo) {
                    final MoveTo to = (MoveTo) element;
                    buf.append(String.format("M %s %s",
                            Basic.toString(to.getX(), 4),
                            Basic.toString(to.getY(), 4)));
                } else if (element instanceof LineTo) {
                    final LineTo to = (LineTo) element;
                    buf.append(String.format("L %s %s",
                            Basic.toString(to.getX(), 4),
                            Basic.toString(to.getY(), 4)));
                } else if (element instanceof ArcTo) {
                    // rx, ry, x-axis-rotation, large-arc-flag, sweep-flag, x, y
                    final ArcTo to = (ArcTo) element;
                    buf.append(String.format("A %s %s %s %s %s %s %s",
                            Basic.toString(to.getRadiusX(), 4),
                            Basic.toString(to.getRadiusY(), 4),
                            Basic.toString(to.getXAxisRotation(), 4),
                            Basic.toString(to.getX(), 4),
                            Basic.toString(to.getY(), 4),
                            to.isLargeArcFlag(), to.isSweepFlag()));
                } else if (element instanceof QuadCurveTo) {
                    final QuadCurveTo to = (QuadCurveTo) element;
                    buf.append(String.format("Q %s %s %s %s",
                            Basic.toString(to.getControlX(), 4),
                            Basic.toString(to.getControlY(), 4),
                            Basic.toString(to.getX(), 4),
                            Basic.toString(to.getY(), 4)));

                } else if (element instanceof CubicCurveTo) {
                    final CubicCurveTo to = (CubicCurveTo) element;
                    buf.append(String.format("C %s %s %s %s %s %s",
                            Basic.toString(to.getControlX1(), 4),
                            Basic.toString(to.getControlY1(), 4),
                            Basic.toString(to.getControlX2(), 4),
                            Basic.toString(to.getControlY2(), 4),
                            Basic.toString(to.getX(), 4),
                            Basic.toString(to.getY(), 4)));
                }
            }
            if (!first)
                buf.append("'");
        }
        buf.append(String.format(" %s %s", Basic.toString(ev.getStrokeWidth(), 2), ((Color) ev.getStroke()).toString()));

        if (ev.getLabel() != null && ev.getLabel().getText().length() > 0) {
            final Labeled label = ev.getLabel();

            buf.append(String.format(" L: '%s' %s %s", label.getText(),
                    Basic.toString(label.getTranslateX(), 4),
                    Basic.toString(label.getTranslateY(), 4)));
            buf.append(String.format(" %s '%s'",
                    ((Color) label.getTextFill()).toString(),
                    FontUtils.toString(label.getFont())));
        }
        return buf.toString();
    }

    /**
     * creates an edge view
     *
     * @param np
     * @param graph
     * @param graphTab
     * @param id2node
     * @return edge view
     * @throws IOExceptionWithLineNumber
     */
    public static EdgeView2D valueOf(NexusStreamParser np, PhyloGraph graph, Graph2DTab graphTab, Map<Integer, Node> id2node) throws IOExceptionWithLineNumber {
        np.matchIgnoreCase("E:");

        final Node v = id2node.get(np.getInt());
        final Node w = id2node.get(np.getInt());
        if (v == null || w == null)
            throw new IOExceptionWithLineNumber("Invalid node id", np.lineno());
        final Edge e = graph.newEdge(v, w);

        np.matchIgnoreCase("S:");
        final String svgString = np.getWordRespectCase();
        final StringTokenizer svgTokenizer = new StringTokenizer(svgString);
        if (!svgTokenizer.hasMoreTokens())
            throw new IOExceptionWithLineNumber("Empty svg path", np.lineno());

        final ArrayList<PathElement> elements = new ArrayList<>();
        try {
            while (svgTokenizer.hasMoreTokens()) {
                String command = svgTokenizer.nextToken();
                switch (command) {
                    case "M": {
                        elements.add(new MoveTo(Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken())));
                        break;
                    }
                    case "L": {
                        elements.add(new LineTo(Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken())));
                        break;
                    }
                    case "A": {
                        elements.add(new ArcTo(Double.parseDouble(svgTokenizer.nextToken()),
                                Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken()),
                                Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken()),
                                Boolean.parseBoolean(svgTokenizer.nextToken()), Boolean.parseBoolean(svgTokenizer.nextToken())));
                        break;
                    }
                    case "Q": {
                        elements.add(new QuadCurveTo(
                                Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken()),
                                Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken())));
                        break;
                    }
                    case "C": {
                        elements.add(new CubicCurveTo(
                                Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken()),
                                Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken()),
                                Double.parseDouble(svgTokenizer.nextToken()), Double.parseDouble(svgTokenizer.nextToken())));
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new IOExceptionWithLineNumber("Failed to parse line: " + Basic.getShortName(ex.getClass()) + ": " + ex.getMessage(), np.lineno());
        }

        double strokeWidth = np.getDouble();
        final Color color = Color.valueOf(np.getWordRespectCase());

        final String labelText;
        final double lx;
        final double ly;
        final Color labelColor;
        final Font labelFont;

        if (np.peekMatchIgnoreCase("L:")) {
            np.matchIgnoreCase("L:");
            labelText = np.getWordRespectCase();
            lx = np.getDouble();
            ly = np.getDouble();
            labelColor = Color.valueOf(np.getWordRespectCase());
            labelFont = FontUtils.valueOf(np.getWordRespectCase());
        } else {
            labelText = null;
            lx = ly = 0;
            labelColor = null;
            labelFont = null;
        }

        final EdgeView2D ev;
        if (elements.size() == 2 && elements.get(0) instanceof MoveTo && elements.get(1) instanceof LineTo) {
            ev = graphTab.createEdgeView(e, new Point2D(((MoveTo) elements.get(0)).getX(), ((MoveTo) elements.get(0)).getY()),
                    new Point2D(((LineTo) elements.get(1)).getX(), ((LineTo) elements.get(1)).getY()), labelText);
        } else if (elements.size() >= 2) {
            ev = graphTab.createEdgeView(e, elements, labelText);
        } else
            throw new IOExceptionWithLineNumber("Failed to create edge view", np.lineno());

        ev.setStrokeWidth(strokeWidth);
        ev.setStroke(color);

        if (labelText != null && labelFont != null) {
            final Labeled labeled = ev.getLabel();
            labeled.setFont(labelFont);
            labeled.setTextFill(labelColor);
            labeled.setTranslateX(labeled.getTranslateX() + lx);
            labeled.setTranslateY(labeled.getTranslateY() + ly);
        }
        return ev;
    }
}
