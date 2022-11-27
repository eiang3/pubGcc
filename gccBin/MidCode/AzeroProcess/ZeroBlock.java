package gccBin.MidCode.AzeroProcess;

import gccBin.MidCode.AfirstProcess.SetOp;
import gccBin.MidCode.Line.AssignLine;
import gccBin.MidCode.Line.Line;

import java.util.*;


public class ZeroBlock {
    private final ArrayList<Line> lines; //基本块的所有lines

    private final int index; //基本块的索引

    private final ArrayList<ZeroBlock> inBlocks;  //该基本块的前继基本块
    private final ArrayList<ZeroBlock> outBlocks; //该基本块的后继基本块
    private boolean gotoExit; //基本块是否要跳到结尾基本块

    //活跃变量分析
    private final HashSet<String> use_active;
    private final HashSet<String> def_active;
    private HashSet<String> in_active;
    private final HashSet<String> out_active;

    //基本块的划分
    //1 第一条语句
    //2 被跳转到的第一条语句
    //3 紧跟在跳转语句后的第一条语句
    private String label; //b label 结尾
    private String jump;
    private String func; // call func 结尾
    private String retFunc; // ret ...结尾

    public ZeroBlock(int index) {
        this.index = index;

        this.inBlocks = new ArrayList<>();
        this.outBlocks = new ArrayList<>();
        this.gotoExit = false;

        this.use_active = new HashSet<>();
        this.def_active = new HashSet<>();
        this.in_active = new HashSet<>();
        this.out_active = new HashSet<>();

        this.lines = new ArrayList<>();
    }

    /**
     * 分析以得到活跃变量分析里的use集和def集
     * +
     * 得到def变量的活跃范围
     * pre:line 应该是顺序输入的
     */
    public void parseLine_active() {
        for (Line line : lines) {
            String lineDef = line.getGen_tempVar();
            HashSet<String> lineUse = line.getUse_tempVar();
            //当一个var在line里既定义又使用，则先算使用的
            for (String var : lineUse) {
                if (!def_active.contains(var)) {
                    use_active.add(var);
                }
            }
            if (lineDef != null && !use_active.contains(lineDef)) {
                def_active.add(lineDef);
            }
        }
    }

    public void renewOut_active() {
        for (ZeroBlock block : outBlocks) {
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
     * block只需要添加out-block
     * 相应的out-block直接添加in-block
     *
     * @param block *
     */
    public void addOutBlock(ZeroBlock block) {
        this.outBlocks.add(block);
        block.addInBlock(this);
    }

    private void addInBlock(ZeroBlock block) {
        this.inBlocks.add(block);
    }

    //******************** get set 方法 *****************************//
    public void addLine(Line line) {
        this.lines.add(line);
    }

    public void setRetFunc(String retFunc) {
        if (retFunc.equals("main")) {
            this.gotoExit = true;
            return;
        }
        this.retFunc = retFunc;
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

    private HashSet<String> getIn_active() {
        return in_active;
    }


    public void usableExp() {
        HashMap<String, AssignLine> A = new HashMap<>();//右部对应
        for (Line line : lines) {
            if (line instanceof AssignLine) {
                AssignLine assignLine = (AssignLine) line;
                for (AssignLine o : A.values()) {
                    //不按顺序，所以可能不是最棒的
                    assignLine.judgeRightEqual_exchange(o);
                }
                A.remove(assignLine.getAns());
                A.put(assignLine.getAns(), assignLine);
            }
        }
    }

    public void deleteUselessExp() {
        HashSet<String> act = new HashSet<>(this.out_active);
        if (lines.size() == 0) return;

        ListIterator<Line> iterator = lines.listIterator();
        Line line = null;

        if (iterator.hasNext()) line = iterator.next();
        if (line == null) return;

        if (!act.contains(line.getGen())) iterator.remove();
        act.remove(line.getGen_tempVar());
        act.addAll(line.getUse_tempVar());

        if (iterator.hasPrevious()) {
            line = iterator.previous();
            if (!act.contains(line.getGen())) iterator.remove();
            act.remove(line.getGen_tempVar());
            act.addAll(line.getUse_tempVar());
        }
    }
}
