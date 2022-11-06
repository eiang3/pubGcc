package SymbolTableBin.Element;

import SymbolTableBin.TableSymbol;
import SymbolTableBin.TypeTable;

import java.io.IOException;

public class ElementVar extends ElementTable {


    private final TypeTable type;

    private String oldName;

    public ElementVar(String name, TypeTable type) {
        super(name, type, TypeTable.VAR, 0);

        this.type = type;
    }



    /**
     * 对于有多个web的变量，进行符号表项的更新
     *
     * @param name *
     * @return *
     */
    public ElementVar myCopy(String name) {
        return new ElementVar(name, type);
    }


    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getOldName() {
        return oldName;
    }
}
