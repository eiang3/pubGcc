package SymbolTableBin.Element;

import SymbolTableBin.TableSymbol;
import SymbolTableBin.TypeTable;

import java.io.IOException;

public class ElementVar extends ElementTable {
    private int subScript;  //如果有重命名的变量，需要添加下标保证它们的名字不一样。

    private final TypeTable type;

    public ElementVar(String name, TypeTable type) {
        super(name, type, TypeTable.VAR, 0);
        this.subScript = -1;
        this.type = type;
    }

    public void setSubScript(int subScript) {
        this.subScript = subScript;
    }

    /**
     * 对于有多个web的变量，进行符号表项的更新
     *
     * @param name
     * @return
     */
    public ElementVar myCopy(String name) {
        return new ElementVar(name, type);
    }

    public void refreshName(TableSymbol tableSymbol) throws IOException {
        tableSymbol.remove(this);
        super.setName(this.getIRName());
        tableSymbol.addElement(this);
    }

    @Override
    public String getIRName() {
        if (this.subScript == -1) {
            return super.getName();
        } else {
            return super.getName() + "$" + subScript;
        }
    }
}
