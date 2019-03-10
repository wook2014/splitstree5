package splitstree5.gui.editinputtab.collapsing;

import org.fxmisc.richtext.CodeArea;
import org.reactfx.collection.LiveList;

import java.util.ArrayList;

public class NexusBlockCollapser {

    private CodeArea codeArea;
    private ArrayList<NexusBlockCollapseInfo> nexusBlockCollapseInfos;
    private ArrayList<Integer> lineIndices = new ArrayList<>();

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
     * Collapse nexus block
     * @param startLineNumber line number of begin key word showed on viewer
     */

    public void collapseBlock(int startLineNumber){
        for (NexusBlockCollapseInfo i : this.nexusBlockCollapseInfos){
            if (i.getStartLine() == startLineNumber) {
                removeLinesRangeFromList(i.getStartLine(), i.getEndLine());
                updatePositions(i.getStartPosition(), i.getEndPosition());

                codeArea.replaceText(i.getStartPosition(), i.getEndPosition(), "<< Collapsed Block >>");
                i.setCollapsed(true);
            }
        }
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
        for (int i = start; i<end; i++) {
            Integer toRemove = i;
            this.lineIndices.remove(toRemove);
        }
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

}
