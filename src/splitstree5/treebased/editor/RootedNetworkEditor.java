/*
 *  RootedNetworkEditor.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.treebased.editor;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jloda.fx.undo.UndoManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.phylo.PhyloTree;

public class RootedNetworkEditor extends Application {
    private Parent root;
    private RootedNetworkEditorController controller;
    private final PhyloTree phyloTree = new PhyloTree();
    private final UndoManager undoManager = new UndoManager();
    private final PhyloTreeView phyloTreeView = new PhyloTreeView(phyloTree, undoManager);

    @Override
    public void start(Stage stage) throws Exception {
        final ExtendedFXMLLoader<RootedNetworkEditorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        controller.getMainPane().getChildren().add(phyloTreeView.getWorld());

        final Scene scene = new Scene(root);

        stage.setTitle("Rooted Phylogenetic Network Capture");
        //stage.getIcons().addAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(scene);
        stage.sizeToScene();

        ControlBindings.setup(this);

        stage.show();
    }

    public RootedNetworkEditorController getController() {
        return controller;
    }

    public PhyloTree getPhyloTree() {
        return phyloTree;
    }

    public PhyloTreeView getPhyloTreeView() {
        return phyloTreeView;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }
}
