/*
 * LabelsEditor.java Copyright (C) 2020. Daniel H. Huson
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

package splitstree5.gui.graphlabels;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
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
import jloda.fx.control.RichTextLabel;
import jloda.fx.find.NodeLabelSearcher;
import jloda.fx.find.SearchManager;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;
import splitstree5.gui.graphtab.base.GraphTabBase;
import splitstree5.gui.graphtab.base.NodeViewBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Deprecated
public class LabelsEditor {

    private final LabelsEditorController controller;
    private final Stage stage;
    final private HTMLEditor htmlEditor;
    private SearchManager searchManager = new SearchManager();
    private RichTextLabel label;
    private String originalLabel;
    private boolean imgAdded = false;

    public LabelsEditor() {

        // todo: clear after Format calling, extra button for Format

        final ExtendedFXMLLoader<LabelsEditorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());
        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.sizeToScene();

        htmlEditor = controller.getHtmlEditor();
        controller.getFindAll().setSelected(false);
        controller.getApply2all().setDisable(true);

        controller.getApplyStyle().setOnAction(event -> {
            controller.getHTML_Area().setText(htmlEditor.getHtmlText());

            Text theText = new Text(label.getText().replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", ""));
            theText.setFont(label.getFont());

            WebView wb = (WebView) htmlEditor.lookup("WebView");
            wb.getEngine().executeScript("window.getSelection().removeAllRanges()");
            //System.err.println(wb.getEngine().executeScript("window.innerWidth - document.documentElement.clientWidth"));//todo use for scrollbars

            Platform.runLater(() -> {// todo: only works for the first time?
                label.setGraphic(takeSnapshot(wb));
                label.setText(htmlEditor.getHtmlText()); // todo: do not save new text in label
                label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            });
        });

        controller.getUpdateHTMLButton().setOnAction(event -> {
            controller.getHTML_Area().setText(htmlEditor.getHtmlText());
        });

        controller.getFindAll().selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                String textInWB = htmlEditor.getHtmlText().replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
                searchManager.setSearchText(textInWB);
                searchManager.findAll();
                controller.getApply2all().setDisable(false);
            } else {
                searchManager.doUnselectAll();
                controller.getApply2all().setDisable(true);
            }
        });

        controller.getReset().setOnAction(event -> { //todo apply in viewer or editor?
            this.htmlEditor.setHtmlText(this.originalLabel);
        });

        controller.getUpdateView().setOnAction(event -> {
            this.htmlEditor.setHtmlText(controller.getHTML_Area().getText());
        });

        controller.getFindAll().setVisible(false);
        controller.getApply2all().setVisible(false);
        controller.getSeparator().setVisible(false);
    }

    public LabelsEditor(NodeLabelSearcher nodeLabelSearcher, GraphTabBase graphTabBase) {
        this();
        controller.getFindAll().setVisible(true);
        controller.getApply2all().setVisible(true);
        controller.getSeparator().setVisible(true);

        searchManager.setSearcher(nodeLabelSearcher);
        controller.getApply2all().setOnAction(event -> {
            // todo does not work for numbers!
            for (jloda.graph.Node n : nodeLabelSearcher.getSelectionModel().getSelectedItems()) {
                //System.err.println("node "+n.toString());
                NodeViewBase nv = (NodeViewBase) graphTabBase.getNode2view().get(n);
                nv.setLabel(mergeHTMLStyles(nv.getLabel().getText(), htmlEditor.getHtmlText()));
            }
        });
    }

    public void show() {
        customizeHTMLEditor();
        stage.show();
        stage.toFront();
        controller.getFindAll().setSelected(false);
    }

    public void setLabel(RichTextLabel label) {
        this.label = label;
        String font = label.getFont().getFamily();
        Double size = label.getFont().getSize();
        htmlEditor.setHtmlText("<span style=\"font-size: " + size + "; font-family:" + font + ";\">" + label.getText() + "</span>");
        controller.getHTML_Area().setText(htmlEditor.getHtmlText());
        this.originalLabel = htmlEditor.getHtmlText();
    }

    private void setStyledText(Labeled label) {
        //todo
        Font font = label.getFont();

        htmlEditor.setHtmlText(label.getText());
    }

    private String mergeHTMLStyles(String newLabel, String insertion) {

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

    private void customizeHTMLEditor() {

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

            for (String s : toDelete) {
                htmlEditor.lookup(s).setVisible(false);
                htmlEditor.lookup(s).setManaged(false);
            }

            // horizontal lines between the buttons
            for (Node n : htmlEditor.lookupAll(".separator")) {
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
                });

                Label label1ImgScale = new Label("Scale label:");
                Slider imgScale = new Slider(0, 2, 1);
                imgScale.setShowTickMarks(true);
                imgScale.setShowTickLabels(true);
                imgScale.setMajorTickUnit(0.5);

                CheckBox showText = new CheckBox("Show text");

                if (label.getGraphic() == null) {
                    imgScale.setDisable(true);
                    showText.setDisable(true);
                }
                label.graphicProperty().addListener((observable, oldValue, newValue) -> {
                    imgScale.adjustValue(1);
                    if (label.graphicProperty().isNull().get()) {
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

                if (!imgAdded) {
                    bar.getItems().addAll(separator, loadImg, label1ImgScale, imgScale); //showText);
                    imgAdded = true;
                }
            }

        });
    }


    private void openImage(RichTextLabel label) {
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
                    htmlEditor.setHtmlText(label.getText() + "<img src=\"" + "file:///" + file.getAbsolutePath() + "\">");
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

    public static ImageView takeSnapshot(WebView wb) {
        final int scalingFactor = 3;
        SnapshotParameters sp = new SnapshotParameters();
        sp.setTransform(Transform.scale(scalingFactor, scalingFactor)); // improve quality
        WritableImage snapshot1 = wb.snapshot(sp, null);
        Rectangle2D transparencyBounds = applyTransparency(snapshot1);

        ImageView iw = new ImageView(snapshot1);
        iw.setViewport(transparencyBounds);
        iw.setPreserveRatio(true);
        iw.setFitHeight(transparencyBounds.getHeight() / scalingFactor);
        return iw;
    }

    public static Rectangle2D applyTransparency(WritableImage writableImage) {
        PixelWriter raster = writableImage.getPixelWriter();
        //final int scrollbarOffset = 42; //todo?
        double width = writableImage.getWidth(); //- scrollbarOffset;
        double height = writableImage.getHeight(); //- scrollbarOffset;
        double top = height / 2;
        double bottom = 0;
        double left = width / 2;
        double right = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color c = writableImage.getPixelReader().getColor(x, y);
                if (c.equals(Color.WHITE))
                    raster.setColor(x, y, Color.TRANSPARENT);
                else {
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                }
            }
        }
        return new Rectangle2D(left, top, right - left, bottom - top);
    }

}
