package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.firstProcess.JudgeExpElement;

import java.util.HashSet;

public class Line {
    private String midCodeLine;
    private TableSymbol tableSymbol;
    private int index;

    private String gen; //只能有一个定义
    private HashSet<String> use; //可能有多个使用

    public Line(String s,int line,TableSymbol tableSymbol){
        this.midCodeLine = s;
        this.tableSymbol = tableSymbol;
        this.index = line;
        //this.gen
        this.use = new HashSet<>();
    }

    public void setMidCodeLine(String midCodeLine) {
        this.midCodeLine = midCodeLine;
    }

    public String getMidCodeLine() {
        return midCodeLine;
    }

    public void setTableSymbol(TableSymbol tableSymbol) {
        this.tableSymbol = tableSymbol;
    }

    public TableSymbol getTableSymbol() {
        return tableSymbol;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void addGen(String name){
        if(JudgeExpElement.isVar(name,tableSymbol)) {
            this.gen = name;
        }
    }

    public void addUse(String name){
        if(JudgeExpElement.isVar(name,tableSymbol)) {
            this.use.add(name);
        }
    }

    public String getGen() {
        return gen;
    }

    public HashSet<String> getUse() {
        return use;
    }
}
