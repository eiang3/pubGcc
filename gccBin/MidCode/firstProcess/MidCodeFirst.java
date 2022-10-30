package gccBin.MidCode.firstProcess;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Line.LabelLine;
import gccBin.MidCode.Line.Line;
import gccBin.MidCode.MidCodeLines;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;

/**
 * 主要是进行冲突图的构建工作
 */
public class MidCodeFirst {
    private static MidCodeFirst midCodeFirst;

    private BufferedReader bufferedReader;

    private final HashMap<String, VarNode> name2Node;
    private final HashMap<Integer,BasicBlock> basicBlocks; //后续可能会删基本块
    private int basicBlockIndex;

    private final HashMap<String,Integer> label2Block;

    private TableSymbol nowTable; //

    private String rLine;

    private MidCodeFirst() {
        name2Node = new HashMap<>();
        basicBlocks = new HashMap<>();
        label2Block = new HashMap<>();
        basicBlockIndex = 0;
    }

    public void beginFirstScan() throws IOException {
        readLine();
        while (rLine != null) {
            if (!isAnnotate()) {
                Line line = MidCodeLines.getInstance().addLines(rLine, nowTable);
                distributeGenAndUse(line);
            }
            readLine();
        }
    }

    private void firstScanBlockOp(Line line){
        if(line.getIndex() == 0){
            basicBlocks.put(basicBlockIndex,new BasicBlock(basicBlockIndex++));
        } else if (line instanceof LabelLine) {
            basicBlocks.put(basicBlockIndex,new BasicBlock((basicBlockIndex++)));
        }
    }

    private void distributeGenAndUse(Line line) {
        for (String name : line.getGen()) {
            name2Node.get(name).addGen(line.getIndex());
        }
        for (String name : line.getUse()) {
            name2Node.get(name).addUse(line.getIndex());
        }
    }

    public void inTableSymbol() {
        nowTable = nowTable.getNextChild();
    }

    public void leaveTableSymbol() {
        nowTable = nowTable.getFather();
    }

    public void setNowTable(TableSymbol nowTable) {
        this.nowTable = nowTable;
    }

    private boolean isAnnotate() {
        if (rLine.length() < 2) return false;
        return rLine.charAt(0) == '#' && rLine.charAt(1) == '#';
    }

    public void addVarNode(String name,TableSymbol tableSymbol){
        VarNode node = new VarNode(name,tableSymbol);
        this.name2Node.put(name,node);
    }

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
