package gccBin.MidCode.firstProcess;

import gccBin.MIPS.tool.MemManager;
import gccBin.MidCode.Line.Line;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

public class BasicBlock {
    private final int index; //基本块的索引

    private final BitSet sum; //所有在这个基本块里的line

    private final ArrayList<BasicBlock> inBlocks;
    private final ArrayList<BasicBlock> outBlocks;
    private boolean gotoExit;

    /**
     * 以下是到达定义数据流分析
     * basic 保存了每个var的定义点集。
     * 前两个是到达定义分析的辅助数组
     */
    private final HashMap<String, ArrayList<Integer>> varName2DefX;
    private final HashSet<String> genVarNames;

    private final BitSet gen;
    private final BitSet kill;
    private final BitSet in_def; //def点
    private BitSet out_def;

    /**
     * 以下是活跃变量分析
     * 下半部分是辅助分析变量
     */
    private HashSet<String> use; //使用先于定义
    private HashSet<String> def;
    private HashSet<String> in_active;
    private HashSet<String> out_active;

    private final HashMap<String, Integer> varName2lastUse;
    private final HashMap<String, Integer> varName2Ptr;
    private final HashMap<String, BitSet> varName2ActiveScope;
    /**
     * 一下是留给基本块连接操作使用
     */
    private String jump;
    private String label;
    private String func;
    private String retFunc;

    public BasicBlock(int index) {
        this.index = index;
        varName2DefX = new HashMap<>();
        genVarNames = new HashSet<>();
        gen = new BitSet();
        kill = new BitSet();
        sum = new BitSet();
        in_def = new BitSet();
        out_def = new BitSet();

        use = new HashSet<>();
        def = new HashSet<>();
        in_active = new HashSet<>();
        out_active = new HashSet<>();

        varName2lastUse = new HashMap<>();
        varName2ActiveScope = new HashMap<>();
        varName2Ptr = new HashMap<>();

        this.inBlocks = new ArrayList<>();
        this.outBlocks = new ArrayList<>();
        gotoExit = false;
    }

    /**
     * 分析以得到活跃变量分析里的use集和def集
     * pre:line 应该是顺序输入的
     *
     * @param line
     */
    public void parseLine_active(Line line) {
        if (line != null) {
            int index = line.getIndex();
            String d = line.getGen();
            HashSet<String> u = line.getUse();

            for (String var : u) {
                if (!def.contains(var)) {
                    use.add(var);
                }
                varName2lastUse.put(var, index);
            }

            if (use.contains(d)) {
                def.add(d);
            }
            /**
             *
             */
            if (d != null && !u.contains(d)){
                varName2Ptr.put(d,index);
                varName2ActiveScope.put(d,new BitSet());
            }

            for (String var : u) {
                if(varName2Ptr.containsKey(var)) {
                    int pre = varName2Ptr.get(var);
                    BitSet extend = new BitSet();
                    extend.set(pre,index+1);
                    varName2ActiveScope.get(var).or(extend);
                    varName2Ptr.put(var,index);
                }
            }
        }
    }

    /**
     * 将只在基本块内活跃的变量的 活跃点extend
     */
    public void extendVarOnlyActiveInBlock(){
        for(String var:varName2ActiveScope.keySet()){
            if(!in_active.contains(var)){
                VarNode varNode = VarNodeManager.getInstance().getOneVar(var);
                varNode.extendActiveScope(varName2ActiveScope.get(var));
            }
        }
    }

    public void renewOut_active() {
        for (BasicBlock block : outBlocks) {
            out_active.addAll(block.getIn_active());
        }
    }

    /**
     * 得到一个变量从基本块开始到基本块内最后一次使用的范围，
     *
     * @param name 变量名
     * @return
     */
    public BitSet activeScopeFormStartToLastUse(String name) {
        int last = varName2lastUse.get(name);
        int start = getStart();
        if (start < 0) return new BitSet();

        BitSet ret = new BitSet();
        ret.set(start, last + 1);
        return ret;
    }

    /**
     * true:表示集合更新了
     * false：表示集合没有更新
     *
     * @return
     */
    public boolean renewIn_active() {
        HashSet<String> old = new HashSet<>(in_active);
        in_active = SetOp.streamSet(use, out_active, def);
        return !old.equals(in_active);
    }

    /**
     * 以下是到达定义分析
     */
    public void renewIn_def() {
        for (BasicBlock block : inBlocks) {
            in_def.or(block.getOut_def());
        }
    }

    /**
     * true 说明out集改变了
     * false 说明没有改变
     *
     * @return
     */
    public boolean renewOut_def() {
        BitSet preOut = (BitSet) out_def.clone();
        out_def = SetOp.streamSet(gen, in_def, kill);
        return !out_def.equals(preOut);
    }

    public void finishKill_def() {
        for (String var : genVarNames) {
            BitSet varGen = VarNodeManager.getInstance().getOneVarGen(var);
            varGen.and(gen); //var在此基本块的定义点
            varGen.xor(gen); //var在此基本块的kill点
            kill.or(varGen); //加入kill集
        }
    }

    public void parseLine_def(Line line) {
        if (line != null && line.getGen() != null) {
            int index = line.getIndex();
            String name = line.getGen();
            gen.set(index);
            this.genVarNames.add(name);
            if (varName2DefX.containsKey(name)) {
                varName2DefX.get(name).add(index);
            } else {
                ArrayList<Integer> arr = new ArrayList<>();
                arr.add(index);
                varName2DefX.put(name, arr);
            }
        }
        if (line != null) sum.set(line.getIndex());
    }

    /**
     * 如果一个var在in集中，这里得到这个var可能的use集（到下一个定义）(包括)
     *
     * @param name
     * @return
     */
    public BitSet getUseFromStart(String name) {
        if (varName2DefX.containsKey(name)) {
            int firstDef = varName2DefX.get(name).get(0);
            BitSet ret = (BitSet) sum.clone();
            int length = ret.length();
            ret.clear(firstDef + 1, length);
            //firstDef是从0开始定义的，所以其必定<length
            return ret;
        } else {
            return (BitSet) sum.clone();
        }
    }

    /**
     * 如果一个var在gen集中，找到这个def点（不包括）到下一个def点（包括）的位置集
     */
    public BitSet getUseFromMid(String var, int index) {
        BitSet ret = (BitSet) sum.clone();
        int start = ret.nextSetBit(0);
        int end = ret.length();

        ret.clear(start, index + 1); //不包括
        ArrayList<Integer> arr = varName2DefX.get(var);
        int i = arr.indexOf(index);
        if (i == arr.size() - 1) {  //var在这个块里的最后一个定义点
            return ret;
        }
        int next = arr.get(i + 1);
        ret.clear(next + 1, end); //包括
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

    public BitSet getIn_def() {
        return (BitSet) in_def.clone();
    }

    public BitSet getOut_def() {
        return (BitSet) out_def.clone();
    }

    public BitSet getGen() {
        return (BitSet) gen.clone();
    }

    private HashSet<String> getIn_active() {
        return in_active;
    }

    public HashSet<String> getOut_active() {
        return out_active;
    }

    public boolean inActiveContains(String a) {
        return in_active.contains(a);
    }

    public boolean outActiveContains(String a) {
        return out_active.contains(a);
    }

    /**
     * in_active ∪ out_active
     *
     * @return
     */
    public HashSet<String> inOverlapOut() {
        HashSet<String> ret = new HashSet<>(in_active);
        HashSet<String> sub = new HashSet<>(in_active);
        sub.removeAll(out_active);
        ret.removeAll(sub);
        return ret;
    }

    /**
     * in_active - out_active
     *
     * @return
     */
    public HashSet<String> inMinusOut() {
        HashSet<String> ret = new HashSet<>(in_active);
        ret.removeAll(out_active);
        return ret;
    }

    public int getStart() {
        return sum.nextSetBit(0);
    }

    public int getEnd() {
        return sum.length();
    }
}
