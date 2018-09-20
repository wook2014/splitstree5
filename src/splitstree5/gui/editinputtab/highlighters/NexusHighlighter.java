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
            "dimensions",
            "format",
            "taxlabels", "matrix",
            "properties", "cycle",
            //"translate", "DRAW", "VERTICES", "VLABELS", "EDGES" -> single keywords
    };

    private static final String[] OPTIONS = new String[] {
            "ntax", "nchar", "nsplits",
            "labels", "triangle", "diagonal",
            "weights", "confidences", "intervals", "fit"
    };

    private static final String[] BLOCKS = new String[] {
            "data", "taxa", "characters", "distances", "trees",
            "splits", "network", "traits", "analysis", "viewer",
            "splitstree5", "traits"
    };

    // todo rename !
    private static final String KEYWORD_PATTERN = "(?<KEYWORD>(?<ff>(?i)\\b(" + String.join("|", KEYWORDS) + ")\\b)"+
                        "(?<OPTION>(?i)(?!\\h+" + String.join("|\\h+", BLOCKS)+")[^;]*;(?!\\h*\\R*end))?)";
                        // everything between keyword and semicolon not ending with "end" and not blocks word

    private static final String BLOCK_PATTERN = "(?i)\\b(" + String.join("|", BLOCKS) + ")\\b";
    private static final String PAREN_PATTERN = "[()]";
    //private static final String BRACE_PATTERN = "\\{|\\}";
    //private static final String BRACKET_PATTERN = "\\[|\\]";
    //private static final String SEMICOLON_PATTERN = "\\;";
    private static final String COMMENT_PATTERN = "\\[(.|\\R)*?]";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    //private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String NUMBER_PATTERN = "\\b\\d+\\.\\d+\\b|\\b\\d+\\b";

    private static final Pattern PATTERN = Pattern.compile(
            "(" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    //+ "|(?<BRACE>" + BRACE_PATTERN + ")"
                    //+ "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<BLOCK>" + BLOCK_PATTERN + ")"
                    + "|(?<COLLAPSED><< Collapsed \\w+Block >>)"
                    //+ "|(?<NUMBER>" + NUMBER_PATTERN + ") "
    );

    // NUMBER_PATTERN = "\\b[-+]?[0-9]*\\.?[0-9]+\\b([eE][-+]?[0-9]+\\b)?";
    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();

        while(matcher.find()) {
            String styleClass =
                    //matcher.group("NUMBER") != null ? "number" :
                    matcher.group("KEYWORD") != null ? "keyword" :
                    //matcher.group("OPTION") != null ? "option" :
                    matcher.group("BLOCK") != null ? "block" :
                    matcher.group("COLLAPSED") != null ? "collapsed" :
                    matcher.group("PAREN") != null ? "paren" :
                    //matcher.group("BRACE") != null ? "brace" :
                    //matcher.group("BRACKET") != null ? "bracket" :
                    //matcher.group("SEMICOLON") != null ? "semicolon" :
                    //matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            if (styleClass.equals("keyword")) {
                spansBuilder.add(Collections.singleton("keyword"),
                        matcher.end("ff") - matcher.start("ff"));
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
