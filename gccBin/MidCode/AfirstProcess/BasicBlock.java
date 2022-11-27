package gccBin.MidCode.AfirstProcess;

import gccBin.MidCode.Line.AssignLine;
import gccBin.MidCode.Line.Line;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 基本块
 * ok
 */
public class BasicBlock {
    private final int index; //基本块的索引

    private final BitSet sum; //所有在这个基本块里的line

    private final ArrayList<BasicBlock> inBlocks; //流入这个基本块的基本块
    private final ArrayList<BasicBlock> outBlocks; //这个基本块可以跳到的基本块
    private boolean gotoExit;

    //到达定义分析
    private final HashMap<String, ArrayList<Integer>> varName2GenX;
    private final HashSet<String> genVarNames;

    private final BitSet gen_def;
    private final BitSet kill_def;
    private final BitSet in_def;
    private BitSet out_def;

    //活跃变量分析
    private final HashSet<String> use_active; //使用先于定义
    private final HashSet<String> def_active;
    private HashSet<String> in_active;
    private final HashSet<String> out_active;

    //  x = a + b
    private final HashMap<String, Integer> varName2FirstDef;//var第一次的定义点
    private final HashMap<String, Integer> varName2lastUse; //var最后一次使用的点
    private final HashMap<String, Integer> varName2Ptr; //var的指针
    //var的活跃范围【仅针对不在in集里的变量】
    private final HashMap<String, BitSet> varName2ActiveScope;

    // b label 结尾
    private String label;
    private String jump;

    private String func; // call func 结尾
    private String retFunc; // ret ...结尾

    public BasicBlock(int index) {
        this.index = index;

        varName2GenX = new HashMap<>();
        genVarNames = new HashSet<>();
        gen_def = new BitSet();
        kill_def = new BitSet();
        sum = new BitSet();
        in_def = new BitSet();
        out_def = new BitSet();

        use_active = new HashSet<>();
        def_active = new HashSet<>();
        in_active = new HashSet<>();
        out_active = new HashSet<>();

        varName2FirstDef = new HashMap<>();
        varName2lastUse = new HashMap<>();
        varName2ActiveScope = new HashMap<>();
        varName2Ptr = new HashMap<>();

        this.inBlocks = new ArrayList<>();
        this.outBlocks = new ArrayList<>();
        gotoExit = false;
    }

    /**
     * 分析以得到活跃变量分析里的use集和def集
     * +
     * 得到def变量的活跃范围
     * pre:line 应该是顺序输入的
     *
     * @param line *
     */
    public void parseLine_active(Line line) {
        if (line instanceof AssignLine) {
            int index = line.getIndex();
            String lineDef = line.getGen();
            HashSet<String> lineUse = line.getUse();

            //当一个var在line里既定义又使用，则先算使用的
            for (String var : lineUse) {
                if (!def_active.contains(var)) {
                    use_active.add(var);
                }
                varName2lastUse.put(var, index);
            }

            if (lineDef != null && !use_active.contains(lineDef)) {
                def_active.add(lineDef);
            }
            //debug
            if(lineDef != null && lineDef.equals("num") || lineUse.contains("num")){
                int lalalala;
            }
            //对于只存在于基本块内的活跃变量分析
            //一个定义：这个定义的最后一次使用
            if (lineDef != null && !lineUse.contains(lineDef)) {
                varName2Ptr.put(lineDef, index);
                varName2ActiveScope.put(lineDef, new BitSet());
                if (!varName2FirstDef.containsKey(lineDef)) {
                    varName2FirstDef.put(lineDef, index);
                }
            }

            for (String var : lineUse) {
                if (varName2Ptr.containsKey(var)) {
                    int pre = varName2Ptr.get(var);
                    BitSet extend = new BitSet();
                    extend.set(pre, index + 1);
                    varName2ActiveScope.get(var).or(extend);
                    varName2Ptr.put(var, index);
                }
            }
        }
    }

    public void renewOut_active() {
        for (BasicBlock block : outBlocks) {
            out_active.addAll(block.getIn_active());
        }
    }

    /**
     * true:表示集合更新了
     * false：表示集合没有更新
     *
     * @return *
     */
    public boolean renewIn_active() {
        HashSet<String> old = new HashSet<>(in_active);
        in_active = SetOp.streamSet(use_active, out_active, def_active);
        return !old.equals(in_active);
    }

    /**
     * 将不在in也不必再out里的  活跃点extend
     */
    public void extendVar_OnlyActiveInBlock() {
        for (String var : varName2ActiveScope.keySet()) {
            if (!in_active.contains(var) && !out_active.contains(var)) {
                VarWeb varWeb = VarNodeManager.getInstance().getOneVarWeb(var);
                varWeb.extendActiveScope(varName2ActiveScope.get(var));
            }
        }
    }

    /**
     * 得到一个变量从基本块开始,到基本块内最后一次使用的范围，
     *
     * @param name 变量名
     * @return *
     */
    public BitSet varActiveScope_onlyIn(String name) {
        if (varName2lastUse.containsKey(name)) {
            int last = varName2lastUse.get(name);
            int start = getStart();
            if (start < 0 || start > last + 1) return new BitSet();

            BitSet ret = new BitSet();
            ret.set(start, last + 1);
            return ret;
        }
        return new BitSet();
    }

    /**
     * 得到一个变量，从基本块内第一次定义，到基本块最后一次使用的范围
     *
     * @param name *
     * @return *
     */
    public BitSet varActiveScope_onlyOut(String name) {
        if (varName2FirstDef.containsKey(name)) {
            int first = varName2FirstDef.get(name);
            int last = getEnd();
            if (last < first || first < 0) return new BitSet();
            BitSet ret = new BitSet();
            ret.set(first, last);
            return ret;
        }
        return new BitSet();
    }


    /**
     * 以下是到达定义分析
     */
    public void parseLine_def(Line line) {
        if (line != null && line.getGen() != null) {
            int index = line.getIndex();
            String name = line.getGen();
            gen_def.set(index);
            this.genVarNames.add(name);
            if (varName2GenX.containsKey(name)) {
                varName2GenX.get(name).add(index);
            } else {
                ArrayList<Integer> arr = new ArrayList<>();
                arr.add(index);
                varName2GenX.put(name, arr);
            }
        }
        if (line != null) sum.set(line.getIndex());
    }


    public void renewIn_def() {
        for (BasicBlock block : inBlocks) {
            in_def.or(block.getOut_def());
        }
    }

    /**
     * true 说明out集改变了
     * false 说明没有改变
     *
     * @return *
     */
    public boolean renewOut_def() {
        BitSet preOut = (BitSet) out_def.clone();
        out_def = SetOp.streamSet(gen_def, in_def, kill_def);
        return !out_def.equals(preOut);
    }

    /**
     * 完成kill集的构建
     */
    public void finishKill_def() {
        for (String var : genVarNames) {
            BitSet varGen = VarNodeManager.getInstance().getOneVarGen(var);
            varGen = SetOp.differenceSet(varGen,gen_def);
            kill_def.or(varGen); //加入kill集
        }
    }

    /**
     * 如果一个var在in集中，这里得到这个var可能的use集（到下一个定义）(包括)
     *
     * @param name *
     * @return *
     */
    public BitSet getUseFromStart(String name) {
        if (varName2GenX.containsKey(name)) {
            int firstDef = varName2GenX.get(name).get(0);
            BitSet ret = (BitSet) sum.clone();
            int length = getEnd();
            if(firstDef + 1 > length || firstDef + 1 < 0 || length < 0) {
                return new BitSet();
            }
            ret.clear(firstDef + 1, length);
            return ret;
        } else {
            return (BitSet) sum.clone();
        }
    }

    /**
     * 如果一个var在gen集中，找到这个def点（不包括）到
     * 下一个def点（包括）|结尾的位置集
     *
     *
     * @param var *
      * @param index *
     * @return *
     */
    public BitSet getUseFromMid(String var, int index) {
        if(varName2GenX.containsKey(var)) {
            BitSet ret = (BitSet) sum.clone();
            int start = ret.nextSetBit(0);
            int end = ret.length();
            if (start < 0 || start > index + 1) return new BitSet();
            ret.clear(start, index + 1); //不包括

            ArrayList<Integer> arr = varName2GenX.get(var);
            int i = arr.indexOf(index);
            if (i == arr.size() - 1) {  //var在这个块里的最后一个定义点
                return ret;
            }
            int next = arr.get(i + 1);
            ret.clear(next + 1, end); //包括
            return ret;
        }
        return new BitSet();
    }

    /**
     * in_active ∪ out_active
     *
     * @return *
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
     * @return *
     */
    public HashSet<String> inMinusOut() {
        HashSet<String> ret = new HashSet<>(in_active);
        ret.removeAll(out_active);
        return ret;
    }

    /**
     * out_active - in_active
     *
     * @return *
     */
    public HashSet<String> outMinusIn() {
        HashSet<String> ret = new HashSet<>(out_active);
        ret.removeAll(in_active);
        return ret;
    }

    /**
     * block只需要添加out-block
     * 相应的out-block直接添加in-block
     * @param block *
     */
    public void addOutBlock(BasicBlock block) {
        this.outBlocks.add(block);
        block.addInBlock(this);
    }

    public void addInBlock(BasicBlock block) {
        this.inBlocks.add(block);
    }

    //******************** get set 方法 *****************************//
    public void setRetFunc(String retFunc) {
        if (retFunc.equals("main")) {
            this.gotoExit = true;
            return;
        }
        this.retFunc = retFunc;
    }


    public int getStart() {
        return sum.nextSetBit(0);
    }

    public int getEnd() {
        return sum.length();
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
    public BitSet getIn_def() {
        return (BitSet) in_def.clone();
    }

    public BitSet getOut_def() {
        return (BitSet) out_def.clone();
    }

    public BitSet getGen_def() {
        return (BitSet) gen_def.clone();
    }

    private HashSet<String> getIn_active() {
        return in_active;
    }

    //debug
    public void printfInBlock(){
        System.out.print("in-block:{");
        for(BasicBlock block:inBlocks){
            System.out.print(block.getIndex()+",");
        }
        System.out.println("}");
    }

    public void printfOutBlock(){
        System.out.print("out-block:{");
        for(BasicBlock block:outBlocks){
            System.out.print(block.getIndex()+",");
        }
        System.out.println("}");
    }

    public BitSet getKill_def() {
        return (BitSet) kill_def.clone();
    }
}
