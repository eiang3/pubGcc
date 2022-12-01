package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MIPS.SubOp;
import gccBin.MidCode.AoriginalProcess.IRTagManage;
import gccBin.MidCode.Judge;

import java.util.HashSet;

public class Line {
    private final String midCodeLine;       //最初的IR,对于AssignLine外的line应该是固定的
    private TableSymbol tableSymbol;        //符号表
    private int index;                      //index，在冲突图构建的时候要用

    private String gen; //只能有一个定义
    private final HashSet<String> use; //可能有多个使用


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
    }

    public boolean addGen_first(String name) {
        if (Judge.is_LocalVar(name, tableSymbol)) {
            this.gen = name;
            return true;
        }
        return false;
    }

    public boolean addUse_first(String name) {
        if (Judge.is_LocalVar(name, tableSymbol)) {
            this.use.add(name);
            return true;
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////
    //   一些乱七八糟但不能删的方法
    /////////////////////////////////////////////////////////////////////

    //对gen重命名，注意一个line的gen只有一个
    public void renameGen(String old, String name) {

    }

    //对use重命名，因为一个lines的use可能有多个，所以需要和oldName比较
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


    public void removeUse(String s) {
        this.use.remove(s);
    }

    ///////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return getMidCodeLine();
    }


    private final HashSet<String> use_zero = new HashSet<>();
    private String gen_zero;

    public void setGen_zero(String gen_zero) {
        this.gen_zero = gen_zero;
    }

    public void addUse_Zero(String vtn) {
        if (Judge.isZeroActive(vtn)) {
            this.use_zero.add(vtn);
            if (Judge.isTemp(vtn) && !vtn.equals(gen_zero)) {
                IRTagManage.getInstance().addUse(vtn);
            }
        }
    }

    public void removeUse_Zero(String vtn) {
        this.use_zero.remove(vtn);
    }

    public void addUse_Zero_firstTime(String t2) {
        if (Judge.isZeroActive(t2)) {
            this.use_zero.add(t2);
        }
    }

    public String getGen_zero() {
        if (gen_zero!=null && Judge.isArrayValue(gen_zero)) {
            return SubOp.getArrName(gen_zero);
        }
        return gen_zero;
    }

    public HashSet<String> getUse_zero() {
        HashSet<String> ret = new HashSet<>();
        for (String ans : use_zero) {
            if (Judge.isArrayValue(ans)) {
                ret.add(SubOp.getArrName(ans));
            } else {
                ret.add(ans);
            }
        }
        return ret;
    }

}
