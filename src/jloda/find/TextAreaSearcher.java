/*
 *  Copyright (C) 2018 Daniel H. Huson
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

package jloda.find;

import javafx.scene.control.TextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * JTextArea searcher
 * Daniel Huson, 7.2008
 */
public class TextAreaSearcher implements ITextSearcher {
    final TextArea textArea;

    private final String name;

    /**
     * constructor
     */
    public TextAreaSearcher(String name, TextArea textArea) {
        this.name = name;
        this.textArea = textArea;
    }

    /**
     * get the name for this type of search
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Find first instance
     *
     * @param regularExpression
     * @return - returns boolean: true if text found, false otherwise
     */
    public boolean findFirst(String regularExpression) {
        textArea.positionCaret(0);
        return singleSearch(regularExpression, true);
    }

    /**
     * Find next instance
     *
     * @param regularExpression
     * @return - returns boolean: true if text found, false otherwise
     */
    public boolean findNext(String regularExpression) {
        return singleSearch(regularExpression, true);
    }

    /**
     * Find previous instance
     *
     * @param regularExpression
     * @return - returns boolean: true if text found, false otherwise
     */
    public boolean findPrevious(String regularExpression) {
        return textArea != null && singleSearch(regularExpression, false);
    }

    /**
     * Replace selection with current. Does nothing if selection invalid.
     *
     * @param regularExpression
     * @param replaceText
     */
    public boolean replaceNext(String regularExpression, String replaceText) {
        if (textArea == null) return false;
        if (findNext(regularExpression)) {
            textArea.replaceSelection(replaceText);
            return true;
        }
        return false;
    }

    /**
     * Replace all occurrences of text in document, subject to options.
     *
     * @param regularExpression
     * @param replaceText
     * @return number of instances replaced
     */
    public int replaceAll(String regularExpression, String replaceText, boolean selectionOnly) {
        if (textArea == null) return 0;



        final String source;
        if (selectionOnly)
            source = textArea.getSelectedText();
        else
            source = textArea.getText();

        int count = 0;

        if (source != null) {
            final Pattern pattern = Pattern.compile(regularExpression);
            final Matcher matcher = pattern.matcher(source);
            //We do a manual count of the number of >>non-overlapping<< patterns match.
            int pos = 0;
            while (matcher.find(pos)) {
                count++;
                pos = matcher.end();
            }

            if (selectionOnly)
                textArea.replaceSelection(matcher.replaceAll(replaceText));
            else {
                textArea.setText(matcher.replaceAll(replaceText));
                textArea.positionCaret(0);
            }

        }
        return count;
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public boolean isGlobalFindable() {
        return textArea != null && textArea.getText().length() > 0;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public boolean isSelectionFindable() {
        return textArea != null && textArea.getSelectedText() != null && textArea.getSelectedText().length() > 0;
    }

    /**
     * Selects all occurrences of text in document, subject to options and constraints of document type
     *
     * @param pattern
     */
    public int findAll(String pattern) {
        //Not implemented for text editors.... as we cannot select multiple chunks of text.
        return 0;
    }

    /**
     * something has been changed or selected, update view
     */
    public void updateView() {
    }

    /**
     * does this searcher support find all?
     *
     * @return true, if find all supported
     */
    public boolean canFindAll() {
        return false;
    }

    /**
     * set select state of all objects
     *
     * @param select
     */
    public void selectAll(boolean select) {
        if (select) {
            textArea.selectAll();
        } else {
            textArea.positionCaret(0);
        }
    }


    //We start the search at the end of the selection, which could be the dot or the mark.

    private int getSearchStart() {
        if (textArea == null) return 0;
        return Math.max(textArea.getAnchor(), textArea.getCaretPosition());
    }


    private void selectMatched(Matcher matcher) {
        textArea.selectRange(matcher.start(), matcher.end());
    }

    private boolean singleSearch(String regularExpression, boolean forward) throws PatternSyntaxException {
        if (textArea == null) return false;

        //Do nothing if there is no text.
        if (regularExpression.length() == 0)
            return false;

        //Search begins at the end of the currently selected portion of text.
        int currentPoint = getSearchStart();


        boolean found = false;

        Pattern pattern = Pattern.compile(regularExpression);

        String source = textArea.getText();
        Matcher matcher = pattern.matcher(source);

        if (forward)
            found = matcher.find(currentPoint);
        else {
            //This is an inefficient algorithm to handle reverse search. It is a temporary
            //stop gap until reverse searching is built into the API.
            //TODO: Check every once and a while to see when matcher.previous() is implemented in the API.
            //TODO: Consider use of GNU find/replace.
            //TODO: use regions to make searching more efficient when we know the length of the search string to match.
            int pos = 0;
            int searchFrom = 0;
            //System.err.println("Searching backwards before " + currentPoint);
            while (matcher.find(searchFrom) && matcher.end() < currentPoint) {
                pos = matcher.start();
                searchFrom = matcher.end();
                found = true;
                //System.err.println("\tfound at [" + pos + "," + matcher.end() + "]" + " but still looking");
            }
            if (found)
                matcher.find(pos);
            //System.err.println("\tfound at [" + pos + "," + matcher.end() + "]");
        }

        if (!found && currentPoint != 0) {
            matcher = pattern.matcher(source);
            found = matcher.find();
        }

        if (!found)
            return false;

        //System.err.println("Pattern found between positions " + matcher.start() + " and " + matcher.end());
        selectMatched(matcher);
        return true;
    }

    /**
     * set scope global rather than selected
     *
     * @param globalScope
     */
    public void setGlobalScope(boolean globalScope) {
    }

    /**
     * get scope global rather than selected
     *
     * @return true, if search scope is global
     */
    public boolean isGlobalScope() {
        return textArea != null;
    }
}
