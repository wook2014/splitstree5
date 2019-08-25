package splitstree5.gui.graphlabels;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jloda.fx.util.ExtendedFXMLLoader;
import jloda.util.ProgramProperties;
import splitstree5.main.MainWindow;

public class LabelsEditor {

    private final LabelsEditorController controller;
    private final Stage stage;

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

        controller.getCloseButton().setOnAction((e) -> {
            stage.close();
        });

        controller.getText().setText(label.getText());
        controller.getText().setFont(label.getFont());
        controller.getText().setStyle(label.getStyle());

        controller.getBold().selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                 if (newValue)
                     controller.getText().setStyle("-fx-font-weight: bold;");
                 else
                     controller.getText().setStyle("-fx-font-weight: normal;");
            }
        });

        controller.getApplyStyle().setOnAction(event -> {

            int width = (int) label.getFont().getSize() * label.getText().length();
            int height = (int) label.getFont().getSize() + 4;
            //System.err.println("font " + width + "---"+height);

            SnapshotParameters sp = new SnapshotParameters();
            sp.setViewport(new Rectangle2D(4, 4, width, height));

            Image snapshot = controller.getText().snapshot(sp, null);

            ImageView iw = new ImageView(snapshot);
            label.setGraphic(iw);
            label.setTextFill(Color.TRANSPARENT);
        });
    }

    public void show() {
        stage.show();
    }

}
