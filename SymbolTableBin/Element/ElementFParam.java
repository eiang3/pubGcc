package SymbolTableBin.Element;

import SymbolTableBin.TypeTable;
import SymbolTableBin.Value;
import gccBin.MIPS.tool.Reg;

public class ElementFParam extends ElementTable {
    private int index;
    private Value twoDim;
    public ElementFParam(String name, TypeTable type, TypeTable decl,
                         int dimension,int index,int falseRow,Value twoDim){
        super(name,type,decl,dimension,falseRow);
        this.index = index;
        if(index <= 4 && index >= 1){
            super.setReg(Reg.getFParamReg(index));
        } else {
            int off = (index-5)*4;
            super.setMemOff(off);
        }
        this.twoDim = twoDim;
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

    public int getTwoDim(){
        return twoDim.getNum();
    }
}
