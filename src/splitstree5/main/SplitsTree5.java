/*
 *  SplitsTree5.java Copyright (C) 2020 Daniel H. Huson
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

import com.briksoftware.javafx.platform.osx.OSXIntegration;
import javafx.application.Application;
import javafx.stage.Stage;
import jloda.fx.util.ArgsOptions;
import jloda.fx.util.ProgramExecutorService;
import jloda.fx.util.ResourceManagerFX;
import jloda.fx.window.SplashScreen;
import jloda.fx.window.WindowGeometry;
import jloda.util.Basic;
import jloda.util.ProgramProperties;

import java.io.File;
import java.time.Duration;

public class SplitsTree5 extends Application {
    private static String[] inputFilesAtStartup;
    private static boolean showMessageWindow;

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
        ResourceManagerFX.addResourceRoot(SplitsTree5.class, "splitstree5/resources");
        ProgramProperties.getProgramIconsFX().setAll(ResourceManagerFX.getIcons("SplitsTree5-16.png", "SplitsTree5-32.png", "SplitsTree5-48.png", "SplitsTree5-64.png", "SplitsTree5-128.png"));

        Basic.restoreSystemOut(System.err); // send system out to system err
        Basic.startCollectionStdErr();
        ProgramProperties.setProgramName(Version.NAME);
        ProgramProperties.setProgramVersion(Version.SHORT_DESCRIPTION);
        ProgramProperties.setUseGUI(true);

        SplashScreen.setVersionString(ProgramProperties.getProgramVersion());
        SplashScreen.setImageResourceName("SplitsTree5-splash.png");

        final ArgsOptions options = new ArgsOptions(args, SplitsTree5.class, "Interactive computation of phylogenetic trees and networks");
        options.setAuthors("Daniel H. Huson and David J. Bryant. Some code by Daria Evseeva and others.");
        // options.setLicense("This is free software, licensed under the terms of the GNU General Public License, Version 3.");
        options.setLicense("This is an early (BETA) version of SplitsTree5, made available for testing purposes. Source code will be released with first official version");
        options.setVersion(ProgramProperties.getProgramVersion());

        options.comment("Input:");
        inputFilesAtStartup = options.getOption("-i", "input", "Input file(s)", new String[0]);

        options.comment(ArgsOptions.OTHER);

        showMessageWindow = options.getOption("-w", "messageWindow", "Show the message window", true);
        final String defaultPreferenceFile;
        if (ProgramProperties.isMacOS())
            defaultPreferenceFile = System.getProperty("user.home") + "/Library/Preferences/SplitsTree5.def";
        else
            defaultPreferenceFile = System.getProperty("user.home") + File.separator + ".SplitsTree5.def";
        final String propertiesFile = options.getOption("-p", "propertiesFile", "Properties file", defaultPreferenceFile);
        final boolean silentMode = options.getOption("-S", "silentMode", "Silent mode", false);
        ProgramExecutorService.setNumberOfCoresToUse(options.getOption("-t", "threads", "Maximum number of threads to use in a parallel algorithm (0=all available)", 0));
        options.done();

        ProgramProperties.load(propertiesFile);

        System.err.println("Java version: " + System.getProperty("java.version"));

        if (silentMode) {
            Basic.stopCollectingStdErr();
            Basic.hideSystemErr();
            Basic.hideSystemOut();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            SplashScreen.showSplash(Duration.ofSeconds(5));

            final MainWindow mainWindow = new MainWindow();

            final WindowGeometry windowGeometry = new WindowGeometry(ProgramProperties.get("WindowGeometry", "50 50 800 800"));

            mainWindow.show(primaryStage, windowGeometry.getX(), windowGeometry.getY(), windowGeometry.getWidth(), windowGeometry.getHeight());

            if (showMessageWindow) {
                mainWindow.getMenuController().getShowMessageWindowMenuItem().fire();
                System.err.println(Basic.stopCollectingStdErr()); // collected lines will be sent to message window
            }

            if (inputFilesAtStartup != null && inputFilesAtStartup.length > 0) {
                System.err.println("NOT IMPLEMENTED: load files from command line");
                // todo: implement
            }

            if (false) {
                // setup about and preferences menu for apple:
                OSXIntegration.init();
                OSXIntegration.populateAppleMenu(() -> SplashScreen.showSplash(Duration.ofMinutes(1)), () -> System.err.println("Preferences"));
            }
        } catch (Exception ex) {
            Basic.caught(ex);
        }
    }

    public void stop() {
        ProgramProperties.store();
        System.exit(0);
    }
}
