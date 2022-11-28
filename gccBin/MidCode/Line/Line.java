package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MidCode.AoriginalProcess.IRTagManage;
import gccBin.MidCode.Judge;

import java.util.HashMap;
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
        this.use_zero = new HashSet<>();
    }

    public boolean addGen_both(String name) {
        if (Judge.isZeroActive(name)) {
            this.gen_zero = name;
        }
        if (Judge.is_LocalVar(name, tableSymbol)) {
            this.gen = name;
            return true;
        }
        return false;
    }

    public boolean addUse_both(String name) {
        if (Judge.isZeroActive(name)) {
            this.use_zero.add(name);
        }
        if (Judge.is_LocalVar(name, tableSymbol)) {
            this.use.add(name);
            return true;
        }
        return false;
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

///////////////////////////////////////////////////////////////////////////

    private String gen_zero;
    private final HashSet<String> use_zero;
    //所有再活跃变量分析的时候会用到的变量。包括局部变量和临时变量

    public void clearTwoUseSet() {
        this.use.clear();
        this.use_zero.clear();
    }

    public String getGen_zero() {
        return gen_zero;
    }

    public HashSet<String> getUse_zero() {
        return use_zero;
    }

    /**
     * 仅调整temp变量的使用次数
     */
    public void decreaseUseAllForAssign() {
        for (String str : use_zero) {
            if (Judge.isTemp(str) && !gen_zero.equals(str)) {
                IRTagManage.getInstance().delete(str);
            }
        }
    }

    public void decreaseUse(String str) {
        if (Judge.isTemp(str) && !str.equals(gen_zero)) {
            IRTagManage.getInstance().delete(str);
        }
    }


    public void increaseUse(String str) {
        if (Judge.isTemp(str) && !str.equals(gen_zero)) {
            IRTagManage.getInstance().addUse(str);
        }
    }

    //复写传播需要更改相关的使用变量，在所有line中，只有assignLine可能使用
    //局部变量，其余均使用的是临时变量。
    public void copyPropagation(HashMap<String, String> copy) {
    }

    /**
     * 除AssignLine外的其余变量使用，仅有temp
     *
     * @param name temp
     */
    public void addUseTemp_Zero(String name) {
        if (Judge.isTemp(name)) {
            this.use_zero.add(name);
        }
    }

    public void addGenTemp_Zero(String name) {
        if (Judge.isTemp(name)) {
            this.gen_zero = name;
        }
    }

    //将一个TempUse改为另一个TempUse，并调整变量
    // 这是为非AssignLine的Line准备的
    public void exchangeTempUseZero(String n, String o) {
        if (n.equals(o)) return;
        decreaseUse(o); //将原来的删去
        use_zero.remove(o); //必须的
        increaseUse(n);
        use_zero.add(n);
    }

    public void removeFromBothUse(String str) {
        use.remove(str);
        use_zero.remove(str);
    }

    @Override
    public String toString() {
        return getMidCodeLine();
    }
}
