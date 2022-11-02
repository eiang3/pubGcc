package SymbolTableBin.Element;

import SymbolTableBin.TypeTable;
import SymbolTableBin.Value;

public class ElementConst extends ElementTable {
    private final Value value;  //值 一维const拥有这个属性

    public ElementConst(String name, TypeTable type, Value value){
        super(name,type,TypeTable.CONST,0);
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
