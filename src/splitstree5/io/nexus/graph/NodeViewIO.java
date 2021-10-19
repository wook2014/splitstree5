/*
 * NodeViewIO.java Copyright (C) 2021. Daniel H. Huson
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

package splitstree5.io.nexus.graph;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import jloda.fx.control.RichTextLabel;
import jloda.fx.shapes.ISized;
import jloda.fx.shapes.NodeShape;
import jloda.fx.util.FontUtils;
import jloda.graph.Node;
import jloda.phylo.PhyloGraph;
import jloda.util.IOExceptionWithLineNumber;
import jloda.util.StringUtils;
import jloda.util.parse.NexusStreamParser;
import splitstree5.gui.graphtab.base.Graph2DTab;
import splitstree5.gui.graphtab.base.NodeView2D;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * node view input and output methods
 * Daniel Huson, 3.2018
 */
public class NodeViewIO {
    /**
     * write a node view to an output string
     *
     * @param nv
     * @return string
     */
    public static String toOutputString(NodeView2D nv) {
        final StringBuilder buf = new StringBuilder();
		buf.append(String.format("N: %d %s %sf", nv.getNode().getId(), StringUtils.toString(nv.getLocation().getX(), 4), StringUtils.toString(nv.getLocation().getY(), 4)));
        final Shape shape = nv.getShape();
        if (shape != null) {
            buf.append(String.format(" S: %s %s %s %s %s %s", NodeShape.valueOf(shape),
					StringUtils.toString(nv.getShapeGroup().getTranslateX(), 4),
					StringUtils.toString(nv.getShapeGroup().getTranslateY(), 4),
					StringUtils.toString(shape instanceof ISized ? ((ISized) shape).getWidth() : shape.getLayoutBounds().getWidth(), 4),
					StringUtils.toString(shape instanceof ISized ? ((ISized) shape).getHeight() : shape.getLayoutBounds().getHeight(), 4),
					shape.getFill().toString()));
        }
        if (nv.getLabel() != null) {
			final RichTextLabel label = nv.getLabel();


			buf.append(String.format(" L: '%s' %s %s", StringUtils.protectBackSlashes(label.getText()),
					StringUtils.toString(label.getTranslateX(), 4),
					StringUtils.toString(label.getTranslateY(), 4)));
			buf.append(String.format(" %s '%s'", label.getTextFill().toString(), FontUtils.toString(label.getFont())));
		}
        if (nv.getNumberOfWorkingTaxonIds() > 0) {
			buf.append(String.format(" T: %s", StringUtils.toString(nv.getWorkingTaxa(), " ")));
        }
        return buf.toString();
    }

    /**
     * parse a node from a nexus parser
     *
     * @param graph2DTab
     * @param id2node
     * @return
     * @throws IOExceptionWithLineNumber
     */
    public static NodeView2D valueOf(NexusStreamParser np, PhyloGraph graph, Graph2DTab graph2DTab, Map<Integer, Node> id2node) throws IOExceptionWithLineNumber {
        np.matchIgnoreCase("N:");
        final int id = np.getInt();
        final double x = np.getDouble();
        final double y = np.getDouble();
        final NodeShape shape;
        final double sx;
        final double sy;
        final double sw;
        final double sh;
        final Color shapeColor;
        if (np.peekMatchIgnoreCase("S:")) {
			np.matchIgnoreCase("S:");
			shape = StringUtils.valueOfIgnoreCase(NodeShape.class, np.getWordRespectCase());

            sx = np.getDouble();
            sy = np.getDouble();
            sw = np.getDouble();
            sh = np.getDouble();
            shapeColor = Color.valueOf(np.getWordRespectCase());
        } else {
            shape = null;
            sx = sy = sw = sh = 0;
            shapeColor = null;
        }
        final String labelText;
        final double lx;
        final double ly;
        final Color labelColor;
        final Font labelFont;

        if (np.peekMatchIgnoreCase("L:")) {
            np.matchIgnoreCase("L:");
            labelText = np.getWordRespectCase().replaceAll(Pattern.quote("<n>"), "\n");
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

        final Node v = graph.newNode();
        if (np.peekMatchIgnoreCase("T:")) {
            np.matchIgnoreCase("T:");
            while (np.peekInteger()) {
                graph.addTaxon(v, np.getInt());
            }
        }

        final NodeView2D nv = graph2DTab.createNodeView(v, graph.getTaxa(v), new Point2D(x, y), shape, sw, sh, labelText);
        id2node.put(id, nv.getNode());

        if (shape != null) {
            nv.getShapeGroup().setTranslateX(sx);
            nv.getShapeGroup().setTranslateY(sy);
            nv.getShape().setFill(shapeColor);
        }
        if (labelText != null && labelFont != null) {
            final RichTextLabel labeled = nv.getLabel();
            labeled.setFont(labelFont);
            labeled.setTextFill(labelColor);
            labeled.setTranslateX(lx);
            labeled.setTranslateY(ly);
        }
        return nv;
    }
}
