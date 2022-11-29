package gccBin.MidCode.AzeroProcess;

import gccBin.MidCode.Line.*;
import gccBin.MidCode.LineManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ZeroBlockManager {
    private static ZeroBlockManager zeroBlockManager;

    //以下是基本块构建相关的变量
    private final HashMap<Integer, ZeroBlock> basicBlocks; //后续可能会删基本块
    private ZeroBlock bExit; //basicBlocks的最后一个
    private int basicBlockNum; //basicBlocks的数量
    private final HashMap<String, ZeroBlock> label2Block; // 唯一
    private final HashMap<String, ZeroBlock> funcName2Block; //唯一
    private final HashMap<String, ArrayList<Integer>> call2nextBlockId; //可能不唯一，call是block的最后一句
    private String funcName; //实时函数名字
    private ZeroBlock nowBlock; //实时基本块

    private ZeroBlockManager() {
        basicBlocks = new HashMap<>();
        basicBlockNum = 0;
        label2Block = new HashMap<>();
        funcName2Block = new HashMap<>();
        call2nextBlockId = new HashMap<>();
    }

    public static ZeroBlockManager getInstance() {
        if (zeroBlockManager == null) {
            zeroBlockManager = new ZeroBlockManager();
        }
        return zeroBlockManager;
    }

    //一个经过测试的基本块连接模块
    public void build_Block(Line line) {
        if (line == null || line.getLineLength() == 0) return;
        if (line instanceof LabelLine) { //以label开始的基本块
            nowBlock = new ZeroBlock(basicBlockNum);
            nowBlock.addLine(line);
            basicBlocks.put(basicBlockNum++, nowBlock);
            label2Block.put(((LabelLine) line).getLabel(), nowBlock);
            return;
        } else if (line instanceof FuncDefLine) { //以int|void func ()开始的基本块
            nowBlock = new ZeroBlock(basicBlockNum);
            nowBlock.addLine(line);
            basicBlocks.put(basicBlockNum++, nowBlock);
            funcName = ((FuncDefLine) line).getName();
            funcName2Block.put(funcName, nowBlock);
            return;
        }

        if (nowBlock == null) { //刚开始的语句|跳转语句下一句
            nowBlock = new ZeroBlock(basicBlockNum);
            basicBlocks.put(basicBlockNum++, nowBlock);
        }

        nowBlock.addLine(line);
        if (line instanceof BLine) { //跳转函数
            BLine bLine = (BLine) line;
            nowBlock.setLabel(bLine.getLabel());
            nowBlock.setJump(bLine.getB());
            nowBlock = null;
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
        } else if (line instanceof RetLine) { //返回到call语句的下一个block
            nowBlock.setRetFunc(funcName);
            ((RetLine) line).setFuncName(funcName);
            nowBlock = null;
        }
    }

    public void setBExit() { // 设置结束基本块
        this.bExit = new ZeroBlock(basicBlockNum);
        basicBlocks.put(basicBlockNum++, bExit);
    }

    /**
     * 将所有基本块连接，并初始化kill集
     * 不包括exit块，因为其没有out-block
     */
    public void connectAll() {
        for (int i = 0; i < basicBlocks.size() - 1; i++) {
            ZeroBlock b = basicBlocks.get(i);
            connectBlock(b);
        }
    }

    private void connectBlock(ZeroBlock block) {
        if (block.isGotoExit()) {
            block.addOutBlock(bExit);
        } else if (block.getLabel() != null) { // b** label
            if (label2Block.containsKey(block.getLabel())) {
                block.addOutBlock(label2Block.get(block.getLabel()));
                if (!block.getJump().equals("b")) {
                    block.addOutBlock(basicBlocks.get(block.getIndex() + 1));
                }
            }
        } else if (block.getFunc() != null) { //call func
            if (funcName2Block.containsKey(block.getFunc())) {
                block.addOutBlock(funcName2Block.get(block.getFunc()));
            }
        } else if (block.getRetFunc() != null) { //ret (exp)
            if (call2nextBlockId.containsKey(block.getRetFunc())) {
                for (Integer integer : call2nextBlockId.get(block.getRetFunc())) {
                    block.addOutBlock(basicBlocks.get(integer));
                }
            }
        } else {
            block.addOutBlock(basicBlocks.get(block.getIndex() + 1));
        }
    }


    public void ActiveVarAnalysis() { //基本块进行活跃变量分析
        for (ZeroBlock block : basicBlocks.values()) {
            block.clear();
            block.parseLines_use_def_act();
        }
        boolean myContinue = true;
        while (myContinue) {
            myContinue = false;
            for (int i = basicBlockNum - 1; i >= 0; i--) {
                ZeroBlock block = basicBlocks.get(i);
                block.renewOut_active();
                if (block.renewIn_active()) myContinue = true;
            }
        }
    }

    public void usableExpAndCopyPropagation() {
        for (int index : basicBlocks.keySet()) {
            ZeroBlock zeroBlock = basicBlocks.get(index);
            zeroBlock.usableExpAndCopyPropagation();
        }
    }

    public void deleteUselessExp() {
        for (int index : basicBlocks.keySet()) {
            ZeroBlock zeroBlock = basicBlocks.get(index);
            zeroBlock.deleteUselessExp();
        }
    }

    public void addLineToLIneManager() {
        for (int index : basicBlocks.keySet()) {
            ZeroBlock zeroBlock = basicBlocks.get(index);
            LineManager.getInstance().addLineArray(zeroBlock.getLines());
        }
    }

    public void printfBlockAndActive(String times) throws IOException {
        FileWriter fileWriter = new FileWriter("zeroblock" + times + ".txt");
        for (int index : basicBlocks.keySet()) {
            ZeroBlock zeroBlock = basicBlocks.get(index);
            zeroBlock.printfZeroBlock(fileWriter);
        }
        fileWriter.close();
    }


    //复写传播部分
    private final HashMap<String, String> name2copy = new HashMap<>();//

    public HashMap<String, String> getName2copy() {
        return name2copy;
    }

    public void addCopy(String ans, String copy) {
        this.name2copy.put(ans, copy);
    }

    public void removeCopy(String ans) {
        this.name2copy.remove(ans);
    }

    public void clearCopy(){
        this.name2copy.clear();
    }

}
