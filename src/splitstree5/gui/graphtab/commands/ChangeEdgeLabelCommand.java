/*
 * ChangeEdgeLabelCommand.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.graphtab.commands;

import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import jloda.fx.undo.UndoableRedoableCommand;
import jloda.graph.Edge;
import jloda.phylo.PhyloGraph;
import jloda.util.ProgramProperties;
import splitstree5.gui.graphtab.base.EdgeViewBase;

/**
 * Change edge label
 * Daniel Huson, 1.2018
 */
public class ChangeEdgeLabelCommand extends UndoableRedoableCommand {
    private final String oldText;
    private final String newText;
    private final Edge e;
    private final EdgeViewBase ev;
    private final PhyloGraph graph;

    public ChangeEdgeLabelCommand(Edge e, EdgeViewBase ev, PhyloGraph graph) {
        super("Change Label");
        this.ev = ev;
        this.e = e;
        this.graph = graph;
        this.oldText = (ev.getLabel() != null ? ev.getLabel().getText() : null);
        this.newText = graph.getLabel(e);
    }

    @Override
    public void undo() {
        if (oldText == null)
            ev.setLabel((Labeled) null);
        else
            ev.getLabel().setText(oldText);
        graph.setLabel(e, oldText);
    }

    @Override
    public void redo() {
        if (oldText == null) {
            Label label = new Label(newText);
            label.setFont(ProgramProperties.getDefaultFontFX());
            ev.setLabel(label);
        } else
            ev.getLabel().setText(newText);
        graph.setLabel(e, newText);
    }

    @Override
    public boolean isUndoable() {
        return ev.getEdge().getOwner() != null; // still contained in graph
    }

    @Override
    public boolean isRedoable() {
        return ev.getEdge().getOwner() != null; // still contained in graph
    }

}
