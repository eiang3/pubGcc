package gccBin.MidCode.Line;


import SymbolTableBin.TableSymbol;
import gccBin.MidCode.firstProcess.VarNodeManager;

/**
 * var int i
 */
public class VarDeclLine extends Line {
    private final String name;

    public VarDeclLine(String s, int line, TableSymbol tableSymbol, String[] str) {
        super(s, line, tableSymbol);
        this.name = str[2];
        VarNodeManager.getInstance().addVarNode(name, tableSymbol);
    }

    public String getName() {
        return name;
    }
}
