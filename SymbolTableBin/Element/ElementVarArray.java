package SymbolTableBin.Element;

import SymbolTableBin.Element.ElementTable;
import SymbolTableBin.TypeTable;
import SymbolTableBin.Value;

public class ElementVarArray extends ElementTable {
    private final Value oneDim; //一维
    private final Value twoDim; //二维

    public ElementVarArray(String name, TypeTable type, TypeTable decl,
                           int dimension, Value oneDim,
                           Value twoDim,int falseRow) {
        super(name, type, decl, dimension,falseRow);
        this.oneDim = oneDim;
        this.twoDim = twoDim;
    }

    /**
     *得到二维数组的第二维长度
     */
    public int getLen(){
        return twoDim.getNum();
    }

}
