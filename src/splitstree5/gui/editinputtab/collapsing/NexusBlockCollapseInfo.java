package splitstree5.gui.editinputtab.collapsing;

import org.fxmisc.richtext.CodeArea;

import java.util.Arrays;

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

    public void setCollapsed(boolean collapsed){
        this.collapsed = collapsed;
    }
}
