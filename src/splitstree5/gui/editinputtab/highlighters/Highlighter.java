package splitstree5.gui.editinputtab.highlighters;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public interface Highlighter {

    StyleSpans<Collection<String>> computeHighlighting(String text);

}
