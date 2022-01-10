package splitstree5.core.algorithms.distances2splits.neighbornet.NeighborNetPCG;

public class NnetTest {
    public static void main(String[] args)  {
        TridiagonalMatrix.test(1000);
        //CircularSplitAlgorithms.test(10);
        //BlockXMatrix.test(12);
        long startTime = System.currentTimeMillis();
        //NeighborNetSplits.test(50);
        long finishTime = System.currentTimeMillis();
        //System.err.println("Block Pivot took "+ (finishTime-startTime)+ " milliseconds");
    }
}
