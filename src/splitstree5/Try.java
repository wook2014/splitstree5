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

package splitstree5;

import javafx.application.Application;
import javafx.stage.Stage;
import splitstree5.core.Document;
import splitstree5.io.nexus.NexusFileIO;

/**
 * try some ideas
 * Created by huson on 12/9/16.
 */
public class Try extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        final Document document = new Document();
        document.setFileName("examples/distances.nex");

        NexusFileIO.parse(document);
    }
}