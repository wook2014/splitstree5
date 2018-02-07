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

package splitstree5.gui.auxwindow;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;
import splitstree5.core.Document;
import splitstree5.gui.ViewerTab;
import splitstree5.gui.graphtab.SplitsViewTab;
import splitstree5.gui.graphtab.TreeViewTab;
import splitstree5.gui.texttab.TextViewTab;
import splitstree5.main.MainWindow;
import splitstree5.main.MainWindowManager;
import splitstree5.menu.MenuController;

/**
 * auxiliary window for showing tabs
 * Daniel  Huson, 1.2018
 */
public class AuxWindow implements IStageSupplier {
    @Override
    public Stage supplyStage(Tab tab, double width, double height) {
        final AuxWindowController controller;
        final Parent root;
        final MenuController menuController;

        {
            final ExtendedFXMLLoader<AuxWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(AuxWindow.class);
            root = extendedFXMLLoader.getRoot();
            controller = extendedFXMLLoader.getController();
        }
        {
            final ExtendedFXMLLoader<MenuController> extendedFXMLLoader = new ExtendedFXMLLoader<>(MenuController.class);
            menuController = extendedFXMLLoader.getController();
        }

        final Stage stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIcons());

        menuController.setupFullScreenMenuSupport(stage);

        if (tab.getGraphic() == null || !(tab.getGraphic() instanceof Labeled))
            tab.setGraphic(new Label(tab.getText()));
        tab.setClosable(tab instanceof TextViewTab);

        if (tab instanceof ViewerTab) {
            final Document document = ((ViewerTab) tab).getMainWindow().getDocument();
            controller.getTabPane().getTabs().addListener((InvalidationListener) observable -> {
                final StringBuilder buf = new StringBuilder();
                boolean first = true;
                for (Tab aTab : controller.getTabPane().getTabs()) {
                    if (first)
                        first = false;
                    else
                        buf.append(",");
                    if (aTab.getGraphic() instanceof Labeled)
                        buf.append(" ").append(((Labeled) aTab.getGraphic()).getText());
                    else
                        buf.append(" ").append(aTab.getText());
                }
                buf.append(" - ");
                stage.titleProperty().bind(Bindings.concat(buf.toString()).concat(document.nameProperty()).concat(" - " + ProgramProperties.getProgramName()));
            });
            stage.titleProperty().bind(Bindings.concat("Aux Window - ").concat(document.nameProperty()).concat(" SplitsTree5"));
        } else {
            stage.setTitle("Aux Window - " + ((Labeled) tab.getGraphic()).getText() + " - " + ProgramProperties.getProgramName());
        }

        controller.getTabPane().getTabs().add(tab);
        controller.getBorderPane().setTop(menuController.getMenuBar());
        if (tab instanceof SplitsViewTab) {
            ((SplitsViewTab) tab).updateMenus(menuController);
        } else if (tab instanceof TreeViewTab) {
            ((TreeViewTab) tab).updateMenus(menuController);
        } else
            menuController.unbindAndDisableAllMenuItems();
        menuController.getCloseMenuItem().setOnAction((e) -> close(stage));
        menuController.getCloseMenuItem().setDisable(false);

        stage.setScene(new Scene(root, width, height));
        stage.sizeToScene();

        return stage;
    }

    /**
     * close the stage such that on close request handler is fired if present
     *
     * @param stage
     */
    private void close(Stage stage) {
        if (stage.getOnCloseRequest() != null)
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        else
            stage.close();
        ;
    }

    @Override
    public void openedStage(Stage stage, Tab tab) {
        if (tab instanceof ViewerTab) {
            MainWindow mainWindow = ((ViewerTab) tab).getMainWindow();
            MainWindowManager.getInstance().addAuxiliaryWindow(mainWindow, stage);
        }
    }

    @Override
    public void closedStage(Stage stage, Tab tab) {
        if (tab instanceof ViewerTab) {
            MainWindow mainWindow = ((ViewerTab) tab).getMainWindow();
            MainWindowManager.getInstance().removeAuxiliaryWindow(mainWindow, stage);
        }
    }
}
