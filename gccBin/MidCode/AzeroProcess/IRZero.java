package gccBin.MidCode.AzeroProcess;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.TableSymbol;

import gccBin.MidCode.Line.Line;
import gccBin.MidCode.LineManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 复写传播，公共子表达式删除，
 */
public class IRZero {
    private static IRZero irZero;

    private BufferedReader bufferedReader;
    private TableSymbol nowTable;
    private String rLine;

    private IRZero() {
    }


    public void begin() throws IOException {
        readLine();
        //1 建立所有基本块
        while (rLine != null) {
            if (!isAnnotate()) {
                Line line = LineManager.getInstance().parseLines(rLine, nowTable);
                ZeroBlockManager.getInstance().build_Block(line);
            }
            readLine();
        }
        optimize(); //优化

        //6 将每个基本块的表达式送到LineManager里
        LineManager.getInstance().clearLineManager();
        ZeroBlockManager.getInstance().addLineToLIneManager();
        //LineManager.getInstance().printfLines(1);
    }

    public void optimize() throws IOException {
        //2 进行基本块的连接
        ZeroBlockManager.getInstance().setBExit();
        ZeroBlockManager.getInstance().connectAll();

        ZeroBlockManager.getInstance().printfBlockAndActive("_original_");

        //4 进行可用表达式分析 & 复写传播
        ZeroBlockManager.getInstance().usableExpAndCopyPropagation();
        ZeroBlockManager.getInstance().usableExpAndCopyPropagation();

        ZeroBlockManager.getInstance().printfBlockAndActive("_afterUsable_");

        //3 进行基本块的活跃变量分析&无用表达式删除
        ZeroBlockManager.getInstance().ActiveVarAnalysis();
        ZeroBlockManager.getInstance().deleteUselessExp();

        ZeroBlockManager.getInstance().ActiveVarAnalysis();
        ZeroBlockManager.getInstance().deleteUselessExp();

        ZeroBlockManager.getInstance().ActiveVarAnalysis();
        ZeroBlockManager.getInstance().deleteUselessExp();
        //5 进行无用表达式删除
        ZeroBlockManager.getInstance().printfBlockAndActive("_afterDelete_");
    }

    /////////////////////////////////////////////////////////////////////////
    ///////  辅助方法
    /////////////////////////////////////////////////////////////////////////
    public void inTableSymbol() {
        nowTable = nowTable.getNextChild();
    }

    public void leaveTableSymbol() {
        nowTable = nowTable.getFather();
    }

    public void setRootTable() {
        this.nowTable = APIIRSymTable.getInstance().getRootTable();
    }

    private boolean isAnnotate() {
        if (rLine.length() < 2) return false;
        return rLine.charAt(0) == '#' && rLine.charAt(1) == '#';
    }

    ///////////////////////////////////////////////////////////////////////
    ////////////////////////////以下是单例模式基本功能/////////////////////////
    //////////////////////////////////////////////////////////////////////
    public static IRZero getInstance() {
        if (irZero == null) {
            irZero = new IRZero();
        }
        return irZero;
    }

    public void open() throws IOException {
        setRootTable();
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
