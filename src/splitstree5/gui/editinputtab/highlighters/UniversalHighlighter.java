package splitstree5.gui.editinputtab.highlighters;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public class UniversalHighlighter implements Highlighter {

    private final String css = "styles.css";

    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        return null;
    }

    @Override
    public String getCSS() {
        return this.css;
    }
}
