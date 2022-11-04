package SymbolTableBin;

import gccBin.MIPS.tool.Reg;

public class Position {
    private boolean hasReg;
    private Reg reg;

    private int off;
    public Position() {
        hasReg = false;
        this.off = -1;
    }

    public void setReg(Reg reg) {
        hasReg = true;
        this.reg = reg;
    }

    public void setOff(int off) {
        this.off = off;
    }

    public boolean isHasReg() {
        return hasReg;
    }

    public int getOff() {
        return off;
    }

    public Reg getReg() {
        return reg;
    }
}
