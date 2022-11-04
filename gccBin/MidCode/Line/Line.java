package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Judge;

import java.util.HashSet;

public class Line {
    private String midCodeLine; //最初的ir。
    private TableSymbol tableSymbol;
    private int index;

    private String gen; //只能有一个定义
    private final HashSet<String> use; //可能有多个使用

    public Line(String s, int line, TableSymbol tableSymbol) {
        this.midCodeLine = s;
        this.tableSymbol = tableSymbol;
        this.index = line;
        //this.gen
        this.use = new HashSet<>();
    }

    public String getMidCodeLine() {
        return midCodeLine;
    }

    public void setMidCodeLine(String midCodeLine) {
        this.midCodeLine = midCodeLine;
    }

    public int getOriginalLine() {
        return midCodeLine.length();
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

    public boolean addGen(String name) {
        if (Judge.isVar(name, tableSymbol)) {
            this.gen = name;
            return true;
        }
        return false;
    }

    public boolean addUse(String name) {
        if (Judge.isVar(name, tableSymbol)) {
            this.use.add(name);
            return true;
        }
        return false;
    }

    public String getGen() {
        return gen;
    }

    public HashSet<String> getUse() {
        return use;
    }

    /**
     * 对gen重命名，注意一个line的gen只有一个
     *
     * @param name
     */
    public void renameGen(String old, String name) {
        ;
    }

    public void setGen(String gen) {
        this.gen = gen;
    }

    /**
     * 对use重命名，因为一个lines的use可能有多个，所以需要和oldName比较
     *
     * @param old
     * @param name
     */
    public void renameUse(String old, String name) {
        ;
    }

    public void replaceOneUse(String old, String last) {
        this.use.remove(old);
        this.use.add(last);
    }
}
