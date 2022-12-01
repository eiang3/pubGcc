package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;
import gccBin.MIPS.SubOp;
import gccBin.MidCode.AoriginalProcess.IRTagManage;
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

public class AssignLine extends Line {
    private String t1;
    private String t2;
    private String op;
    private String ans;

    private boolean pureAssign; //x = y;
    private boolean oneAssign; // x = - | ! y
    private boolean twoAssign; // x = y (+-*/%>><<) w

    //gen and use ans ansIsUse t1|t2isUse 都是针对最后的局部变量寄存器分配的
    //如果分配不完的话，针对这两个带数组的，均使得t2对应数组下标。
    private boolean arrayRefresh; // a[vtn] = vtn
    private boolean arrayValue; // vtn = a[vtn]
    private String arrName;

    // 用来进行第二次复杂的分析的时候
    private boolean t1IsUse;
    private boolean t2IsUse;
    private boolean ansIsGen;

    public AssignLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        t1 = null;
        t2 = null;
        ans = null;
        op = null;

        pureAssign = false;
        oneAssign = false;
        twoAssign = false;

        arrName = null;
        arrayRefresh = false;
        arrayValue = false;

        t1IsUse = false;
        t2IsUse = false;
        ansIsGen = false;

        parse(ele);
    }

    public void parse(String[] ele) {
        ans = ele[0];
        super.setGen_zero(ans);
        ansIsGen = super.addGen_first(ans);
        if (ele.length == 3) {
            pureAssign = true;
            t1 = ele[2];
            addT1_firstTime();
            judgeArray();
        } else if (ele.length == 4) {
            oneAssign = true;
            t1 = ele[3];
            op = ele[2];
            addT1_firstTime();
        } else if (ele.length == 5) {
            twoAssign = true;
            t1 = ele[2];
            op = ele[3];
            t2 = ele[4];
            addT1_firstTime();
            addT2_firstTime();
        }
    }

    public void addT1() {
        super.addUse_Zero(t1);
        t1IsUse = super.addUse_first(t1);
    }

    public void addT2() {
        super.addUse_Zero(t2);
        t2IsUse = super.addUse_first(t2);
    }

    public void addT1_firstTime() {
        super.addUse_Zero_firstTime(t1);
        t1IsUse = super.addUse_first(t1);
    }

    public void addT2_firstTime() {
        super.addUse_Zero_firstTime(t2);
        t2IsUse = super.addUse_first(t2);
    }

    public void deleteT1() {
        if (t1 != null) {
            super.removeUse(t1);
            super.removeUse_Zero(t1);
            t1IsUse = false;
            if (Judge.isTemp(t1) && !t1.equals(getAns())) {
                IRTagManage.getInstance().delete(t1);
            }
        }
        t1 = null;
    }

    public void deleteT2() {
        if (t2 != null) {
            super.removeUse(t2);
            super.removeUse_Zero(t2);
            t2IsUse = false;
            if (Judge.isTemp(t2) && !t2.equals(getAns())) {
                IRTagManage.getInstance().delete(t2);
            }
        }
        t2 = null;
    }

    public void deleteAllT() {
        deleteT1();
        deleteT2();
    }
    ///////////////////////////////////////////////////////////////////////
    //             提前化简部分
    //////////////////////////////////////////////////////////////////////

    /**
     * pre      :   两个AssignLine的右部否相等
     * result   :   把调用的line的右部换为被调用等右部
     */
    public void commonExpSub(String newRight) {
        // 如果已经替换过了，而且需要再次替换的比原先还晚，就不再替换了，不重复替换
        if (Judge.isTemp(newRight) && isPureAssign() && Judge.isTemp(getT1())) {
            int newT1Index = Judge.getTempIndex(newRight);
            int newT2Index = Judge.getTempIndex(getT1());
            if (newT1Index > newT2Index) return;
        }
        this.pureAssign = true;
        this.oneAssign = false;
        this.twoAssign = false;
        this.arrayValue = false;    //公共子表达式删除不会在右边替换为a[vtn]的
        this.op = null;
        deleteT1();
        deleteT2();
        this.t1 = newRight;
        addT1();
    }


    public String getRight() {
        if (arrayValue) {
            return arrName + "[" + t2 + "]";
        } else if (isPureAssign()) {
            return t1;
        } else if (isOneAssign()) {
            return op + " " + t1;
        } else {
            return t1 + " " + op + " " + t2;
        }
    }

    public void copyPropagation(HashMap<String, String> copy) {
        if (copy.containsKey(t1)) {
            String newT1 = copy.get(t1);
            deleteT1();
            t1 = newT1;
            addT1();
        }
        //t2或者是数组的下标
        if (t2 != null && copy.containsKey(t2)) {
            String newT2 = copy.get(t2);
            deleteT2();
            t2 = newT2;
            addT2();
        }
    }

    public void judgeArray() {
        if (Judge.isArrayValue(ans)) {
            arrName = SubOp.getArrName(ans);
            t2 = SubOp.getArrSubscript(ans);
            addT2_firstTime();
            arrayRefresh = true;
            arrayValue = false;
        } else if (Judge.isArrayValue(t1)) {
            arrName = SubOp.getArrName(t1);
            t2 = SubOp.getArrSubscript(t1);
            addT2_firstTime();
            arrayValue = true;
            arrayRefresh = false;
        } else {
            arrayRefresh = false;
            arrayValue = false;
            arrName = null;
        }
    }


    /**
     * 如果操作数都是数字，或者左值是a[vtb](显然这时候没必要将a[vtn]替换到右边)
     *
     * @return *
     */
    public boolean needCommonExpAnalysis() {
        if (arrayRefresh) return false;
        boolean allNum = false;
        String T1 = getT1();
        String T2 = getT2();
        if (T1 != null && T2 != null) {
            allNum = (Judge.isNumber(T1) && Judge.isNumber(T2));
        } else if (T1 != null) {
            allNum = Judge.isNumber(T1);
        } else if (T2 != null) {
            allNum = Judge.isNumber(T2);
        }
        return !allNum;
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
        if (arrayRefresh) {
            return arrName + "[" + t2 + "] = " + t1;
        } else if (arrayValue) {
            return ans + " = " + arrName + "[" + t2 + "]";
        } else if (isPureAssign()) {
            return ans + " = " + t1;
        } else if (isOneAssign()) {
            return ans + " = " + op + " " + t1;
        } else {
            return ans + " = " + t1 + " " + op + " " + t2;
        }
    }

    public String getT1() {
        if (arrayValue) {
            return arrName + "[" + t2 + "]";
        }
        return t1;
    }

    public String getT2() {
        return t2;
    }

    public String getOp() {
        return op;
    }

    public String getAns() {
        if (arrayRefresh) return arrName + "[" + t2 + "]";
        else return ans;
    }

    public boolean isPureAssign() {
        return pureAssign;
    }

    public boolean isOneAssign() {
        return oneAssign;
    }

    public boolean isTwoAssign() {
        return twoAssign;
    }


    public boolean isArrayRefresh() {
        return arrayRefresh;
    }

    public boolean isArrayValue() {
        return arrayValue;
    }

    //普通判断  x = a[exp]  (This), 但是exp = a
    public boolean shouldDelete(String a) {
        return a.equals(getT1()) || a.equals(t2) || a.equals(getAns());
    }

    /**
     * x = a[i]
     * 需要判断a[i]是否已经该改变,且信息说明穿进来的是一个数组
     *
     * @param arrName *
     * @param arrSub  *
     * @return *
     */
    public boolean shouldDelete(String arrName, String arrSub) {
        if (arrayValue || arrayRefresh) {
            if (Judge.isNumber(arrSub) && Judge.isNumber(this.t2)) {
                return arrName.equals(this.arrName) && arrSub.equals(this.t2);
            } else {
                return arrName.equals(this.arrName);
            }
        }
        return false;
    }

}
