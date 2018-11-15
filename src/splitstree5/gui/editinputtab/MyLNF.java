package splitstree5.gui.editinputtab;

import com.sun.istack.internal.Nullable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.function.IntFunction;

public class MyLNF implements IntFunction<Node> {

    private static final Insets DEFAULT_INSETS = new Insets(0.0D, 5.0D, 0.0D, 5.0D);
    private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
    private static final Font DEFAULT_FONT;
    private static final Background DEFAULT_BACKGROUND;
    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;

    ///
    private boolean style = false;
    private int[] hide = {Integer.MAX_VALUE, Integer.MAX_VALUE};
    ///

    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area) {
        return get(area, (digits) -> {
            return "%1$" + digits + "s";
        });
    }

    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area, IntFunction<String> format) {
        return new MyLNF(area, format);
    }

    //+++
    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area, int[] hide) {
        return new MyLNF(area, (digits) -> {
            return "%1$" + digits + "s";
        }, hide);
    }

    private MyLNF(GenericStyledArea<?, ?, ?> area, IntFunction<String> format) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
    }

    //+++
    private MyLNF(GenericStyledArea<?, ?, ?> area, IntFunction<String> format, int[] hide) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.hide = hide;
    }

    public Node apply(int idx) {
        Label lineNo;
        if (idx < hide[0]) {
            Val<String> formatted = this.nParagraphs.map((n) -> {
                return this.format(idx + 1, n);
            });
            lineNo = new Label();
            lineNo.setFont(DEFAULT_FONT);
            lineNo.setBackground(DEFAULT_BACKGROUND);
            lineNo.setTextFill(DEFAULT_TEXT_FILL);
            lineNo.setPadding(DEFAULT_INSETS);
            lineNo.setAlignment(Pos.TOP_RIGHT);
            lineNo.getStyleClass().add("lineno");
            lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
        } else {
            if(idx==hide[0]){
                lineNo = new Label();
                Val<String> formatted = this.nParagraphs.map((n) -> {
                    return this.format("+", n);
                });
                lineNo.setFont(DEFAULT_FONT);
                lineNo.setBackground(DEFAULT_BACKGROUND);
                lineNo.setTextFill(Color.web("#0000FF"));
                lineNo.setPadding(DEFAULT_INSETS);
                lineNo.setAlignment(Pos.TOP_RIGHT);
                lineNo.getStyleClass().add("lineno");
                lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
            }else{
                int offset = hide[1] - hide[0];
                Val<String> formatted = this.nParagraphs.map((n) -> {
                    return this.format(idx + 1 + offset, n + offset);
                });
                lineNo = new Label();
                lineNo.setFont(DEFAULT_FONT);
                lineNo.setBackground(DEFAULT_BACKGROUND);
                lineNo.setTextFill(DEFAULT_TEXT_FILL);
                lineNo.setPadding(DEFAULT_INSETS);
                lineNo.setAlignment(Pos.TOP_RIGHT);
                lineNo.getStyleClass().add("lineno");
                lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
            }
        }
        return lineNo;
    }

    private String format(int x, int max) {
        int digits = (int)Math.floor(Math.log10((double)max)) + 1;
        return String.format((String)this.format.apply(digits), x);
    }

    //+++
    private String format(String x, int max) {
        int digits = (int)Math.floor(Math.log10((double)max)) + 1;
        return String.format((String)this.format.apply(digits), x);
    }

    static {
        DEFAULT_FONT = Font.font("monospace", FontPosture.ITALIC, 13.0D);
        DEFAULT_BACKGROUND = new Background(new BackgroundFill[]{new BackgroundFill(Color.web("#ddd"), (CornerRadii)null, (Insets)null)});
    }

    ///
    /*public void setHide(int i){
        this.hide = i;
    }*/
}
