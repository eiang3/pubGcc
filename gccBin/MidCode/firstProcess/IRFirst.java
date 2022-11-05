package gccBin.MidCode.firstProcess;

import SymbolTableBin.TableSymbol;
import gccBin.MIPS.tool.MemManager;
import gccBin.MidCode.Line.*;
import gccBin.MidCode.LineManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 主要是进行冲突图的构建工作
 */

public class IRFirst {
    private static IRFirst irFirst;

    private BufferedReader bufferedReader;

    private TableSymbol nowTable;

    private String rLine;

    private IRFirst() {
    }

    /**
     * 分析original IR
     * ①对有不同网变量进行重命名
     * ②得到全局寄存器分配方案
     *
     * @throws IOException *
     */
    public void begin() throws IOException {
        readLine();
        while (rLine != null) {
            if (!isAnnotate()) {
                Line line = LineManager.getInstance().addLines(rLine, nowTable);
                BasicBlockManager.getInstance().build_Block_And_Parse_def(line);
                VarNodeManager.getInstance().distributeGenAndUseToVar(line);
            }
            readLine();
        }
        //打印lines集
        LineManager.getInstance().printfLines();

        //设置结尾基本块
        BasicBlockManager.getInstance().setBExit();

        //将所有基本块连接，并初始化kill_def集
        BasicBlockManager.getInstance().connectAllAndInitKill();

        //进行到达定义分析
        BasicBlockManager.getInstance().compute_defInAndOut();

        //计算每个节点的定义——使用链
        BasicBlockManager.getInstance().initAllDefUseChain();

        //每个节点建网
        VarNodeManager.getInstance().generateWeb();

        //对每个节点建的网进行处理
        VarNodeManager.getInstance().separateVarWebs();

        //基本块活跃变量分析
        BasicBlockManager.getInstance().block_ActiveVarAnalysis();

        //debug
        /*LineManager.getInstance().printfLines();

        BasicBlockManager.getInstance().printfBlockMessage();

        VarNodeManager.getInstance().printfVarNodeMessage();
        VarNodeManager.getInstance().printfVarWebMassage();*/

        //新节点的活跃范围分析
        BasicBlockManager.getInstance().varNode_ActiveScopeAnalysis();

        //得到变量冲突图
        VarNodeManager.getInstance().getClashGraph();

        //得到全局变量分配方案

        //首先将得到的web移到RegAllocation里
        RegAllocation.getInstance().addNodeToLeaveSet(
                VarNodeManager.getInstance().getName2Web());

        //完成s-reg分配
        RegAllocation.getInstance().finishRegAllocation();
    }

    public void inTableSymbol() {
        nowTable = nowTable.getNextChild();
    }

    public void leaveTableSymbol() {
        nowTable = nowTable.getFather();
    }

    public void setRootTable(TableSymbol nowTable) {
        this.nowTable = nowTable;
    }

    private boolean isAnnotate() {
        if (rLine.length() < 2) return false;
        return rLine.charAt(0) == '#' && rLine.charAt(1) == '#';
    }

    ////////////////////////////以下是单例模式基本功能////////////////////////////////////////////
    public static IRFirst getInstance() {
        if (irFirst == null) {
            irFirst = new IRFirst();
        }
        return irFirst;
    }

    public void open() throws IOException {
        File inputFile = new File("midCode.txt");
        bufferedReader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(inputFile.toPath()), StandardCharsets.UTF_8));
    }

    public void close() throws IOException {
        this.bufferedReader.close();
    }

    public void readLine() throws IOException {
        rLine = this.bufferedReader.readLine();
    }
}
