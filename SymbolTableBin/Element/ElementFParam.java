package SymbolTableBin.Element;

import SymbolTableBin.TypeTable;

public class ElementFParam extends ElementTable {
    private final int index;
    public ElementFParam(String name, TypeTable type, TypeTable decl,
                         int dimension, int index){
        super(name,type,decl,dimension);
        this.index = index;
    }

    /***
     * 这个参数是函数的第几个参数:下标从1开始
     * @return
     */
    public int getIndex() {
        return index;
    }
}
