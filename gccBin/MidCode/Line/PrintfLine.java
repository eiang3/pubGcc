package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * printf exp
 * printf str_
 */

public class PrintfLine extends Line {
    private final String exp;

    public PrintfLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        exp = ele[1];
        super.addUse_Zero_firstTime(exp);
    }

    public String getExp() {
        return exp;
    }

    @Override
    public String getMidCodeLine() {
        return "&printf " + exp;
    }

}
