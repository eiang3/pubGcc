package gccBin.MidCode.Line;

import SymbolTableBin.APIIRSymTable;
import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TableSymbol;
import gccBin.MIPS.SubOp;
import gccBin.MIPS.tool.Reg;
import gccBin.MIPS.tool.TempRegPool;
import gccBin.MIPS.tool.mipsIns;
import gccBin.MidCode.Judge;
import gccBin.UnExpect;

import java.io.IOException;

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

    private boolean t1IsUse; //代表着是非全局var,以后可以直接用
    private boolean t2IsUse;
    private final boolean ansIsGen;

    private boolean pureAssign; //x = y;
    private boolean oneOpr; // x = - | ! y
    private boolean twoOpr; // x = y (+-*/%>><<) w


    public AssignLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        pureAssign = false;
        oneOpr = false;
        twoOpr = false;
        ans = ele[0];
        ansIsGen = super.addGen(ans);
        if (ele.length == 3) {
            pureAssign = true;
            t1 = ele[2];
            t1IsUse = super.addUse(t1);
        } else if (ele.length == 4) {
            oneOpr = true;
            t1 = ele[3];
            op = ele[2];
            t1IsUse = super.addUse(t1);
        } else if (ele.length == 5) {
            twoOpr = true;
            t1 = ele[2];
            op = ele[3];
            t2 = ele[4];
            t1IsUse = super.addUse(t1);
            t2IsUse = super.addUse(t2);
        }
    }

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

    /**
     *  next is static handle for mips generate
     */

    /**
     * ok
     * temp1 = (-|!) temp2
     *
     * @param answer temp1
     * @param op     -|!
     * @param temp   temp2
     * @throws IOException e
     */
    public static void assignOne(String answer, String op, String temp) throws IOException {
        if (TempRegPool.getInstance().inReg(temp)) { //temp in reg
            Reg ans = TempRegPool.getInstance().replace(answer, temp); // replace
            mipsIns.negOrNot(ans, op);
        } else if (TempRegPool.getInstance().inMem(temp)) { // temp in mem
            Reg ans = TempRegPool.getInstance().addToPool(answer);
            if (ans == null) { //ans in mem
                TempRegPool.getInstance().moveFromMem(Reg.r1, temp);
                mipsIns.negOrNot(Reg.r1, op);
                TempRegPool.getInstance().storeToMem(Reg.r1, answer);
            } else { //ans in reg
                TempRegPool.getInstance().moveFromMem(ans, temp);
                mipsIns.negOrNot(ans, op);
            }
        } else UnExpect.printf(temp + " is a temp not in reg and mem");

        TempRegPool.getInstance().delete(temp);
    }

    /**
     * t1 = (-|!) number
     *
     * @param answer t1
     * @param op     -|!
     * @param number number
     * @throws IOException e
     */
    public static void assignOne(String answer, String op, int number) throws IOException {
        Reg ans = TempRegPool.getInstance().addToPool(answer);
        if (ans != null) {
            assignOneSubHandle(ans, op, number);
        } else {
            assignOneSubHandle(Reg.l1, op, number);
            TempRegPool.getInstance().storeToMem(Reg.l1, answer);
        }
    }

    private static void assignOneSubHandle(Reg ans, String op, int number) throws IOException {
        if (op.equals("-")) {
            mipsIns.li(ans, number * -1);
        } else if (op.equals("!")) {
            number = (number == 0) ? 1 : 0;
            mipsIns.li(ans, number);
        } else UnExpect.printf(op + " is not ! or -");
    }

    /**
     * ans = addr >> 2
     * ans = t1 << 2
     * ans = exp1 +-/*% exp2
     *
     * @param answer      ans
     * @param temp1       temp1
     * @param op          op
     * @param temp2       temp2
     * @param tableSymbol table
     * @throws IOException e
     */
    public static void assignTwo(String answer, String temp1, String op, String temp2, TableSymbol tableSymbol) throws IOException {
        ElementTable elementTable = APIIRSymTable.getInstance().findElementRecur(tableSymbol, temp1);
        if (Judge.isAddr(elementTable, temp1)) {
            mipsIns.add_reg_o(Reg.r1, Reg.$fp, elementTable.getMemOff());
            mipsIns.sll(Reg.r1, Reg.r1, 2);
            allocateAnsTemp_storeReg(answer, Reg.r1);
            return;
        }
        int num1 = (Judge.isNumber(temp1)) ? Integer.parseInt(temp1) : 0;
        int num2 = (Judge.isNumber(temp2)) ? Integer.parseInt(temp2) : 0;
        if (Judge.isNumber(temp1) && Judge.isNumber(temp2)) {
            assignTwo_TwoNumber(answer, num1, op, num2);
        } else if (Judge.isTemp(temp1) && Judge.isNumber(temp2)) {
            assignTwo_Temp_Number(answer, temp1, op, num2);
        } else if (Judge.isNumber(temp1) && Judge.isTemp(temp2)) {
            assignTwo_Number_Temp(answer, num1, op, temp2);
        } else if (Judge.isTemp(temp1) && Judge.isTemp(temp2)) {
            assignTwo_TwoTemp(answer, temp1, op, temp2);
        } else UnExpect.unexpect("assignTwo");
    }

    public static void allocateAnsTemp_storeReg(String answer, Reg reg) throws IOException {
        Reg ans = TempRegPool.getInstance().addToPool(answer);
        if (TempRegPool.getInstance().inReg(answer)) {
            mipsIns.move(ans, reg);
        } else if (TempRegPool.getInstance().inMem(answer)) {
            TempRegPool.getInstance().storeToMem(reg, answer);
        } else UnExpect.tempNotInMemAndReg(answer);
    }

    public static void assignTwo_TwoTemp(String answer, String temp1, String op, String temp2) throws IOException {
        Reg t1 = TempRegPool.getInstance().getTempInReg(Reg.r1, temp1);
        Reg t2 = TempRegPool.getInstance().getTempInReg(Reg.r2, temp2);
        if (TempRegPool.getInstance().inMem(temp1) && TempRegPool.getInstance().inMem(temp2)) {
            Reg reg = TempRegPool.getInstance().addToPool(answer);
            if (TempRegPool.getInstance().inReg(answer)) {
                mipsIns.compute(reg, t1, op, t2);
            } else if (TempRegPool.getInstance().inMem(answer)) {
                mipsIns.compute(Reg.l1, t1, op, t2);
                TempRegPool.getInstance().storeToMem(Reg.r1, answer);
            } else UnExpect.tempNotInMemAndReg(answer);
        } else if (TempRegPool.getInstance().inReg(temp1)) {
            mipsIns.compute_first(op, t1, t2);
            TempRegPool.getInstance().replace(answer, temp1);
        } else if (TempRegPool.getInstance().inReg(temp2)) {
            mipsIns.compute_second(op, t1, t2);
            TempRegPool.getInstance().replace(answer, temp2);
        } else UnExpect.printf("strange error!");
        TempRegPool.getInstance().delete(temp1);
        TempRegPool.getInstance().delete(temp2);
    }

    public static void assignTwo_TwoNumber(String answer, int num1, String op, int num2) throws IOException {
        Reg ans = TempRegPool.getInstance().addToPool(answer);
        int result = SubOp.compute(num1, op, num2);
        if (TempRegPool.getInstance().inReg(answer)) {
            mipsIns.li(ans, result);
        } else if (TempRegPool.getInstance().inMem(answer)) {
            mipsIns.li(Reg.l1, result);
            TempRegPool.getInstance().storeToMem(Reg.l1, answer);
        } else UnExpect.tempNotInMemAndReg(answer);
    }

    public static void assignTwo_Temp_Number(String answer, String temp1, String op, int number) throws IOException {
        Reg t1 = TempRegPool.getInstance().getTempInReg(Reg.r1, temp1);
        if (TempRegPool.getInstance().inReg(temp1)) {
            mipsIns.compute_first(op, t1, number);
            TempRegPool.getInstance().replace(answer, temp1);
        } else if (TempRegPool.getInstance().inMem(temp1)) {
            Reg ans = TempRegPool.getInstance().addToPool(answer);
            if (TempRegPool.getInstance().inReg(answer)) {
                mipsIns.compute(ans, t1, op, number);
            } else if (TempRegPool.getInstance().inMem(answer)) {
                mipsIns.compute_first(op, t1, number);
                TempRegPool.getInstance().storeToMem(t1, answer);
            } else UnExpect.tempNotInMemAndReg(answer);
        } else UnExpect.tempNotInMemAndReg(temp1);
        TempRegPool.getInstance().delete(temp1);
    }

    public static void assignTwo_Number_Temp(String answer, int num1, String op, String temp2) throws IOException {
        Reg t2 = TempRegPool.getInstance().getTempInReg(Reg.r2, temp2);
        if (Judge.isPlus(op)) {
            mipsIns.add_reg_o(t2, t2, num1);
            TempRegPool.getInstance().replace(answer, temp2);
        } else {
            mipsIns.li(Reg.r1, num1);
            if (TempRegPool.getInstance().inReg(temp2)) {
                mipsIns.compute_second(op, Reg.r1, t2);
                TempRegPool.getInstance().replace(answer, temp2);
            } else if (TempRegPool.getInstance().inMem(temp2)) {
                Reg ans = TempRegPool.getInstance().addToPool(answer);
                if (TempRegPool.getInstance().inReg(answer)) {
                    mipsIns.compute(ans, Reg.r1, op, t2);
                } else if (TempRegPool.getInstance().inMem(answer)) {
                    mipsIns.compute_first(op, Reg.r1, t2);
                    TempRegPool.getInstance().storeToMem(Reg.r1, answer);
                } else UnExpect.tempNotInMemAndReg(answer);
            } else UnExpect.tempNotInMemAndReg(temp2);
        }
        TempRegPool.getInstance().delete(temp2);
    }
}
