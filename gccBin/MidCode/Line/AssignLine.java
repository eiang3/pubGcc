package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

import java.util.HashSet;

/**
 * 数组指针。
 * exp : 123 | $tx | a | a[$tx] | a[a] | a[123]
 * <p>
 * a[0] = exp
 * j = exp  【3】
 * a = b (* | - | * | / | % | >> | << ) c 【5】
 * a = ( - | ! ) b 【4】
 * j = 3
 * i = RET
 * z = a[x]
 */

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
            pureAssign = true;
            t1 = ele[2];
            t1IsUse = super.addUse_both(t1);
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

    ///////////////////////////////////////////////////////////////////////
    //             提前化简部分
    //////////////////////////////////////////////////////////////////////

    /**
     * 判断两个AssignLine的右部是否相等
     *
     * @param assignLine *
     */
    public void judgeRightEqual_exchange(AssignLine assignLine) {
        boolean ret = false;
        if (this.isPureAssign() && assignLine.isPureAssign()) {
            ret = this.t1.equals(assignLine.getT1());
        } else if (this.isOneOpr() && assignLine.isOneOpr()) {
            ret = this.op.equals(assignLine.getOp()) &&
                    this.t1.equals(assignLine.getT1());
        } else if (this.isTwoOpr() && assignLine.isTwoOpr()) {
            ret = this.t1.equals(assignLine.getT1()) &&
                    this.op.equals(assignLine.getOp()) &&
                    this.t2.equals(assignLine.getT2());
        }

        if (ret) {
            this.pureAssign = true;
            this.oneOpr = false;
            this.twoOpr = false;
            this.t1 = assignLine.getAns();
            super.clearUse();
            t1IsUse = addUse_both(t1);
            if (twoOpr) t2IsUse = addUse_both(t2);
        }
    }

    public void exchangeRight(AssignLine assignLine) {
        this.pureAssign = true;
        this.oneOpr = false;
        this.twoOpr = false;
        this.t1 = assignLine.getAns();
        super.clearUse();
        t1IsUse = addUse_both(t1);
        if (twoOpr) t2IsUse = addUse_both(t2);
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


}
