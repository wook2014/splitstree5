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

import com.sun.istack.internal.NotNull;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.Test;
import splitstree5.core.Document;
import splitstree5.core.algorithms.trees2splits.TreeSelector;
import splitstree5.core.connectors.AConnector;
import splitstree5.core.dag.ANode;
import splitstree5.core.dag.DAG;
import splitstree5.core.datablocks.ADataNode;
import splitstree5.core.datablocks.SplitsBlock;
import splitstree5.core.datablocks.TreesBlock;
import splitstree5.core.filters.TreeFilter;
import splitstree5.gui.connectorview.ConnectorView;
import splitstree5.io.nexus.NexusFileParser;

import java.io.IOException;

/**
 * test the DAG view
 * Created by huson on 12/31/16.
 */
public class DAGViewTest extends Application {
    @Test
    public void test() throws Exception {
        init();
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        document.setFileName("test/nexus/trees49-notaxa.nex");
        NexusFileParser.parse(document);

        DAG dag = document.getDag();

        final DAGView dagView = new DAGView(document);

        dagView.getFlowPane().getChildren().add(createNodeView(document, dag.getTopTaxaNode()));
        dagView.getFlowPane().getChildren().add(createNodeView(document, dag.getTaxaFilter()));
        dagView.getFlowPane().getChildren().add(createNodeView(document, dag.getWorkingTaxaNode()));
        dagView.getFlowPane().getChildren().add(createNodeView(document, dag.getTopDataNode()));
        dagView.getFlowPane().getChildren().add(createNodeView(document, dag.getTopFilter()));
        dagView.getFlowPane().getChildren().add(createNodeView(document, dag.getWorkingDataNode()));

        if (dag.getWorkingDataNode().getDataBlock() instanceof TreesBlock) {
            final TreeSelector treeSelector = new TreeSelector();
            AConnector connector = dag.createConnector(dag.getWorkingDataNode(), new ADataNode<>(new SplitsBlock()), treeSelector);
            dagView.getFlowPane().getChildren().add(createNodeView(document, connector));
            dagView.getFlowPane().getChildren().add(createNodeView(document, connector.getChild()));
        }

        if (dag.getWorkingDataNode().getDataBlock() instanceof TreesBlock) {
            final TreeSelector treeSelector = new TreeSelector();
            AConnector connector = new TreeFilter(dag.getWorkingTaxaNode().getDataBlock(), dag.getWorkingDataNode(), new ADataNode<>(new TreesBlock()));
            dagView.getFlowPane().getChildren().add(createNodeView(document, connector));
            dagView.getFlowPane().getChildren().add(createNodeView(document, connector.getChild()));
        }


    }

    public Node createNodeView(Document document, @NotNull ANode node) {
        final Group group = new Group();
        final Rectangle rectangle = new Rectangle(200, 100);
        node.stateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case VALID:
                    rectangle.setFill(Color.LIGHTGREEN);
                    break;
                case COMPUTING:
                    rectangle.setFill(Color.LIGHTYELLOW);
                    break;
                case FAILED:
                    rectangle.setFill(Color.PINK);
                    break;
                default:
                    rectangle.setFill(Color.LIGHTGRAY);
            }
        });
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.DARKGRAY);
        group.getChildren().add(rectangle);

        final Label label = new Label(node.getName());
        label.setLayoutX(4);
        label.setLayoutY(4);
        group.getChildren().add(label);

        if (node instanceof AConnector) {
            Button openButton = new Button("Open...");
            openButton.setOnAction((e) -> {
                try {
                    ConnectorView view = new ConnectorView(document, (AConnector) node);
                    view.show();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
            openButton.setPrefWidth(70);
            openButton.setPrefHeight(30);
            openButton.setLayoutX(rectangle.getWidth() - 70);
            openButton.setLayoutY(rectangle.getHeight() - 30);
            group.getChildren().add(openButton);
        } else if (node instanceof ADataNode) {
            final Label sizeLabel = new Label();
            node.stateProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    sizeLabel.setText("Size=" + ((ADataNode) node).getDataBlock().size());
                }
            });
            sizeLabel.setLayoutX(4);
            sizeLabel.setLayoutY(24);
            group.getChildren().add(sizeLabel);
        }

        return group;
    }
}