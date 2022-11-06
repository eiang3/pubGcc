package gccBin.MIPS.tool;

import gccBin.MIPS.MIPS;
import gccBin.MidCode.Judge;
import gccBin.MidCode.LineManager;
import gccBin.MidCode.original.IRTagManage;
import gccBin.UnExpect;

import java.io.IOException;

public class MipsIns {

    /**
     * compute
     */
    public static void add_ans_reg_regOrNum(Reg ans, Reg reg, int number) throws IOException {
        write("add " + ans + "," + reg + "," + number);
    }

    public static void add_ans_reg_regOrNum(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("add " + ans + "," + reg1 + "," + reg2);
    }

    public static void sub_ans_reg_regOrNum(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("sub " + ans + "," + reg1 + "," + reg2);
    }

    public static void sub_ans_reg_regOrNum(Reg ans, Reg reg1, int number) throws IOException {
        write("sub " + ans + "," + reg1 + "," + number);
    }

    public static void mul_ans_reg_reg(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("mult " + reg1 + "," + reg2);
        write("mflo " + ans);
    }

    public static void div_ans_reg_reg(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("div " + reg1 + "," + reg2);
        write("mflo " + ans);
    }

    public static void mod_ans_reg_reg(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("div " + reg1 + "," + reg2);
        write("mfhi " + ans);
    }

    /**
     * other
     */
    public static void move_reg_reg(Reg regTo, Reg regFrom) throws IOException {
        write("move " + regTo + "," + regFrom);
    }

    public static void la_ans_label(Reg ans, String label) throws IOException {
        write("la " + ans + "," + label);
    }

    public static void lw_ans_num_baseReg(Reg ans, int number, Reg base) throws IOException {
        write("lw " + ans + "," + number + "(" + base + ")");
    }

    public static void sw_value_num_baseReg(Reg value, int number, Reg base) throws IOException {
        write("sw " + value + "," + number + "(" + base + ")");
    }

    public static void lw_ans_label_base(Reg ans, String label, Reg base) throws IOException {
        write("lw " + ans + "," + label + "(" + base + ")");
    }

    public static void sw_value_label_base(Reg value, String label, Reg base) throws IOException {
        write("sw " + value + "," + label + "(" + base + ")");
    }

    public static void lw_ans_label_num(Reg ans, String label, int number) throws IOException {
        write("lw " + ans + "," + label + " + " + number);
    }

    public static void lw_ans_label(Reg ans, String label) throws IOException {
        write("lw " + ans + "," + label);
    }

    public static void sw_value_label_num(Reg value, String label, int number) throws IOException {
        write("sw " + value + "," + label + " + " + number);
    }

    public static void sw_value_label(Reg value, String label) throws IOException {
        write("sw " + value + "," + label);
    }

    public static void lw_ans_base(Reg ans, Reg base) throws IOException {
        write("lw " + ans + "," + "(" + base + ")");
    }

    public static void sw_value_base(Reg value, Reg base) throws IOException {
        write("sw " + value + "," + "(" + base + ")");
    }

    public static void li_ans_num(Reg reg, int number) throws IOException {
        write("li " + reg + "," + number);
    }

    public static void sll_ans_regx_num(Reg ans, Reg regx, int number) throws IOException {
        write("sll " + ans + "," + regx + "," + number);
    }

    public static void srl_ans_regx_num(Reg ans, Reg regx, int number) throws IOException {
        write("srl " + ans + "," + regx + "," + number);
    }

    public static void not_ans_regx(Reg ans, Reg regx) throws IOException {
        write("not " + ans + "," + regx);
    }

    public static void neg_ans_regx(Reg ans, Reg operand) throws IOException {
        write("not " + ans + "," + operand);
        write("add " + ans + "," + ans + "," + 1);
    }

    public static void neg_reg(Reg ans) throws IOException {
        neg_ans_regx(ans, ans);
    }

    /**
     * !
     *
     * @param reg reg
     * @throws IOException e
     */
    public static void not_ans(Reg reg) throws IOException {
        String labelLiOne = IRTagManage.getInstance().newLabel();
        String labelEnd = IRTagManage.getInstance().newLabel();
        write("beq " + Reg.$zero + "," + reg + "," + labelLiOne);
        write("move " + reg + "," + Reg.$zero);
        write("b " + labelEnd);
        write(labelLiOne + ":");
        write("li " + reg + ",1");
        write(labelEnd + ":");
    }

    public static void bCond_reg1_reg2_label(String b, Reg t1, Reg t2, String label) throws IOException {
        write(b + " " + t1 + "," + t2 + "," + label);
    }

    public static void printfStr(String s) throws IOException {
        if (MemManager.getInstance().inRegToStore(Reg.$a0)) push(Reg.$a0);
        la_ans_label(Reg.$a0, s);
        li_ans_num(Reg.$v0, 4);
        write("syscall");
        if (MemManager.getInstance().inRegToStore(Reg.$a0)) pop(Reg.$a0);
    }

    public static void printfInt(int s) throws IOException {
        if (MemManager.getInstance().inRegToStore(Reg.$a0)) push(Reg.$a0);
        li_ans_num(Reg.$a0, s);
        li_ans_num(Reg.$v0, 1);
        write("syscall");
        if (MemManager.getInstance().inRegToStore(Reg.$a0)) pop(Reg.$a0);
    }

    public static void printfExp(Reg s) throws IOException {
        if (MemManager.getInstance().inRegToStore(Reg.$a0)) push(Reg.$a0);
        move_reg_reg(Reg.$a0, s);
        li_ans_num(Reg.$v0, 1);
        write("syscall");
        if (MemManager.getInstance().inRegToStore(Reg.$a0)) pop(Reg.$a0);
    }

    public static void scanfInt() throws IOException {
        li_ans_num(Reg.$v0, 5);
        write("syscall");
    }

    public static void b_Label(String label) throws IOException {
        write("b " + label);
    }

    public static void jal_label(String label) throws IOException {
        write("jal " + label);
    }

    public static void jr_reg(Reg reg) throws IOException {
        write("jr " + reg);
    }

    public static void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

    /**
     * t1 = (- | !) t1
     *
     * @param ans t1
     * @param op  - | !
     */
    public static void negOrNot_ans(String op, Reg ans) throws IOException {
        if (op.equals("-")) {
            neg_reg(ans);
        } else if (op.equals("!")) {
            not_ans(ans);
        } else UnExpect.printf(op + " is not ! or -");
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
    public static void compute_ans_r1_op_r2(Reg ans, Reg temp1, String op, Reg temp2) throws IOException {
        if (Judge.isPlus(op)) {
            add_ans_reg_regOrNum(ans, temp1, temp2);
        } else if (Judge.isMinus(op)) {
            sub_ans_reg_regOrNum(ans, temp1, temp2);
        } else if (Judge.isMul(op)) {
            mul_ans_reg_reg(ans, temp1, temp2);
        } else if (Judge.isDiv(op)) {
            div_ans_reg_reg(ans, temp1, temp2);
        } else if (Judge.isMod(op)) {
            mod_ans_reg_reg(ans, temp1, temp2);
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
        compute_ans_r1_op_r2(temp1, temp1, op, temp2);
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
        compute_ans_r1_op_r2(temp2, temp1, op, temp2);
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
    public static void compute(Reg ans, Reg temp1, String op, int number, Reg afloat) throws IOException {
        if (Judge.isPlus(op)) {
            add_ans_reg_regOrNum(ans, temp1, number);
        } else if (Judge.isMinus(op)) {
            sub_ans_reg_regOrNum(ans, temp1, number);
        } else if (Judge.isSll(op)) {
            sll_ans_regx_num(ans, temp1, number);
        } else if (Judge.isSrl(op)) {
            srl_ans_regx_num(ans, temp1, number);
        } else {
            li_ans_num(afloat, number);
            compute_ans_r1_op_r2(ans, temp1, op, afloat);
        }
    }

    /**
     * compute reg and number and store answer in first reg
     *
     * @param op     operate
     * @param ans    op1 ans ans
     * @param number number
     */
    public static void compute_first(String op, Reg ans, int number, Reg afloat) throws IOException {
        compute(ans, ans, op, number, afloat);
    }

    /**
     * 把数组的存储地址 >> 2 存进ans里
     *
     * @param ans *
     * @param off *
     */
    public static void address_srl_2(Reg ans, int off) throws IOException {
        add_ans_reg_regOrNum(ans, Reg.$fp, off);
        srl_ans_regx_num(ans, ans, 2);
    }

    public static void push(Reg reg) throws IOException {
        sub_ans_reg_regOrNum(Reg.$sp, Reg.$sp, 4);
        sw_value_base(reg, Reg.$sp);
    }

    public static void pop(Reg reg) throws IOException {
        lw_ans_base(reg, Reg.$sp);
        add_ans_reg_regOrNum(Reg.$sp, Reg.$sp, 4);
    }

}
