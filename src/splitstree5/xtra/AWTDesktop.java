/*
 *  AWTDesktop.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.xtra;

import javax.swing.*;
import java.awt.*;

public class AWTDesktop {

    public static void main(String[] args) {
        new AWTDesktop();
    }

    public AWTDesktop() {
        final Desktop desktop = Desktop.getDesktop();

        desktop.setAboutHandler(e ->
                JOptionPane.showMessageDialog(null, "About dialog")
        );
        desktop.setPreferencesHandler(e ->
                JOptionPane.showMessageDialog(null, "Preferences dialog")
        );
        desktop.setQuitHandler((e, r) -> {
                    JOptionPane.showMessageDialog(null, "Quit dialog");
                    System.exit(0);
                }
        );

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("java.awt.Desktop");
            frame.setSize(new Dimension(600, 400));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
