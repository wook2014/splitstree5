/*
 *  PhyloEditorWindow.java Copyright (C) 2020 Daniel H. Huson
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

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.fx.util.MemoryUsage;
import jloda.fx.window.IMainWindow;
import jloda.fx.window.MainWindowManager;
import jloda.util.Basic;
import jloda.util.FileOpenManager;
import jloda.util.ProgramProperties;
import splitstree5.tools.phyloedit.actions.PhyloEditorFileOpener;

import java.util.Arrays;

/**
 * the phylo editor main window
 * Daniel Huson, 1.2020
 */
public class PhyloEditorWindow implements IMainWindow {
    private Parent root;
    private PhyloEditorWindowController controller;
    private final PhyloEditor editor = new PhyloEditor(this);
    private Stage stage;

    public PhyloEditorWindow() {
        Platform.setImplicitExit(false);

        {
            final ExtendedFXMLLoader<PhyloEditorWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
            root = extendedFXMLLoader.getRoot();
            controller = extendedFXMLLoader.getController();
        }

        FileOpenManager.setExtensions(Arrays.asList(new FileChooser.ExtensionFilter("Nexus", "*.nexus", "*.nex"),
                new FileChooser.ExtensionFilter("All", "*.*")));
        FileOpenManager.setFileOpener(new PhyloEditorFileOpener());
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public IMainWindow createNew() {
        return new PhyloEditorWindow();
    }

    @Override
    public void show(Stage stage0, double screenX, double screenY, double width, double height) {
        if (stage == null)
            stage = new Stage();
        this.stage = stage0;
        stage.getIcons().addAll(ProgramProperties.getProgramIconsFX());

        final Scene scene = new Scene(root, width, height);

        stage.setScene(scene);
        stage.sizeToScene();
        stage.setX(screenX);
        stage.setY(screenY);

        getStage().titleProperty().addListener((e) -> MainWindowManager.getInstance().fireChanged());

        ControlBindings.setup(this);

        final MemoryUsage memoryUsage = MemoryUsage.getInstance();
        //controller.getMemoryUsageLabel().textProperty().bind(memoryUsage.memoryUsageStringProperty());

        editor.fileNameProperty().addListener(c -> {
            stage.setTitle(Basic.getFileNameWithoutPath(editor.getFileName()) + (editor.isDirty() ? "*" : "")
                    + " - " + ProgramProperties.getProgramName());
        });

        editor.dirtyProperty().addListener(c -> {
            stage.setTitle(Basic.getFileNameWithoutPath(editor.getFileName()) + (editor.isDirty() ? "*" : "")
                    + " - " + ProgramProperties.getProgramName());
        });

        editor.setFileName("Untitled.nexus");

        stage.show();
    }

    @Override
    public boolean isEmpty() {
        return editor.getGraph().getNumberOfNodes() == 0;
    }

    @Override
    public void close() {
        stage.hide();
    }


    public PhyloEditorWindowController getController() {
        return controller;
    }

    public PhyloEditor getEditor() {
        return editor;
    }
}
