/*
 * CodeAreaStyler.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package splitstree5.gui.editinputtab;

import org.fxmisc.richtext.CodeArea;
import splitstree5.gui.editinputtab.highlighters.Highlighter;
import splitstree5.gui.editinputtab.highlighters.NexusHighlighter;
import splitstree5.gui.editinputtab.highlighters.UniversalHighlighter;
import splitstree5.gui.editinputtab.highlighters.XMLHighlighter;

/**
 * Keeper for highlighting listeners
 */

public class CodeAreaStyler {
    private static boolean debug = false;

    //todo change codearea highlighting color

    private Highlighter highlighter = new NexusHighlighter();

    public CodeAreaStyler(CodeArea codeArea) {

        /*
         * Add listeners for highlighting type checking
         */

        codeArea.textProperty().addListener((observableValue, s, t1) -> {
            if (t1.length() >= 6 && t1.replaceAll("^\\n+", "").substring(0, 6).toLowerCase().equals("#nexus")) {
                highlighter = new NexusHighlighter();
            }
        });

        codeArea.textProperty().addListener((observableValue, s, t1) -> {
            if (t1.length() != 0 && t1.replaceAll("^\\n+", "").startsWith("<")) {
                highlighter = new XMLHighlighter();
            }
        });

        codeArea.textProperty().addListener((observableValue, s, t1) -> {
            if (t1.length() >= 6 && !t1.replaceAll("^\\n+", "").substring(0, 6).toLowerCase().equals("#nexus")
                    && !t1.replaceAll("^\\n+", "").startsWith("<")) {
                highlighter = new UniversalHighlighter();
            }
        });
    }

    private static String returnFirstLine(String s) {
        if (s.length() == 0 || !s.contains("\\n"))
            return s;
        else
            return s.substring(0, s.indexOf("\\n"));
    }

    public Highlighter getHighlighter() {
        return this.highlighter;
    }
}
