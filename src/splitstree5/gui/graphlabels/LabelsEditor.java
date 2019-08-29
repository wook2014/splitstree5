package splitstree5.gui.graphlabels;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Transform;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;
import org.fxmisc.richtext.InlineCssTextArea;
import splitstree5.main.MainWindow;

public class LabelsEditor {

    private final LabelsEditorController controller;
    private final Stage stage;
    private InlineCssTextArea inlineCssTextArea = new InlineCssTextArea();

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

        controller.getArea().getChildren().add(inlineCssTextArea);
        inlineCssTextArea.insertText(0, label.getText());
        inlineCssTextArea.setStyle(fontToCSS(label.getFont()));

        controller.getBold().selectedProperty().addListener(new ChangeListener<Boolean>() {
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
        });

        controller.getCloseButton().setOnAction((e) -> {
            stage.close();
        });
    }

    public void show() {
        stage.show();
    }

    private static String fontToCSS(Font font){
        return "-fx-background-color: transparent; "+
                "-fx-font-family:" + font.getFamily() +
                "; -fx-font-size:" + font.getSize() + "px;";
    }

}
