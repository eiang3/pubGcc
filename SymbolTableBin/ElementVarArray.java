package SymbolTableBin;

public class ElementVarArray extends ElementTable{
    private Value oneDim; //一维
    private final Value twoDim; //二维

    public ElementVarArray(String name, TypeTable type, TypeTable decl,
                           int dimension, Value oneDim,
                           Value twoDim) {
        super(name, type, decl, dimension);
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
