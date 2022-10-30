package gccBin.MIPS;

import gccBin.Lex.Symbol;

public class Operate {
    private String op;

    public Operate(String op){
        this.op = op;
    }

    public Operate(){}

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public boolean isPlus(){
        return op.equals("+");
    }

    public boolean isMinus(){
        return op.equals("-");
    }

    public boolean isMul(){
        return op.equals("*");
    }

    public boolean isDiv(){
        return op.equals("/");
    }

    public boolean isMod(){
        return op.equals("%");
    }

    public boolean isCondJump(){
        return op.endsWith("bge") ||
                op.equals("ble") ||
                op.equals("bgt") ||
                op.equals("bne") ||
                op.equals("blt") ||
                op.equals("beq");
    }

}
