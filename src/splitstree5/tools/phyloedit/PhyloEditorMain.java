/*
 *  PhyloEditorMain.java Copyright (C) 2020 Daniel H. Huson
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

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;

public class PhyloEditorMain extends Application {
    private Parent root;
    private PhyloEditorController controller;
    private final PhyloEditor editor = new PhyloEditor(this);
    private Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        final ExtendedFXMLLoader<PhyloEditorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();

        controller.getMainPane().getChildren().add(editor.getWorld());

        final Scene scene = new Scene(root);

        stage.setTitle("Rooted Phylogenetic Network Capture");
        //stage.getIcons().addAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(scene);
        stage.sizeToScene();

        ControlBindings.setup(this);

        stage.show();
    }

    public PhyloEditorController getController() {
        return controller;
    }


    public PhyloEditor getEditor() {
        return editor;
    }

    public Stage getStage() {
        return stage;
    }
}
