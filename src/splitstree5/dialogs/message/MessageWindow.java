/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package splitstree5.dialogs.message;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import jloda.fx.ExtendedFXMLLoader;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import splitstree5.main.MainWindow;
import splitstree5.main.MainWindowManager;

import java.io.PrintStream;

public class MessageWindow {
    private static MessageWindow instance;
    private static BooleanProperty visible = new SimpleBooleanProperty(false);

    private final MessageWindowController controller;
    private final Stage stage;

    private final PrintStream printStream;

    private MessageWindow() {
        final ExtendedFXMLLoader<MessageWindowController> extendedFXMLLoader = new ExtendedFXMLLoader<>(this.getClass());
        controller = extendedFXMLLoader.getController();
        stage = new Stage();
        stage.getIcons().setAll(ProgramProperties.getProgramIcons());

        stage.setScene(new Scene(extendedFXMLLoader.getRoot()));
        //stage.sizeToScene();
        final MainWindow parentMainWindow = MainWindowManager.getInstance().getLastFocusedMainWindow();
        if (parentMainWindow != null) {
            stage.setX(parentMainWindow.getStage().getX());
            stage.setY(parentMainWindow.getStage().getY() + parentMainWindow.getStage().getHeight() - 10);
            stage.setWidth(Math.max(stage.getScene().getWidth(), parentMainWindow.getStage().getWidth()));
            stage.setHeight(150);
        }
        stage.setTitle("Message Window - " + ProgramProperties.getProgramName());
        stage.setOnHiding((e) -> Basic.restoreSystemErr());

        Basic.sendSystemErrToSystemOut();

        printStream = createPrintStream(controller.getTextArea());

        controller.getClearMenuItem().setOnAction((e) -> controller.getTextArea().clear());
        controller.getClearMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());

        controller.getSelectAllMenuItem().setOnAction((e) -> controller.getTextArea().selectAll());
        controller.getSelectAllMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());

        controller.getSelectNoneMenuItem().setOnAction((e) -> controller.getTextArea().selectRange(0, 0));
        controller.getSelectNoneMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());

        controller.getCopyMenuItem().setOnAction((e) -> controller.getTextArea().copy());
        controller.getCopyMenuItem().disableProperty().bind(controller.getTextArea().selectedTextProperty().isEmpty());

        controller.getClearMenuItem().setOnAction((e) -> controller.getTextArea().clear());
        controller.getClearMenuItem().disableProperty().bind(controller.getTextArea().textProperty().isEmpty());


        visible.bind(stage.showingProperty());
        setVisible(true);
    }

    public static MessageWindow getInstance() {
        if (instance == null)
            instance = new MessageWindow();
        return instance;
    }

    public void setVisible(boolean visible) {
        if (visible) {
            if (!stage.isShowing()) {
                stage.show();
                Basic.restoreSystemErr(printStream);
            }
            stage.toFront();
            stage.getIcons().setAll(ProgramProperties.getProgramIcons()); // seem to need to refresh these
        } else {
            if (stage.isShowing()) {
                stage.hide();
            }
        }
    }

    public boolean isVisible() {
        return stage.isShowing();
    }

    public static BooleanProperty visibleProperty() {
        return visible;
    }

    public void append(TextArea textArea, String string) {
        Platform.runLater(() -> {
            textArea.setText(textArea.getText() + string);
            textArea.positionCaret(textArea.getText().length());
        });
    }

    private PrintStream createPrintStream(TextArea textArea) {
        return new PrintStream(System.out) {
            public void println(String x) {
                MessageWindow.this.append(textArea, x);
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(String x) {
                MessageWindow.this.append(textArea, x);
            }

            public void println(Object x) {
                MessageWindow.this.append(textArea, x.toString());
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(Object x) {
                MessageWindow.this.append(textArea, x.toString());
            }

            public void println(boolean x) {
                MessageWindow.this.append(textArea, "" + x);
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(boolean x) {
                MessageWindow.this.append(textArea, "" + x);
            }

            public void println(int x) {
                MessageWindow.this.append(textArea, "" + x);
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(int x) {
                MessageWindow.this.append(textArea, "" + x);
            }

            public void println(float x) {
                MessageWindow.this.append(textArea, "" + x);
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(float x) {
                MessageWindow.this.append(textArea, "" + x);
            }

            public void println(char x) {
                MessageWindow.this.append(textArea, "" + x);
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(char x) {
                MessageWindow.this.append(textArea, "" + x);
            }

            public void println(double x) {
                MessageWindow.this.append(textArea, "" + x);
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(double x) {
                MessageWindow.this.append(textArea, "" + x);
            }

            public void println(char[] x) {
                MessageWindow.this.append(textArea, Basic.toString(x));
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(char[] x) {
                MessageWindow.this.append(textArea, Basic.toString(x));
            }

            public void println(long x) {
                MessageWindow.this.append(textArea, "" + x);
                MessageWindow.this.append(textArea, "\n");
            }

            public void print(long x) {
                MessageWindow.this.append(textArea, "" + x);
            }

            public void write(byte[] buf, int off, int len) {
                for (int i = 0; i < len; i++)
                    write(buf[off + i]);
            }

            public void write(byte b) {
                print((char) b);
            }

            public void setError() {
            }

            public boolean checkError() {
                return false;
            }

            public void flush() {
            }
        };
    }


}
