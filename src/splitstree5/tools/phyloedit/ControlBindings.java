/*
 *  ControlBindings.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.tools.phyloedit;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import jloda.fx.undo.UndoManager;
import jloda.graph.Edge;
import jloda.graph.Node;
import jloda.phylo.PhyloTree;
import splitstree5.tools.phyloedit.actions.CreateNodeCommand;
import splitstree5.tools.phyloedit.actions.DeleteNodesCommand;
import splitstree5.tools.phyloedit.actions.PasteImageCommand;

import java.io.IOException;
import java.io.StringWriter;

public class ControlBindings {

    public static void setup(PhyloEditorMain main) {
        final PhyloEditorController controller = main.getController();
        final PhyloEditor editor = main.getEditor();
        final PhyloTree graph = editor.getGraph();
        final UndoManager undoManager = editor.getUndoManager();

        editor.getGraphFX().getEdgeList().addListener((ListChangeListener<Edge>) c -> {
            while (c.next()) {
                for (Edge e : c.getAddedSubList()) {
                    if (e.getTarget().getInDegree() > 1) {
                        for (Edge f : e.getTarget().inEdges()) {
                            graph.setSpecial(f, true);
                            graph.setWeight(f, 0);
                        }
                    }
                }
                for (Edge e : c.getRemoved()) {
                    if (e.getTarget().getInDegree() <= 1) {
                        for (Edge f : e.getTarget().inEdges()) {
                            graph.setSpecial(f, false);
                            graph.setWeight(f, 1);
                        }
                    }
                }
            }
        });

        final Pane mainPane = controller.getMainPane();

        controller.getUndoMenuItem().setOnAction(e -> undoManager.undo());
        controller.getUndoMenuItem().disableProperty().bind(undoManager.undoableProperty().not());
        controller.getUndoMenuItem().textProperty().bind(undoManager.undoNameProperty());
        controller.getRedoMenuItem().setOnAction(e -> undoManager.redo());
        controller.getRedoMenuItem().disableProperty().bind(undoManager.redoableProperty().not());
        controller.getRedoMenuItem().textProperty().bind(undoManager.redoNameProperty());

        controller.getSelectAllMenuItem().setOnAction(e -> {
            editor.getGraph().nodes().forEach((v) -> editor.getNodeSelection().select(v));
        });
        controller.getSelectNoneMenuItem().setOnAction(e -> editor.getNodeSelection().clearSelection());
        controller.getSelectNoneMenuItem().disableProperty().bind(Bindings.isEmpty(editor.getNodeSelection().getSelectedItems()));

        controller.getPasteMenuItem().setOnAction(e -> {
            final Clipboard cb = Clipboard.getSystemClipboard();
            if (cb.hasImage()) {
                Image image = cb.getImage();
                undoManager.doAndAdd(new PasteImageCommand(main.getStage(), controller, image));
            }
        });

        controller.getDeleteMenuItem().setOnAction(e ->
                undoManager.doAndAdd(new DeleteNodesCommand(mainPane, editor)));
        controller.getDeleteMenuItem().disableProperty().bind(editor.getNodeSelection().sizeProperty().isEqualTo(0));

        mainPane.setOnMousePressed((e) -> {
            if (e.getClickCount() == 2) {
                final Point2D location = mainPane.sceneToLocal(e.getSceneX(), e.getSceneY());
                undoManager.doAndAdd(new CreateNodeCommand(mainPane, editor, location.getX(), location.getY()));
            } else if (e.getClickCount() == 1 && !e.isShiftDown())
                controller.getSelectNoneMenuItem().getOnAction().handle(null);
        });

        final BooleanProperty isLeafLabeledDAG = new SimpleBooleanProperty(false);
        LabeledDAGProperties.setup(controller.getStatusFlowPane(), editor.getGraphFX(), isLeafLabeledDAG);

        controller.getCopyNewickMenuItem().setOnAction(e -> {
            try (StringWriter w = new StringWriter()) {
                final Node root = LabeledDAGProperties.findRoot(editor.getGraph());
                if (root != null) {
                    editor.getGraph().setRoot(root);
                    editor.getGraph().write(w, false);
                    final ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(w.toString() + ";");
                    Clipboard.getSystemClipboard().setContent(clipboardContent);
                }
            } catch (IOException ignored) {
            }

        });
        controller.getCopyNewickMenuItem().disableProperty().bind(isLeafLabeledDAG.not());

        controller.getCopyNewickButton().setOnAction(controller.getCopyNewickMenuItem().getOnAction());
        controller.getCopyNewickButton().disableProperty().bind(controller.getCopyNewickMenuItem().disableProperty());
    }

}
