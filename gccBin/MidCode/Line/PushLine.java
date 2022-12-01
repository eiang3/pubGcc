package gccBin.MidCode.Line;

import SymbolTableBin.TableSymbol;

/**
 * push exp
 */
//
public class PushLine extends Line {
    private final String exp;

    public PushLine(String s, int line, TableSymbol tableSymbol, String[] ele) {
        super(s, line, tableSymbol);
        exp = ele[1];
        super.addUse_Zero_firstTime(exp);
    }

    public String getExp() {
        return exp;
    }

    @Override
    public String getMidCodeLine() {
        return "&push " + exp;
    }
}
