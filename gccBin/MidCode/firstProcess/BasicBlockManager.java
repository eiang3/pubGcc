package gccBin.MidCode.firstProcess;

import gccBin.MidCode.Line.*;
import gccBin.MidCode.LineManager;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

public class BasicBlockManager {
    private static BasicBlockManager basicBlockManager;

    private final HashMap<Integer, BasicBlock> basicBlocks; //后续可能会删基本块

    private BasicBlock bExit;
    private int basicBlockNum;


    private final HashMap<String, BasicBlock> label2Block; // 唯一
    private final HashMap<String, BasicBlock> funcName2Block; //唯一
    //可能不唯一
    private final HashMap<String, ArrayList<Integer>> call2nextBlockId; // call是block的最后一句

    private String funcName;//
    private BasicBlock nowBlock;

    private BasicBlockManager() {
        basicBlocks = new HashMap<>();
        basicBlockNum = 0;
        label2Block = new HashMap<>();
        funcName2Block = new HashMap<>();
        call2nextBlockId = new HashMap<>();
    }

    public static BasicBlockManager getInstance() {
        if (basicBlockManager == null) {
            basicBlockManager = new BasicBlockManager();
        }
        return basicBlockManager;
    }

    /**
     * 节点的活跃范围分析，
     * 因为依托block展开，所以该方法在block里
     *
     * var in:
     * in + out,block的sum
     * in: block 从开始到最后一次使用
     * none: 在block里面进行活跃度分析
     */
    public void varNodeActiveScopeAnalysis() {
        for (BasicBlock block : basicBlocks.values()) {
            HashSet<String> inOverlapOut = block.inOverlapOut();
            for (String var : inOverlapOut) {
                VarWeb varWeb = VarNodeManager.getInstance().getOneVarWeb(var);
                varWeb.extendActiveScope(block.getSum());
            }

            HashSet<String> inMinusOut = block.inMinusOut();
            for (String var : inMinusOut) {
                VarWeb varWeb = VarNodeManager.getInstance().getOneVarWeb(var);
                varWeb.extendActiveScope(block.activeScopeFormStartToLastUse(var));
            }

            block.extendVarOnlyActiveInBlock();
        }
    }

    /**
     * pre：在变量的定义使用链和网分析结束后
     * 基本块的活跃变量分析，从后往前
     */
    public void blockActiveVarAnalysis() {
        for (BasicBlock block : basicBlocks.values()) {
            int s = block.getStart();
            int e = block.getEnd();
            for (int i = s; i >= 0 && i < e; i++) {
                Line line = LineManager.getInstance().getLine(i);
                block.parseLine_active(line);
            }
        }

        boolean myContinue = true;
        while (myContinue) {
            myContinue = false;
            for (int i = basicBlockNum - 1; i >= 0; i--) {
                BasicBlock block = basicBlocks.get(i);
                block.renewOut_active();
                if (block.renewIn_active()) myContinue = true;
            }
        }
    }

    public void computeInAndOut() {
        boolean myContinue = true;
        while (myContinue) {
            myContinue = false;
            for (int i = 0; i < basicBlockNum; i++) {
                BasicBlock block = basicBlocks.get(i);
                block.renewIn_def();
                boolean change = block.renewOut_def();
                if (change) myContinue = true;
            }
        }
    }


    public void createBlockAndInitGneOp(Line line) {
        if (line == null || line.getLineLength() == 0) return;
        if (line instanceof LabelLine) { //以label开始的基本块
            nowBlock = new BasicBlock(basicBlockNum);
            basicBlocks.put(basicBlockNum, nowBlock);
            basicBlockNum++;
            nowBlock.parseLine_def(line);

            LabelLine labelLine = (LabelLine) line;
            label2Block.put(labelLine.getLabel(), nowBlock);
        } else if (line instanceof FuncDefLine) { //以int|void func ()开始的基本块
            nowBlock = new BasicBlock(basicBlockNum);
            basicBlocks.put(basicBlockNum, nowBlock);
            nowBlock.parseLine_def(line);
            basicBlockNum++;

            funcName = ((FuncDefLine) line).getName();
            funcName2Block.put(funcName, nowBlock);
        }
        if (nowBlock == null) { //刚开始的语句|跳转语句下一句
            nowBlock = new BasicBlock(basicBlockNum);
            basicBlocks.put(basicBlockNum, nowBlock);
            basicBlockNum++;
            nowBlock.parseLine_def(line);
        } else if (line instanceof BLine) { //跳转函数
            BLine bLine = (BLine) line;
            nowBlock.parseLine_def(line);

            nowBlock.setLabel(bLine.getLabel());
            nowBlock.setJump(bLine.getB());
            nowBlock = null;

        } else if (line instanceof CallFuncLine) {  //跳转到函数定义call
            CallFuncLine callFuncLine = (CallFuncLine) line;
            nowBlock.parseLine_def(line);

            nowBlock.setFunc(callFuncLine.getFuncName());
            if (call2nextBlockId.containsKey(callFuncLine.getFuncName())) {
                call2nextBlockId.get(callFuncLine.getFuncName()).add(nowBlock.getIndex() + 1);
            } else {
                ArrayList<Integer> arr = new ArrayList<>();
                arr.add(nowBlock.getIndex() + 1);
                call2nextBlockId.put(callFuncLine.getFuncName(), arr);
            }
            nowBlock = null;

        } else if (line instanceof RetLine) { //返回到call语句的下一个block
            nowBlock.parseLine_def(line);
            nowBlock.setRetFunc(funcName);
            ((RetLine) line).setFuncName(funcName);
            nowBlock = null;
        } else {
            nowBlock.parseLine_def(line);
        }
    }

    public void connectAllAndInitKill() {
        for (int i = 0; i < basicBlocks.size() - 1; i++) {
            BasicBlock b = basicBlocks.get(i);
            connectBlockAndInitKill(b);
        }
    }

    private void connectBlockAndInitKill(BasicBlock block) {
        block.finishKill_def();
        if (block.isGotoExit()) {
            block.addOutBlock(bExit);
        } else if (block.getLabel() != null) { // b** label
            block.addOutBlock(label2Block.get(block.getLabel()));
            if (!block.getJump().equals("b")) {
                block.addOutBlock(basicBlocks.get(block.getIndex() + 1));
            }
        } else if (block.getFunc() != null) { //call func
            block.addOutBlock(funcName2Block.get(block.getFunc()));
        } else if (block.getRetFunc() != null) { //ret (exp)
            for (Integer integer : call2nextBlockId.get(block.getRetFunc())) {
                block.addOutBlock(basicBlocks.get(integer));
            }
        } else {
            block.addOutBlock(basicBlocks.get(block.getIndex() + 1));
        }
    }

    public void setBExit() {
        this.bExit = new BasicBlock(basicBlockNum);
        basicBlocks.put(basicBlockNum, bExit);
        basicBlockNum++;
    }

    /**
     * 获得所有变量的定义——使用链
     */
    public void initAllDefUseChain() {
        for (int i = 0; i < basicBlockNum; i++) {
            BasicBlock block = basicBlocks.get(i);
            if (block.getSum().length() == 0) continue; //空的基本块

            BitSet in = block.getIn_def(); //首先判断in集
            /*if(i == 9){
                int a = 2;
            }*/
            //  bug
            for (int j = block.getIn_def().nextSetBit(0); j < in.length() && j >= 0; j++) {
                if (in.get(j)) { //如果这一位是1说明该位有定义
                    Line line = LineManager.getInstance().getLine(j);
                    VarNode varNode = VarNodeManager.getInstance().getOneVar(line.getGen());
                    BitSet mayUse = block.getUseFromStart(varNode.getName());
                    varNode.renewUseDefChain(j, mayUse);
                }
            }

            //再判断gen集。
            BitSet genSum = block.getGen_def();
            for (int j = genSum.nextSetBit(0); j < genSum.length() && j >= 0; j++) {
                if (genSum.get(j)) {
                    Line line = LineManager.getInstance().getLine(j);
                    VarNode varNode = VarNodeManager.getInstance().getOneVar(line.getGen());
                    BitSet mayUse = block.getUseFromMid(varNode.getName(), j);
                    varNode.renewUseDefChain(j, mayUse);
                }
            }
        }
    }

    public void printfBlockMessage(){
        for(int key:basicBlocks.keySet()){
            BasicBlock block = basicBlocks.get(key);
            System.out.println("index "+block.getIndex());
            System.out.println(block.getSum());
            System.out.println("in :"+block.getIn_def());
            System.out.println("gen "+block.getGen_def());
            System.out.println();
        }
    }
}
