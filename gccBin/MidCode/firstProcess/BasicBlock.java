package gccBin.MidCode.firstProcess;

import gccBin.MidCode.Line.Line;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

public class BasicBlock {
    private final int index;

    /**
     * 以下是到达定义数据流分析
     * basic 保存了每个var的定义点集。
     */
    private final HashMap<String, ArrayList<Integer>> varName2DefX;

    private final HashSet<String> genVarNames;
    private final BitSet genSum;
    private final BitSet killSum;
    private final BitSet sum;
    private final BitSet in; //def点
    private final BitSet out;

    private ArrayList<BasicBlock> inBlocks;
    private ArrayList<BasicBlock> outBlocks;
    private boolean gotoExit;

    private String jump;
    private String label;
    private String func;

    private String retFunc;

    public BasicBlock(int index) {
        this.index = index;
        varName2DefX = new HashMap<>();
        genVarNames = new HashSet<>();
        genSum = new BitSet();
        killSum = new BitSet();
        sum = new BitSet();
        in = new BitSet();
        out = new BitSet();

        this.inBlocks = new ArrayList<>();
        this.outBlocks = new ArrayList<>();
        gotoExit = false;
    }

    public void renewIn() {
        for (BasicBlock block : inBlocks) {
            in.or(block.getOut());
        }
    }

    /**
     * true 说明out集改变了
     * false 说明没有改变
     *
     * @return
     */
    public boolean renewOut() {
        BitSet inMinusKill = (BitSet) killSum.clone();
        inMinusKill.and(in);
        inMinusKill.xor(in);
        BitSet preOut = (BitSet) out.clone();
        out.or(genSum);
        out.or(inMinusKill);
        return out.equals(preOut);
    }

    public void finishKill() {
        for (String var : genVarNames) {
            BitSet varGen = VarNodeManager.getInstance().getOneVarGen(var);
            varGen.and(genSum); //var在此基本块的定义点
            varGen.xor(genSum); //var在此基本块的kill点
            killSum.or(varGen); //加入kill集
        }
    }

    public void parseLine(Line line) {
        if (line.getGen() != null) {
            int index = line.getIndex();
            String name = line.getGen();
            genSum.set(index);
            this.genVarNames.add(name);
            if(varName2DefX.containsKey(name)){
                varName2DefX.get(name).add(index);
            } else {
                ArrayList<Integer> arr = new ArrayList<>();
                arr.add(index);
                varName2DefX.put(name,arr);
            }
        }
        sum.set(line.getIndex());
    }

    /**
     * 如果一个var在in集中，这里得到这个var可能的use集（到下一个定义）(包括)
     * @param name
     * @return
     */
    public BitSet getUseFromStart(String name) {
        if (varName2DefX.containsKey(name)) {
            int firstDef = varName2DefX.get(name).get(0);
            BitSet ret = (BitSet) sum.clone();
            int length = ret.length();
            ret.clear(firstDef+1,length);
            //firstDef是从0开始定义的，所以其必定<length
            return ret;
        } else {
            return (BitSet) sum.clone();
        }
    }

    /**
     *如果一个var在gen集中，找到这个def点（不包括）到下一个def点（包括）的位置集
     */
    public BitSet getUseFromMid(String var,int index){
        BitSet ret = (BitSet) sum.clone();
        int start = ret.nextSetBit(0);
        int end = ret.length();

        ret.clear(start,index+1); //不包括
        ArrayList<Integer> arr = varName2DefX.get(var);
        int i = arr.indexOf(index);
        if(i == arr.size()-1){  //var在这个块里的最后一个定义点
            return ret;
        }
        int next = arr.get(i+1);
        ret.clear(next+1,end); //包括
        return ret;
    }

    public void addInBlock(BasicBlock block) {
        this.inBlocks.add(block);
    }

    public void addOutBlock(BasicBlock block) {
        this.outBlocks.add(block);
        block.addInBlock(this);
    }

    public BitSet getSum() {
        return (BitSet) sum.clone();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setFunc(String func) {
        this.func = func;
    }

    public boolean isGotoExit() {
        return gotoExit;
    }

    public void setJump(String jump) {
        this.jump = jump;
    }

    public int getIndex() {
        return index;
    }

    public String getJump() {
        return jump;
    }

    public String getLabel() {
        return label;
    }

    public String getFunc() {
        return func;
    }

    public String getRetFunc() {
        return retFunc;
    }

    public void setRetFunc(String retFunc) {
        if (retFunc.equals("main")) {
            this.gotoExit = true;
            return;
        }
        this.retFunc = retFunc;
    }

    public BitSet getIn() {
        return (BitSet) in.clone();
    }

    public BitSet getOut() {
        return (BitSet) out.clone();
    }

    public BitSet getGenSum() {
        return (BitSet) genSum.clone();
    }
}
