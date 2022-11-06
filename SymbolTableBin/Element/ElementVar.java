package SymbolTableBin.Element;

import SymbolTableBin.TableSymbol;
import SymbolTableBin.TypeTable;

import java.io.IOException;

public class ElementVar extends ElementTable {


    private final TypeTable type;

    private String oldName;

    public ElementVar(String name, TypeTable type,int falseRow) {
        super(name, type, TypeTable.VAR, 0,falseRow);

        this.type = type;
    }



    /**
     * 对于有多个web的变量，进行符号表项的更新
     *
     * @param name *
     * @return *
     */
    public ElementVar myCopy(String name) {
        return new ElementVar(name, type,super.getFalseRow());
    }


    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getOldName() {
        return oldName;
    }
}
