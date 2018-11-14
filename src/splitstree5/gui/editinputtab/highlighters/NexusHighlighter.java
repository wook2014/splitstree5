package splitstree5.gui.editinputtab.highlighters;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NexusHighlighter implements Highlighter {

    private static final String[] KEYWORDS = new String[] {
            "begin", "end", "endblock",
            "dimensions", "matrix",
            "format", "title",
            "taxlabels", "matrix",
            "properties", "cycle", "draw"
    };

    private static final String[] NETWORK_KEYWORDS = new String[] {
            "TRANSLATE", "VERTICES", "VLABELS", "EDGES", "ELABELS"
    };

    private static final String[] BLOCKS = new String[] {
            "data", "taxa", "characters", "distances", "trees",
            "splits", "network", "traits", "analysis", "viewer",
            "splitstree5", "traits"
    };

    private static final String KEYWORD_PATTERN = "(?<KEYWORDSLINE>(?<KEYWORD>(?i)\\b(" + String.join("|", KEYWORDS) + ")\\b)"+
                        "(?<OPTION>(?i)(?!\\h+" + String.join("|\\h+", BLOCKS)+")[^;]*;(?!\\h*\\R*end))?)";
                        // everything between keyword and semicolon not ending with "end" and not blocks word

    private static final String BLOCK_PATTERN = "(?i)\\b(" + String.join("|", BLOCKS) + ")\\b";
    private static final String NETWORK_KEYWORDS_PATTERN = "(?i)\\b(" + String.join("|", NETWORK_KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "[()]";
    private static final String COMMENT_PATTERN = "\\[(.|\\R)*?]";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    private static final Pattern PATTERN = Pattern.compile(
            "(" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<BLOCK>" + BLOCK_PATTERN + ")"
                    + "|(?<NK>" + NETWORK_KEYWORDS_PATTERN + ")"
                    + "|(?<COLLAPSED><< Collapsed \\w+Block >>)"
    );

    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();

        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORDSLINE") != null ? "keyword" :
                    matcher.group("BLOCK") != null ? "block" :
                    matcher.group("NK") != null ? "keyword" :
                    matcher.group("COLLAPSED") != null ? "collapsed" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            if (styleClass.equals("keyword")) {
                spansBuilder.add(Collections.singleton("keyword"),
                        matcher.end("NK") - matcher.start("NK"));
                spansBuilder.add(Collections.singleton("keyword"),
                        matcher.end("KEYWORD") - matcher.start("KEYWORD"));
                if (matcher.group("OPTION") != null && matcher.group("BLOCK") == null)
                    spansBuilder.add(Collections.singleton("option"),
                            matcher.end("OPTION") - matcher.start("OPTION"));
            } else
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());

            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
