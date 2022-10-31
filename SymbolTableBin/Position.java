package SymbolTableBin;

import gccBin.MIPS.Reg;

public class Position {
    private boolean hasReg;
    private Reg reg;

    public Position() {
        hasReg = false;
    }

    public void setReg(Reg reg) {
        hasReg = true;
        this.reg = reg;
    }
}
