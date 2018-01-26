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

import javafx.beans.property.*;
import jloda.fx.CallableService;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import splitstree5.core.connectors.TaskWithProgressListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * search manager
 * Daniel Huson, 1.2018
 */
public class SearchManager {
    private final CallableService<Integer> service = new CallableService<>();

    private final ObjectProperty<ISearcher> searcher = new SimpleObjectProperty<>();

    private final BooleanProperty disabled = new SimpleBooleanProperty(true);

    private final BooleanProperty caseSensitiveOption = new SimpleBooleanProperty(false);
    private final BooleanProperty wholeWordsOnlyOption = new SimpleBooleanProperty(false);
    private final BooleanProperty regularExpressionsOption = new SimpleBooleanProperty(false);

    private final BooleanProperty forwardDirection = new SimpleBooleanProperty(true);
    private final BooleanProperty globalScope = new SimpleBooleanProperty(true);
    private final BooleanProperty equateUnderscoreWithSpace = new SimpleBooleanProperty(true);

    private final StringProperty searchText = new SimpleStringProperty();
    private final StringProperty replaceText = new SimpleStringProperty();

    private final StringProperty message = new SimpleStringProperty();

    /**
     * constructor
     */
    public SearchManager() {
        disabled.bind(searcher.isNull());

        service.setOnScheduled((e) -> message.set(""));

        service.setOnFailed((e) -> System.err.println("Search failed: " + service.getException()));

        // any change clears the message:
        caseSensitiveOption.addListener(c -> message.set(""));
        wholeWordsOnlyOption.addListener(c -> message.set(""));
        regularExpressionsOption.addListener(c -> message.set(""));
        forwardDirection.addListener(c -> message.set(""));
        globalScope.addListener(c -> message.set(""));
        equateUnderscoreWithSpace.addListener(c -> message.set(""));
        searchText.addListener(c -> message.set(""));
        replaceText.addListener(c -> message.set(""));
    }

    /**
     * erase the current selection
     */
    public void doUnselectAll() {
        searcher.get().selectAll(false);
    }

    /**
     * find the first occurrence
     */
    public void findFirst() {
        final TaskWithProgressListener<Integer> task = new TaskWithProgressListener<Integer>() {
            @Override
            public Integer call() throws Exception {
                return doFindFirst(getProgressListener()) ? 1 : 0;
            }
        };
        service.setCallable(task);
        service.setOnSucceeded((e) -> {
            message.set(service.getValue() > 0 ? "Found" : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * find the first next
     */
    public void findNext() {
        final TaskWithProgressListener<Integer> task = new TaskWithProgressListener<Integer>() {
            @Override
            public Integer call() throws Exception {
                return doFindNext(getProgressListener()) ? 1 : 0;
            }
        };
        service.setCallable(task);
        service.setOnSucceeded((e) -> {
            message.set(service.getValue() > 0 ? "Found" : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * find all
     */
    public void findAll() {
        final TaskWithProgressListener<Integer> task = new TaskWithProgressListener<Integer>() {
            @Override
            public Integer call() throws Exception {
                return doFindAll(getProgressListener());
            }
        };
        service.setCallable(task);
        service.setOnSucceeded((e) -> {
            message.set(service.getValue() > 0 ? "Found " + service.getValue() : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * replace and find
     */
    public void findAndReplace() {
        final TaskWithProgressListener<Integer> task = new TaskWithProgressListener<Integer>() {
            @Override
            public Integer call() throws Exception {
                return doFindAndReplace(getProgressListener()) ? 1 : 0;
            }
        };
        service.setCallable(task);
        service.setOnSucceeded((e) -> {
            message.set(service.getValue() > 0 ? "Replaced" : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * replace all
     */
    public void replaceAll() {
        final TaskWithProgressListener<Integer> task = new TaskWithProgressListener<Integer>() {
            @Override
            public Integer call() throws Exception {
                return doReplaceAll(getProgressListener());
            }
        };
        service.setCallable(task);
        service.setOnSucceeded((e) -> {
            message.set(service.getValue() > 0 ? "Replaced " + service.getValue() : "No matches");
            if (service.getValue() > 0)
                getSearcher().updateView();
        });
        service.restart();
    }

    /**
     * find the first occurrence of the query
     */
    private boolean doFindFirst(ProgressListener progress) throws CanceledException {
        if (isDisabled())
            return false;

        boolean changed = false;
        getSearcher().selectAll(false);
        if (getSearcher() instanceof IObjectSearcher) {
            IObjectSearcher oSearcher = (IObjectSearcher) getSearcher();
            boolean ok = isForwardDirection() ? oSearcher.gotoFirst() : oSearcher.gotoLast();


            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            final Pattern pattern = Pattern.compile(regexp);

            progress.setMaximum(oSearcher.numberOfObjects());
            progress.setProgress(0);

            while (ok) {
                if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                    String label = oSearcher.getCurrentLabel();
                    if (label == null)
                        label = "";
                    if (isEquateUnderscoreWithSpace())
                        label = label.replaceAll("_", " ");
                    if (matches(pattern, label)) {
                        oSearcher.setCurrentSelected(true);
                        changed = true;
                        break;
                    }
                }
                ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                progress.incrementProgress();
            }
        } else if (getSearcher() instanceof ITextSearcher) {
            ITextSearcher tSearcher = (ITextSearcher) getSearcher();
            tSearcher.setGlobalScope(isGlobalScope());

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            changed = tSearcher.findFirst(regexp);
        }
        return changed;
    }

    /**
     * find the next occurrence of the query
     */
    private boolean doFindNext(ProgressListener progressListener) throws CanceledException {
        if (isDisabled())
            return false;

        boolean changed = false;
        if (getSearcher() instanceof IObjectSearcher) {
            IObjectSearcher oSearcher = (IObjectSearcher) getSearcher();
            boolean ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();

            progressListener.setMaximum(-1);

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            final Pattern pattern = Pattern.compile(regexp);
            while (ok) {
                if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                    String label = oSearcher.getCurrentLabel();
                    if (label == null)
                        label = "";
                    if (isEquateUnderscoreWithSpace())
                        label = label.replaceAll("_", " ");
                    if (matches(pattern, label)) {
                        oSearcher.setCurrentSelected(true);
                        changed = true;
                        break;
                    }
                }
                ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                progressListener.checkForCancel();
            }
        } else if (getSearcher() instanceof ITextSearcher) {
            ITextSearcher tSearcher = (ITextSearcher) searcher;
            tSearcher.setGlobalScope(isGlobalScope());

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            if (isForwardDirection()) {
                changed = tSearcher.findNext(regexp);
            } else
                changed = tSearcher.findPrevious(regexp);
        }
        return changed;
    }

    /**
     * select all occurrences of the query string
     */
    private int doFindAll(ProgressListener progressListener) throws CanceledException {
        if (isDisabled())
            return 0;

        int count = 0;
        if (getSearcher() instanceof IObjectSearcher) {
            IObjectSearcher oSearcher = (IObjectSearcher) getSearcher();
            boolean ok = oSearcher.gotoFirst();

            progressListener.setMaximum(oSearcher.numberOfObjects());

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            final Pattern pattern = Pattern.compile(regexp);
            while (ok) {
                if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                    String label = oSearcher.getCurrentLabel();
                    if (label == null)
                        label = "";
                    if (isEquateUnderscoreWithSpace())
                        label = label.replaceAll("_", " ");
                    boolean select = matches(pattern, label);
                    if (select) {
                        oSearcher.setCurrentSelected(true);
                        count++;
                    }
                }
                ok = oSearcher.gotoNext();
                progressListener.incrementProgress();
            }
        } else if (getSearcher() instanceof ITextSearcher) {
            ITextSearcher tSearcher = (ITextSearcher) getSearcher();
            tSearcher.setGlobalScope(isGlobalScope());
            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            count = tSearcher.findAll(regexp);
        }
        return count;
    }

    /**
     * replace current or next occurrence of the query string
     */
    private boolean doFindAndReplace(ProgressListener progressListener) throws CanceledException {
        if (isDisabled())
            return false;

        boolean changed = false;
        if (getSearcher() instanceof IObjectSearcher) {
            IObjectSearcher oSearcher = (IObjectSearcher) getSearcher();

            progressListener.setMaximum(-1);

            boolean ok = oSearcher.isCurrentSet();
            if (!ok)
                ok = isForwardDirection() ? oSearcher.gotoFirst() : oSearcher.gotoLast();

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            final Pattern pattern = Pattern.compile(regexp);

            while (ok) {
                if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                    String label = oSearcher.getCurrentLabel();
                    if (isEquateUnderscoreWithSpace())
                        label = label.replaceAll("_", " ");
                    if (label == null)
                        label = "";
                    String replace = getReplacement(pattern, getReplaceText(), label);
                    if (replace != null && !label.equals(replace)) {
                        oSearcher.setCurrentSelected(true);
                        oSearcher.setCurrentLabel(replace);
                        changed = true;
                        break;
                    }
                }

                ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                progressListener.checkForCancel();
            }
        } else if (getSearcher() instanceof ITextSearcher) {
            ITextSearcher tSearcher = (ITextSearcher) getSearcher();
            tSearcher.setGlobalScope(isGlobalScope());

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            changed = tSearcher.replaceNext(regexp, getReplaceText());
        }
        return changed;
    }

    /**
     * replace all occurrences of the query string
     */
    private int doReplaceAll(ProgressListener progressListener) throws CanceledException {
        if (isDisabled())
            return 0;

        int count = 0;
        boolean changed = false;

        if (getSearcher() instanceof IObjectSearcher) {
            IObjectSearcher oSearcher = (IObjectSearcher) getSearcher();
            boolean ok = isForwardDirection() ? oSearcher.gotoFirst() : oSearcher.gotoLast();
            progressListener.setMaximum(oSearcher.numberOfObjects());

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            final Pattern pattern = Pattern.compile(regexp);

            while (ok) {
                if (isGlobalScope() || oSearcher.isCurrentSelected()) {
                    String label = oSearcher.getCurrentLabel();
                    if (label == null)
                        label = "";
                    if (isEquateUnderscoreWithSpace())
                        label = label.replaceAll("_", " ");
                    String replace = getReplacement(pattern, getReplaceText(), label);
                    if (replace != null && !replace.equals(label)) {
                        oSearcher.setCurrentSelected(true);
                        oSearcher.setCurrentLabel(replace);
                        changed = true;
                        count++;
                    }
                }
                ok = isForwardDirection() ? oSearcher.gotoNext() : oSearcher.gotoPrevious();
                progressListener.incrementProgress();
            }
        } else if (getSearcher() instanceof ITextSearcher) {
            ITextSearcher tSearcher = (ITextSearcher) getSearcher();
            tSearcher.setGlobalScope(isGlobalScope());

            final String regexp = prepareRegularExpression(isEquateUnderscoreWithSpace() ? getSearchText().replaceAll("_", " ") : getSearchText());
            count = tSearcher.replaceAll(regexp, getReplaceText(), !isGlobalScope());
            if (count > 0)
                changed = true;
        }
        return count;
    }

    /**
     * does label match pattern?
     *
     * @param pattern
     * @param label
     * @return true, if match
     */
    private boolean matches(Pattern pattern, String label) {
        if (label == null)
            label = "";
        Matcher matcher = pattern.matcher(label);
        return matcher.find();
    }

    /**
     * determines whether pattern matches label.
     *
     * @param pattern
     * @param replacement
     * @param label
     * @return result of replacing query by replace string in label
     */
    private String getReplacement(Pattern pattern, String replacement, String label) {
        if (label == null)
            label = "";
        if (replacement == null)
            replacement = "";

        Matcher matcher = pattern.matcher(label);
        return matcher.replaceAll(replacement);
    }

    /**
     * prepares the regular expression that reflects the chosen find options
     *
     * @param query
     * @return regular expression
     */
    private String prepareRegularExpression(String query) {
        if (query == null)
            query = "";

        String regexp = "" + query; //Copy the search string over.

        /* Reg expression or not? If not regular expression, we need to surround the above
        with quote literals: \Q expression \E just in case there are some regexp characters
        already there. Note - this will fail if string already contains \E or \Q !!!!!!! */
        if (!isRegularExpressionsOption()) {
            if (regexp.contains("\\E"))
                throw new PatternSyntaxException("Illegal character ''\\'' in search string", query, -1);
            // TODO: this doesn't seem to work here, perhaps needs 1.5?
            regexp = '\\' + "Q" + regexp + '\\' + "E";
        }

        if (isWholeWordsOnlyOption())
            regexp = "\\b" + regexp + "\\b";

        /* Check if case insensitive - if it is, then append (?i) before string */
        if (!isCaseSensitiveOption())
            regexp = "(?i)" + regexp;

        //System.err.println(regexp);
        return regexp;
    }

    public ISearcher getSearcher() {
        return searcher.get();
    }

    public ObjectProperty<ISearcher> searcherProperty() {
        return searcher;
    }

    public void setSearcher(ISearcher searcher) {
        this.searcher.set(searcher);
    }

    public boolean isCaseSensitiveOption() {
        return caseSensitiveOption.get();
    }

    public BooleanProperty caseSensitiveOptionProperty() {
        return caseSensitiveOption;
    }

    public void setCaseSensitiveOption(boolean caseSensitiveOption) {
        this.caseSensitiveOption.set(caseSensitiveOption);
    }

    public boolean isWholeWordsOnlyOption() {
        return wholeWordsOnlyOption.get();
    }

    public BooleanProperty wholeWordsOnlyOptionProperty() {
        return wholeWordsOnlyOption;
    }

    public void setWholeWordsOnlyOption(boolean wholeWordsOnlyOption) {
        this.wholeWordsOnlyOption.set(wholeWordsOnlyOption);
    }

    public boolean isRegularExpressionsOption() {
        return regularExpressionsOption.get();
    }

    public BooleanProperty regularExpressionsOptionProperty() {
        return regularExpressionsOption;
    }

    public void setRegularExpressionsOption(boolean regularExpressionsOption) {
        this.regularExpressionsOption.set(regularExpressionsOption);
    }

    public boolean isForwardDirection() {
        return forwardDirection.get();
    }

    public BooleanProperty forwardDirectionProperty() {
        return forwardDirection;
    }

    public void setForwardDirection(boolean forwardDirection) {
        this.forwardDirection.set(forwardDirection);
    }

    public boolean isGlobalScope() {
        return globalScope.get();
    }

    public BooleanProperty globalScopeProperty() {
        return globalScope;
    }

    public void setGlobalScope(boolean globalScope) {
        this.globalScope.set(globalScope);
    }

    public boolean isEquateUnderscoreWithSpace() {
        return equateUnderscoreWithSpace.get();
    }

    public BooleanProperty equateUnderscoreWithSpaceProperty() {
        return equateUnderscoreWithSpace;
    }

    public void setEquateUnderscoreWithSpace(boolean equateUnderscoreWithSpace) {
        this.equateUnderscoreWithSpace.set(equateUnderscoreWithSpace);
    }

    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public String getSearchText() {
        return searchText.get();
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public String getReplaceText() {
        return replaceText.get();
    }

    public StringProperty replaceTextProperty() {
        return replaceText;
    }

    public void setReplaceText(String replaceText) {
        this.replaceText.set(replaceText);
    }

    public void close() {
        service.cancel();
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public void cancel() {
        service.cancel();
    }
}
