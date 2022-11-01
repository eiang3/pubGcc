package gccBin.MIPS.tool;

import gccBin.MIPS.MIPS;
import gccBin.MidCode.original.IRTagManage;

import java.io.IOException;

public class MIPSIns {
    private static MIPSIns mipsIns;

    private MIPSIns() {
    }

    public static MIPSIns get() {
        if (mipsIns == null) {
            mipsIns = new MIPSIns();
        }
        return mipsIns;
    }

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

    public static void add_reg_o(Reg ans, Reg reg, int number) throws IOException {
        write("add " + ans + "," + reg + "," + number);
    }

    public static void add_reg_o(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("add " + ans + "," + reg1 + "," + reg2);
    }

    public static void sub_reg_o(Reg ans, Reg reg1, Reg reg2) throws IOException {
        write("sub " + ans + "," + reg1 + "," + reg2);
    }

    public static void sub_reg_o(Reg ans, Reg reg1,int number) throws IOException {
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

    public static void li(Reg reg,int number) throws IOException {
        write("li "+reg+","+number);
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

    public static void logicalNot(Reg reg) throws IOException {
        String labelStoreOne = IRTagManage.getInstance().newLabel();
        String labelEnd = IRTagManage.getInstance().newLabel();
        write("beq " + Reg.$zero + "," + reg + "," + labelStoreOne);
        write("move " + reg + "," + Reg.$zero);
        write("b " + labelEnd);
        write(labelStoreOne);
        write("li " + reg + ",1");
        write(labelEnd + ":");
    }

    public static void bCond(String b,Reg t1,Reg t2,String label) throws IOException {
        write(b+" "+t1+","+t2+","+label);
    }

    public static void printfStr(String s) throws IOException {
        la_label(Reg.$a0,s);
        li(Reg.$v0,4);
        write("syscall");
    }

    public static void printfInt(int s) throws IOException {
        li(Reg.$a0,s);
        li(Reg.$v0,1);
        write("syscall");
    }

    public static void printfExp(Reg s) throws IOException {
        move(Reg.$a0,s);
        li(Reg.$v0,1);
        write("syscall");
    }
    public static void scanfInt() throws IOException {
        li(Reg.$v0,5);
        write("syscall");
    }
    public static void write(String s) throws IOException {
        MIPS.getInstance().write(s);
    }

    public void writeNotNext(String s) throws IOException {
        MIPS.getInstance().writeNotNext(s);
    }
}
