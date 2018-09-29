package jloda.find;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.reactfx.value.Val;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * CodeArea searcher
 * Daniel Huson, Daria Evseeva 7.2018
 */
public class CodeAreaSearcher implements ITextSearcher {

    private final CodeArea codeArea;

    private final BooleanProperty globalFindable = new SimpleBooleanProperty(false);
    private final BooleanProperty selectionReplaceable = new SimpleBooleanProperty(false);

    private final String name;

    /**
     * constructor
     */
    public CodeAreaSearcher(String name, CodeArea codeArea) {
        this.name = name;
        this.codeArea = codeArea;
        if (codeArea != null) {
            //globalFindable.bind(codeArea.textProperty().isNotEmpty());
            //selectionReplaceable.bind(codeArea.selectedTextProperty().isNotEmpty());
            globalFindable.bind(Val.map(codeArea.lengthProperty(), n -> n == 0));
            selectionReplaceable.bind(Val.map(codeArea.lengthProperty(), n -> n == 0));
        }
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
        //codeArea.positionCaret(0);
        codeArea.moveTo(0); //todo test
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
        return codeArea != null && singleSearch(regularExpression, false);
    }

    /**
     * Replace selection with current. Does nothing if selection invalid.
     *
     * @param regularExpression
     * @param replaceText
     */
    public boolean replaceNext(String regularExpression, String replaceText) {
        if (codeArea == null) return false;
        if (findNext(regularExpression)) {
            codeArea.replaceSelection(replaceText);
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
        if (codeArea == null) return 0;

        final ArrayList<IndexRange> occurrences = new ArrayList<>();
        {
            final IndexRange indexRange;
            if (selectionOnly)
                indexRange = codeArea.getSelection();
            else
                indexRange = null;

            final Pattern pattern = Pattern.compile(regularExpression);
            final Matcher matcher = pattern.matcher(codeArea.getText());
            int pos = 0;

            int offset = 0; // need to take into account that text length may change during replacement

            while (matcher.find(pos)) {
                if (indexRange == null || matcher.start() >= indexRange.getStart() && matcher.end() <= indexRange.getEnd()) {
                    occurrences.add(new IndexRange(matcher.start() + offset, matcher.end() + offset));
                    offset += replaceText.length() - (matcher.end() - matcher.start());
                }
                pos = matcher.end();
            }
        }

        for (IndexRange range : occurrences) {
            codeArea.replaceText(range, replaceText);

        }
        return occurrences.size();
    }

    /**
     * is a global find possible?
     *
     * @return true, if there is at least one object
     */
    public ReadOnlyBooleanProperty isGlobalFindable() {
        return globalFindable;
    }

    /**
     * is a selection find possible
     *
     * @return true, if at least one object is selected
     */
    public ReadOnlyBooleanProperty isSelectionFindable() {
        return selectionReplaceable;
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
            codeArea.selectAll();
        } else {
            //codeArea.positionCaret(0); // todo test
            codeArea.moveTo(0);
        }
    }


    //We start the search at the end of the selection, which could be the dot or the mark.

    private int getSearchStart() {
        if (codeArea == null) return 0;
        return Math.max(codeArea.getAnchor(), codeArea.getCaretPosition());
    }


    private void selectMatched(Matcher matcher) {
        codeArea.selectRange(matcher.start(), matcher.end());
    }

    private boolean singleSearch(String regularExpression, boolean forward) throws PatternSyntaxException {
        if (codeArea == null) return false;

        //Do nothing if there is no text.
        if (regularExpression.length() == 0)
            return false;

        //Search begins at the end of the currently selected portion of text.
        int currentPoint = getSearchStart();


        boolean found = false;

        Pattern pattern = Pattern.compile(regularExpression);

        String source = codeArea.getText();
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
        return codeArea != null;
    }
}