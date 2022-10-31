package gccBin.MidCode.firstProcess;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Line.*;
import gccBin.MidCode.LineManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * 主要是进行冲突图的构建工作
 */

public class MidCodeFirst {
    private static MidCodeFirst midCodeFirst;

    private BufferedReader bufferedReader;

    private TableSymbol nowTable; //

    private String rLine;

    private MidCodeFirst() {
    }

    public void beginFirstScan() throws IOException {
        readLine();
        //第一遍：得到line基本信息，创建基本块，将定义点使用点分发到相应变量。
        while (rLine != null) {
            if (!isAnnotate()) {
                Line line = LineManager.getInstance().addLines(rLine, nowTable);
                BasicBlockManager.getInstance().createBlockAndInitGneOp(line);
                VarNodeManager.getInstance().distributeGenAndUseToVar(line);
            }
            readLine();
        }
        BasicBlockManager.getInstance().setBExit();
        BasicBlockManager.getInstance().connectAllAndInitKill();

        BasicBlockManager.getInstance().computeInAndOut();

        BasicBlockManager.getInstance().initAllDefUseChain();

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
    public static MidCodeFirst getInstance() {
        if (midCodeFirst == null) {
            midCodeFirst = new MidCodeFirst();
        }
        return midCodeFirst;
    }

    public void open() throws IOException {
        File inputFile = new File("midCode.txt");
        bufferedReader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(inputFile.toPath()), "UTF-8"));
    }

    public void close() throws IOException {
        this.bufferedReader.close();
    }

    public void readLine() throws IOException {
        rLine = this.bufferedReader.readLine();
    }
}
