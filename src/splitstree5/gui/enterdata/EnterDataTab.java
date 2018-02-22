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

package splitstree5.gui.enterdata;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import jloda.util.Basic;
import splitstree5.dialogs.importer.FileOpener;
import splitstree5.gui.texttab.TextViewTab;
import splitstree5.main.MainWindow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * tab for entering data
 * Daniel Huson, 2.2018
 */
public class EnterDataTab extends TextViewTab {
    /**
     * constructor
     *
     * @param mainWindow
     */
    public EnterDataTab(MainWindow mainWindow) {
        super(new SimpleStringProperty("Enter Data"));
        getTextArea().setEditable(true);

        final ToolBar toolBar = new ToolBar();

        final Button applyButton = new Button("Apply");
        toolBar.getItems().add(applyButton);
        applyButton.disableProperty().bind(getTextArea().textProperty().isEmpty());

        applyButton.setOnAction((e) -> {
            try {
                final File file = File.createTempFile(String.format("tmp%8d", System.currentTimeMillis()), ".sptr5");
                try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
                    w.write(getTextArea().getText());
                }
                FileOpener.open(mainWindow, file.getPath());
            } catch (Exception ex) {
                Basic.caught(ex);
            }
        });

    }
}
