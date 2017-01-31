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

package splitstree5.gui.dagview;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.undo.UndoManager;
import splitstree5.utils.ExtendedFXMLLoader;
import splitstree5.utils.Option;

import java.io.IOException;
import java.util.ArrayList;

/**
 * create a connector view
 * Created by huson on 12/31/16.
 */
public class DAGView {
    private final Document document;
    private final Parent root;
    private final DAGViewController controller;
    private final UndoManager undoManager;
    private Stage stage;

    private final Group nodeViews = new Group();
    private final Group edgeViews = new Group();

    private final ArrayList<Option> options = new ArrayList<>();

    /**
     * constructor
     */
    public DAGView(Document document) throws IOException {
        this.document = document;

        final ExtendedFXMLLoader<DAGViewController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        root = extendedFXMLLoader.getRoot();
        controller = extendedFXMLLoader.getController();
        undoManager = new UndoManager();

        controller.getCenterPane().getChildren().addAll(edgeViews, nodeViews);
        show();
    }

    public Document getDocument() {
        return document;
    }

    public Pane getCenterPane() {
        return controller.getCenterPane();
    }

    public Group getNodeViews() {
        return nodeViews;
    }

    public Group getEdgeViews() {
        return edgeViews;
    }

    /**
     * show this view
     */
    public void show() {
        stage = new Stage();
        stage.setTitle("DAG Viewer - SplitsTree5");
        stage.setScene(new Scene(root, 600, 400));

        stage.setX(100 + ConnectorView.windowCount * 40);
        stage.setY(200 + ConnectorView.windowCount * 40);
        ConnectorView.windowCount++;

        stage.show();
    }
}