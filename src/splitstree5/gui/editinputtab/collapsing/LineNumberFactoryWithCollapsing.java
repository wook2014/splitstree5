/*
 * LineNumberFactoryWithCollapsing.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package splitstree5.gui.editinputtab.collapsing;

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
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import java.util.function.IntFunction;

public class LineNumberFactoryWithCollapsing implements IntFunction<Node> {

    private static final Insets DEFAULT_INSETS = new Insets(0.0D, 5.0D, 0.0D, 5.0D);
    private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
    private static final Font DEFAULT_FONT;
    private static final Background DEFAULT_BACKGROUND;
    private final Val<Integer> nParagraphs;
    private final IntFunction<String> format;

    ///
    private boolean style = false;
    //private int[] hide = {Integer.MAX_VALUE, Integer.MAX_VALUE};
    private NexusBlockCollapser nexusBlockCollapser;
    ///

    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area) {
        return get(area, (digits) -> {
            return "%1$" + digits + "s";
        });
    }

    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area, IntFunction<String> format) {
        return new LineNumberFactoryWithCollapsing(area, format);
    }

    //+++
    /*public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area, int[] hide) {
        return new LineNumberFactoryWithCollapsing(area, (digits) -> {
            return "%1$" + digits + "s";
        }, hide);
    }*/
    //+++
    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area, NexusBlockCollapser nexusBlockCollapser) {
        return new LineNumberFactoryWithCollapsing(area, (digits) -> {
            return "%1$" + digits + "s";
        }, nexusBlockCollapser);
    }

    private LineNumberFactoryWithCollapsing(GenericStyledArea<?, ?, ?> area, IntFunction<String> format) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
    }

    //+++
    /*private LineNumberFactoryWithCollapsing(GenericStyledArea<?, ?, ?> area, IntFunction<String> format, int[] hide) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.hide = hide;
    }*/
    //+++
    private LineNumberFactoryWithCollapsing(GenericStyledArea<?, ?, ?> area, IntFunction<String> format,
                                            NexusBlockCollapser nexusBlockCollapser) {
        this.nParagraphs = LiveList.sizeOf(area.getParagraphs());
        this.format = format;
        this.nexusBlockCollapser = nexusBlockCollapser;
    }

    public Node apply(int idx) {
        Label lineNo;
        Val<String> formatted = this.nParagraphs.map((n) -> {
            //System.err.println("line--- " + idx);
            //return this.format(idx + 1, n);
            int x = nexusBlockCollapser.getLineIndices().get(nexusBlockCollapser.getLineIndices().size()-1);
            /*if(idx !=-1) {
                System.err.println("line" + nexusBlockCollapser.getLineIndices().get(idx));
                return this.format(nexusBlockCollapser.getLineIndices().get(idx), x-1);
            }else
            //System.err.println("line"+idx);
                return this.format(0, x-1);*/
            return this.format(nexusBlockCollapser.getIndexFromList(idx), x-1);
        });
        lineNo = new Label();
        lineNo.setFont(DEFAULT_FONT);
        lineNo.setBackground(DEFAULT_BACKGROUND);
        lineNo.setTextFill(DEFAULT_TEXT_FILL);
        lineNo.setPadding(DEFAULT_INSETS);
        lineNo.setAlignment(Pos.TOP_RIGHT);
        lineNo.getStyleClass().add("lineno");
        lineNo.textProperty().bind(formatted.conditionOnShowing(lineNo));
        /*if (idx < hide[0]) {
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
        }*/

        // mouse interactions with labels to make them clickable
        lineNo.setOnMouseClicked(click -> {
            System.err.println("Clicked on label! "+lineNo.getText());

            int labelId = Integer.parseInt(lineNo.getText().replaceAll(" ",""));
            nexusBlockCollapser.handleBlock(labelId);
            System.err.println("Indices!");
            for(Integer i : nexusBlockCollapser.getLineIndices())
                System.err.print(i+"-");
        });
        lineNo.setOnMouseEntered(e -> {
            System.err.println("Over label! ");
            lineNo.setStyle("-fx-font-weight: bold; " +
                    "-fx-font: 16px \"monospace\"; -fx-text-fill: black");
        });
        lineNo.setOnMouseExited(e -> {
            System.err.println("Exited label! ");
            lineNo.setStyle(DEFAULT_FONT.toString());
        });
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
