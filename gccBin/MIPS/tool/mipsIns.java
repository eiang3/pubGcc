package gccBin.MIPS.tool;

import gccBin.MIPS.MIPS;
import gccBin.MidCode.Judge;
import gccBin.MidCode.original.IRTagManage;
import gccBin.UnExpect;

import java.io.IOException;

public class mipsIns {
    private static gccBin.MIPS.tool.mipsIns mipsIns;

    private mipsIns() {
    }

    public static gccBin.MIPS.tool.mipsIns get() {
        if (mipsIns == null) {
            mipsIns = new mipsIns();
        }
        return mipsIns;
    }

    /**
     * compute
     */
    public static void add_reg_o(Reg ans, Reg reg, int number) throws IOException {
        write("add " + ans + "," + reg + "," + number);
    }

    public static void add_reg_o(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("add " + ans + "," + reg1 + "," + reg2);
    }

    public static void sub_reg_o(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("sub " + ans + "," + reg1 + "," + reg2);
    }

    public static void sub_reg_o(Reg ans, Reg reg1, int number) throws IOException {
        write("sub " + ans + "," + reg1 + "," + number);
    }

    public static void mult_reg_reg(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("mult " + reg1 + "," + reg2);
        write("mflo " + ans);
    }

    public static void div_reg_reg(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("div " + reg1 + "," + reg2);
        write("mflo " + ans);
    }

    public static void mod_reg_reg(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("div " + reg1 + "," + reg2);
        write("mfhi " + ans);
    }

    /**
     * other
     */
    public static void move(Reg regTo, Reg regFrom) throws IOException {
        write("move " + regTo + "," + regFrom);
    }

    public static void la_label(Reg ans, String label) throws IOException {
        write("la " + ans + "," + label);
    }

    public static void lw_number_reg(Reg ans, int number, Reg addr) throws IOException {
        write("lw " + ans + "," + number + "(" + addr + ")");
    }

    public static void sw_number_reg(Reg value, int number, Reg addr) throws IOException {
        write("sw " + value + "," + number + "(" + addr + ")");
    }

    public static void lw_label_reg(Reg ans, String label, Reg addr) throws IOException {
        write("lw " + ans + "," + label + "(" + addr + ")");
    }

    public static void sw_label_reg(Reg value, String label, Reg addr) throws IOException {
        write("sw " + value + "," + label + "(" + addr + ")");
    }

    public static void lw_label_number(Reg ans, String label, int number) throws IOException {
        write("lw " + ans + "," + label + "+" + number);
    }

    public static void sw_label_number(Reg value, String label, int number) throws IOException {
        write("sw " + value + "," + label + "+" + number);
    }

    public static void lw_reg(Reg ans, Reg addr) throws IOException {
        write("lw " + ans + "," + "(" + addr + ")");
    }

    public static void sw_reg(Reg value, Reg addr) throws IOException {
        write("sw " + value + "," + "(" + addr + ")");
    }

    public static void li(Reg reg, int number) throws IOException {
        write("li " + reg + "," + number);
    }

    public static void sll(Reg ans, Reg oper, int number) throws IOException {
        write("sll " + ans + "," + oper + "," + number);
    }

    public static void srl(Reg ans, Reg oper, int number) throws IOException {
        write("srl " + ans + "," + oper + "," + number);
    }

    public static void not(Reg ans, Reg oper) throws IOException {
        write("not " + ans + "," + oper);
    }

    public static void negative(Reg ans, Reg operand) throws IOException {
        write("not " + ans + "," + operand);
        write("add " + ans + "," + ans + "," + 1);
    }

    public static void negative(Reg ans) throws IOException {
        write("not " + ans + "," + ans);
        write("add " + ans + "," + ans + "," + 1);
    }

    /**
     * !
     *
     * @param reg reg
     * @throws IOException e
     */
    public static void logicalNot(Reg reg) throws IOException {
        String labelLiOne = IRTagManage.getInstance().newLabel();
        String labelEnd = IRTagManage.getInstance().newLabel();
        write("beq " + Reg.$zero + "," + reg + "," + labelLiOne);
        write("move " + reg + "," + Reg.$zero);
        write("b " + labelEnd);
        write(labelLiOne);
        write("li " + reg + ",1");
        write(labelEnd + ":");
    }

    public static void bCond(String b, Reg t1, Reg t2, String label) throws IOException {
        write(b + " " + t1 + "," + t2 + "," + label);
    }

    public static void printfStr(String s) throws IOException {
        la_label(Reg.$a0, s);
        li(Reg.$v0, 4);
        write("syscall");
    }

    public static void printfInt(int s) throws IOException {
        li(Reg.$a0, s);
        li(Reg.$v0, 1);
        write("syscall");
    }

    public static void printfExp(Reg s) throws IOException {
        move(Reg.$a0, s);
        li(Reg.$v0, 1);
        write("syscall");
    }

    public static void scanfInt() throws IOException {
        li(Reg.$v0, 5);
        write("syscall");
    }

    public static void bLabel(String label) throws IOException {
        write("b " + label);
    }

    public static void jalLabel(String label) throws IOException {
        write("jal " + label);
    }

    public static void jr(Reg reg) throws IOException {
        write("jr " + reg);
    }

    public static void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

    public void writeNotNext(String s) throws IOException {
        MIPS.getInstance().writeNotNext(s);
    }

    /**
     * t1 = (- | !) t1
     *
     * @param ans t1
     * @param op  - | !
     */
    public static void negOrNot(Reg ans, String op) throws IOException {
        if (op.equals("-")) {  // op
            negative(ans);
        } else if (op.equals("!")) {
            logicalNot(ans);
        } else UnExpect.printf(op + " is not ! and -");
    }

    /**
     * compute
     *
     * @param ans   ans
     * @param op    operate
     * @param temp1 t1
     * @param temp2 t2
     * @throws IOException e
     */
    public static void compute(Reg ans, Reg temp1, String op, Reg temp2) throws IOException {
        if (Judge.isPlus(op)) {
            add_reg_o(ans, temp1, temp2);
        } else if (Judge.isMinus(op)) {
            sub_reg_o(ans, temp1, temp2);
        } else if (Judge.isMul(op)) {
            mult_reg_reg(ans, temp1, temp2);
        } else if (Judge.isDiv(op)) {
            div_reg_reg(ans, temp1, temp2);
        } else if (Judge.isMod(op)) {
            mod_reg_reg(ans, temp1, temp2);
        } else if (Judge.isSll(op)) {
            sll(ans, temp1, 2);
        } else if (Judge.isSrl(op)) {
            srl(ans, temp1, 2);
        }
    }

    /**
     * compute and store to first
     *
     * @param op    operate +-*%<< >>/
     * @param temp1 ans and op1
     * @param temp2 op2
     * @throws IOException e
     */
    public static void compute_first(String op, Reg temp1, Reg temp2) throws IOException {
        compute(temp1, temp1, op, temp2);
    }

    /**
     * compute and store to second
     *
     * @param op    operate +-*%<< >>/
     * @param temp1 ans and op1
     * @param temp2 op2
     * @throws IOException e
     */
    public static void compute_second(String op, Reg temp1, Reg temp2) throws IOException {
        compute(temp2, temp1, op, temp2);
    }

    /**
     * compute reg and number
     * (数字应该是第二个操作数)
     *
     * @param ans    ans
     * @param temp1  temp1
     * @param op     operate
     * @param number number
     * @throws IOException e
     */
    public static void compute(Reg ans, Reg temp1, String op, int number) throws IOException {
        if (Judge.isPlus(op)) {
            add_reg_o(ans, temp1, number);
        } else if (Judge.isMinus(op)) {
            sub_reg_o(ans, temp1, number);
        } else if (Judge.isSll(op)) {
            sll(ans, temp1, number);
        } else if (Judge.isSrl(op)) {
            srl(ans, temp1, number);
        } else {
            li(Reg.r2, number);
            compute(ans, temp1, op, Reg.r2);
        }
    }

    /**
     * compute reg and number and store ans in first reg
     *
     * @param op     operate
     * @param ans    op1 ans ans
     * @param number number
     */
    public static void compute_first(String op, Reg ans, int number) throws IOException {
        compute(ans, ans, op, number);
    }
}
