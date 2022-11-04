package SymbolTableBin.Element;

import SymbolTableBin.TypeTable;
import gccBin.MIPS.tool.Reg;

public class ElementFParam extends ElementTable {
    private int index;
    public ElementFParam(String name, TypeTable type, TypeTable decl,
                         int dimension,int index){
        super(name,type,decl,dimension);
        this.index = index;
        if(index <= 4 && index >= 1){
            super.setReg(Reg.getFParamReg(index));
        } else {
            int off = (index-5)*4;
            super.setMemOff(off);
        }
    }

    /***
     * 这个参数是函数的第几个参数:下标从1开始
     * @return *
     */
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
