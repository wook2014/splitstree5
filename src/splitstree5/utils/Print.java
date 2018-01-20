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

package splitstree5.utils;

import javafx.application.Platform;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import splitstree5.gui.utils.Alert;

/**
 * print a  node
 * Daniel Huson, 1.2018
 */
public class Print {
    private static PageLayout pageLayoutSelected;

    /**
     * print the given node
     *
     * @param owner
     * @param node
     */
    public static void print(Stage owner, Node node) {
        final PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            if (job.showPrintDialog(owner)) {
                System.err.println(job.getJobSettings());

                final PageLayout pageLayout = (pageLayoutSelected != null ? pageLayoutSelected : job.getJobSettings().getPageLayout());

                final Scale scale;
                if (node.getBoundsInParent().getWidth() > pageLayout.getPrintableWidth() || node.getBoundsInParent().getHeight() > pageLayout.getPrintableHeight()) {
                    System.err.println("Temporarily scaling node to fit page layout");
                    double factor = Math.min(pageLayout.getPrintableWidth() / node.getBoundsInParent().getWidth(), pageLayout.getPrintableHeight() / node.getBoundsInParent().getHeight());
                    scale = new Scale(factor, factor);
                    node.getTransforms().add(scale);
                } else
                    scale = null;

                job.jobStatusProperty().addListener((c, o, n) -> {
                    System.err.println("Status: " + o + " -> " + n);
                    if (scale != null && n != PrinterJob.JobStatus.NOT_STARTED && n != PrinterJob.JobStatus.PRINTING) {
                        Platform.runLater(() -> node.getTransforms().remove(scale));
                    }

                });
                if (job.printPage(pageLayout, node))
                    job.endJob();
            }
        } else
            new Alert("Failed to create Printer Job");
    }

    /**
     * show the page layout dialog
     *
     * @param owner
     */
    public static void showPageLayout(Stage owner) {
        final PrinterJob job = PrinterJob.createPrinterJob();
        if (job.showPageSetupDialog(owner)) {
            pageLayoutSelected = (job.getJobSettings().getPageLayout());
        }

    }
}
