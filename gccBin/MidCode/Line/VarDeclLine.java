package gccBin.MidCode.Line;


import SymbolTableBin.TableSymbol;
import gccBin.MidCode.Judge;
import gccBin.MidCode.AfirstProcess.VarNodeManager;

/**
 * var int i
 */
public class VarDeclLine extends Line {
    private final String name;


    public VarDeclLine(String s, int line, TableSymbol tableSymbol, String[] str) {
        super(s, line, tableSymbol);
        this.name = str[2];
        if (Judge.is_LocalVar(name,tableSymbol)) {
            VarNodeManager.getInstance().addVarNode(name, tableSymbol);
        }
    }

    public String getName() {
        return name;
    }
}
