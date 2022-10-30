package gccBin.MidCode.firstProcess;

import java.util.ArrayList;
import java.util.BitSet;

public class BasicBlock {
    private int index;

    private BitSet genSum;
    private BitSet killSum;
    private BitSet sum;
    private BitSet in;
    private BitSet out;

    private ArrayList<Integer> inBlocks;
    private ArrayList<Integer> outBlocks;

    public BasicBlock(int index){
        this.index = index;
        this.inBlocks = new ArrayList<>();
        this.outBlocks = new ArrayList<>();
    }


}
