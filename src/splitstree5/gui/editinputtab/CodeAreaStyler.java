/*
 *  CodeAreaStyler.java Copyright (C) 2020 Daniel H. Huson
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

package splitstree5.gui.editinputtab;

import javafx.beans.value.ObservableValue;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.value.Val;
import splitstree5.gui.editinputtab.highlighters.Highlighter;
import splitstree5.gui.editinputtab.highlighters.NexusHighlighter;
import splitstree5.gui.editinputtab.highlighters.UniversalHighlighter;
import splitstree5.gui.editinputtab.highlighters.XMLHighlighter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keeper for highlighting listeners
 */

public class CodeAreaStyler {

    //todo change codearea highlighting color

    private Highlighter highlighter = new NexusHighlighter();

    private boolean collapsingActive = false;
    private boolean hold;
    private int chIdx;
    private HashMap<String, String> tmpBlocksKeeper = new HashMap<>();

    public CodeAreaStyler(CodeArea codeArea){

        /*
         * Add listeners for highlighting type checking
         */
        Val.map(codeArea.textProperty(), n -> n.length() >= 6
                && n.replaceAll("^\\n+", "").substring(0, 6).toLowerCase().equals("#nexus"))
                .addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            System.err.println("Use Nexus highlighter");
                            highlighter = new NexusHighlighter();
                        }
                    }
                });

        Val.map(codeArea.textProperty(), n -> n.length() != 0 &&
                n.replaceAll("^\\n+", "").startsWith("<"))
                .addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            System.err.println("Use xml highlighter");
                            highlighter = new XMLHighlighter();
                        }
                    }
                });

        Val.map(codeArea.textProperty(), n -> n.length() >= 6
                && !n.replaceAll("^\\n+", "").substring(0, 6).toLowerCase().equals("#nexus")
                && !n.replaceAll("^\\n+", "").startsWith("<"))
                .addListener(new javafx.beans.value.ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            System.err.println("Use universal highlighter");
                            highlighter = new UniversalHighlighter();
                        }
                    }
                });

        /*
         * Block collapsing
         */

        /*codeArea.setMouseOverTextDelay(Duration.ofMillis(200));
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {

            if (collapsingActive){
                int i = e.getCharacterIndex();
                Point2D pos = e.getScreenPosition();

                if (codeArea.getStyleAtPosition(i).toString().contains("block")
                        || codeArea.getStyleAtPosition(i).toString().contains("collapsed")) {
                    codeArea.setCursor(Cursor.HAND);
                    hold = true;
                    chIdx = e.getCharacterIndex();
                }
            }
        });
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
            if (collapsingActive)
                codeArea.setCursor(Cursor.TEXT);
        });

        codeArea.setOnMouseClicked(click -> {

            if (hold && collapsingActive) {

                //int chIdx = click.getCharacterIndex();
                System.out.println(codeArea.getStyleAtPosition(chIdx).toString());

                if (codeArea.getStyleAtPosition(chIdx).toString().contains("block")){
                    collapseBlock(chIdx, codeArea);
                }

                if (codeArea.getStyleAtPosition(chIdx).toString().contains("collapsed")) {

                    for (String i : tmpBlocksKeeper.keySet()) {
                        System.out.println(tmpBlocksKeeper.get(i).substring(0, 10));
                    }

                    final String CB = "(<< Collapsed )(\\w+)(Block >>)";
                    Pattern PATTERN = Pattern.compile(CB);
                    Matcher matcher = PATTERN.matcher(codeArea.getText());

                    int cbStart = 0, cbEnd = 0;
                    String key;
                    while (matcher.find()) {
                        cbStart = matcher.start();
                        cbEnd = matcher.end();
                        if (matcher.end() > chIdx)
                            break;
                    }

                    System.out.println(codeArea.getText().substring(cbStart, cbEnd));

                    //int blockNr = Integer.parseInt(matcher.group(2));
                    //System.out.println("collapsed nr. "+matcher.group(2));

                    if (cbStart != 0 || cbEnd != 0) {
                        key = matcher.group(2);
                        codeArea.replaceText(cbStart, cbEnd, tmpBlocksKeeper.get(key));
                        tmpBlocksKeeper.remove(key);
                        //codeArea.setParagraphGraphicFactory(LineNumberFactoryWithCollapsing.get(codeArea));
                        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
                    }

                    /*int cbStart = chIdx, cbEnd = chIdx;
                    while(codeArea.getText().charAt(cbStart) != '<')
                        cbStart--;
                    while(codeArea.getText().charAt(cbEnd) != '>')
                        cbEnd++;
                    cbStart--;
                    cbEnd++;

                    final String CB = "(< Collapsed )(\\w+)(Block >)";
                    Pattern PATTERN = Pattern.compile(CB);

                    String CB_string = getKeyWord(chIdx, false);
                    System.out.println(CB_string);
                    Matcher matcher = PATTERN.matcher(CB_string);

                    if (matcher.find()) {
                        String key = matcher.group(2);
                        codeArea.replaceText(cbStart, cbEnd, tmpBlocksKeeper22.get(key));
                        tmpBlocksKeeper22.remove(key);
                    }<------- end of block comment!!!
                }
                hold = false; // prevents double collapsing after continuous clicks
            }
        });*/


    }


    private static String returnFirstLine(String s){
        if (s.length() == 0 || !s.contains("\\n"))
            return s;
        else
            return s.substring(0, s.indexOf("\\n"));
    }

    private void collapseBlock(int charIndex, CodeArea codeArea) {

        int start2remove = 0;
        int end2remove = 0;
        //int endOffset = codeArea.getText().substring(0, chIdx).length();

        Pattern PATTERN1 = Pattern.compile("(?i)\\b(end|endblock)\\b;");
        Pattern PATTERN2 = Pattern.compile("(?i)\\bbegin\\b");
        Matcher matcher1 = PATTERN1.matcher(codeArea.getText().substring(charIndex));
        Matcher matcher2 = PATTERN2.matcher(codeArea.getText().substring(0, charIndex));

        // the first end
        if (matcher1.find())
            end2remove = matcher1.end() + charIndex + 1;
        // the last begin
        while (matcher2.find())
            start2remove = matcher2.start();

        System.out.println("start2remove "+start2remove);
        System.out.println("end2remove "+end2remove);
        //System.out.println("Block Nr. "+beginCounter);

        //tmpBlocksKeeper.put(beginCounter, codeArea.getText().substring(start2remove, end2remove));

        if (start2remove != 0 || end2remove != 0) {
            /*blocksCounter ++;
            tmpBlocksKeeper.put(blocksCounter, codeArea.getText().substring(start2remove, end2remove));
            codeArea.replaceText(start2remove, end2remove, "<< Collapsed block "+blocksCounter+">>");
            System.out.println("Block Nr. "+blocksCounter);*/

            /*int linesCounter = paragraphStart;
            int sum = start2remove;
            while(sum < end2remove){
                sum = sum + codeArea.getParagraph(paragraphStart).length();
                linesCounter++;
            }*/
            int[] replaceRange = getLinesRangeByIndex(start2remove, end2remove, codeArea);

            String keyWord = getKeyWord(charIndex, true, codeArea);
            tmpBlocksKeeper.put(keyWord, codeArea.getText().substring(start2remove, end2remove));
            codeArea.replaceText(start2remove, end2remove, "<< Collapsed "+keyWord+"Block >>");
            System.out.println("Block Nr. "+keyWord);

            //int[] replaceRange = {paragraphStart, linesCounter};
            // todo delete? -- codeArea.setParagraphGraphicFactory(LineNumberFactoryWithCollapsing.get(codeArea, replaceRange));
        }
    }

    private String getKeyWord(int position, boolean collapse, CodeArea codeArea) {

        int leftPos = position;
        int rightPos = position;

        if (collapse) {
            while(Character.isLetter(codeArea.getText().charAt(leftPos)))
                leftPos--;
            while(Character.isLetter(codeArea.getText().charAt(rightPos)))
                rightPos++;
            leftPos++;
        } else {
            while(codeArea.getText().charAt(leftPos) != '<')
                leftPos--;
            while(codeArea.getText().charAt(leftPos) != '>')
                rightPos++;
            rightPos++;
        }
        return codeArea.getText().substring(leftPos, rightPos);
    }


    private int[] getLinesRangeByIndex(int startIdx, int endItx, CodeArea codeArea){

        /*int startLine = 0;
        int sum = 0;
        while(sum < startIdx){
            sum = sum + codeArea.getParagraph(startLine).length();
            startLine++;
        }


        int endLine = startLine++;
        sum = startIdx;
        while(sum < endItx){
            sum = sum + codeArea.getParagraph(endLine).length();
            System.err.println(codeArea.getParagraph(endLine)+"--------");
            endLine++;
        }*/

        System.err.println(Arrays.toString(codeArea.getText(0, startIdx).split("\n")));
        System.err.println(codeArea.getText(startIdx, endItx));

        int endLine = codeArea.getText(0, endItx).split("\n").length;
        int startLine = endLine - codeArea.getText(startIdx, endItx).split("\n").length;

        System.err.println("Start line of block "+startLine);
        System.err.println("End line of block "+endLine);

        return new int[]{startLine, endLine};
    }


    public Highlighter getHighlighter(){
        return this.highlighter;
    }

    public void setCollapsingActive(boolean active){
        this.collapsingActive = active;
    }
}
