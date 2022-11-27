package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * printf exp
 * printf str_
 */
public class PrintfLine extends Line {
    private final String t;

    public PrintfLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        t = ele[1];
        super.addUse_Temp(t);
    }

    public String getT() {
        return t;
    }
}
