package splitstree5.gui.graphlabels;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Transform;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;
import splitstree5.main.MainWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class LabelsEditor {

    private final LabelsEditorController controller;
    private final Stage stage;
    final private HTMLEditor htmlEditor;


    public LabelsEditor(MainWindow parentMainWindow, Labeled label){

        final ExtendedFXMLLoader<LabelsEditorController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();

        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIconsFX());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        stage.sizeToScene();

        stage.setX(parentMainWindow.getStage().getX() + 50);
        stage.setY(parentMainWindow.getStage().getY() + 50);

        stage.setTitle("Labels Editor");

        htmlEditor = controller.getHtmlEditor();
        customizeHTMLEditor(label);


        String html = label.getText();
        htmlEditor.setHtmlText(html);
        controller.getHTML_Area().setText(htmlEditor.getHtmlText());

        controller.getApplyStyle().setOnAction(event -> {
            controller.getHTML_Area().setText(htmlEditor.getHtmlText());

            Text theText = new Text(label.getText().replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", ""));
            theText.setFont(label.getFont());
            //final Bounds bounds = theText.getBoundsInLocal(); //htmlEditor.getLayoutBounds();

            SnapshotParameters sp = new SnapshotParameters();
            sp.setViewport(new Rectangle2D(0, 0, 300, 300));
            //sp.setFill(Color.TRANSPARENT);
            //sp.setTransform(Transform.scale(3, 3)); // improve quality

            WebView wb = (WebView) htmlEditor.lookup("WebView");
            final Bounds bounds = label.getBoundsInParent();
            wb.setPrefHeight(bounds.getHeight()+40);
            wb.setPrefWidth(bounds.getWidth()+20);

            Image snapshot = wb.snapshot(sp, null);
            ImageView iw = new ImageView(snapshot);
            //iw.setFitWidth(bounds.getWidth());
            //iw.setFitHeight(bounds.getHeight());
            label.setGraphic(iw);
            label.setText(htmlEditor.getHtmlText());
            label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            /*WebView wb = new WebView();
            wb.setPrefHeight(bounds.getHeight()+20);
            wb.setPrefWidth(bounds.getWidth()+20);
            WebEngine webEngine = wb.getEngine();
            label.setGraphic(wb);
            webEngine.loadContent(htmlEditor.getHtmlText());

            //label.setTextFill(Color.TRANSPARENT);
            label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);*/
            //label.setText(inlineCssTextArea.getText());

        });

        /*controller.getBold().selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                IndexRange ir = inlineCssTextArea.getSelection();

                if (newValue) {
                    if (ir.getLength() == 0) {
                        inlineCssTextArea.setStyle(0, inlineCssTextArea.getLength(),
                                inlineCssTextArea.getStyle() + "; -fx-font-weight: bold;");
                    } else {
                        inlineCssTextArea.setStyle(ir.getStart(), ir.getEnd(),
                                inlineCssTextArea.getStyle() + "; -fx-font-weight: bold;");
                    }
                } else {
                    inlineCssTextArea.setStyle(0, inlineCssTextArea.getLength(),
                            inlineCssTextArea.getStyle()+ "; -fx-font-weight: normal;");
                }
            }
        });

        controller.getApplyStyle().setOnAction(event -> {

            final Bounds bounds = inlineCssTextArea.getLayoutBounds();

            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            sp.setTransform(Transform.scale(3, 3)); // improve quality

            Image snapshot = inlineCssTextArea.snapshot( sp, null);

            ImageView iw = new ImageView(snapshot);
            iw.setFitWidth(bounds.getWidth());
            iw.setFitHeight(bounds.getHeight());

            label.setGraphic(iw);
            label.setTextFill(Color.TRANSPARENT);
            label.setText(inlineCssTextArea.getText());
        });*/

    }

    public void show() {
        stage.show();
    }

    private static String fontToCSS(Font font){
        return "-fx-background-color: transparent; "+
                "-fx-font-family:" + font.getFamily() +
                "; -fx-font-size:" + font.getSize() + "px;";
    }


    /*
    HTML Editor Selectors:

    ".separator", ".top-toolbar", ".bottom-toolbar", "WebView"

    ".html-editor-cut", ".html-editor-copy", ".html-editor-paste",
    ".html-editor-strike", ".html-editor-hr"

    ".html-editor-align-left"
    ".html-editor-align-center"
    ".html-editor-align-right"
    ".html-editor-align-justify"
    ".html-editor-outdent"
    ".html-editor-indent"
    ".html-editor-bullets"
    ".html-editor-numbers"

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
                Label label1ImgScale = new Label("Scale label:");
                Slider imgScale = new Slider(0, 2, 1);
                imgScale.setShowTickMarks(true);
                imgScale.setShowTickLabels(true);
                imgScale.setMajorTickUnit(0.5);

                if (label.getGraphic() == null)
                    imgScale.setDisable(true);
                label.graphicProperty().addListener(new ChangeListener<Node>() {
                    @Override
                    public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
                        imgScale.adjustValue(1);
                        if (label.getGraphic() == null)
                            imgScale.setDisable(true);
                        else
                            imgScale.setDisable(false);
                    }
                });

                imgScale.valueProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        label.getGraphic().setScaleY(newValue.doubleValue());
                        label.getGraphic().setScaleX(newValue.doubleValue());
                    }
                });

                bar.getItems().addAll(separator, loadImg, label1ImgScale, imgScale);
                loadImg.setOnAction(event -> {
                    openImage(label);
                });
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


    // hide buttons containing nodes whose image url matches a given name pattern.
    public void hideImageNodesMatching(Node node, Pattern imageNamePattern, int depth) {
        if (node instanceof ImageView) {
            ImageView imageView = (ImageView) node;
            String url = imageView.getImage().getUrl();
            if (url != null && imageNamePattern.matcher(url).matches()) {
                Node button = imageView.getParent().getParent();
                button.setVisible(false); button.setManaged(false);
            }
        }
        if (node instanceof Parent)
            for (Node child : ((Parent) node).getChildrenUnmodifiable())
                hideImageNodesMatching(child, imageNamePattern, depth + 1);
    }


}
