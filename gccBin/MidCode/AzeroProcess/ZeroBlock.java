package gccBin.MidCode.AzeroProcess;

import gccBin.MIPS.SubOp;
import gccBin.MidCode.AfirstProcess.SetOp;
import gccBin.MidCode.Judge;
import gccBin.MidCode.Line.AssignLine;
import gccBin.MidCode.Line.Line;

import java.io.FileWriter;
import java.io.IOException;
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

    public void clear() {
        this.use_active.clear();
        this.def_active.clear();
        this.in_active.clear();
        this.out_active.clear();
    }

    //分析以得到活跃变量分析里的use集和def集
    public void parseLines_use_def_act() {
        for (Line line : lines) {
            String lineDef = line.getGen_zero();
            HashSet<String> lineUse = line.getUse_zero();
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

    //******************** get set 方法 *****************************//

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

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void usableExpAndCopyPropagation() {
        if (index == 14) {
            int a = 1;
        }

        ZeroBlockManager.getInstance().clearCopy();

        HashMap<String, AssignLine> A = new HashMap<>(); // name -> assignLine
        HashMap<String, String> B = new HashMap<>(); //right.first  -> name
        for (Line line : lines) {
            if (line instanceof AssignLine) {
                AssignLine assignLine = (AssignLine) line;
                if(assignLine.toString().equals("m = 0")){
                    int a = 0;
                }
                //a[vtn] = vtn 不需要进行公共子表达式删除，但是需要更新据此AB中的右值，
                if (assignLine.isArrayRefresh()) {
                    //  不需要进行公共子表达式删除
                    //  复写传播
                    assignLine.copyPropagation(ZeroBlockManager.getInstance().getName2copy());

                    String refreshArrName = SubOp.getArrName(assignLine.getAns());
                    String refreshArrSub = SubOp.getArrSubscript(assignLine.getAns());
                    //  更新复写传播 删 + 增
                    ZeroBlockManager.getInstance().removeCopy(assignLine.getAns());
                    ZeroBlockManager.getInstance().addCopy(assignLine.getAns(), assignLine.getRight());
                    // 更新公共子表达式 删 + 不用增
                    ArrayList<String> keys = new ArrayList<>(A.keySet());
                    for (String key : keys) {
                        AssignLine assignA = A.get(key);
                        if (assignA.shouldDelete(refreshArrName, refreshArrSub)) {
                            A.remove(key);
                            if (B.containsKey(assignA.getRight()) && B.get(assignA.getRight()).equals(assignA.getAns()))
                                B.remove(assignA.getRight());
                        }
                    }
                } else if (assignLine.needCommonExpAnalysis()) {
                    String lineAns = assignLine.getAns();
                    String lineRight = assignLine.getRight();

                    //公共子表达删除
                    if (B.containsKey(lineRight)) assignLine.commonExpSub(B.get(lineRight));
                    //复写传播
                    assignLine.copyPropagation(ZeroBlockManager.getInstance().getName2copy());

                    lineAns = assignLine.getAns();
                    lineRight = assignLine.getRight();

                    //更新复写传播 删 + 增
                    if (assignLine.isPureAssign()) {
                        ZeroBlockManager.getInstance().removeCopy(lineAns);
                        ZeroBlockManager.getInstance().addCopy(lineAns, lineRight);
                    }

                    //更新公共子表达式  删 + 增
                    ArrayList<String> keys = new ArrayList<>(A.keySet());
                    for (String key : keys) {
                        AssignLine assignA = A.get(key);
                        if (assignA.shouldDelete(lineAns)) {
                            A.remove(key);
                            if (B.containsKey(assignA.getRight())
                                    && B.get(assignA.getRight()).equals(assignA.getAns()))
                                B.remove(assignA.getRight());
                        }
                    }
                    A.put(lineAns, assignLine);
                    if (!B.containsKey(lineRight)) B.put(lineRight, lineAns);
                } else { //复写传播更新，右侧是数字
                    String lineAns = assignLine.getAns();
                    String lineRight = assignLine.getRight();
                    //更新复写传播 删 + 增
                    if (assignLine.isPureAssign()) {
                        ZeroBlockManager.getInstance().removeCopy(lineAns);
                        ZeroBlockManager.getInstance().addCopy(lineAns, lineRight);
                    }
                }
            }
        }
    }

    public void deleteUselessExp() {
        HashSet<String> active = new HashSet<>(this.out_active);
        if (lines.size() == 0) return;

        ListIterator<Line> iterator = lines.listIterator();
        Line line;

        while (iterator.hasNext()) iterator.next();

        while (iterator.hasPrevious()) {
            line = iterator.previous();
            if (line instanceof AssignLine) {
                AssignLine assignLine = (AssignLine) line;
                if (!active.contains(assignLine.getGen_zero())
                        && !Judge.isArrayValue(assignLine.getAns())) {
                    assignLine.deleteAllT();
                    iterator.remove();
                }
            }
            active.remove(line.getGen_zero());
            active.addAll(line.getUse_zero());
        }
    }

    public void printfZeroBlock(FileWriter fileWriter) throws IOException {
        fileWriter.write("\n\n\nBlock " + index);

        //in block message
        fileWriter.write("\nin blocks:");
        for (ZeroBlock zeroBlock : inBlocks) {
            fileWriter.write(zeroBlock.index + " ");
        }

        fileWriter.write("\nin active:");
        //in active
        fileWriter.write(in_active.toString());


        //lines
        fileWriter.write("\nblock Lines:");
        for (Line line : lines) {
            fileWriter.write("\n" + line.getMidCodeLine());
        }


        //out block message
        fileWriter.write("\nout blocks:");
        for (ZeroBlock zeroBlock : outBlocks) {
            fileWriter.write(zeroBlock.index + " ");
        }

        //out active
        fileWriter.write("\nout active:");
        //in active
        fileWriter.write(out_active.toString());
    }
}
