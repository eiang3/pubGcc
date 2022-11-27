package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Judge;

import java.util.HashSet;

public class Line {
    private final String midCodeLine;       //最初的IR,对于AssignLine外的line应该是固定的
    private TableSymbol tableSymbol;        //符号表
    private int index;                      //index，在冲突图构建的时候要用

    private String gen; //只能有一个定义
    private final HashSet<String> use; //可能有多个使用

    private String gen_tempVar;
    private final HashSet<String> use_tempVar;

    /**
     * @param s           对于assignLine来说，会更新的
     * @param index       index计数之后会再次更新的
     * @param tableSymbol *
     */
    public Line(String s, int index, TableSymbol tableSymbol) {
        this.midCodeLine = s;
        this.index = index;
        this.tableSymbol = tableSymbol;
        this.use = new HashSet<>();
        this.use_tempVar = new HashSet<>();
    }

    public boolean addGen_both(String name) {
        if (!Judge.isNumber(name)) {
            this.gen_tempVar = name;
        }

        if (Judge.is_LocalVar(name, tableSymbol)) {
            this.gen = name;
            return true;
        }
        return false;
    }

    public boolean addUse_both(String name) {
        if (!Judge.isNumber(name)) {
            this.use_tempVar.add(name);
        }

        if (Judge.is_LocalVar(name, tableSymbol)) {
            this.use.add(name);
            return true;
        }
        return false;
    }

    public void addUse_Temp(String name) {
        if (Judge.isTemp(name)) {
            this.use_tempVar.add(name);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //   一些乱七八糟但不能删的方法
    /////////////////////////////////////////////////////////////////////

    /**
     * 对gen重命名，注意一个line的gen只有一个
     *
     * @param name *
     */
    public void renameGen(String old, String name) {

    }

    /**
     * 对use重命名，因为一个lines的use可能有多个，所以需要和oldName比较
     *
     * @param old  *
     * @param name *
     */
    public void renameUse(String old, String name) {

    }

    public void replaceOneUse(String old, String last) {
        this.use.remove(old);
        this.use.add(last);
    }

    ///////////////////////////////////////////////////////////////////////////////
    ////   get   set  方法
    //////////////////////////////////////////////////////////////////////////////
    public String getMidCodeLine() {
        return midCodeLine;
    }

    public int getLineLength() {
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

    public void setGen(String gen) {
        this.gen = gen;
    }

    public String getGen() {
        return gen;
    }

    public HashSet<String> getUse() {
        return use;
    }

    public void clearUse() {
        this.use.clear();
        this.use_tempVar.clear();
    }

    public String getGen_tempVar() {
        return gen_tempVar;
    }

    public HashSet<String> getUse_tempVar() {
        return use_tempVar;
    }
}
