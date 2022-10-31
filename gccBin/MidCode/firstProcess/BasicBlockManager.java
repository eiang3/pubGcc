package gccBin.MidCode.firstProcess;

import gccBin.MidCode.Line.*;
import gccBin.MidCode.LineManager;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

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

    public static BasicBlockManager getInstance(){
        if(basicBlockManager == null){
            basicBlockManager = new BasicBlockManager();
        }
        return basicBlockManager;
    }

    public void computeInAndOut(){
        boolean myContinue = true;
        while(myContinue) {
            myContinue = false;
            for (int i = 0; i < basicBlockNum; i++) {
                BasicBlock block = basicBlocks.get(i);
                block.renewIn();
                boolean change = block.renewOut();
                if(change) myContinue = true;
            }
        }
    }


    public void createBlockAndInitGneOp(Line line) {
        if(nowBlock!=null){
            nowBlock.parseLine(line);
        }
        if (nowBlock == null) { //刚开始的语句|跳转语句下一句
            nowBlock = new BasicBlock(basicBlockNum);
            basicBlocks.put(basicBlockNum, nowBlock);
            basicBlockNum++;
            nowBlock.parseLine(line);
        } else if (line instanceof LabelLine) { //以label开始的基本块
            nowBlock = new BasicBlock(basicBlockNum);
            basicBlocks.put(basicBlockNum, nowBlock);
            basicBlockNum++;

            LabelLine labelLine = (LabelLine) line;
            label2Block.put(labelLine.getLabel(), nowBlock);
        } else if (line instanceof FuncDefLine) { //以int|void func ()开始的基本块
            nowBlock = new BasicBlock(basicBlockNum);
            basicBlocks.put(basicBlockNum, nowBlock);
            basicBlockNum++;
            funcName = ((FuncDefLine) line).getName();
            funcName2Block.put(funcName, nowBlock);
        } else if (line instanceof BLine) { //跳转函数
            BLine bLine = (BLine) line;

            nowBlock.setLabel(bLine.getLabel());
            nowBlock.setJump(bLine.getB());
            nowBlock = null;

            basicBlockNum++;
        } else if (line instanceof CallFuncLine) {  //跳转到函数定义call
            CallFuncLine callFuncLine = (CallFuncLine) line;
            nowBlock.setFunc(callFuncLine.getFuncName());

            if (call2nextBlockId.containsKey(callFuncLine.getFuncName())) {
                call2nextBlockId.get(callFuncLine.getFuncName()).add(nowBlock.getIndex() + 1);
            } else {
                ArrayList<Integer> arr = new ArrayList<>();
                arr.add(nowBlock.getIndex() + 1);
                call2nextBlockId.put(callFuncLine.getFuncName(), arr);
            }
            nowBlock = null;
            basicBlockNum++;
        } else if (line instanceof RetLine) { //返回到call语句的下一个block
            nowBlock.setRetFunc(funcName);
            ((RetLine) line).setFuncName(funcName);
            nowBlock = null;
            basicBlockNum++;
        }
    }

    public void connectAllAndInitKill(){
        for (int i = 0; i < basicBlocks.size() - 1; i++) {
            BasicBlock b = basicBlocks.get(i);
            connectBlockAndInitKill(b);
        }
    }

    private void connectBlockAndInitKill(BasicBlock block) {
        block.finishKill();
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
    public void initAllDefUseChain(){
        for(int i = 0;i < basicBlockNum;i++){
            BasicBlock block = basicBlocks.get(i);
            if(block.getSum().length() == 0) continue; //空的基本块

            BitSet in = block.getIn(); //首先判断in集
            for(int j = block.getSum().nextSetBit(0); j < in.length();j++){
                if(in.get(j)){ //如果这一位是1说明该位有定义
                    Line line = LineManager.getInstance().getLine(j);
                    VarNode varNode = VarNodeManager.getInstance().getOneVar(line.getGen());
                    BitSet mayUse = block.getUseFromStart(varNode.getName());
                    varNode.renewUseDefChain(j,mayUse);
                }
            }
            //再判断gen集。
            BitSet genSum = block.getGenSum();
            for(int j = genSum.nextSetBit(0);j<genSum.length();j++){
                if(genSum.get(j)){
                    Line line = LineManager.getInstance().getLine(j);
                    VarNode varNode = VarNodeManager.getInstance().getOneVar(line.getGen());
                    BitSet mayUse = block.getUseFromMid(varNode.getName(),j);
                    varNode.renewUseDefChain(j,mayUse);
                }
            }
        }
    }
}
