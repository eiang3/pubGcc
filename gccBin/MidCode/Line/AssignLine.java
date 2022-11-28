package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MIPS.SubOp;
import gccBin.MidCode.AzeroProcess.ZeroBlockManager;
import gccBin.MidCode.Judge;

import java.util.HashMap;

/**
 * 数组指针。
 * exp : 123 | $tx | a | a[$tx] | a[a] | a[123]
 *
 * <p>
 * a[0] = exp
 * j = exp  【3】
 * a = b (* | - | * | / | % | >> | << ) c 【5】
 * a = ( - | ! ) b 【4】
 * j = 3
 * i = RET
 * z = a[x]
 */
//
public class AssignLine extends Line {
    private String t1;
    private String t2;
    private String op;
    private String ans;

    private boolean pureAssign; //x = y;
    private boolean oneOpr; // x = - | ! y
    private boolean twoOpr; // x = y (+-*/%>><<) w

    // 用来进行第二次复杂的分析的时候
    private boolean t1IsUse;
    private boolean t2IsUse;
    private boolean ansIsGen;

    public AssignLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        parse(ele);
    }

    public void parse(String[] ele) {
        pureAssign = false;
        oneOpr = false;
        twoOpr = false;

        ans = ele[0];
        ansIsGen = super.addGen_both(ans);
        if (ele.length == 3) {
            //a[exp] = exp
            //temp = a[exp]
            //需要把exp提出来
            pureAssign = true;
            t1 = ele[2];
            t1IsUse = super.addUse_both(t1);
            t2 = null;

            if (Judge.isArrayValue(ans)) t2 = SubOp.getArrSubscript(ans);
            else if (Judge.isArrayValue(t1)) t2 = SubOp.getArrSubscript(t1);
            if (t2 != null) {
                super.addUseTemp_Zero(t2);
            }
        } else if (ele.length == 4) {
            oneOpr = true;
            t1 = ele[3];
            op = ele[2];
            t1IsUse = super.addUse_both(t1);
        } else if (ele.length == 5) {
            twoOpr = true;
            t1 = ele[2];
            op = ele[3];
            t2 = ele[4];
            t1IsUse = super.addUse_both(t1);
            t2IsUse = super.addUse_both(t2);
        }

    }

    /////////////////////////////////////////////////////////////////////////
    //正式分析的时候用到的

    @Override
    public void renameGen(String old, String name) {
        if (ansIsGen && ans.equals(old)) {
            ans = name;
            super.setGen(name);
        }
    }

    @Override
    public void renameUse(String old, String name) {
        if (t1IsUse && t1.equals(old)) {
            t1 = name;
            super.replaceOneUse(old, name);
        }
        if (t2IsUse && t2.equals(old)) {
            t2 = name;
            super.replaceOneUse(old, name);
        }
    }

    @Override
    public String getMidCodeLine() {
        if (isPureAssign()) {
            return ans + " = " + t1;
        } else if (isOneOpr()) {
            return ans + " = " + op + " " + t1;
        } else {
            return ans + " = " + t1 + " " + op + " " + t2;
        }
    }

    public String getT1() {
        return t1;
    }

    public String getT2() {
        return t2;
    }

    public String getOp() {
        return op;
    }

    public String getAns() {
        return ans;
    }

    public boolean isPureAssign() {
        return pureAssign;
    }

    public boolean isOneOpr() {
        return oneOpr;
    }

    public boolean isTwoOpr() {
        return twoOpr;
    }


    ///////////////////////////////////////////////////////////////////////
    //             提前化简部分
    //////////////////////////////////////////////////////////////////////

    /**
     * 前提:两个AssignLine的右部否相等
     * 结果，把调用的line的右部换为被调用等右部
     *
     * @param assignLine *
     */
    public void exchange(AssignLine assignLine) {
        // 如果已经替换过了，而且需要再次替换的比原先还晚，就不再替换了，不重复替换
        String newT1 = assignLine.getAns();
        if (Judge.isTemp(newT1) && isPureAssign() && Judge.isTemp(t1)) {
            int newT1Index = Judge.getTempIndex(newT1);
            int newT2Index = Judge.getTempIndex(t1);
            if (newT1Index > newT2Index) return;
        }
        super.decreaseUseAllForAssign();
        super.clearTwoUseSet();
        this.pureAssign = true;
        this.oneOpr = false;
        this.twoOpr = false;
        this.t1 = assignLine.getAns();
        this.op = null;
        this.t2 = null;
        this.t2IsUse = false;
        t1IsUse = addUse_both(t1);
        super.increaseUse(t1);
        //为了接下来的复写传播
        ZeroBlockManager.getInstance().addCopy(ans, t1);
    }

    /**
     * 返回右边变量
     *
     * @return *
     */
    public String getRight() {
        if (isPureAssign()) {
            return t1;
        } else if (isOneOpr()) {
            return op + " " + t1;
        } else {
            return t1 + " " + op + " " + t2;
        }
    }

    @Override
    public void copyPropagation(HashMap<String, String> copy) {
        if (copy.containsKey(t1) && Judge.isTemp(t1)) {
            super.decreaseUse(t1);
            super.removeFromBothUse(t1);
            t1 = copy.get(t1);
            super.increaseUse(t1);
            t1IsUse = addUse_both(t1);
        }
        if (twoOpr && copy.containsKey(t2) && Judge.isTemp(t2)) {
            super.decreaseUse(t2);
            super.removeFromBothUse(t2);
            t2 = copy.get(t2);
            super.increaseUse(t2);
            t2IsUse = addUse_both(t2);
        }
    }
}
