/*
 * NexusBlockCollapseInfo.java Copyright (C) 2020. Daniel H. Huson
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

import org.fxmisc.richtext.CodeArea;

/**
 * Information about position of a Nexus block in the CodeArea. Used for block collapsing.
 * <p>
 * Created on Mar 2019
 *
 * @author Daria
 */

public class NexusBlockCollapseInfo {

    private int startPosition;
    private int endPosition;
    private int startLine;
    private int endLine;
    private boolean collapsed;

    /* todo idee
    for each line number and each nexus block:
    1. check if less then start of min block
    2. if bigger and collapsed add length as offset
     */

    public NexusBlockCollapseInfo(int start, int end){
        this.startPosition = start;
        this.endPosition = end;

        this.collapsed = false;
        this.startLine = -1;
        this.endLine = -1;
    }

    public void setLinesRangeByIndex(CodeArea codeArea){
        this.endLine = codeArea.getText(0, this.endPosition).split("\n").length;
        this.startLine = endLine - codeArea.getText(this.startPosition, this.endPosition).split("\n").length + 1;
    }

    public int getStartPosition(){
        return this.startPosition;
    }

    public int getEndPosition(){
        return this.endPosition;
    }

    public int getStartLine(){
        return this.startLine;
    }

    public int getEndLine(){
        return this.endLine;
    }

    public void setStartPosition(int startPosition){
        this.startPosition = startPosition;
    }

    public void setEndPosition(int endPosition){
        this.endPosition = endPosition;
    }

    public void setStartLine(int startLine){
        this.startLine = startLine;
    }

    public void setEndLine(int endLine){
        this.endLine = endLine;
    }

    public boolean getCollapsed(){
        return this.collapsed;
    }

    public void setCollapsed(boolean collapsed){
        this.collapsed = collapsed;
    }
}
