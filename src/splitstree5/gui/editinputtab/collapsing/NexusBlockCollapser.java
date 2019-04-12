package splitstree5.gui.editinputtab.collapsing;

import org.fxmisc.richtext.CodeArea;
import org.reactfx.collection.LiveList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Contains functions for collapsing and un-collapsing of Nexus blocks in the CodeArea
 * <p>
 * Created on Mar 2019
 *
 * @author Daria
 */

public class NexusBlockCollapser {

    private CodeArea codeArea;
    private ArrayList<NexusBlockCollapseInfo> nexusBlockCollapseInfos;
    private ArrayList<Integer> lineIndices = new ArrayList<>();
    private HashMap<Integer, String> tmpBlocksKeeper = new HashMap<>();

    public NexusBlockCollapser(CodeArea codeArea, ArrayList<NexusBlockCollapseInfo> info){
        this.codeArea = codeArea;

        this.nexusBlockCollapseInfos = info;
        for(NexusBlockCollapseInfo i : this.nexusBlockCollapseInfos)
            i.setLinesRangeByIndex(codeArea);

        // all lines of code area
        int nLines = LiveList.sizeOf(codeArea.getParagraphs()).getValue();
        for (int i = 1; i<= nLines; i++)
            this.lineIndices.add(i);
    }

    /**
     * Get nexus block by id = startLineNumber and collapse/uncollapse it
     * @param startLineNumber line number of begin key word showed on viewer
     */

    public void handleBlock(int startLineNumber){
        for (NexusBlockCollapseInfo i : this.nexusBlockCollapseInfos){
            if (i.getStartLine() == startLineNumber && !i.getCollapsed()) {
                collapseBlock(i);
            } else if (i.getEndLine() == startLineNumber && i.getCollapsed()) {
                unCollapseBlock(i);
            }
        }
    }

    /**
     * Collapse nexus block
     * @param i Nexus Block
     */

    private void collapseBlock(NexusBlockCollapseInfo i){

        removeLinesRangeFromList(i.getStartLine(), i.getEndLine());
        updatePositions(i.getStartPosition(), i.getEndPosition());

        tmpBlocksKeeper.put(i.getStartLine(), codeArea.getText(i.getStartPosition(), i.getEndPosition()));
        codeArea.replaceText(i.getStartPosition(), i.getEndPosition(), "<< Collapsed Block >>");
        i.setCollapsed(true);
    }

    /**
     * Return the nexus block to the viewer
     * @param i Nexus Block
     */

    private void unCollapseBlock(NexusBlockCollapseInfo i){
        System.err.println("un-collapsing");
        System.err.println(tmpBlocksKeeper.get(i.getStartLine()));

        int replacementLength = "<< Collapsed Block >>".length();
        codeArea.replaceText(i.getStartPosition(), i.getStartPosition()+replacementLength, "");
        codeArea.insertText(i.getStartPosition(), tmpBlocksKeeper.get(i.getStartLine()));
        insertLinesRangeInList(i.getStartLine(), i.getEndLine());
        updatePositionsUnCollapse(i.getStartPosition(), i.getEndPosition());
        i.setCollapsed(false);
    }


    public ArrayList<Integer> getLineIndices(){
        return this.lineIndices;
    }

    public int getIndexFromList(int i){
        if(i == -1)
            return 0;
        else if (i >= this.getLineIndices().size())
            return this.getLineIndices().size() + this.getLineIndices().size() - i;
        else
            return this.getLineIndices().get(i);
    }

    private void removeLinesRangeFromList(int start, int end){
        for (int i = start; i < end; i++) {
            this.lineIndices.remove((Integer) i);
        }
    }

    private void insertLinesRangeInList(int start, int end){
        for (int i = start; i < end; i++) {
            this.lineIndices.add(i);
        }
        Collections.sort(this.lineIndices);
    }

    private void updatePositions(int startPos2Delete, int endPos2Delete){
        int range = endPos2Delete - startPos2Delete;
        int insertion = "<< Collapsed Block >>".length();

        for (NexusBlockCollapseInfo i : this.nexusBlockCollapseInfos){
            if (i.getStartPosition() > endPos2Delete) {
                i.setStartPosition(i.getStartPosition() - range + insertion);
                i.setEndPosition(i.getEndPosition() - range + insertion);
            }
        }
    }

    private void updatePositionsUnCollapse(int startPos2Insert, int endPos2Insert){
        int range = endPos2Insert - startPos2Insert;
        int insertion = "<< Collapsed Block >>".length();

        for (NexusBlockCollapseInfo i : this.nexusBlockCollapseInfos){
            if (i.getStartPosition() > startPos2Insert) {
                i.setStartPosition(i.getStartPosition() + range - insertion);
                i.setEndPosition(i.getEndPosition() + range - insertion);
            }
        }
    }

}
