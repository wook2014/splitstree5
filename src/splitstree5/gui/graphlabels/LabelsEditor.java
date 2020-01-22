/*
 *  LabelsEditor.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.gui.graphlabels;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.find.NodeLabelSearcher;
import jloda.fx.find.SearchManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;
import splitstree5.gui.graphtab.base.GraphTabBase;
import splitstree5.gui.graphtab.base.NodeView2D;
import splitstree5.gui.graphtab.base.NodeViewBase;
import splitstree5.main.MainWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class LabelsEditor {

    private final LabelsEditorController controller;
    private final Stage stage;
    final private HTMLEditor htmlEditor;
    private SearchManager searchManager = new SearchManager();


    public LabelsEditor(MainWindow parentMainWindow, Labeled label,
                        NodeLabelSearcher nodeLabelSearcher, GraphTabBase graphTabBase){

        // todo: clear after Format calling, extra button for Format

        final ExtendedFXMLLoader<LabelsEditorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        searchManager.setSearcher(nodeLabelSearcher);

        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.sizeToScene();

        stage.setX(parentMainWindow.getStage().getX() + 50);
        stage.setY(parentMainWindow.getStage().getY() + 50);

        htmlEditor = controller.getHtmlEditor();
        customizeHTMLEditor(label);
        htmlEditor.setHtmlText(label.getText());
        controller.getHTML_Area().setText(htmlEditor.getHtmlText());

        controller.getApplyStyle().setOnAction(event -> {
            controller.getHTML_Area().setText(htmlEditor.getHtmlText());

            Text theText = new Text(label.getText().replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", ""));
            theText.setFont(label.getFont());

            System.err.println("Font "+label.getFont()); //.getFont());
            //final Bounds bounds = theText.getBoundsInLocal(); //htmlEditor.getLayoutBounds();

            // todo: better function to update size!
            final Bounds bounds = label.getBoundsInParent();
            WebView wb = (WebView) htmlEditor.lookup("WebView");

            final int scalingFactor = 3;
            SnapshotParameters sp = new SnapshotParameters();
            sp.setViewport(new Rectangle2D(scalingFactor, wb.getLayoutY()*scalingFactor,
                    bounds.getWidth()*scalingFactor, bounds.getHeight()*scalingFactor));
            sp.setTransform(Transform.scale(scalingFactor, scalingFactor)); // improve quality


            WritableImage snapshot1 = wb.snapshot(sp, null);
            applyTransparency(snapshot1);

            ImageView iw = new ImageView(snapshot1);
            iw.setPreserveRatio(true);
            iw.setFitHeight(bounds.getHeight());


            label.setGraphic(iw);
            label.setText(htmlEditor.getHtmlText()); // todo: do not save new text in label
            label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        });

        controller.getUpdateHTMLButton().setOnAction(event -> {
            controller.getHTML_Area().setText(htmlEditor.getHtmlText());
        });

        controller.getSearch().setOnAction(event -> {
            String textInWB = htmlEditor.getHtmlText().replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");

            searchManager.setSearchText(textInWB);
            searchManager.findAll();

            // todo does not work for numbers!
            for (jloda.graph.Node n : nodeLabelSearcher.getSelectionModel().getSelectedItems()){
                //System.err.println("node "+n.toString());

                NodeViewBase nv = (NodeViewBase) graphTabBase.getNode2view().get(n);
                nv.setLabel(mergeHTMLStyles(nv.getLabel().getText(), htmlEditor.getHtmlText()));

                NodeView2D.applyHTMLStyle2Label(nv.getLabel());
            }
        });
    }

    public void show() {
        stage.show();
    }

    private void setStyledText(Labeled label){
        //todo
        Font font = label.getFont();

        htmlEditor.setHtmlText(label.getText());
    }

    private String mergeHTMLStyles(String newLabel, String insertion){

        //newLabel = newLabel.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
        insertion = insertion.replaceAll("</?(html|body|head)[^>]*>", "");

        String tag = insertion.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
        newLabel = newLabel.replaceAll(tag, insertion);

        return newLabel;
    }


    /*
    HTML Editor Selectors:

    ".separator", ".top-toolbar", ".bottom-toolbar", "WebView"

    ".html-editor-cut", ".html-editor-copy", ".html-editor-paste",
    ".html-editor-strike", ".html-editor-hr"
     */

    private void customizeHTMLEditor(Labeled label){

        Platform.runLater(() -> {

            String[] toDelete = {
                    ".html-editor-align-left",
                    ".html-editor-align-center",
                    ".html-editor-align-right",
                    ".html-editor-align-justify",
                    ".html-editor-outdent",
                    ".html-editor-indent",
                    ".html-editor-bullets",
                    ".html-editor-numbers",
                    ".font-menu-button",
                    ".html-editor-hr"
            };

            for (String s : toDelete){
                htmlEditor.lookup(s).setVisible(false);
                htmlEditor.lookup(s).setManaged(false);
            }

            // horizontal lines between the buttons
            for (Node n : htmlEditor.lookupAll(".separator")){
                n.setVisible(false);
                n.setManaged(false);
            }

            // Move the toolbars
            /*Node top= htmlEditor.lookup(".top-toolbar");
            GridPane.setConstraints(top,0,0,1,1);
            Node bottom= htmlEditor.lookup(".bottom-toolbar");
            GridPane.setConstraints(bottom,1,0,1,1);
            Node web= htmlEditor.lookup("WebView");
            GridPane.setConstraints(web,0,1,2,1);*/

            // add image import button to the top toolbar.
            Node node = htmlEditor.lookup(".top-toolbar");
            if (node instanceof ToolBar) {
                ToolBar bar = (ToolBar) node;

                Separator separator = new Separator();
                separator.setOrientation(Orientation.VERTICAL);

                Button loadImg = new Button("Open Image");
                loadImg.setOnAction(event -> {
                    openImage(label);
                    label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                });

                Label label1ImgScale = new Label("Scale label:");
                Slider imgScale = new Slider(0, 2, 1);
                imgScale.setShowTickMarks(true);
                imgScale.setShowTickLabels(true);
                imgScale.setMajorTickUnit(0.5);

                CheckBox showText = new CheckBox("Show text");

                if (label.getGraphic() == null){
                    imgScale.setDisable(true);
                    showText.setDisable(true);
                }
                label.graphicProperty().addListener((observable, oldValue, newValue) -> {
                    imgScale.adjustValue(1);
                    if (label.getGraphic() == null){
                        imgScale.setDisable(true);
                        showText.setDisable(true);
                    } else {
                        imgScale.setDisable(false);
                        showText.setDisable(false);
                    }
                });

                imgScale.valueProperty().addListener((observable, oldValue, newValue) -> {
                    label.getGraphic().setScaleY(newValue.doubleValue());
                    label.getGraphic().setScaleX(newValue.doubleValue());
                });

                // todo update graphic size!
                showText.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (oldValue)
                        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    else
                        label.setContentDisplay(ContentDisplay.TOP);
                });

                bar.getItems().addAll(separator, loadImg, label1ImgScale, imgScale, showText);
            }

        });
    }


    private void openImage(Labeled label){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Label Picture");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                String mimetype = Files.probeContentType(file.toPath());
                if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                    Image image = new Image(new FileInputStream(file.getPath()));
                    label.setGraphic(new ImageView(image));
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Image parsing error");
                    alert.setHeaderText("The selected file is not an image type!");
                    alert.setContentText("Please select another file.");
                    alert.showAndWait();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void applyTransparency(WritableImage writableImage){
        PixelWriter raster = writableImage.getPixelWriter();
        for (int x = 0; x < writableImage.getWidth(); x++){
            for (int y = 0; y < writableImage.getHeight(); y++){
                Color c = writableImage.getPixelReader().getColor(x, y);
                if (c.equals(Color.WHITE))
                    raster.setColor(x, y, Color.TRANSPARENT);
            }
        }
    }

}
