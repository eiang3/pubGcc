package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MIPS.tool.Scanf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Line {
    private String midCodeLine;
    private TableSymbol tableSymbol;
    private int index;

    private HashSet<String> gen;
    private HashSet<String> use;

    public Line(String s,int line,TableSymbol tableSymbol){
        this.midCodeLine = s;
        this.tableSymbol = tableSymbol;
        this.index = line;
        this.gen = new HashSet<>();
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
        this.gen.add(name);
    }


}
