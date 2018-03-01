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

package splitstree5.main;

import javafx.application.Application;
import javafx.stage.Stage;
import jloda.util.ArgsOptions;
import jloda.util.Basic;
import jloda.util.ProgramProperties;
import jloda.util.ResourceManager;

import java.io.File;

public class SplitsTree5 extends Application {
    private static String[] inputFilesAtStartup;

    @Override
    public void init() {
    }

    /**
     * main
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        parseArguments(args);
        launch();
    }

    /**
     * parse commandline arguments
     *
     * @param args command line arguments
     * @throws java.lang.Exception
     */
    public static void parseArguments(String[] args) throws Exception {
        Basic.restoreSystemOut(System.err); // send system out to system err
        Basic.startCollectionStdErr();
        ProgramProperties.getProgramIcons().setAll(ResourceManager.getIcon("SplitsTree5-16.png"), ResourceManager.getIcon("SplitsTree5-32.png"),
                ResourceManager.getIcon("SplitsTree5-64.png"), ResourceManager.getIcon("SplitsTree5-128.png"));
        ProgramProperties.setProgramName(Version.NAME);
        ProgramProperties.setProgramVersion(Version.SHORT_DESCRIPTION);
        ProgramProperties.setUseGUI(true);

        final ArgsOptions options = new ArgsOptions(args, SplitsTree5.class, "Interactive computation of phylogenetic trees and networks");
        options.setAuthors("Daniel H. Huson and David J. Bryant. Some code by Daria Evseeva and others.");
        options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setVersion(ProgramProperties.getProgramVersion());

        options.comment("Input:");
        inputFilesAtStartup = options.getOption("-i", "input", "Input file(s)", new String[0]);

        options.comment("Configuration:");
        final String defaultPreferenceFile;
        if (ProgramProperties.isMacOS())
            defaultPreferenceFile = System.getProperty("user.home") + "/Library/Preferences/SplitsTree5.def";
        else
            defaultPreferenceFile = System.getProperty("user.home") + File.separator + ".SplitsTree5.def";
        final String propertiesFile = options.getOption("-p", "propertiesFile", "Properties file", defaultPreferenceFile);
        final boolean showVersion = options.getOption("-V", "version", "Show version string", false);
        final boolean silentMode = options.getOption("-S", "silentMode", "Silent mode", false);

        options.done();

        ProgramProperties.load(propertiesFile);

        if (silentMode) {
            Basic.stopCollectingStdErr();
            Basic.hideSystemErr();
            Basic.hideSystemOut();
        }

        if (showVersion) {
            System.err.println(ProgramProperties.getProgramVersion());
            System.err.println(jloda.util.Version.getVersion(SplitsTree5.class, ProgramProperties.getProgramName()));
            System.err.println("Java version: " + System.getProperty("java.version"));
        }
    }


    @Override
    public void start(Stage primaryStage) {

        final MainWindow mainWindow = new MainWindow();

        mainWindow.show(primaryStage, 100, 100);

        // todo: enable this to have collected lines appear in message window
        // System.err.println(Basic.stopCollectingStdErr()); // collected lines will be sent to message window

        // todo: implement use of showMessagesAtStartup
        if (inputFilesAtStartup != null && inputFilesAtStartup.length > 0) {
            System.err.println("NOT IMPLEMENTED: load files from command line");
            // todo: implement
        }
    }

    public void stop() {
        ProgramProperties.store();
        System.exit(0);
    }
}
