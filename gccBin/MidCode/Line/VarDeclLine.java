package gccBin.MidCode.Line;


import SymbolTableBin.TableSymbol;
import gccBin.MidCode.firstProcess.MidCodeFirst;

/**
 * var int i
 */
public class VarDeclLine extends Line {
    private String name;

    public VarDeclLine(String s, int line,TableSymbol tableSymbol, String[] str) {
        super(s,line, tableSymbol);
        parse(str[2]);
        MidCodeFirst.getInstance().addVarNode(name,tableSymbol);
    }

    private void parse(String str) {
        this.name = str;
    }

    public String getName() {
        return name;
    }
}
