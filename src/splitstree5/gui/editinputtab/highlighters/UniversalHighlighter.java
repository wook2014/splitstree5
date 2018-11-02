package splitstree5.gui.editinputtab.highlighters;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniversalHighlighter implements Highlighter {

    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String COMMENT_PATTERN = "#[^\n]*";
    private static final String FASTA_COMMENT_PATTERN = ";[^\n]*";
    private static final String FASTA_PATTERN = ">[^\n]*";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String NUMBER_PATTERN = "\\b\\d+\\.\\d+\\b|\\b\\d+\\b";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<FASTACOMMENT>" + FASTA_COMMENT_PATTERN + ")"
                    + "|(?<FASTA>" + FASTA_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
    );

    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();

        while(matcher.find()) {
            String styleClass =
                    matcher.group("NUMBER") != null ? "number" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    matcher.group("FASTACOMMENT") != null ? "fasta-comment" :
                    matcher.group("FASTA") != null ? "fasta" :
                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

}
